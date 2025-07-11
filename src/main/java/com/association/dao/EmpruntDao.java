package com.association.dao;

import com.association.model.transaction.Emprunt;
import com.association.model.enums.StatutEmprunt;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EmpruntDao extends GenericDao<Emprunt> {
    List<Emprunt> findByMembre(Long membreId);
    List<Emprunt> findByStatut(StatutEmprunt statut);
    BigDecimal calculerSoldeRestant(Long empruntId);
    boolean verifierEligibilite(Long membreId);
    Map<String, Object> verifierEligibiliteDetail(Long membreId);
    boolean effectuerRemboursement(Long empruntId, BigDecimal montant);
    Optional<Emprunt> findById(Long id);
    List<Emprunt> findByMembreAndStatutNot(Long membreId, StatutEmprunt statut);
    boolean updateStatut(Long empruntId, StatutEmprunt statut);
    boolean archiverEmprunt(Long empruntId);
    List<Emprunt> findArchivedByMembre(Long membreId);
    @Override
    List<Emprunt> findAll();

    int countAllEmprunts();
    int countEmpruntsByStatut(StatutEmprunt statut);
    int countEmpruntsEnRetard();
    Map<String, Integer> getMonthlyEmprunts(int months);
    Map<StatutEmprunt, BigDecimal> getAmountsByStatus();
    }