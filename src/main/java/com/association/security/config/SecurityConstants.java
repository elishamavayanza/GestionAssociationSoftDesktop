package com.association.security.config;

public class SecurityConstants {
    public static final String SECRET = "${jwt.secret}";
    public static final long EXPIRATION_TIME = 86400000; // 1 jour
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/api/auth/signup";
}