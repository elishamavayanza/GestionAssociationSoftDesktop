package com.association.security.service.impl;

import com.association.dao.UtilisateurDao;
import com.association.model.access.Session;
import com.association.model.access.Utilisateur;
import com.association.security.service.AuthenticationService;
import com.association.security.config.LoginPolicy;
import com.association.security.service.PasswordService;

import java.util.Date;
import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final UtilisateurDao utilisateurDao;
    private final PasswordService passwordService;
    private final LoginPolicy loginPolicy;

    public AuthenticationServiceImpl(UtilisateurDao utilisateurDao,
                                     PasswordService passwordService,
                                     LoginPolicy loginPolicy) {
        this.utilisateurDao = utilisateurDao;
        this.passwordService = passwordService;
        this.loginPolicy = loginPolicy;
    }

    @Override
    public Optional<Session> login(String username, String password) {
        Optional<Utilisateur> userOpt = utilisateurDao.findByEmail(username);
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }

        Utilisateur user = userOpt.get();

        if (loginPolicy.isAccountLocked(user)) {
            return Optional.empty();
        }

        if (!passwordService.verifyPassword(password, user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            utilisateurDao.update(user);
            return Optional.empty();
        }

        // Réinitialiser les tentatives après une connexion réussie
        user.setFailedLoginAttempts(0);
        user.setLastLogin(new Date());
        utilisateurDao.update(user);

        Session session = new Session();
        // Initialiser la session
        return Optional.of(session);
    }

    @Override
    public void logout(String sessionId) {
        // Implémentation
    }

    @Override
    public boolean validateSession(String sessionId) {
        // Implémentation
        return false;
    }

    @Override
    public Optional<Utilisateur> getCurrentUser(String sessionId) {
        // Implémentation
        return Optional.empty();
    }
}