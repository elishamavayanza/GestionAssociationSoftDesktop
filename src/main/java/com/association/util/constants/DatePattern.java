package com.association.util.constants;

public enum DatePattern {
    ISO_DATE("yyyy-MM-dd"),
    FRENCH_DATE("dd/MM/yyyy"),
    SHORT_DATE("dd/MM/yyyy"), // Ajouté comme alias de FRENCH_DATE
    DATE_TIME("dd/MM/yyyy HH:mm:ss"), // Added this line
    TIMESTAMP("yyyy-MM-dd HH:mm:ss");

    private final String pattern;

    DatePattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}