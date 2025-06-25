package com.association.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.NOT_FOUND,
                String.format("%s non trouvé avec %s : '%s'", resourceName, fieldName, fieldValue));
    }
}