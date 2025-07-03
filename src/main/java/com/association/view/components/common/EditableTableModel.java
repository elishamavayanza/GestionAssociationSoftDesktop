package com.association.view.components.common;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.List;

public class EditableTableModel extends DefaultTableModel {
    private final MembreDao membreDao;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public EditableTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
        this.membreDao = DAOFactory.getInstance(MembreDao.class);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Permettre l'édition seulement pour les colonnes Nom, Contact et Statut
        return column == 1 || column == 2 || column == 4;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        // Retourner le type de données approprié pour chaque colonne
        switch (columnIndex) {
            case 0: return Long.class;    // ID
            case 1: return String.class;  // Nom
            case 2: return String.class;  // Contact
            case 3: return String.class;  // Date Inscription
            case 4: return String.class;  // Statut
            default: return Object.class;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        // Récupérer l'ID du membre depuis la première colonne
        Long membreId = (Long) getValueAt(row, 0);
        Membre membre = membreDao.findById(membreId).orElse(null);

        if (membre == null) {
            return;
        }

        // Sauvegarder l'ancienne valeur pour rollback si nécessaire
        Object oldValue = getValueAt(row, column);

        // Mettre à jour la valeur dans le modèle
        super.setValueAt(value, row, column);

        try {
            // Mettre à jour la base de données selon la colonne modifiée
            switch (column) {
                case 1: // Nom
                    membre.setNom(value.toString());
                    break;
                case 2: // Contact
                    membre.setContact(value.toString());
                    break;
                case 4: // Statut
                    membre.setStatut(StatutMembre.valueOf(value.toString().toUpperCase()));
                    break;
            }

            // Enregistrer les modifications
            if (!membreDao.update(membre)) {
                // Rollback si l'update a échoué
                super.setValueAt(oldValue, row, column);
            }
        } catch (Exception e) {
            // En cas d'erreur, restaurer l'ancienne valeur
            super.setValueAt(oldValue, row, column);
            e.printStackTrace();
        }
    }

    public void loadMemberData(List<Membre> membres) {
        setRowCount(0); // Effacer les données existantes

        for (Membre membre : membres) {
            addRow(new Object[]{
                    membre.getId(),
                    membre.getNom(),
                    membre.getContact(),
                    membre.getDateInscription() != null ? dateFormat.format(membre.getDateInscription()) : "N/A",
                    getStatusText(membre.getStatut())
            });
        }
    }

    private String getStatusText(StatutMembre statut) {
        if (statut == null) return "INCONNU";

        return switch (statut) {
            case ACTIF -> "Actif";
            case INACTIF -> "Inactif";
            case SUSPENDU -> "Suspendu";
            default -> statut.name();
        };
    }

    public void removeRow(int row) {
        // Récupérer l'ID du membre à supprimer
        Long membreId = (Long) getValueAt(row, 0);

        // Supprimer de la base de données
        if (membreDao.delete(membreId)) {
            // Si la suppression réussit, supprimer la ligne du modèle
            super.removeRow(row);
        }
    }
}