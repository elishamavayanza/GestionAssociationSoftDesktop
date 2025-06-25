package com.association.model;

import java.util.Date;
import com.association.model.enums.TypeRapport;

public class Rapport extends Entity {
    private TypeRapport type;
    private String contenu;
    private Date dateGeneration;

    public Rapport() {
        super();
        this.dateGeneration = new Date();
    }

    public Rapport(Long id, Date dateCreation, TypeRapport type, String contenu, Date dateGeneration) {
        super(id, dateCreation);
        this.type = type;
        this.contenu = contenu;
        this.dateGeneration = dateGeneration;
    }

    // Getters and Setters
    public TypeRapport getType() {
        return type;
    }

    public void setType(TypeRapport type) {
        this.type = type;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Date getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(Date dateGeneration) {
        this.dateGeneration = dateGeneration;
    }
}