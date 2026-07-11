package com.tool.aidubbing.entity;

import com.tool.aidubbing.enums.VideoJobStatus;
import com.tool.aidubbing.enums.VoiceType;
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

    @Enumerated(EnumType.STRING)
    VideoJobStatus status;

    @Column(name = "target_lang")
    String targetLang;

    @Column(name = "duration_minutes")
    Double durationMinutes;

    @Column(name = "current_step")
    String currentStep;

    @Column(name = "voice_id")
    String voiceId;

    @Enumerated(EnumType.STRING)
    VoiceType voiceType;

    @Column(columnDefinition = "TEXT")
    String promptExtra;

    @Column(name = "reference_audio_path")
    String referenceAudioPath;

    @Column(name = "work_dir")
    String workDir;

    @Column(name = "is_short")
    Boolean isShort = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @Column(name = "finished_at")
    Instant finishedAt;

    @Column(name = "file_expires_at")
    Instant fileExpiresAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}