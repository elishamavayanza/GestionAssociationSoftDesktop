package com.association.model.transaction;

import java.math.BigDecimal;
import java.util.Date;
import com.association.model.Entity;
import com.association.model.Membre;

public abstract class Transaction extends Entity {
    private Membre membre;
    private Date dateTransaction;
    private BigDecimal montant;
    private String description;

    public Transaction() {
        super();
    }

    public Transaction(Long id, Date dateCreation, Membre membre, Date dateTransaction,
                       BigDecimal montant, String description) {
        super(id, dateCreation);
        this.membre = membre;
        this.dateTransaction = dateTransaction;
        this.montant = montant;
        this.description = description;
    }

    // Getters and Setters
    public Membre getMembre() {
        return membre;
    }

    public void setMembre(Membre membre) {
        this.membre = membre;
    }

    public Date getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(Date dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}