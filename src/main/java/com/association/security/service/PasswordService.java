package com.association.security.service;

public interface PasswordService {
    String hashPassword(String rawPassword);
    boolean verifyPassword(String rawPassword, String hashedPassword);
    String generateTempPassword(int length);
}