package com.association.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {
    public AuthenticationException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}