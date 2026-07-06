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

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path targetPath = dirPath.resolve(storedFileName);
            file.transferTo(targetPath);

            log.info("Đã lưu file upload: {}", targetPath);
            return targetPath.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Lỗi lưu file upload", e);
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }
    }
}