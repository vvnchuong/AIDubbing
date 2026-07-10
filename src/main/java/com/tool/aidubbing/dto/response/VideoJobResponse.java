package com.tool.aidubbing.dto.response;

import com.tool.aidubbing.enums.VoiceType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoJobResponse {
    Long id;
    String title;
    String sourceType;
    String status;
    String targetLang;
    Boolean isShort;
    String outputPath;
    String currentStep;
    String voiceId;
    VoiceType voiceType;
    String promptExtra;
    String referenceAudioPath;
    Instant createdAt;
    Instant finishedAt;
}