package com.association.manager;

import com.association.dao.EmpruntDao;
import com.association.model.Membre;
import com.association.model.transaction.Emprunt;
import com.association.model.enums.StatutEmprunt;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmpruntManager extends BaseManager<Emprunt> {
    private final EmpruntDao empruntDao;
    private final MembreManager membreManager;
    public static final BigDecimal TAUX_INTERET = new BigDecimal("0.05"); // 5%
    public static final BigDecimal TAUX_PENALITE_PAR_JOUR = new BigDecimal("0.01"); // 1% par jour
    public static final int MAX_JOURS_PENALITE = 30;

    public EmpruntManager(EmpruntDao empruntDao, MembreManager membreManager) {
        super(empruntDao);
        this.empruntDao = empruntDao;
        this.membreManager = membreManager;
    }

    public boolean demanderEmprunt(Long membreId, BigDecimal montant, Date dateRemboursement, String description) {
        if (!verifierEligibilite(membreId)) {
            return false;
        }

        return membreManager.findById(membreId).map(membre -> {
            Emprunt emprunt = new Emprunt();
            emprunt.setMembre(membre);
            emprunt.setMontant(montant);
            emprunt.setDateTransaction(new Date());
            emprunt.setDateCreation(new Date()); // ← très important !
            emprunt.setDateRemboursement(dateRemboursement);
            emprunt.setDescription(description);
            return create(emprunt);
        }).orElse(false);
    }
    public List<Emprunt> getEmpruntsMembre(Long membreId) {
        return empruntDao.findByMembre(membreId);
    }

    public List<Emprunt> getEmpruntsByStatut(StatutEmprunt statut) {
        return empruntDao.findByStatut(statut);
    }

    public boolean verifierEligibilite(Long membreId) {
        return empruntDao.verifierEligibilite(membreId);
    }

    public Map<String, Object> verifierEligibiliteDetail(Long membreId) {
        return empruntDao.verifierEligibiliteDetail(membreId);
    }

    // Dans EmpruntManager.java
    public boolean effectuerRemboursement(Long empruntId, BigDecimal montant) {
        return empruntDao.findById(empruntId)
                .map(emprunt -> {
                    if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Le montant doit être positif");
                    }

                    BigDecimal soldeRestant = emprunt.calculerSoldeRestant();
                    if (montant.compareTo(soldeRestant) > 0) {
                        throw new IllegalArgumentException("Le montant dépasse le solde restant");
                    }

                    return empruntDao.effectuerRemboursement(empruntId, montant);
                })
                .orElse(false);
    }

    public BigDecimal getSoldeRestant(Long empruntId) {
        return empruntDao.calculerSoldeRestant(empruntId);
    }

    public Optional<Emprunt> findById(Long empruntId) {
        return empruntDao.findById(empruntId);
    }

}