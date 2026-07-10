package com.tool.aidubbing.controller;

import com.tool.aidubbing.dto.request.VideoJobCreationRequest;
import com.tool.aidubbing.dto.response.ApiResponse;
import com.tool.aidubbing.dto.response.VideoJobResponse;
import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.exception.AppException;
import com.tool.aidubbing.service.FileStorageService;
import com.tool.aidubbing.service.VideoJobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jobs")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoJobController {

    VideoJobService videoJobService;
    FileStorageService fileStorageService;

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/upload")
    public ApiResponse<String> uploadVideo(
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.<String>builder()
                .message("Uploaded video file successfully.")
                .result(fileStorageService.save(file))
                .build();
    }

    // --- ENDPOINT THÊM MỚI: Upload file audio mẫu ---
    @PostMapping("/upload-audio")
    public ApiResponse<String> uploadAudio(
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.<String>builder()
                .message("Uploaded audio file successfully.")
                .result(fileStorageService.saveAudio(file))
                .build();
    }

    @PostMapping
    public ApiResponse<VideoJobResponse> createJob(
            @RequestBody VideoJobCreationRequest request) {
        return ApiResponse.<VideoJobResponse>builder()
                .message("Job created.")
                .result(videoJobService.createJob(currentUserId(), request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VideoJobResponse> getJob(
            @PathVariable Long id) {
        return ApiResponse.<VideoJobResponse>builder()
                .message("Job retrieved successfully.")
                .result(videoJobService.getJobById(currentUserId(), id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<VideoJobResponse>> getMyJobs() {
        return ApiResponse.<List<VideoJobResponse>>builder()
                .message("Jobs retrieved successfully.")
                .result(videoJobService.getMyJobs(currentUserId()))
                .build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResult(
            @PathVariable Long id) {
        VideoJobResponse job = videoJobService.getJobById(currentUserId(), id);

        if (!"DONE".equals(job.getStatus()) || job.getOutputPath() == null)
            throw new AppException(ErrorCode.JOB_NOT_READY);

        File file = new File(job.getOutputPath());
        if (!file.exists())
            throw new AppException(ErrorCode.JOB_NOT_READY);

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("video/mp4"))
                .body(resource);
    }
}