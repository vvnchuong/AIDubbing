package com.tool.aidubbing.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoiceOption {

    String id;
    String label;
    String previewAudioUrl;

}
