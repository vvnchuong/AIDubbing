package com.tool.aidubbing.controller;

import com.tool.aidubbing.dto.request.VideoJobCreationRequest;
import com.tool.aidubbing.dto.response.ApiResponse;
import com.tool.aidubbing.dto.response.VideoJobResponse;
import com.tool.aidubbing.service.VideoJobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jobs")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoJobController {

    VideoJobService videoJobService;

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping
    public ApiResponse<VideoJobResponse> createJob(@RequestBody VideoJobCreationRequest request) {
        return ApiResponse.<VideoJobResponse>builder()
                .message("Job created.")
                .result(videoJobService.createJob(currentUserId(), request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VideoJobResponse> getJob(@PathVariable Long id) {
        return ApiResponse.<VideoJobResponse>builder()
                .message("OK")
                .result(videoJobService.getJobById(currentUserId(), id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<VideoJobResponse>> getMyJobs() {
        return ApiResponse.<List<VideoJobResponse>>builder()
                .message("OK")
                .result(videoJobService.getMyJobs(currentUserId()))
                .build();
    }
}