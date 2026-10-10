package com.lms.exception;

/**
 * Base exception for all LMS application-specific exceptions.
 * Demonstrates OOP Exception Handling hierarchy.
 */
public class LMSException extends Exception {
    private final String errorCode;

    public LMSException(String message) {
        super(message);
        this.errorCode = "LMS_GENERAL_ERROR";
    }

    public LMSException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public LMSException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "LMS_INTERNAL_ERROR";
    }

    public LMSException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
