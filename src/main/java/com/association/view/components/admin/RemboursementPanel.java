package com.association.view.components.admin;

import com.association.manager.EmpruntManager;
import com.association.model.transaction.Emprunt;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import com.association.model.enums.StatutEmprunt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;

public class RemboursementPanel extends JPanel {
    private final EmpruntManager empruntManager;
    private Long empruntId;
    private Emprunt emprunt;

    private JLabel montantInitialLabel;
    private JLabel interetLabel;
    private JLabel penaliteLabel;
    private JLabel montantTotalLabel;
    private JLabel montantDejaRembourseLabel;
    private JLabel soldeRestantLabel;
    private JTextField montantRemboursementField;
    private JButton validerButton;
    private JButton rembourserTotalButton;

    public RemboursementPanel(EmpruntManager empruntManager) {
        this.empruntManager = empruntManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.CARD_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel principal avec défilement
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Colors.CARD_BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Colors.CARD_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Titre
        JLabel titleLabel = new JLabel("DÉTAILS DE REMBOURSEMENT");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel);

        // Panel des détails de l'emprunt
        JPanel detailsPanel = createDetailsPanel();
        mainPanel.add(detailsPanel);

        // Panel de saisie du remboursement
        JPanel remboursementPanel = createRemboursementPanel();
        mainPanel.add(remboursementPanel);

