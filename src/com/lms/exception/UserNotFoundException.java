package com.lms.exception;

public class UserNotFoundException extends LMSException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }
}
