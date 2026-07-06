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
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024 * 1024; // 2GB
    private static final String ALLOWED_EXTENSION = ".mp4";

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(ALLOWED_EXTENSION)) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("video/mp4")) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        String extension = ALLOWED_EXTENSION;

        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path targetPath = dirPath.resolve(storedFileName);
            file.transferTo(targetPath);

            log.info("Uploaded file saved: {}", targetPath);
            return targetPath.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to save uploaded file", e);
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }
    }
}