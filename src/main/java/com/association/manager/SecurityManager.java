package com.association.manager;

import com.association.dao.UtilisateurDao;
import com.association.manager.dto.LoginRequest;
import com.association.security.model.Utilisateur;
import com.association.util.file.FileStorageService;
import com.association.util.utils.ValidationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.io.InputStream;
import java.util.Optional;

public class SecurityManager {
    private final UtilisateurDao utilisateurDao;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final FileStorageService fileStorageService;

    public SecurityManager(UtilisateurDao utilisateurDao, FileStorageService fileStorageService) {
        this.utilisateurDao = utilisateurDao;
        this.fileStorageService = fileStorageService;

    }

    public Utilisateur authenticate(LoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.getUsername().isEmpty()) {
            System.out.println("LoginRequest vide ou username manquant");
            return null;
        }

        System.out.println("Tentative de connexion avec: " + loginRequest.getUsername());

        // Recherche d'abord par email
        Optional<Utilisateur> userOpt = utilisateurDao.findByEmail(loginRequest.getUsername());
        System.out.println("Résultat recherche par email: " + userOpt.isPresent());

        // Si non trouvé, recherche par username
        if (!userOpt.isPresent()) {
            userOpt = utilisateurDao.findByUsername(loginRequest.getUsername());
            System.out.println("Résultat recherche par username: " + userOpt.isPresent());
        }

        if (!userOpt.isPresent()) {
            System.out.println("Utilisateur non trouvé dans la base");
            return null;
        }

        Utilisateur utilisateur = userOpt.get();
        System.out.println("Utilisateur trouvé: " + utilisateur.getUsername());
        System.out.println("Compte actif: " + utilisateur.isActive());

        if (!utilisateur.isActive()) {
            System.out.println("Compte inactif");
            return null;
        }

        System.out.println("Comparaison mot de passe...");
        System.out.println("Mot de passe fourni: " + loginRequest.getPassword());
        System.out.println("Mot de passe stocké (hash): " + utilisateur.getPassword());

        boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), utilisateur.getPassword());
        System.out.println("Résultat comparaison: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println("Mot de passe incorrect");
            return null;
        }

        // Réinitialisation des tentatives échouées
        if (utilisateur.getFailedLoginAttempts() > 0) {
            System.out.println("Réinitialisation des tentatives échouées");
            utilisateur.setFailedLoginAttempts(0);
            utilisateurDao.update(utilisateur);
        }

        return utilisateur;
    }
    public boolean uploadUserAvatar(Long userId, byte[] avatarFile) {
        // Implémentation du stockage de l'avatar
        Optional<Utilisateur> userOpt = utilisateurDao.findById(userId);
        if (userOpt.isPresent() && userOpt.get().getAvatar() != null) {
//            return fileStorageService.load(userOpt.get().getAvatar());
        }
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