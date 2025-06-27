package com.association.security.service;

import com.association.model.access.Session;
import com.association.model.access.Utilisateur;
import java.util.Optional;

public interface AuthenticationService {
    Optional<Session> login(String username, String password);
    void logout(String sessionId);
    boolean validateSession(String sessionId);
    Optional<Utilisateur> getCurrentUser(String sessionId);
}