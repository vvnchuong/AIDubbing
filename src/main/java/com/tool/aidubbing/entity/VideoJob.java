package com.tool.aidubbing.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Data
@Table(name = "video_jobs")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    String title;

    @Column(name = "source_type")
    String sourceType;

    @Column(name = "input_path")
    String inputPath;

    @Column(name = "output_path")
    String outputPath;

    @Column(nullable = false)
    String status = "PENDING";

    @Column(name = "target_lang")
    String targetLang;

    @Column(name = "is_short")
    Boolean isShort = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @Column(name = "finished_at")
    Instant finishedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}