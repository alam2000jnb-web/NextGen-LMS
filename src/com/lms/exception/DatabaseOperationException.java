package com.lms.exception;

public class DatabaseOperationException extends LMSException {
    public DatabaseOperationException(String message) {
        super(message, "DB_ERROR");
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, "DB_ERROR", cause);
    }
}
