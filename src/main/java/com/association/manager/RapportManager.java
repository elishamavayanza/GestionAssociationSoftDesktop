package com.association.manager;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.dao.RapportDao;
import com.association.model.Membre;
import com.association.model.Rapport;
import com.association.model.enums.TypeRapport;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RapportManager extends BaseManager<Rapport> {
    private final RapportDao rapportDao;

    public RapportManager(RapportDao rapportDao) {
        super(rapportDao);
        this.rapportDao = rapportDao;
    }

    public Rapport genererRapportMembres() {
        // Récupérer les données nécessaires
        MembreDao membreDao = DAOFactory.getInstance(MembreDao.class);
        List<Membre> membres = membreDao.findAll();
        Map<String, Integer> statsParStatut = new HashMap<>();
        Map<String, Integer> inscriptionsMensuelles = membreDao.getMonthlyRegistrations(12); // 12 derniers mois
        Map<String, Integer> membresParTrancheAge = membreDao.getMembersByAgeGroup();

        // Calculer les statistiques
        for (Membre membre : membres) {
            String statut = membre.getStatut().name();
            statsParStatut.put(statut, statsParStatut.getOrDefault(statut, 0) + 1);
        }

        // Construire le contenu du rapport
        StringBuilder contenu = new StringBuilder();
        contenu.append("=== RAPPORT DES MEMBRES ===\n\n");

        // Section Statistiques générales
        contenu.append("STATISTIQUES GENERALES\n");
        contenu.append("----------------------\n");
        contenu.append(String.format("Total membres: %d\n", membres.size()));
        statsParStatut.forEach((statut, count) ->
                contenu.append(String.format("- %s: %d (%.1f%%)\n", statut, count, (count * 100.0 / membres.size())))
        );
        contenu.append("\n");

        // Section Inscriptions mensuelles
        contenu.append("INSCRIPTIONS MENSUELLES (12 derniers mois)\n");
        contenu.append("-----------------------------------------\n");
        inscriptionsMensuelles.forEach((mois, count) ->
                contenu.append(String.format("- %s: %d\n", mois, count))
        );
        contenu.append("\n");

        // Section Répartition par âge
        contenu.append("REPARTITION PAR TRANCHE D'AGE\n");
        contenu.append("---------------------------\n");
        membresParTrancheAge.forEach((tranche, count) ->
                contenu.append(String.format("- %s: %d\n", tranche, count))
        );
        contenu.append("\n");

        // Section Liste des membres
        contenu.append("LISTE DES MEMBRES\n");
        contenu.append("----------------\n");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        membres.forEach(membre -> {
            contenu.append(String.format("- %s (ID: %d)\n", membre.getNom(), membre.getId()));
            contenu.append(String.format("  Statut: %s\n", membre.getStatut()));
            contenu.append(String.format("  Date inscription: %s\n", dateFormat.format(membre.getDateInscription())));
            if (membre.getContact() != null && !membre.getContact().isEmpty()) {
                contenu.append(String.format("  Contact: %s\n", membre.getContact()));
            }
            contenu.append("\n");
        });

        // Créer et retourner le rapport
        Rapport rapport = new Rapport();
        rapport.setType(TypeRapport.MEMBRES);
        rapport.setContenu(contenu.toString());
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

    public Rapport genererRapport(TypeRapport type, boolean includeDetails) {
        Rapport rapport = new Rapport();
        rapport.setType(type);
        rapport.setDateGeneration(new Date());

        switch(type) {
            case MEMBRES:
                rapport.setContenu("Contenu du rapport des membres..." + (includeDetails ? "\nDétails complets inclus" : ""));
                break;
            case FINANCIER:
                rapport.setContenu("Contenu du rapport financier..." + (includeDetails ? "\nDétails complets inclus" : ""));
                break;
            // Ajoutez d'autres cas selon vos besoins
            default:
                rapport.setContenu("Contenu du rapport par défaut..." + (includeDetails ? "\nDétails complets inclus" : ""));
        }

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