package com.tool.aidubbing.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoJobCreationRequest {
    String title;
    String sourceType;
    String inputPath;
    String targetLang;
    Boolean isShort;
}