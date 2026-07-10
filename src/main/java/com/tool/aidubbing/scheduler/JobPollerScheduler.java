package com.tool.aidubbing.scheduler;

import com.tool.aidubbing.entity.VideoJob;
import com.tool.aidubbing.repository.UserRepository;
import com.tool.aidubbing.repository.VideoJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.util.List;

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

    @Value("${app.python-executable}")
    private String pythonExecutable;

    private volatile boolean isProcessing = false;

    @Scheduled(fixedDelay = 5000)
    public void processNextPendingJob() {
        if (isProcessing)
            return;

        List<VideoJob> pendingJobs = videoJobRepository
                .findByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingJobs.isEmpty())
            return;

        VideoJob job = pendingJobs.get(0);
        isProcessing = true;

        try {
            runJob(job);
        } catch (Exception e) {
            log.error("Unexpected error occurred while processing jobId={}", job.getId(), e);
            job.setStatus("FAILED");
            refundQuota(job);
            videoJobRepository.save(job);
        } finally {
            isProcessing = false;
        }
    }

    private void runJob(VideoJob job) throws Exception {
        log.info("Start processing jobId={}, inputPath={}", job.getId(), job.getInputPath());

        job.setStatus("PROCESSING");
        videoJobRepository.save(job);

        String outputPath = jobOutputDir + "/job_" + job.getId() + ".mp4";
        new File(jobOutputDir).mkdirs();

        ProcessBuilder pb = new ProcessBuilder(
                pythonExecutable, "run_job.py",
                "--input", job.getInputPath(),
                "--output", outputPath,
                "--job-id", String.valueOf(job.getId())
        );
        pb.directory(new File(videoLingoDir));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (var reader = process.inputReader()) {
            reader.lines().forEach(line -> {
                log.info("[jobId={}] {}", job.getId(), line);

                if (line.startsWith("[STEP]")) {
                    String step = line.substring("[STEP]".length()).trim();
                    job.setCurrentStep(step);
                    videoJobRepository.save(job);
                }
            });
        }

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            job.setStatus("DONE");
            job.setOutputPath(outputPath);
            job.setFinishedAt(Instant.now());
            job.setFileExpiresAt(Instant.now().plus(30, java.time.temporal.ChronoUnit.MINUTES));
            job.setCurrentStep(null);
            log.info("Finished processing jobId={}, outputPath={}", job.getId(), outputPath);
        } else {
            job.setStatus("FAILED");
            job.setFinishedAt(Instant.now());
            job.setFileExpiresAt(Instant.now().plus(30, java.time.temporal.ChronoUnit.MINUTES));
            job.setCurrentStep(null);
            refundQuota(job);
            log.error("Failed processing jobId={}, exitCode={}", job.getId(), exitCode);
        }

        videoJobRepository.save(job);
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
}