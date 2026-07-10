package com.tool.aidubbing.service;

import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${app.upload-audio-dir}")
    private String uploadAudioDir;

    private static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024 * 1024; // 2GB
    private static final String ALLOWED_VIDEO_EXTENSION = ".mp4";
    private static final List<String> ALLOWED_AUDIO_EXTENSIONS = Arrays.asList(".mp3", ".wav");
    private static final List<String> ALLOWED_AUDIO_CONTENT_TYPES = Arrays.asList("audio/mpeg", "audio/wav", "audio/x-wav");

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(ALLOWED_VIDEO_EXTENSION)) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("video/mp4")) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        return saveFileToDisk(file, uploadDir, ALLOWED_VIDEO_EXTENSION);
    }

    public String saveAudio(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE); // Bạn có thể đổi sang ErrorCode.INVALID_AUDIO_FILE nếu có
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        // Validate extension
        String lowercaseName = originalName.toLowerCase();
        String detectedExtension = ALLOWED_AUDIO_EXTENSIONS.stream()
                .filter(lowercaseName::endsWith)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VIDEO_FILE));

        // Validate Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        return saveFileToDisk(file, uploadAudioDir, detectedExtension);
    }

    private String saveFileToDisk(MultipartFile file, String targetDir, String extension) {
        String storedFileName = UUID.randomUUID() + extension;
        try {
            Path dirPath = Paths.get(targetDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path targetPath = dirPath.resolve(storedFileName);
            file.transferTo(targetPath);

            log.info("Uploaded file saved thành công tại: {}", targetPath);
            return storedFileName;
        } catch (IOException e) {
            log.error("Failed to save uploaded file to {}", targetDir, e);
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }
    }

    public String resolveUploadedFilePath(String storedFileName) {
        return resolvePath(storedFileName, uploadDir);
    }

    public String resolveUploadedAudioPath(String storedFileName) {
        return resolvePath(storedFileName, uploadAudioDir);
    }

    private String resolvePath(String storedFileName, String baseDir) {
        if (storedFileName == null
                || storedFileName.contains("..")
                || storedFileName.contains("/")
                || storedFileName.contains("\\")) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        Path base = Paths.get(baseDir).normalize();
        Path resolved = base.resolve(storedFileName).normalize();

        if (!resolved.startsWith(base) || !Files.exists(resolved)) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        return resolved.toAbsolutePath().toString();
    }
}