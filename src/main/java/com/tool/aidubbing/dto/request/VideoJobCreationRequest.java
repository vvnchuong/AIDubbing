package com.tool.aidubbing.dto.request;

import com.tool.aidubbing.enums.VoiceType;
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
    String voiceId;
    VoiceType voiceType;
    String promptExtra;
    String referenceAudioPath;
    Boolean isShort;
}