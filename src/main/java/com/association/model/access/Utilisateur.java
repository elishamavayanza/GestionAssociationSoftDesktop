package com.association.model.access;

import com.association.model.Entity;
import com.association.model.enums.UserRole;
import com.association.util.file.FileStorageService;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Utilisateur extends Entity {
    private String username;
    private String password; // hashé
    private String email;
    private boolean isActive = true;
    private Date lastLogin;
    private int failedLoginAttempts = 0;
    private Set<UserRole> roles = new HashSet<>();
    private String avatar; // chemin/URL de l'image
    private transient FileStorageService fileStorageService; // transient pour éviter la sérialisation

    // Constructeur par défaut
    public Utilisateur() {
    }

    // Constructeur avec paramètres
    public Utilisateur(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.isActive = true;
        this.roles = new HashSet<>();
    }

    // Getters et Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // Méthodes métier
    public boolean changePassword(String oldPassword, String newPassword) {
        // Implémentation à faire dans un service avec vérification du hash
        return false;
    }

    public void resetPassword() {
        // Implémentation à faire : générer mot de passe temporaire, hasher et notifier
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public boolean hasRole(UserRole role) {
        return this.roles.contains(role);
    }

    public boolean uploadAvatar(byte[] file, FileStorageService storageService) {
        if (file == null || storageService == null) {
            return false;
        }

        try {
            String fileName = storageService.generateUniqueFilename("avatar");
            String filePath = storageService.storeFile(file, "avatars");
            if (filePath != null) {
                this.avatar = filePath;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getAvatarUrl() {
        return this.avatar != null ? "/avatars/" + this.avatar : "/default-avatar.png";
    }

    public byte[] getAvatarData() {
        if (this.avatar == null || this.avatar.isEmpty()) {
            return null;
        }

        try {
            // Utilisez votre FileStorageService pour charger les données
            InputStream inputStream = fileStorageService.loadFile(this.avatar);
            if (inputStream != null) {
                return inputStream.readAllBytes();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public byte[] loadAvatarData() {
        if (this.avatar == null || this.avatar.isEmpty() || fileStorageService == null) {
            return null;
        }

        try (InputStream inputStream = fileStorageService.loadFile(this.avatar)) {
            return inputStream != null ? inputStream.readAllBytes() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}