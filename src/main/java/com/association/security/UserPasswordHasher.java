package com.association.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class UserPasswordHasher {

    private static UserPasswordHasher instance;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    private UserPasswordHasher() {}

    public static UserPasswordHasher getInstance () {
        if (instance == null) {
            instance = new UserPasswordHasher();
        }
        return instance;
    }

    public boolean verifyPassword (String plainText, String hashedPassword) {
        return passwordEncoder.matches(plainText, hashedPassword);
    }

    public String hashPassword (String plainTextPassword) {
        return passwordEncoder.encode(plainTextPassword);
    }
}
