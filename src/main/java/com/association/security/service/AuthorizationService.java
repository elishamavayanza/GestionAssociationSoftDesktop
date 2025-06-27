package com.association.security.service;

import com.association.model.enums.UserRole;

public interface AuthorizationService {
    boolean hasPermission(Long userId, String resource, String action);
    void assignRole(Long userId, UserRole role);
    void revokeRole(Long userId, UserRole role);
}