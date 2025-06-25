package com.association.security.service;

import com.association.security.model.Role;

public interface AuthorizationService {
    boolean hasPermission(Long userId, String resource, String action);
    void assignRole(Long userId, Role role);
    void revokeRole(Long userId, Role role);
}