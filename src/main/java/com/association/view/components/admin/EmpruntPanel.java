package com.association.view.components.admin;

import com.association.manager.EmpruntManager;
import com.association.manager.MembreManager;
import com.association.model.transaction.Emprunt;
import com.association.util.constants.DatePattern;
import com.association.util.utils.DateUtil;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EmpruntPanel extends JPanel implements Refreshable {
    private Long membreId;
    private final EmpruntManager empruntManager;
    private final MembreManager membreManager;
    private JTable empruntTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton refreshButton;
    private JTabbedPane parentTabbedPane; // Référence au tabbedPane parent

    public EmpruntPanel(Long membreId, EmpruntManager empruntManager, MembreManager membreManager, JTabbedPane parentTabbedPane) {
        this.membreId = membreId;
        this.empruntManager = empruntManager;
        this.membreManager = membreManager;
        this.parentTabbedPane = parentTabbedPane; // Stockez la référence
        initComponents();
        loadEmprunts();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.CARD_BACKGROUND);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel supérieur avec boutons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topPanel.setBackground(Colors.CARD_BACKGROUND);

        // Bouton Ajouter
        addButton = new JButton("Nouvel emprunt");
        addButton.setFont(Fonts.buttonFont());
        addButton.setBackground(Colors.CURRENT_DANGER);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> {
            Map<String, Object> eligibility = empruntManager.verifierEligibiliteDetail(membreId);
            String message = formatEligibilityMessage(eligibility);

            if ((boolean) eligibility.get("eligible")) {
                int option = JOptionPane.showConfirmDialog(
                        this,
                        message + "\nSouhaitez-vous continuer avec la demande d'emprunt?",
                        "Vérification d'éligibilité",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    showAddEmpruntDialog(e);
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        message,
                        "Non éligible",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        topPanel.add(addButton);

        // Dans initComponents()
        JButton checkButton = new JButton("Vérifier éligibilité");
        checkButton.setFont(Fonts.buttonFont());
        checkButton.setBackground(Colors.CURRENT_INFO);
        checkButton.setForeground(Color.WHITE);
        checkButton.setFocusPainted(false);
        checkButton.addActionListener(e -> {
            Map<String, Object> eligibility = empruntManager.verifierEligibiliteDetail(membreId);
            String message = formatEligibilityMessage(eligibility);

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Statut d'éligibilité",
                    (boolean) eligibility.get("eligible") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        });
        topPanel.add(checkButton);

        // Bouton Actualiser
        refreshButton = new JButton("Actualiser");
        refreshButton.setFont(Fonts.buttonFont());
        refreshButton.setBackground(Colors.CURRENT_INFO);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadEmprunts());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Tableau des emprunts
        String[] columnNames = {"ID", "Date", "Montant", "Remboursé", "Solde", "Statut", "Date Remb."};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        empruntTable = new JTable(tableModel);
        empruntTable.setFont(Fonts.tableFont());
        empruntTable.getTableHeader().setFont(Fonts.tableHeaderFont());
        empruntTable.setRowHeight(30);
        empruntTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        empruntTable.setShowGrid(false);
        empruntTable.setIntercellSpacing(new Dimension(0, 0));
        empruntTable.setBackground(Colors.CARD_BACKGROUND);
        empruntTable.setForeground(Colors.TEXT);

        // Ajoutez le MouseListener pour le double-clic
        empruntTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-clic
                    int row = empruntTable.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        // Basculer vers l'onglet "Rembt"
                        switchToRemboursementTab();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(empruntTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Colors.CARD_BACKGROUND);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Dans EmpruntPanel.java
    private void switchToRemboursementTab() {
        if (parentTabbedPane != null) {
            int selectedRow = empruntTable.getSelectedRow();
            if (selectedRow >= 0) {
                Long empruntId = (Long) tableModel.getValueAt(selectedRow, 0);
                BigDecimal soldeRestant = empruntManager.getSoldeRestant(empruntId);

                // Trouver l'onglet Rembt et passer les données
                for (int i = 0; i < parentTabbedPane.getTabCount(); i++) {
                    if ("Rembt".equals(parentTabbedPane.getTitleAt(i))) {
                        Component comp = parentTabbedPane.getComponentAt(i);
                        if (comp instanceof JScrollPane) {
                            Component view = ((JScrollPane) comp).getViewport().getView();
                            if (view instanceof RemboursementPanel) {
                                ((RemboursementPanel) view).setEmpruntData(empruntId, soldeRestant);
                            }
                        }
                        parentTabbedPane.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void loadEmprunts() {
        tableModel.setRowCount(0); // Effacer les données existantes

        List<Emprunt> emprunts = empruntManager.getEmpruntsMembre(membreId);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DatePattern.DATE_TIME.getPattern());

        for (Emprunt emprunt : emprunts) {
            Object[] rowData = {
                    emprunt.getId(),
                    dateFormat.format(emprunt.getDateTransaction()),
                    formatCurrency(emprunt.getMontant()),
                    formatCurrency(emprunt.getMontantRembourse()),
                    formatCurrency(emprunt.calculerSoldeRestant()),
                    emprunt.getStatut().toString(),
                    emprunt.getDateRemboursement() != null ?
                            dateFormat.format(emprunt.getDateRemboursement()) : "N/A"
            };
            tableModel.addRow(rowData);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 FCFA";
        return String.format("%,.0f FCFA", amount);
    }

    private void showAddEmpruntDialog(ActionEvent e) {
        // Vérifier l'éligibilité avant d'afficher le dialogue
        Map<String, Object> eligibility = empruntManager.verifierEligibiliteDetail(membreId);

        if (!(boolean) eligibility.get("eligible")) {
            // Construire un message détaillé
            StringBuilder message = new StringBuilder("Le membre n'est pas éligible à un emprunt pour les raisons suivantes:\n\n");
            @SuppressWarnings("unchecked")
            List<String> raisons = (List<String>) eligibility.get("raisons");

            for (String raison : raisons) {
                if (!raison.contains("éligible")) { // Exclure le message positif
                    message.append("- ").append(raison).append("\n");
                }
            }

            JOptionPane.showMessageDialog(this,
                    message.toString(),
                    "Non éligible",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Nouvel emprunt");
        dialog.setModal(true);
        dialog.setSize(400, 350); // Légèrement agrandi pour le message d'éligibilité
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colors.CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Ajout d'un message d'éligibilité
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel eligibilityLabel = new JLabel("✔ Membre éligible à l'emprunt");
        eligibilityLabel.setForeground(Colors.CURRENT_SUCCESS);
        eligibilityLabel.setFont(Fonts.smallBoldFont());
        panel.add(eligibilityLabel, gbc);

        // Montant
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Montant (FCFA):"), gbc);

        gbc.gridy = 2;
        JTextField montantField = new JTextField();
        montantField.setFont(Fonts.textFieldFont());
        panel.add(montantField, gbc);

        // Date remboursement
        gbc.gridy = 3;
        panel.add(new JLabel("Date de remboursement:"), gbc);

        gbc.gridy = 4;
        JTextField dateField = new JTextField(DateUtil.formatDate(new Date(), DatePattern.FRENCH_DATE));
        dateField.setFont(Fonts.textFieldFont());
        panel.add(dateField, gbc);

        // Description
        gbc.gridy = 5;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridy = 6;
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(Fonts.textFieldFont());
        descriptionArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Colors.CARD_BACKGROUND);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(ev -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(ev -> {
            try {
                BigDecimal montant = new BigDecimal(montantField.getText().replaceAll("[^\\d.]", ""));
                Date dateRemboursement = DateUtil.parseDate(dateField.getText(), DatePattern.FRENCH_DATE)
                        .orElseThrow(() -> new IllegalArgumentException("Date invalide"));
                String description = descriptionArea.getText();

                if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Le montant doit être positif");
                }

                if (dateRemboursement.before(new Date())) {
                    throw new IllegalArgumentException("La date de remboursement doit être dans le futur");
                }

                if (empruntManager.demanderEmprunt(membreId, montant, dateRemboursement, description)) {
                    JOptionPane.showMessageDialog(this, "Emprunt enregistré avec succès");
                    loadEmprunts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement de l'emprunt",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Montant invalide. Format attendu: 50000 ou 50000.00",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private String formatEligibilityMessage(Map<String, Object> eligibility) {
        @SuppressWarnings("unchecked")
        List<String> raisons = (List<String>) eligibility.get("raisons");
        StringBuilder message = new StringBuilder();

        if ((boolean) eligibility.get("eligible")) {
            message.append("✔ Membre éligible à l'emprunt\n\n");
            message.append("Détails:\n");
            message.append("- Contributions totales: ").append(formatCurrency((BigDecimal) eligibility.get("contributions"))).append("\n");
            message.append("- Statut: ").append(eligibility.get("statut")).append("\n");

            if (eligibility.get("dernierEmprunt") != null) {
                long jours = (long) eligibility.get("joursDepuisDernierEmprunt");
                message.append("- Dernier emprunt il y a ").append(jours).append(" jours\n");
            }
        } else {
            message.append("✖ Membre non éligible\n\n");
            message.append("Raisons:\n");
            for (String raison : raisons) {
                if (!raison.contains("éligible")) {
                    message.append("- ").append(raison).append("\n");
                }
            }
        }

        return message.toString();
    }

    @Override
    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        loadEmprunts();
    }
}