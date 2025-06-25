package com.association.manager;

import com.association.dao.EmpruntDao;
import com.association.model.Membre;
import com.association.model.transaction.Emprunt;
import com.association.model.enums.StatutEmprunt;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class EmpruntManager extends BaseManager<Emprunt> {
    private final EmpruntDao empruntDao;
    private final MembreManager membreManager;

    public EmpruntManager(EmpruntDao empruntDao, MembreManager membreManager) {
        super(empruntDao);
        this.empruntDao = empruntDao;
        this.membreManager = membreManager;
    }

    public boolean demanderEmprunt(Long membreId, BigDecimal montant) {
        if (!verifierEligibilite(membreId)) {
            return false;
        }

        return membreManager.findById(membreId).map(membre -> {
            Emprunt emprunt = new Emprunt();
            emprunt.setMembre(membre);
            emprunt.setMontant(montant);
            emprunt.setDateTransaction(new Date());
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

    public void effectuerRemboursement(Long empruntId, BigDecimal montant) {
        empruntDao.findById(empruntId).ifPresent(emprunt -> {
            emprunt.rembourser(montant);
            update(emprunt);
        });
    }

    public BigDecimal getSoldeRestant(Long empruntId) {
        return empruntDao.calculerSoldeRestant(empruntId);
    }
}