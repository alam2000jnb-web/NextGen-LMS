package com.lms.exception;

public class ValidationException extends LMSException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }
}
