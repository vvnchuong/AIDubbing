package com.tool.aidubbing.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubtitleItem {

    int index;
    Double start;
    Double end;
    String originalSub;
    String translatedSub;

}
