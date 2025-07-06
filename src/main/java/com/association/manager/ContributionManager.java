package com.association.manager;

import com.association.dao.ContributionDao;

import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Contribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class ContributionManager extends BaseManager<Contribution> implements Observer {

    private final Observable observable = new Observable() {
        @Override
        public void notifyObservers(Object arg) {
            setChanged();
            super.notifyObservers(arg);
        }
    };

    private final ContributionDao contributionDao;
    private final MembreManager membreManager;
    private static final Logger logger = LoggerFactory.getLogger(ContributionManager.class);

    public ContributionManager(ContributionDao contributionDao, MembreManager membreManager) {
        super(contributionDao);
        this.contributionDao = contributionDao;
        this.membreManager = membreManager;

        // S'enregistrer comme observateur du DAO
    }

    public boolean supprimerContribution(Long membreId, BigDecimal montant, LocalDate date, String typeContribution) {
        // Convertir LocalDate en java.util.Date pour la comparaison
        Date searchDate = java.sql.Date.valueOf(date);

        // Trouver la contribution correspondante
        List<Contribution> contributions = findByMembreAndType(membreId, TypeContribution.valueOf(typeContribution));

        for (Contribution contribution : contributions) {
            if (contribution.getMontant().compareTo(montant) == 0 &&
                    contribution.getDateTransaction().equals(searchDate)) {
                // Supprimer la contribution trouvée
                return delete(contribution.getId());
            }
        }
        return false;
    }


    public boolean enregistrerContribution(Long membreId, BigDecimal montant, LocalDate dateContribution, String typeContribution) {
        return membreManager.findById(membreId).map(membre -> {
            Contribution contribution = new Contribution();
            contribution.setMembre(membre);
            contribution.setMontant(montant);
            contribution.setDateTransaction(java.sql.Date.valueOf(dateContribution));
            contribution.setTypeContribution(TypeContribution.valueOf(typeContribution));
            contribution.setDescription("Contribution " + typeContribution.toLowerCase());
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

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Contribution) {
            Contribution contribution = (Contribution) arg;
            logger.info("Contribution modifiée reçue: {}", contribution.getId());
            // Transmettre la notification aux observateurs du Manager
            notifyObservers(arg);
        } else if (arg instanceof Long) {
            Long contributionId = (Long) arg;
            logger.info("Contribution supprimée reçue: {}", contributionId);
            // Transmettre la notification aux observateurs du Manager
            notifyObservers(arg);
        }
    }

    public List<Contribution> findByMembreAndType(Long membreId, TypeContribution type) {
        return contributionDao.findByMembreAndType(membreId, type);
    }

    public void addObserver(Observer o) {
        observable.addObserver(o);
    }

    public void removeObserver(Observer o) {
        observable.deleteObserver(o);
    }

    protected void notifyObservers(Object arg) {
        observable.notifyObservers(arg); // Utilise maintenant l'override
    }
}