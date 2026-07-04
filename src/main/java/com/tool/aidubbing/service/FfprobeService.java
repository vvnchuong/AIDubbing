package com.tool.aidubbing.service;

import com.tool.aidubbing.enums.ErrorCode;
import com.tool.aidubbing.exception.AppException;
import org.springframework.stereotype.Service;

@Service
public class FfprobeService {

    public double getVideoDurationMinutes(String inputPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    inputPath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output;
            try (var reader = process.inputReader()) {
                output = reader.readLine();
            }
            process.waitFor();

            if (output == null || output.isBlank())
                throw new AppException(ErrorCode.INVALID_VIDEO_FILE);

            double seconds = Double.parseDouble(output.trim());
            return seconds / 60.0;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FILE);
        }
    }
}