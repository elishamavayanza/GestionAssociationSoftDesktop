package com.association.manager;

import com.association.dao.ContributionDao;

import com.association.model.Membre;
import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Contribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


public class ContributionManager extends BaseManager<Contribution> implements Observer {

    private final ContributionDao contributionDao;
    private final MembreManager membreManager;
    private static final Logger logger = LoggerFactory.getLogger(ContributionManager.class);

    public ContributionManager(ContributionDao contributionDao, MembreManager membreManager) {
        super(contributionDao);
        this.contributionDao = contributionDao;
        this.membreManager = membreManager;

        contributionDao.addObserver(this);

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
            // Vérifier si une contribution existe déjà
            List<Contribution> existing = findByMembreAndType(membreId, TypeContribution.valueOf(typeContribution))
                    .stream()
                    .filter(c -> c.getDateTransaction().equals(java.sql.Date.valueOf(dateContribution)))
                    .filter(c -> c.getMontant().compareTo(montant) == 0)
                    .collect(Collectors.toList());

            if (!existing.isEmpty()) {
                logger.warn("Contribution existante trouvée pour membre {}, date {} et montant {}",
                        membreId, dateContribution, montant);
                return false; // ou return true si vous considérez que c'est OK
            }

            Contribution contribution = new Contribution();
            contribution.setMembre(membre);
            contribution.setMontant(montant);
            contribution.setDateTransaction(java.sql.Date.valueOf(dateContribution));
            contribution.setTypeContribution(TypeContribution.valueOf(typeContribution));
            contribution.setDescription("Contribution " + typeContribution.toLowerCase());
            return create(contribution);
        }).orElse(false);
    }

    public boolean enregistrerContribution(Long membreId, BigDecimal montant, LocalDate dateContribution,
                                           String typeContribution, String description) {
        return membreManager.findById(membreId).map(membre -> {
            Contribution contribution = new Contribution();
            contribution.setMembre(membre);
            contribution.setMontant(montant);
            contribution.setDateTransaction(java.sql.Date.valueOf(dateContribution));
            contribution.setTypeContribution(TypeContribution.valueOf(typeContribution));
            contribution.setDescription(description);
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

    public List<Contribution> findByMembreAndType(Long membreId, TypeContribution type) {
        return contributionDao.findByMembreAndType(membreId, type);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Contribution) {
            Contribution contribution = (Contribution) arg;
            logger.info("Contribution modifiée reçue par l'observateur - ID: {}, Membre: {}, Montant: {}",
                    contribution.getId(),
                    contribution.getMembre().getId(),
                    contribution.getMontant());

            // Exemple d'actions possibles:
            // - Mettre à jour des statistiques en cache
            // - Notifier d'autres composants du système
            // - Vérifier des règles métier

        } else if (arg instanceof Long) {
            Long contributionId = (Long) arg;
            logger.info("Contribution supprimée reçue par l'observateur - ID: {}", contributionId);

            // Nettoyage ou mise à jour si nécessaire
        }
    }

    public Map<String, Object> getContributionStats() {
        return contributionDao.getContributionStats();
    }

    public Map<TypeContribution, BigDecimal> getContributionsByType() {
        return contributionDao.getContributionsByType();
    }

    public Map<String, BigDecimal> getMonthlyContributions(int months) {
        return contributionDao.getMonthlyContributions(months);
    }

    public Map<String, BigDecimal> getTopContributors(int limit) {
        // Implémentation existante ou à adapter
        Map<String, BigDecimal> topContributors = new LinkedHashMap<>();
        List<Membre> membres = contributionDao.findTopContributors(
                Date.from(LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                new Date(),
                limit
        );

        for (Membre membre : membres) {
            BigDecimal total = contributionDao.calculerTotalContributionsMembre(membre.getId());
            topContributors.put(membre.getNom(), total);
        }

        return topContributors;
    }

}