package com.tool.aidubbing.scheduler;

import com.tool.aidubbing.entity.VideoJob;
import com.tool.aidubbing.enums.VideoJobStatus;
import com.tool.aidubbing.repository.UserRepository;
import com.tool.aidubbing.repository.VideoJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobPollerScheduler {

    private final VideoJobRepository videoJobRepository;
    private final UserRepository userRepository;

    @Value("${app.videolingo-dir}")
    private String videoLingoDir;

    @Value("${app.job-output-dir}")
    private String jobOutputDir;

    @Value("${app.job-workdir-root}")
    private String jobWorkdirRoot;

    @Value("${app.python-executable}")
    private String pythonExecutable;

    private volatile boolean isProcessing = false;

    @Scheduled(fixedDelay = 5000)
    public void processNextPendingJob() {
        if (isProcessing) return;

        List<VideoJob> pendingJobs = videoJobRepository
                .findByStatusOrderByCreatedAtAsc(VideoJobStatus.PENDING);
        if (pendingJobs.isEmpty()) return;

        VideoJob job = pendingJobs.get(0);
        isProcessing = true;

        try {
            runSubStage(job);
        } catch (Exception e) {
            log.error("Unexpected error occurred while processing jobId={}", job.getId(), e);
            job.setStatus(VideoJobStatus.FAILED);
            refundQuota(job);
            videoJobRepository.save(job);
        } finally {
            isProcessing = false;
        }
    }

    public synchronized void continueDubStage(Long jobId) {
        VideoJob job = videoJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        if (job.getStatus() != VideoJobStatus.SUB_READY) {
            throw new RuntimeException("Job không ở trạng thái SUB_READY, không thể tiếp tục dub");
        }

        while (isProcessing) {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
        }

        isProcessing = true;
        try {
            runDubStage(job);
        } catch (Exception e) {
            log.error("Unexpected error occurred while continuing dub jobId={}", job.getId(), e);
            job.setStatus(VideoJobStatus.FAILED);
            refundQuota(job);
            videoJobRepository.save(job);
        } finally {
            isProcessing = false;
        }
    }

    private void runSubStage(VideoJob job) throws Exception {
        log.info("Start SUB stage jobId={}, inputPath={}", job.getId(), job.getInputPath());

        job.setStatus(VideoJobStatus.PROCESSING);
        videoJobRepository.save(job);

        String finalInputPath = tryResizeTo720p(job);

        Process process = startPythonProcess(List.of(
                "--input", finalInputPath,
                "--job-id", String.valueOf(job.getId()),
                "--stage", "sub"
        ));

        int exitCode = readAndLog(job, process);
        cleanupResizedFile(job, finalInputPath);

        if (exitCode != 0) {
            job.setStatus(VideoJobStatus.FAILED);
            job.setFinishedAt(Instant.now());
            job.setCurrentStep(null);
            refundQuota(job);
            videoJobRepository.save(job);
            log.error("SUB stage failed jobId={}, exitCode={}", job.getId(), exitCode);
            return;
        }

        // Copy toàn bộ output/ ra workDir riêng của job, rồi dọn output/ để job khác dùng ngay
        String workDir = jobWorkdirRoot + "/job_" + job.getId();
        copyDirectory(Path.of(videoLingoDir, "output"), Path.of(workDir, "output"));
        deleteDirectoryContents(Path.of(videoLingoDir, "output"));

        job.setWorkDir(workDir);
        job.setStatus(VideoJobStatus.SUB_READY);
        job.setCurrentStep(null);
        videoJobRepository.save(job);

        log.info("SUB stage done, jobId={} now waiting at SUB_READY, workDir={}", job.getId(), workDir);
    }

    private void runDubStage(VideoJob job) throws Exception {
        log.info("Start DUB stage jobId={}", job.getId());

        // copy ngược workDir -> output/ (output/ lúc này đang trống vì job khác không dùng)
        copyDirectory(Path.of(job.getWorkDir(), "output"), Path.of(videoLingoDir, "output"));

        job.setStatus(VideoJobStatus.PROCESSING);
        videoJobRepository.save(job);

        String outputPath = jobOutputDir + "/job_" + job.getId() + ".mp4";
        new File(jobOutputDir).mkdirs();

        Process process = startPythonProcess(List.of(
                "--output", outputPath,
                "--job-id", String.valueOf(job.getId()),
                "--stage", "dub"
        ));

        int exitCode = readAndLog(job, process);

        if (exitCode == 0) {
            job.setStatus(VideoJobStatus.FINISHED);
            job.setOutputPath(outputPath);
            job.setFinishedAt(Instant.now());
            job.setFileExpiresAt(Instant.now().plus(30, java.time.temporal.ChronoUnit.MINUTES));
            job.setCurrentStep(null);
            log.info("DUB stage done jobId={}, outputPath={}", job.getId(), outputPath);
        } else {
            job.setStatus(VideoJobStatus.FAILED);
            job.setFinishedAt(Instant.now());
            job.setCurrentStep(null);
            refundQuota(job);
            log.error("DUB stage failed jobId={}, exitCode={}", job.getId(), exitCode);
        }

        deleteDirectoryRecursive(Path.of(job.getWorkDir()));
        job.setWorkDir(null);
        videoJobRepository.save(job);
    }

    private Process startPythonProcess(List<String> stageArgs) throws IOException {
        var cmd = new java.util.ArrayList<String>();
        cmd.add(pythonExecutable);
        cmd.add("run_job.py");
        cmd.addAll(stageArgs);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(videoLingoDir));
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private int readAndLog(VideoJob job, Process process) throws Exception {
        try (var reader = process.inputReader()) {
            reader.lines().forEach(line -> {
                log.info("[jobId={}] {}", job.getId(), line);
                if (line.startsWith("[STEP]")) {
                    job.setCurrentStep(line.substring("[STEP]".length()).trim());
                    videoJobRepository.save(job);
                }
            });
        }
        return process.waitFor();
    }

    private String tryResizeTo720p(VideoJob job) {
        String finalInputPath = job.getInputPath();
        try {
            log.info("[jobId={}] Đang kiểm tra và tự động tối ưu hóa độ phân giải về 720p...", job.getId());

            String optimizedInputPath = jobOutputDir + "/optimized_input_" + job.getId() + ".mp4";
            new File(jobOutputDir).mkdirs();

            ProcessBuilder resizePb = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", job.getInputPath(),
                    "-vf", "scale='min(1280,iw)':-2",
                    "-c:v", "libx264", "-preset", "fast", "-crf", "22", "-c:a", "copy",
                    optimizedInputPath
            );
            resizePb.redirectErrorStream(true);
            Process resizeProcess = resizePb.start();

            try (var reader = resizeProcess.inputReader()) {
                reader.lines().forEach(line -> log.info("[jobId={}] [ffmpeg-resize] {}", job.getId(), line));
            }

            int resizeExitCode = resizeProcess.waitFor();
            if (resizeExitCode == 0) {
                log.info("[jobId={}] Tối ưu hóa video về 720p thành công!", job.getId());
                finalInputPath = optimizedInputPath;
            } else {
                log.warn("[jobId={}] Không thể resize video, tiếp tục dùng file gốc.", job.getId());
            }
        } catch (Exception e) {
            log.error("[jobId={}] Lỗi khi tối ưu hóa video, dùng file gốc.", job.getId(), e);
        }
        return finalInputPath;
    }

    private void cleanupResizedFile(VideoJob job, String finalInputPath) {
        if (!finalInputPath.equals(job.getInputPath())) {
            if (!new File(finalInputPath).delete()) {
                log.warn("[jobId={}] Không thể xóa file resize tạm: {}", job.getId(), finalInputPath);
            }
        }
    }

    private void refundQuota(VideoJob job) {
        if (job.getDurationMinutes() == null) return;

        userRepository.findById(job.getUserId()).ifPresent(user -> {
            int refunded = (int) (user.getQuotaMinutesLeft() + job.getDurationMinutes());
            user.setQuotaMinutesLeft(refunded);
            userRepository.save(user);
            log.info("Refunded {} minutes of quota to userId={} due to FAILED jobId={}",
                    job.getDurationMinutes(), user.getId(), job.getId());
        });
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target);
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(src -> {
                try {
                    Path dest = target.resolve(source.relativize(src));
                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void deleteDirectoryContents(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .filter(p -> !p.equals(dir))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    private void deleteDirectoryRecursive(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }
}