        scrollPane.setViewportView(mainPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createTitledBorder("Détails de l'emprunt"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Montant initial
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Montant emprunté:"), gbc);
        montantInitialLabel = createDetailLabel();
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(montantInitialLabel, gbc);

        // Intérêt
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Intérêt (5%):"), gbc);
        interetLabel = createDetailLabel();
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(interetLabel, gbc);

        // Pénalités
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Pénalités de retard:"), gbc);
        penaliteLabel = createDetailLabel();
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(penaliteLabel, gbc);

        // Montant total dû
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel totalLabel = new JLabel("Montant total dû:");
        totalLabel.setFont(Fonts.smallBoldFont());
        panel.add(totalLabel, gbc);
        montantTotalLabel = createDetailLabel();
        montantTotalLabel.setFont(Fonts.smallBoldFont());
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(montantTotalLabel, gbc);

        // Montant déjà remboursé
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Déjà remboursé:"), gbc);
        montantDejaRembourseLabel = createDetailLabel();
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(montantDejaRembourseLabel, gbc);

        // Solde restant
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel soldeLabel = new JLabel("Solde restant:");
        soldeLabel.setFont(Fonts.mediumBoldFont());
        panel.add(soldeLabel, gbc);
        soldeRestantLabel = createDetailLabel();
        soldeRestantLabel.setFont(Fonts.mediumBoldFont());
        soldeRestantLabel.setForeground(Colors.CURRENT_DANGER);
        gbc.gridx = 1; gbc.gridy = 5;
        panel.add(soldeRestantLabel, gbc);

        return panel;
    }

    private JPanel createRemboursementPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createTitledBorder("Effectuer un remboursement"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Montant à rembourser
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Montant à rembourser (FCFA):"), gbc);

        montantRemboursementField = new JTextField();
        montantRemboursementField.setFont(Fonts.textFieldFont());
        montantRemboursementField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateInput(); }
            public void removeUpdate(DocumentEvent e) { validateInput(); }
            public void insertUpdate(DocumentEvent e) { validateInput(); }
        });
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(montantRemboursementField, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Colors.CARD_BACKGROUND);

        rembourserTotalButton = new JButton("Rembourser totalité");
        rembourserTotalButton.setFont(Fonts.buttonFont());
        rembourserTotalButton.setBackground(Colors.CURRENT_SUCCESS);
        rembourserTotalButton.setForeground(Color.WHITE);
        rembourserTotalButton.addActionListener(e -> {
            if (emprunt != null) {
                montantRemboursementField.setText(formatCurrency(emprunt.calculerSoldeRestant()));
            }
        });
        buttonPanel.add(rembourserTotalButton);

        validerButton = new JButton("Valider remboursement");
        validerButton.setFont(Fonts.buttonFont());
        validerButton.setBackground(Colors.CURRENT_PRIMARY);
        validerButton.setForeground(Color.WHITE);
        validerButton.addActionListener(this::validerRemboursement);
        buttonPanel.add(validerButton);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JLabel createDetailLabel() {
        JLabel label = new JLabel("0 FCFA");
        label.setFont(Fonts.textFieldFont());
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    public void setEmpruntData(Long empruntId, BigDecimal soldeRestant) {
        this.empruntId = empruntId;
        this.emprunt = empruntManager.findById(empruntId).orElse(null);
        // Forcer l'affichage du solde passé en paramètre
        if (soldeRestant != null) {
            soldeRestantLabel.setText(formatCurrency(soldeRestant));
        }
        updateDisplay();
    }

    private void updateDisplay() {
        if (emprunt == null) return;

        montantInitialLabel.setText(formatCurrency(emprunt.getMontant()));

        BigDecimal interet = emprunt.getMontant().multiply(new BigDecimal("0.05"));
        interetLabel.setText(formatCurrency(interet));

        BigDecimal penalite = BigDecimal.ZERO;
        if (emprunt.getStatut() == StatutEmprunt.EN_RETARD) {            penalite = emprunt.calculerSoldeRestant().subtract(
                    emprunt.getMontant().add(interet).subtract(emprunt.getMontantRembourse()));
        }
        penaliteLabel.setText(formatCurrency(penalite));

        BigDecimal totalDu = emprunt.getMontant().add(interet).add(penalite);
        montantTotalLabel.setText(formatCurrency(totalDu));

        montantDejaRembourseLabel.setText(formatCurrency(emprunt.getMontantRembourse()));

        soldeRestantLabel.setText(formatCurrency(emprunt.calculerSoldeRestant()));

        montantRemboursementField.setText("");
    }

    private void validateInput() {
        try {
            String text = montantRemboursementField.getText().replaceAll("[^\\d]", "");
            if (!text.isEmpty()) {
                BigDecimal montant = new BigDecimal(text);
                if (emprunt != null && montant.compareTo(emprunt.calculerSoldeRestant()) > 0) {
                    montantRemboursementField.setForeground(Colors.CURRENT_DANGER);
                } else {
                    montantRemboursementField.setForeground(Colors.TEXT);
                }
            }
        } catch (NumberFormatException e) {
            montantRemboursementField.setForeground(Colors.CURRENT_DANGER);
        }
    }

    private void validerRemboursement(ActionEvent e) {
        if (emprunt == null || empruntId == null) {
            showError("Aucun emprunt sélectionné");
            return;
        }

        try {
            String text = montantRemboursementField.getText().replaceAll("[^\\d]", "");
            if (text.isEmpty()) {
                showError("Veuillez saisir un montant");
                return;
            }

            BigDecimal montant = new BigDecimal(text);
            BigDecimal soldeRestant = emprunt.calculerSoldeRestant();

            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Le montant doit être positif");
                return;
            }

            if (montant.compareTo(soldeRestant) > 0) {
                showError("Le montant saisi dépasse le solde restant");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "<html>Confirmez-vous le remboursement de <b>" + formatCurrency(montant) + "</b> ?<br>" +
                            "Solde restant après remboursement: " + formatCurrency(soldeRestant.subtract(montant)) + "</html>",
                    "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = empruntManager.effectuerRemboursement(empruntId, montant);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Remboursement effectué avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    emprunt = empruntManager.findById(empruntId).orElse(null);
                    updateDisplay();
                } else {
                    showError("Erreur lors du remboursement");
                }
            }
        } catch (NumberFormatException ex) {
            showError("Montant invalide");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private String formatCurrency(BigDecimal amount) {
        return NumberFormat.getNumberInstance().format(amount) + " FCFA";
    }
}