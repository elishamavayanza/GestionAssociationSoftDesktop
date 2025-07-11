package com.association.manager;

import com.association.dao.ContributionDao;
import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.dao.RapportDao;
import com.association.model.Membre;
import com.association.model.Rapport;
import com.association.model.enums.TypeContribution;
import com.association.model.enums.TypeRapport;
import com.association.model.transaction.Contribution;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

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
        // Récupérer les données nécessaires
        ContributionDao contributionDao = DAOFactory.getInstance(ContributionDao.class);
        MembreDao membreDao = DAOFactory.getInstance(MembreDao.class);

        // Statistiques globales
        BigDecimal totalContributions = contributionDao.calculerTotalContributions();
        List<Contribution> toutesContributions = contributionDao.findAll();
        Map<TypeContribution, BigDecimal> totalParType = new HashMap<>();
        Map<TypeContribution, Integer> nombreContributionsParType = new HashMap<>();

        // Statistiques par période
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -12);
        Date dateDebut = cal.getTime();
        Date dateFin = new Date();
        List<Contribution> contributions12Mois = contributionDao.findByDateBetween(dateDebut, dateFin);
        Map<String, BigDecimal> contributionsMensuelles = new HashMap<>();

        // Meilleurs contributeurs
        List<Membre> topContributeurs = contributionDao.findTopContributors(dateDebut, dateFin, 10);

        // Calcul des statistiques
        for (TypeContribution type : TypeContribution.values()) {
            totalParType.put(type, BigDecimal.ZERO);
            nombreContributionsParType.put(type, 0);
        }

        SimpleDateFormat moisFormat = new SimpleDateFormat("MMM yyyy");
        for (Contribution c : toutesContributions) {
            // Par type
            TypeContribution type = c.getTypeContribution();
            totalParType.put(type, totalParType.get(type).add(c.getMontant()));
            nombreContributionsParType.put(type, nombreContributionsParType.get(type) + 1);

            // Par mois (pour les 12 derniers mois)
            if (contributions12Mois.contains(c)) {
                String mois = moisFormat.format(c.getDateTransaction());
                contributionsMensuelles.put(mois,
                        contributionsMensuelles.getOrDefault(mois, BigDecimal.ZERO).add(c.getMontant()));
            }
        }

        // Construire le contenu du rapport
        StringBuilder contenu = new StringBuilder();
        contenu.append("=== RAPPORT DES CONTRIBUTIONS ===\n\n");

        // Section Statistiques générales
        contenu.append("STATISTIQUES GENERALES\n");
        contenu.append("----------------------\n");
        contenu.append(String.format("Total des contributions: %s\n", formatMontant(totalContributions)));
        contenu.append(String.format("Nombre total de contributions: %d\n\n", toutesContributions.size()));

        contenu.append("REPARTITION PAR TYPE DE CONTRIBUTION\n");
        contenu.append("-----------------------------------\n");
        for (Map.Entry<TypeContribution, BigDecimal> entry : totalParType.entrySet()) {
            TypeContribution type = entry.getKey();
            BigDecimal montant = entry.getValue();
            int nombre = nombreContributionsParType.get(type);
            double pourcentage = totalContributions.doubleValue() > 0 ?
                    (montant.doubleValue() * 100 / totalContributions.doubleValue()) : 0;

            contenu.append(String.format("- %s: %s (%d contributions, %.1f%%)\n",
                    type, formatMontant(montant), nombre, pourcentage));
        }
        contenu.append("\n");

        // Section Contributions mensuelles
        contenu.append("CONTRIBUTIONS MENSUELLES (12 derniers mois)\n");
        contenu.append("-----------------------------------------\n");
        contributionsMensuelles.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> contenu.append(String.format("- %s: %s\n",
                        entry.getKey(), formatMontant(entry.getValue()))));
        contenu.append("\n");

        // Section Top contributeurs
        contenu.append("TOP 10 DES CONTRIBUTEURS (12 derniers mois)\n");
        contenu.append("-----------------------------------------\n");
        for (int i = 0; i < topContributeurs.size(); i++) {
            Membre membre = topContributeurs.get(i);
            BigDecimal totalMembre = contributionDao.calculerTotalContributionsMembre(membre.getId());
            contenu.append(String.format("%d. %s: %s\n",
                    i+1, membre.getNom(), formatMontant(totalMembre)));
        }
        contenu.append("\n");

        // Section Détails des contributions récentes
        contenu.append("DERNIERES CONTRIBUTIONS\n");
        contenu.append("----------------------\n");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        toutesContributions.stream()
                .sorted((c1, c2) -> c2.getDateTransaction().compareTo(c1.getDateTransaction()))
                .limit(20)
                .forEach(c -> {
                    contenu.append(String.format("- %s: %s (%s)\n",
                            c.getMembre().getNom(),
                            formatMontant(c.getMontant()),
                            c.getTypeContribution()));
                    contenu.append(String.format("  Date: %s | Description: %s\n",
                            dateFormat.format(c.getDateTransaction()),
                            c.getDescription()));
                    contenu.append("\n");
                });

        // Créer et retourner le rapport
        Rapport rapport = new Rapport();
        rapport.setType(TypeRapport.FINANCIER);
        rapport.setContenu(contenu.toString());
        rapport.setDateGeneration(new Date());
        create(rapport);

        return rapport;
    }

    private String formatMontant(BigDecimal montant) {
        return String.format("%,.2f €", montant.doubleValue());
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

    public Rapport genererRapportMembreContributionEmprunt(boolean includeDetails) {
        // Récupérer les données nécessaires
        MembreDao membreDao = DAOFactory.getInstance(MembreDao.class);
        ContributionDao contributionDao = DAOFactory.getInstance(ContributionDao.class);
        // EmpruntDao empruntDao = DAOFactory.getInstance(EmpruntDao.class); // Si vous avez un DAO pour les emprunts

        List<Membre> membres = membreDao.findAll();

        // Construire le contenu du rapport
        StringBuilder contenu = new StringBuilder();
        contenu.append("=== RAPPORT MEMBRE/CONTRIBUTION/EMPRUNT ===\n\n");

        // Section pour chaque membre
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (Membre membre : membres) {
            contenu.append(String.format("MEMBRE: %s (ID: %d)\n", membre.getNom(), membre.getId()));
            contenu.append(String.format("Statut: %s\n", membre.getStatut()));
            contenu.append(String.format("Date inscription: %s\n", dateFormat.format(membre.getDateInscription())));

            // Contributions du membre
            List<Contribution> contributions = contributionDao.findByMembre(membre.getId());
            BigDecimal totalContributions = contributionDao.calculerTotalContributionsMembre(membre.getId());

            contenu.append("\nCONTRIBUTIONS:\n");
            contenu.append(String.format("Total: %s\n", formatMontant(totalContributions)));
            contenu.append(String.format("Nombre: %d\n", contributions.size()));

            if (includeDetails && !contributions.isEmpty()) {
                contenu.append("Détails:\n");
                for (Contribution c : contributions) {
                    contenu.append(String.format("- %s: %s (%s)\n",
                            dateFormat.format(c.getDateTransaction()),
                            formatMontant(c.getMontant()),
                            c.getTypeContribution()));
                }
            }

            // Emprunts du membre (à implémenter si vous avez cette fonctionnalité)
        /*
        List<Emprunt> emprunts = empruntDao.findByMembreId(membre.getId());
        contenu.append("\nEMPRUNTS:\n");
        contenu.append(String.format("Total: %d\n", emprunts.size()));

        if (includeDetails && !emprunts.isEmpty()) {
            contenu.append("Détails:\n");
            for (Emprunt e : emprunts) {
                contenu.append(String.format("- %s: %s\n",
                    dateFormat.format(e.getDateEmprunt()),
                    e.getDescription()));
            }
        }
        */

            contenu.append("\n----------------------------------------\n\n");
        }

        // Créer et retourner le rapport
        Rapport rapport = new Rapport();
        rapport.setType(TypeRapport.MEMBRE_CONTRIBUTION_EMPRUNT);
        rapport.setContenu(contenu.toString());
        rapport.setDateGeneration(new Date());
        create(rapport);

        return rapport;
    }
}