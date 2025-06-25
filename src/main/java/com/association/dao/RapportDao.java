package com.association.dao;

import com.association.model.Rapport;
import com.association.model.enums.TypeRapport;
import java.util.Date;
import java.util.List;

public interface RapportDao extends GenericDao<Rapport> {
    List<Rapport> findByType(TypeRapport type);
    List<Rapport> findByDate(Date date);
}