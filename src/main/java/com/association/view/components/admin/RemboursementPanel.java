package com.association.view.components.admin;

import com.association.manager.EmpruntManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;

public class RemboursementPanel extends JPanel {
    private final EmpruntManager empruntManager;
    private Long empruntId;
    private BigDecimal soldeRestant;

    private JLabel montantDetteLabel;
    private JTextField pourcentageField;
    private JTextField montantRemboursementField;
    private JButton validerButton;

    public RemboursementPanel(EmpruntManager empruntManager) {
        this.empruntManager = empruntManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.CARD_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Colors.CARD_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titleLabel = new JLabel("Formulaire de Remboursement");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Montant de la dette
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Montant de la dette:"), gbc);

        montantDetteLabel = new JLabel("0 FCFA");
        montantDetteLabel.setFont(Fonts.textFieldFont());
        gbc.gridx = 1;
        mainPanel.add(montantDetteLabel, gbc);

        // Pourcentage à rembourser
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Pourcentage à rembourser (%):"), gbc);

        pourcentageField = new JTextField();
        pourcentageField.setFont(Fonts.textFieldFont());
        pourcentageField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateMontantRemboursement();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateMontantRemboursement();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateMontantRemboursement();
            }
        });        gbc.gridx = 1;
        mainPanel.add(pourcentageField, gbc);

        // Montant à rembourser
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Montant à rembourser:"), gbc);

        montantRemboursementField = new JTextField();
        montantRemboursementField.setFont(Fonts.textFieldFont());
        montantRemboursementField.setEditable(false);
        gbc.gridx = 1;
        mainPanel.add(montantRemboursementField, gbc);

        // Bouton Valider
        validerButton = new JButton("Valider le remboursement");
        validerButton.setFont(Fonts.buttonFont());
        validerButton.setBackground(Colors.CURRENT_SUCCESS);
        validerButton.setForeground(Color.WHITE);
        validerButton.addActionListener(this::validerRemboursement);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(validerButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    public void setEmpruntData(Long empruntId, BigDecimal soldeRestant) {
        this.empruntId = empruntId;
        this.soldeRestant = soldeRestant;
        updateDisplay();
    }

    private void updateDisplay() {
        montantDetteLabel.setText(String.format("%,.0f FCFA", soldeRestant));
        pourcentageField.setText("");
        montantRemboursementField.setText("");
    }

    private void updateMontantRemboursement() {
        try {
            if (soldeRestant == null || pourcentageField.getText().isEmpty()) {
                montantRemboursementField.setText("");
                return;
            }

            double pourcentage = Double.parseDouble(pourcentageField.getText());
            if (pourcentage < 0 || pourcentage > 100) {
                montantRemboursementField.setText("Pourcentage invalide");
                return;
            }

            BigDecimal montant = soldeRestant.multiply(BigDecimal.valueOf(pourcentage / 100));
            montantRemboursementField.setText(String.format("%,.0f FCFA", montant));
        } catch (NumberFormatException e) {
            montantRemboursementField.setText("Saisie invalide");
        }
    }

    private void validerRemboursement(ActionEvent e) {
        try {
            if (empruntId == null || soldeRestant == null) {
                JOptionPane.showMessageDialog(this, "Aucun emprunt sélectionné",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (pourcentageField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir un pourcentage",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double pourcentage = Double.parseDouble(pourcentageField.getText());
            if (pourcentage <= 0 || pourcentage > 100) {
                JOptionPane.showMessageDialog(this, "Le pourcentage doit être entre 0 et 100",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal montant = soldeRestant.multiply(BigDecimal.valueOf(pourcentage / 100));

            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Confirmez-vous le remboursement de %,.0f FCFA?", montant),
                    "Confirmation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = empruntManager.effectuerRemboursement(empruntId, montant);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Remboursement effectué avec succès");
                    updateDisplay();
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors du remboursement",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un pourcentage valide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}