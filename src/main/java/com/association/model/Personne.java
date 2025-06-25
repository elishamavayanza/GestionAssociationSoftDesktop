package com.association.model;

import java.sql.Date;

public abstract class Personne extends Entity {
    private String nom;
    private String contact;
    private String photo;

    public Personne() {
        super();
    }

    public Personne(Long id, Date dateCreation, String nom, String contact, String photo) {
        super(id, dateCreation);
        this.nom = nom;
        this.contact = contact;
        this.photo = photo;
    }

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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    // Methods
    public boolean uploadPhoto(byte[] file) {
        // Implémentation pour uploader la photo
        return true;
    }

    public String getPhotoUrl() {
        return this.photo;
    }
}