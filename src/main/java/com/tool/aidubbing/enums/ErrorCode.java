package com.tool.aidubbing.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    UNAUTHENTICATED(1001, "Unauthenticated.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission.", HttpStatus.FORBIDDEN),

    USER_NOT_FOUND(2001, "User not found.", HttpStatus.NOT_FOUND),

    JOB_NOT_FOUND(3001, "Job not found.", HttpStatus.NOT_FOUND),

    QUOTA_EXCEEDED(4001, "Quota exceeded.", HttpStatus.BAD_REQUEST),
    INVALID_VIDEO_FILE(4002, "Invalid video file format or size.", HttpStatus.BAD_REQUEST);
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

}
