package com.tool.aidubbing.repository;

import com.tool.aidubbing.entity.VideoJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoJobRepository extends JpaRepository<VideoJob, Long> {

    List<VideoJob> findByUserId(long userId);

}
