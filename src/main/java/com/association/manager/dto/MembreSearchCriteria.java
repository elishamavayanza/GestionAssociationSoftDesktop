package com.association.manager.dto;

import com.association.model.enums.StatutMembre;
import java.util.Date;

public class MembreSearchCriteria {
    private String nom;
    private String contact;
    private StatutMembre statut;
    private Date dateInscriptionFrom;
    private Date dateInscriptionTo;

    // Getters and Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public StatutMembre getStatut() {
        return statut;
    }

    public void setStatut(StatutMembre statut) {
        this.statut = statut;
    }

    public Date getDateInscriptionFrom() {
        return dateInscriptionFrom;
    }

    public void setDateInscriptionFrom(Date dateInscriptionFrom) {
        this.dateInscriptionFrom = dateInscriptionFrom;
    }

    public Date getDateInscriptionTo() {
        return dateInscriptionTo;
    }

    public void setDateInscriptionTo(Date dateInscriptionTo) {
        this.dateInscriptionTo = dateInscriptionTo;
    }
}