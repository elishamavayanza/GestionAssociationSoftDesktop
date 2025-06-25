package com.association.manager;

import com.association.dao.UtilisateurDao;
import com.association.manager.dto.LoginRequest;
import com.association.util.file.FileStorageService;
import org.springframework.security.core.Authentication;

import java.io.InputStream;

public class SecurityManager {
    private final UtilisateurDao utilisateurDao;
    private final FileStorageService fileStorageService;

    public SecurityManager(UtilisateurDao utilisateurDao, FileStorageService fileStorageService) {
        this.utilisateurDao = utilisateurDao;
        this.fileStorageService = fileStorageService;
    }

    public boolean uploadUserAvatar(Long userId, byte[] avatarFile) {
        // Implémentation du stockage de l'avatar
        return true;
    }

    public InputStream getUserAvatar(Long userId) {
        // Récupération de l'avatar
        return null;
    }

    public Boolean deleteUserAvatar(Long userId) {
        // Suppression de l'avatar
        return true;
    }

    public String generateJwtToken(Authentication authentication) {
        // Génération du token JWT
        return null;
    }

    public Authentication authenticateUser(LoginRequest credentials) {
        // Authentification de l'utilisateur
        return null;
    }
}