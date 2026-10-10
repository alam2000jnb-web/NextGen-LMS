package com.lms.exception;

public class AuthenticationException extends LMSException {
    public AuthenticationException(String message) {
        super(message, "AUTH_FAILED");
    }
}
