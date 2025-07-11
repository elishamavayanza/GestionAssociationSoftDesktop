package com.association.model.enums;

public enum TypeRapport {
    MEMBRES("Rapport des membres"),
    FINANCIER("Rapport financier"),
    ACTIVITES("Rapport des activités"),
    MEMBRE_CONTRIBUTION_EMPRUNT("Rapport détaillé membre/contribution/emprunt");

    private final String libelle;

    TypeRapport(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}