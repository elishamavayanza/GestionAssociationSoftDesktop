package com.association.security.service;

import com.association.security.model.Session;
import com.association.security.model.Utilisateur;
import java.util.Optional;

public interface AuthenticationService {
    Optional<Session> login(String username, String password);
    void logout(String sessionId);
    boolean validateSession(String sessionId);
    Optional<Utilisateur> getCurrentUser(String sessionId);
}