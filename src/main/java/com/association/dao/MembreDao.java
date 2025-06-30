package com.association.dao;

import com.association.manager.dto.MembreSearchCriteria;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import java.util.Date;
import java.util.List;

public interface MembreDao extends GenericDao<Membre> {
    List<Membre> findByNom(String nom);
    List<Membre> findByDateInscription(Date date);
    boolean updatePhoto(Long membreId, String photoPath);
    long countByStatut(StatutMembre statut);
    List<Membre> findTopContributors(int limit);
    List<Membre> findAll();
    List<Membre> search(MembreSearchCriteria criteria);

}