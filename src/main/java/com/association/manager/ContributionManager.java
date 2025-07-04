package com.association.manager;

import com.association.dao.ContributionDao;
import com.association.model.Membre;
import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Contribution;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class ContributionManager extends BaseManager<Contribution> {
    private final ContributionDao contributionDao;
    private final MembreManager membreManager;

    public ContributionManager(ContributionDao contributionDao, MembreManager membreManager) {
        super(contributionDao);
        this.contributionDao = contributionDao;
        this.membreManager = membreManager;
    }

    public boolean enregistrerContribution(Long membreId, BigDecimal montant, LocalDate dateContribution) {
        return membreManager.findById(membreId).map(membre -> {
            Contribution contribution = new Contribution();
            contribution.setMembre(membre);
            contribution.setMontant(montant);
            contribution.setDateTransaction(java.sql.Date.valueOf(dateContribution));
            contribution.setTypeContribution(TypeContribution.MENSUEL); // Adaptez selon votre logique
            contribution.setDescription("Contribution hebdomadaire");
            return create(contribution);
        }).orElse(false);
    }

    public List<Contribution> getContributionsMembre(Long membreId) {
        return contributionDao.findByMembre(membreId);
    }

    public List<Contribution> getContributionsBetweenDates(Date start, Date end) {
        return contributionDao.findByDateBetween(start, end);
    }

    public BigDecimal getTotalContributions() {
        return contributionDao.calculerTotalContributions();
    }

    public BigDecimal getTotalContributionsMembre(Long membreId) {
        return contributionDao.calculerTotalContributionsMembre(membreId);
    }

}