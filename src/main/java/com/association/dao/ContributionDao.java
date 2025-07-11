package com.association.dao;

import com.association.model.Membre;
import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Contribution;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ContributionDao extends GenericDao<Contribution> {
    List<Contribution> findByMembre(Long membreId);
    List<Contribution> findByDateBetween(Date start, Date end);
    BigDecimal calculerTotalContributions();
    BigDecimal calculerTotalContributionsMembre(Long membreId);
    List<Membre> findTopContributors(Date startDate, Date endDate, int limit);
    List<Contribution> findByMembreAndType(Long membreId, TypeContribution type);
    Optional<Contribution> findById(Long id);

    Map<String, Object> getContributionStats();
    Map<TypeContribution, BigDecimal> getContributionsByType();
    Map<String, BigDecimal> getMonthlyContributions(int months);
}