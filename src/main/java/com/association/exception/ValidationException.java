package com.association.exception;

import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;

public class ValidationException extends ApiException {
    private final Map<String, List<String>> errors;

    public ValidationException(Map<String, List<String>> errors) {
        super(HttpStatus.BAD_REQUEST, "Erreur de validation");
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }
}