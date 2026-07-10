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
    JOB_NOT_READY(3002, "Job not ready.", HttpStatus.BAD_REQUEST),

    QUOTA_EXCEEDED(4001, "Quota exceeded.", HttpStatus.BAD_REQUEST),
    INVALID_VIDEO_FILE(4002, "Invalid video file format or size.", HttpStatus.BAD_REQUEST),

    PLAN_NOT_FOUND(5001, "Plan not found.", HttpStatus.NOT_FOUND),
    PLAN_HAS_EXISTED(5002, "Plan has existed.", HttpStatus.BAD_REQUEST),

    TRANSACTION_NOT_FOUND(6001, "Transaction not found.", HttpStatus.NOT_FOUND),
    TRANSACTION_ALREADY_PROCESSED(6002, "This transaction has already been processed.", HttpStatus.BAD_REQUEST),
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
