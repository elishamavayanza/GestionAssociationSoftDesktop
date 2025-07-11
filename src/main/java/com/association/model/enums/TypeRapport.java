package com.association.model.enums;

public enum TypeRapport {
    MEMBRES("Rapport des membres"),
    EMPRUNT ("Rapport des emprunt"),
    CONTRIBUTION("Rapport contribution");

    private final String libelle;

    TypeRapport(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}