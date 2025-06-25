package com.association.model;

import java.util.Date;

import com.association.model.enums.StatutMembre;

public class Membre extends Personne {
    private Date dateInscription;
    private StatutMembre statut;

    public Membre() {
        super();
        this.statut = StatutMembre.ACTIF;
    }

    public Membre(Long id, Date dateCreation, String nom, String contact, String photo, Date dateInscription) {
        super(id, (java.sql.Date) dateCreation, nom, contact, photo);
        this.dateInscription = dateInscription;
        this.statut = StatutMembre.ACTIF;
    }

    // Getters and Setters
    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }

    public StatutMembre getStatut() {
        return statut;
    }

    public void setStatut(StatutMembre statut) {
        this.statut = statut;
    }

    public enum StatutEmprunt {
    }
}