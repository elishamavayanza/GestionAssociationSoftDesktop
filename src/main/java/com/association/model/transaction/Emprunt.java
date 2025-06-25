package com.association.model.transaction;

import java.math.BigDecimal;
import java.util.Date;
import com.association.model.Membre;
import com.association.model.enums.StatutEmprunt;

public class Emprunt extends Transaction {
    private BigDecimal montantRembourse = BigDecimal.ZERO;
    private Date dateRemboursement;
    private StatutEmprunt statut;

    public Emprunt() {
        super();
        this.statut = StatutEmprunt.EN_COURS;
    }

    public Emprunt(Long id, Date dateCreation, Membre membre, Date dateTransaction,
                   BigDecimal montant, String description) {
        super(id, dateCreation, membre, dateTransaction, montant, description);
        this.statut = StatutEmprunt.EN_COURS;
    }

    // Getters and Setters
    public BigDecimal getMontantRembourse() {
        return montantRembourse;
    }

    public void setMontantRembourse(BigDecimal montantRembourse) {
        this.montantRembourse = montantRembourse;
    }

    public Date getDateRemboursement() {
        return dateRemboursement;
    }

    public void setDateRemboursement(Date dateRemboursement) {
        this.dateRemboursement = dateRemboursement;
    }

    public StatutEmprunt getStatut() {
        return statut;
    }

    public void setStatut(StatutEmprunt statut) {
        this.statut = statut;
    }

    // Methods
    public BigDecimal calculerSoldeRestant() {
        return getMontant().subtract(montantRembourse);
    }

    public void verifierStatut() {
        if (calculerSoldeRestant().compareTo(BigDecimal.ZERO) <= 0) {
            setStatut(StatutEmprunt.REMBOURSE);
        } else if (new Date().after(dateRemboursement)) {
            setStatut(StatutEmprunt.EN_RETARD);
        } else {
            setStatut(StatutEmprunt.EN_COURS);
        }
    }

    public void rembourser(BigDecimal montant) {
        this.montantRembourse = this.montantRembourse.add(montant);
        verifierStatut();
    }
}