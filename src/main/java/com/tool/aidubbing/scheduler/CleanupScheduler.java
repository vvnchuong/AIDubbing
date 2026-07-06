package com.tool.aidubbing.scheduler;

import com.tool.aidubbing.entity.VideoJob;
import com.tool.aidubbing.repository.VideoJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final VideoJobRepository videoJobRepository;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void cleanupExpiredFiles() {
        Instant now = Instant.now();

        List<VideoJob> expiredJobs = videoJobRepository
                .findByFileExpiresAtBeforeAndOutputPathIsNotNull(now);

        for (VideoJob job : expiredJobs) {
            deleteIfExists(job.getInputPath());
            deleteIfExists(job.getOutputPath());

            job.setOutputPath(null);
            job.setFileExpiresAt(null);
            videoJobRepository.save(job);

            log.info("Cleaned up expired file for jobId={}", job.getId());
        }
    }

    private void deleteIfExists(String path) {
        if (path == null) return;
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            log.warn("Failed to delete file: {}", path);
        }
    }
}