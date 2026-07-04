package com.tool.aidubbing.dto.response;

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
    Instant createdAt;
    Instant finishedAt;
}