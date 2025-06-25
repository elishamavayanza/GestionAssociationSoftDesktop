package com.association.security.config;

import com.association.security.model.Utilisateur;

public class LoginPolicy {
    private int maxAttempts = 5;
    private int lockoutMinutes = 30;
    private int sessionTimeout = 30; // minutes

    public boolean isAccountLocked(Utilisateur user) {
        return user.getFailedLoginAttempts() >= maxAttempts;
    }

    // Getters et Setters

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getLockoutMinutes() {
        return lockoutMinutes;
    }

    public void setLockoutMinutes(int lockoutMinutes) {
        this.lockoutMinutes = lockoutMinutes;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
