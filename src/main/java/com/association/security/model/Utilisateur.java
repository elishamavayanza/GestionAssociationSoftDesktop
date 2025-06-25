package com.association.security.model;

import com.association.model.Entity;

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
    private Set<Role> roles = new HashSet<>();
    private String avatar; // chemin/URL de l'image

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    // Méthodes métier à compléter
    public boolean changePassword(String oldPassword, String newPassword) {
        // Implémentation à faire dans un service avec vérification du hash
        return false;
    }

    public void resetPassword() {
        // Implémentation à faire : générer mot de passe temporaire, hasher et notifier
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    public boolean uploadAvatar(byte[] file) {
        // Implémentation à faire via un service de stockage
        return false;
    }

    public String getAvatarUrl() {
        return this.avatar != null ? "/avatars/" + this.avatar : "/default-avatar.png";
    }
}
