package com.lms.exception;

public class EnrollmentException extends LMSException {
    public EnrollmentException(String message) {
        super(message, "ENROLLMENT_ERROR");
    }
}
