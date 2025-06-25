package com.association.security.config;

public class PasswordPolicy {
    private int minLength = 8;
    private boolean requireUpper = true;
    private boolean requireLower = true;
    private boolean requireDigit = true;
    private boolean requireSpecial = true;
    private int maxAgeDays = 90;

    public boolean validate(String password) {
        if (password == null || password.length() < minLength) return false;

        boolean hasUpper = !requireUpper || password.matches(".*[A-Z].*");
        boolean hasLower = !requireLower || password.matches(".*[a-z].*");
        boolean hasDigit = !requireDigit || password.matches(".*\\d.*");
        boolean hasSpecial = !requireSpecial || password.matches(".*[!@#$%^&*()].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // Getters et Setters

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public boolean isRequireUpper() {
        return requireUpper;
    }

    public void setRequireUpper(boolean requireUpper) {
        this.requireUpper = requireUpper;
    }

    public boolean isRequireLower() {
        return requireLower;
    }

    public void setRequireLower(boolean requireLower) {
        this.requireLower = requireLower;
    }

    public boolean isRequireDigit() {
        return requireDigit;
    }

    public void setRequireDigit(boolean requireDigit) {
        this.requireDigit = requireDigit;
    }

    public boolean isRequireSpecial() {
        return requireSpecial;
    }

    public void setRequireSpecial(boolean requireSpecial) {
        this.requireSpecial = requireSpecial;
    }

    public int getMaxAgeDays() {
        return maxAgeDays;
    }

    public void setMaxAgeDays(int maxAgeDays) {
        this.maxAgeDays = maxAgeDays;
    }
}
