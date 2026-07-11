package com.tool.aidubbing.repository;

import com.tool.aidubbing.entity.VideoJob;
import com.tool.aidubbing.enums.VideoJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface VideoJobRepository extends JpaRepository<VideoJob, Long> {

    List<VideoJob> findByUserId(long userId);

    List<VideoJob> findByStatusOrderByCreatedAtAsc(VideoJobStatus status);

    List<VideoJob> findByFileExpiresAtBeforeAndOutputPathIsNotNull(Instant now);

}
