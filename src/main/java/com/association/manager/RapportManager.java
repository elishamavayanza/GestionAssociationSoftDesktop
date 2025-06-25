package com.association.manager;

import com.association.dao.RapportDao;
import com.association.model.Rapport;
import com.association.model.enums.TypeRapport;
import java.io.File;
import java.util.Date;
import java.util.List;

public class RapportManager extends BaseManager<Rapport> {
    private final RapportDao rapportDao;

    public RapportManager(RapportDao rapportDao) {
        super(rapportDao);
        this.rapportDao = rapportDao;
    }

    public Rapport genererRapportMembres() {
        Rapport rapport = new Rapport();
        rapport.setType(TypeRapport.MEMBRES);
        rapport.setContenu("Contenu du rapport des membres...");
        rapport.setDateGeneration(new Date());
        create(rapport);
        return rapport;
    }

    public Rapport genererRapportContributions() {
        Rapport rapport = new Rapport();
        rapport.setType(TypeRapport.FINANCIER);
        rapport.setContenu("Contenu du rapport des contributions...");
        rapport.setDateGeneration(new Date());
        create(rapport);
        return rapport;
    }

    public Rapport genererRapportEmprunts() {
        Rapport rapport = new Rapport();
        rapport.setType(TypeRapport.FINANCIER);
        rapport.setContenu("Contenu du rapport des emprunts...");
        rapport.setDateGeneration(new Date());
        create(rapport);
        return rapport;
    }

    public List<Rapport> getRapportsByType(TypeRapport type) {
        return rapportDao.findByType(type);
    }

    public List<Rapport> getRapportsByDate(Date date) {
        return rapportDao.findByDate(date);
    }

    public File exporterRapportPDF(Long rapportId) {
        // Implémentation de l'export PDF
        return new File("rapport.pdf");
    }

    public File exporterRapportExcel(Long rapportId) {
        // Implémentation de l'export Excel
        return new File("rapport.xlsx");
    }
}