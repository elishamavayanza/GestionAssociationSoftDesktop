package com.association.exception;

import org.springframework.http.HttpStatus;

public class FileStorageException extends ApiException {
    public FileStorageException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        initCause(cause);
    }
}