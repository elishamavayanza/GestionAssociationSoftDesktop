package com.association.model.transaction;

import java.math.BigDecimal;
import java.util.Date;
import com.association.model.Membre;
import com.association.model.enums.StatutEmprunt;

public class Emprunt extends Transaction {
    private BigDecimal montantRembourse = BigDecimal.ZERO;
    private Date dateRemboursement;
    private StatutEmprunt statut;
    // Taux d'intérêt de base (5%)
    private static final BigDecimal TAUX_INTERET = new BigDecimal("0.05");
    // Taux de pénalité par jour de retard (0.5%)
    private static final BigDecimal PENALITE_PAR_JOUR = new BigDecimal("0.005");
    // Nombre maximum de jours pour calculer la pénalité (pour éviter des montants trop élevés)
    private static final int MAX_JOURS_PENALITE = 60;

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
        BigDecimal montantInitial = getMontant();

        // 1. Calculer le montant avec intérêt de base
        BigDecimal montantAvecInteret = montantInitial.add(montantInitial.multiply(TAUX_INTERET));

        // 2. Calculer les pénalités si en retard
        if (StatutEmprunt.EN_RETARD.equals(statut) && dateRemboursement != null) {
            long joursRetard = calculerJoursRetard();
            if (joursRetard > 0) {
                BigDecimal penalite = montantAvecInteret.multiply(PENALITE_PAR_JOUR)
                        .multiply(new BigDecimal(Math.min(joursRetard, MAX_JOURS_PENALITE)));
                montantAvecInteret = montantAvecInteret.add(penalite);
            }
        }

        return montantAvecInteret.subtract(montantRembourse);
    }

    private long calculerJoursRetard() {
        if (dateRemboursement == null) return 0;
        Date aujourdhui = new Date();
        if (aujourdhui.before(dateRemboursement)) return 0;

        long diff = aujourdhui.getTime() - dateRemboursement.getTime();
        return diff / (1000 * 60 * 60 * 24); // Convertir millisecondes en jours
    }

    public void verifierStatut() {
        if (calculerSoldeRestantSansPenalite().compareTo(BigDecimal.ZERO) <= 0) {
            setStatut(StatutEmprunt.REMBOURSE);
        } else if (new Date().after(dateRemboursement)) {
            setStatut(StatutEmprunt.EN_RETARD);
        } else {
            setStatut(StatutEmprunt.EN_COURS);
        }
    }

    // Nouvelle méthode pour calculer le solde sans pénalité (pour affichage)
    public BigDecimal calculerSoldeRestantSansPenalite() {
        BigDecimal montantAvecInteret = getMontant().add(getMontant().multiply(TAUX_INTERET));
        return montantAvecInteret.subtract(montantRembourse);
    }

    public void rembourser(BigDecimal montant) {
        this.montantRembourse = this.montantRembourse.add(montant);
        verifierStatut();
    }
}