package com.association.view.components.common;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeSupport;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class EditableTableModel extends DefaultTableModel {
    private final MembreDao membreDao;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    public final Map<Long, Map<Integer, Object>> pendingChanges = new HashMap<>();
    private final Map<Long, Map<Integer, Object>> originalValues = new HashMap<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public EditableTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
        this.membreDao = DAOFactory.getInstance(MembreDao.class);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 1 || column == 2 || column == 4;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Long.class;
            case 1: return String.class;
            case 2: return String.class;
            case 3: return String.class;
            case 4: return String.class;
            default: return Object.class;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        Long membreId = (Long) getValueAt(row, 0);
        Object oldValue = getValueAt(row, column);

        // Si pas de changement, ne rien faire
        if ((oldValue != null && oldValue.equals(value)) ||
                (value != null && value.equals(oldValue))) {
            return;
        }

        // Stocker la valeur originale si c'est la première modification
        if (!originalValues.containsKey(membreId)) {
            Map<Integer, Object> originalRowValues = new HashMap<>();
            originalRowValues.put(column, oldValue);
            originalValues.put(membreId, originalRowValues);
        } else if (!originalValues.get(membreId).containsKey(column)) {
            originalValues.get(membreId).put(column, oldValue);
        }

        // Stocker la modification en attente
        pendingChanges.computeIfAbsent(membreId, k -> new HashMap<>()).put(column, value);

        // Mettre à jour l'affichage
        super.setValueAt(value, row, column);

        // Notifier les écouteurs du changement
        pcs.firePropertyChange("pendingChanges", null, pendingChanges);
    }

    public boolean commitChanges() {
        boolean allSuccess = true;

        for (Map.Entry<Long, Map<Integer, Object>> entry : pendingChanges.entrySet()) {
            Long membreId = entry.getKey();
            Membre membre = membreDao.findById(membreId).orElse(null);

            if (membre == null) {
                allSuccess = false;
                continue;
            }

            boolean rowSuccess = true;
            for (Map.Entry<Integer, Object> change : entry.getValue().entrySet()) {
                int column = change.getKey();
                Object value = change.getValue();

                try {
                    switch (column) {
                        case 1:
                            membre.setNom(value.toString());
                            break;
                        case 2:
                            membre.setContact(value.toString());
                            break;
                        case 4:
                            membre.setStatut(StatutMembre.valueOf(value.toString().toUpperCase()));
                            break;
                    }
                } catch (Exception e) {
                    rowSuccess = false;
                    e.printStackTrace();
                }
            }

            if (rowSuccess && !membreDao.update(membre)) {
                rowSuccess = false;
            }

            if (!rowSuccess) {
                allSuccess = false;
                // Rollback pour cette ligne
                rollbackRow(membreId);
            }
        }

        if (allSuccess) {
            pendingChanges.clear();
            originalValues.clear();
        }

        // Notifier les écouteurs après commit
        pcs.firePropertyChange("pendingChanges", null, pendingChanges);
        return allSuccess;
    }

    public void rollbackChanges() {
        for (Map.Entry<Long, Map<Integer, Object>> entry : pendingChanges.entrySet()) {
            Long membreId = entry.getKey();
            rollbackRow(membreId);
        }
        pendingChanges.clear();
        originalValues.clear();

        // Notifier les écouteurs après rollback
        pcs.firePropertyChange("pendingChanges", null, pendingChanges);
    }

    private void rollbackRow(Long membreId) {
        if (!originalValues.containsKey(membreId)) return;

        // Trouver la ligne correspondant au membreId
        for (int row = 0; row < getRowCount(); row++) {
            if (membreId.equals(getValueAt(row, 0))) {
                Map<Integer, Object> originalRowValues = originalValues.get(membreId);
                for (Map.Entry<Integer, Object> entry : originalRowValues.entrySet()) {
                    super.setValueAt(entry.getValue(), row, entry.getKey());
                }
                break;
            }
        }
    }

    public boolean hasPendingChanges() {
        return !pendingChanges.isEmpty();
    }

    public void loadMemberData(List<Membre> membres) {
        // Annuler les modifications non enregistrées avant de charger de nouvelles données
        if (hasPendingChanges()) {
            // Création d'une boîte de dialogue personnalisée
            JDialog confirmDialog = new JDialog();
            confirmDialog.setTitle("Modifications non enregistrées");
            confirmDialog.setModal(true);
            confirmDialog.setLayout(new BorderLayout());
            confirmDialog.setSize(400, 200);
            confirmDialog.setLocationRelativeTo(null);
            confirmDialog.getContentPane().setBackground(Colors.BACKGROUND);

            // Panel du message
            JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
            messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            messagePanel.setBackground(Colors.BACKGROUND);

            // Icône d'avertissement
            JLabel iconLabel = new JLabel(IconManager.getIcon("warning.svg", 48));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            messagePanel.add(iconLabel, BorderLayout.WEST);

            // Message
            JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>"
                    + "Vous avez des modifications non enregistrées.<br>"
                    + "Voulez-vous les annuler avant de charger les nouvelles données ?</div></html>");
            messageLabel.setFont(Fonts.textFieldFont());
            messageLabel.setForeground(Colors.TEXT);
            messagePanel.add(messageLabel, BorderLayout.CENTER);

            confirmDialog.add(messagePanel, BorderLayout.CENTER);

            // Panel des boutons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(Colors.BACKGROUND);

            // Bouton Oui
            JButton yesButton = new JButton("Oui", IconManager.getIcon("yes.svg", 16));
            yesButton.setFont(Fonts.buttonFont());
            yesButton.setBackground(Colors.PRIMARY);
            yesButton.setForeground(Color.WHITE);
            yesButton.setFocusPainted(false);
            yesButton.addActionListener(e -> {
                rollbackChanges();
                confirmDialog.dispose();
                loadDataAfterConfirmation(membres);
            });

            // Bouton Non
            JButton noButton = new JButton("Non", IconManager.getIcon("no.svg", 16));
            noButton.setFont(Fonts.buttonFont());
            noButton.setBackground(Colors.SECONDARY);
            noButton.setForeground(Color.WHITE);
            noButton.setFocusPainted(false);
            noButton.addActionListener(e -> {
                confirmDialog.dispose();
                // Ne pas charger les nouvelles données
            });

            buttonPanel.add(yesButton);
            buttonPanel.add(noButton);
            confirmDialog.add(buttonPanel, BorderLayout.SOUTH);

            confirmDialog.setVisible(true);
            return;
        }

        loadDataAfterConfirmation(membres);
    }

    private void loadDataAfterConfirmation(List<Membre> membres) {
        setRowCount(0);
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
        Long membreId = (Long) getValueAt(row, 0);

        // Supprimer les modifications en attente pour cette ligne
        pendingChanges.remove(membreId);
        originalValues.remove(membreId);

        if (membreDao.delete(membreId)) {
            super.removeRow(row);
        }
    }
}