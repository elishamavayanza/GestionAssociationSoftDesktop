package com.association.util.constants;

public enum DatePattern {
    ISO_DATE("yyyy-MM-dd"),
    FRENCH_DATE("dd/MM/yyyy"),
    TIMESTAMP("yyyy-MM-dd HH:mm:ss");

    private final String pattern;

    DatePattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}