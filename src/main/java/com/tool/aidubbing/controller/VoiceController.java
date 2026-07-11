package com.tool.aidubbing.controller;

import com.tool.aidubbing.dto.response.ApiResponse;
import com.tool.aidubbing.dto.VoiceOption;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/voices")
public class VoiceController {

    @GetMapping
    public ApiResponse<List<VoiceOption>> getAvailableVoices() {
        List<VoiceOption> voices = Arrays.asList(
                VoiceOption.builder()
                        .id("vi-VN-NamMinh")
                        .label("Nam Minh (Tiếng Việt - Miền Bắc)")
                        .previewAudioUrl("/static/previews/vi-namminh.mp3") // Để sẵn hoặc để null nếu chưa deploy file
                        .build(),
                VoiceOption.builder()
                        .id("vi-VN-NuAn")
                        .label("An An (Tiếng Việt - Miền Nam)")
                        .previewAudioUrl("/static/previews/vi-nuan.mp3")
                        .build(),
                VoiceOption.builder()
                        .id("en-US-Brian")
                        .label("Brian (English - US)")
                        .previewAudioUrl("/static/previews/en-brian.mp3")
                        .build(),
                VoiceOption.builder()
                        .id("en-US-Jenny")
                        .label("Jenny (English - US)")
                        .previewAudioUrl("/static/previews/en-jenny.mp3")
                        .build()
        );

        return ApiResponse.<List<VoiceOption>>builder()
                .message("Voices retrieved successfully.")
                .result(voices)
                .build();
    }


}