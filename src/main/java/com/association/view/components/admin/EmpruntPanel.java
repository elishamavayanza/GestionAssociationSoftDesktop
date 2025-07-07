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

public class EmpruntPanel extends JPanel implements Refreshable {
    private Long membreId;
    private final EmpruntManager empruntManager;
    private final MembreManager membreManager;
    private JTable empruntTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton rembourserButton;
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
        addButton.addActionListener(this::showAddEmpruntDialog);
        topPanel.add(addButton);

        // Bouton Rembourser
        rembourserButton = new JButton("Effectuer remboursement");
        rembourserButton.setFont(Fonts.buttonFont());
        rembourserButton.setBackground(Colors.CURRENT_SUCCESS);
        rembourserButton.setForeground(Color.WHITE);
        rembourserButton.setFocusPainted(false);
        rembourserButton.addActionListener(this::showRemboursementDialog);
        topPanel.add(rembourserButton);

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

    private void switchToRemboursementTab() {
        if (parentTabbedPane != null) {
            // Trouver l'index de l'onglet "Rembt"
            for (int i = 0; i < parentTabbedPane.getTabCount(); i++) {
                if ("Rembt".equals(parentTabbedPane.getTitleAt(i))) {
                    parentTabbedPane.setSelectedIndex(i);
                    break;
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
        return String.format("%,.0f FCFA", amount);
    }

    private void showAddEmpruntDialog(ActionEvent e) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Nouvel emprunt");
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colors.CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Montant
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Montant (FCFA):"), gbc);

        gbc.gridy = 1;
        JTextField montantField = new JTextField();
        montantField.setFont(Fonts.textFieldFont());
        panel.add(montantField, gbc);

        // Date remboursement
        gbc.gridy = 2;
        panel.add(new JLabel("Date de remboursement:"), gbc);

        gbc.gridy = 3;
        JTextField dateField = new JTextField(DateUtil.formatDate(new Date(), DatePattern.FRENCH_DATE));
        dateField.setFont(Fonts.textFieldFont());
        panel.add(dateField, gbc);

        // Description
        gbc.gridy = 4;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridy = 5;
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
                BigDecimal montant = new BigDecimal(montantField.getText());
                Date dateRemboursement = DateUtil.parseDate(dateField.getText(), DatePattern.FRENCH_DATE)
                        .orElseThrow(() -> new IllegalArgumentException("Date invalide"));
                String description = descriptionArea.getText();

                if (empruntManager.demanderEmprunt(membreId, montant, dateRemboursement, description)) {
                    JOptionPane.showMessageDialog(this, "Emprunt enregistré avec succès");
                    loadEmprunts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement de l'emprunt",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Montant invalide",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showRemboursementDialog(ActionEvent e) {
        int selectedRow = empruntTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un emprunt",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long empruntId = (Long) tableModel.getValueAt(selectedRow, 0);
        BigDecimal soldeRestant = empruntManager.getSoldeRestant(empruntId);

        JDialog dialog = new JDialog();
        dialog.setTitle("Remboursement d'emprunt");
        dialog.setModal(true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colors.CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Solde restant
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Solde restant:"), gbc);

        gbc.gridy = 1;
        JLabel soldeLabel = new JLabel(formatCurrency(soldeRestant));
        soldeLabel.setFont(Fonts.textFieldFont());
        panel.add(soldeLabel, gbc);

        // Montant remboursement
        gbc.gridy = 2;
        panel.add(new JLabel("Montant remboursé:"), gbc);

        gbc.gridy = 3;
        JTextField montantField = new JTextField(soldeRestant.toString());
        montantField.setFont(Fonts.textFieldFont());
        panel.add(montantField, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Colors.CARD_BACKGROUND);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(ev -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener(ev -> {
            try {
                BigDecimal montant = new BigDecimal(montantField.getText());
                if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Le montant doit être positif");
                }
                if (montant.compareTo(soldeRestant) > 0) {
                    throw new IllegalArgumentException("Le montant ne peut pas dépasser le solde restant");
                }

                empruntManager.effectuerRemboursement(empruntId, montant);
                JOptionPane.showMessageDialog(this, "Remboursement enregistré avec succès");
                loadEmprunts();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Montant invalide",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    @Override
    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        loadEmprunts();
    }
}