package com.association.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends ApiException {
    public AuthorizationException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}