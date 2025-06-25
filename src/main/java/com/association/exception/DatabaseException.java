package com.association.exception;

import org.springframework.http.HttpStatus;

public class DatabaseException extends ApiException {
    public DatabaseException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        initCause(cause);
    }
}