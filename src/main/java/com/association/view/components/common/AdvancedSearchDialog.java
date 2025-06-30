package com.association.view.components.common;

import com.association.manager.dto.MembreSearchCriteria;
import com.association.model.enums.StatutMembre;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class AdvancedSearchDialog extends JDialog {
    private JTextField nomField;
    private JTextField contactField;
    private JComboBox<StatutMembre> statutCombo;
    private JDateChooser dateFromChooser;
    private JDateChooser dateToChooser;
    private JButton searchButton;
    private JButton resetButton;
    private boolean searchPerformed = false;

    public AdvancedSearchDialog(JFrame parent) {
        super(parent, "Recherche Avancée", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Colors.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Critères de Recherche",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                Fonts.buttonFont(),
                Colors.PRIMARY
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nom
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Nom:"), gbc);

        gbc.gridx = 1;
        nomField = new JTextField(15);
        nomField.setFont(Fonts.textFieldFont());
        contentPanel.add(nomField, gbc);

        // Contact
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Contact:"), gbc);

        gbc.gridx = 1;
        contactField = new JTextField(15);
        contactField.setFont(Fonts.textFieldFont());
        contentPanel.add(contactField, gbc);

        // Statut
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Statut:"), gbc);

        gbc.gridx = 1;
        statutCombo = new JComboBox<>(StatutMembre.values());
        statutCombo.insertItemAt(null, 0); // Option vide
        statutCombo.setSelectedIndex(0);
        statutCombo.setFont(Fonts.textFieldFont());
        contentPanel.add(statutCombo, gbc);

        // Date d'inscription (De)
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Date Inscription (De):"), gbc);

        gbc.gridx = 1;
        dateFromChooser = new JDateChooser();
        dateFromChooser.setDateFormatString("dd/MM/yyyy");
        dateFromChooser.setFont(Fonts.textFieldFont());
        contentPanel.add(dateFromChooser, gbc);

        // Date d'inscription (À)
        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(new JLabel("Date Inscription (À):"), gbc);

        gbc.gridx = 1;
        dateToChooser = new JDateChooser();
        dateToChooser.setDateFormatString("dd/MM/yyyy");
        dateToChooser.setFont(Fonts.textFieldFont());
        contentPanel.add(dateToChooser, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Colors.BACKGROUND);

        searchButton = new JButton("Rechercher");
        searchButton.setFont(Fonts.buttonFont());
        searchButton.setBackground(Colors.PRIMARY);
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> {
            searchPerformed = true;
            setVisible(false);
        });
        buttonPanel.add(searchButton);

        resetButton = new JButton("Réinitialiser");
        resetButton.setFont(Fonts.buttonFont());
        resetButton.setBackground(Colors.SECONDARY);
        resetButton.setForeground(Color.WHITE);
        resetButton.addActionListener(e -> resetFields());
        buttonPanel.add(resetButton);

        JButton cancelButton = new JButton("Annuler");
        cancelButton.setFont(Fonts.buttonFont());
        cancelButton.addActionListener(e -> {
            searchPerformed = false;
            setVisible(false);
        });
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        contentPanel.add(buttonPanel, gbc);

        getContentPane().add(contentPanel);
    }

    public void resetFields() {
        nomField.setText("");
        contactField.setText("");
        statutCombo.setSelectedIndex(0);
        dateFromChooser.setDate(null);
        dateToChooser.setDate(null);
    }

    public boolean isSearchPerformed() {
        return searchPerformed;
    }

    public MembreSearchCriteria getSearchCriteria() {
        MembreSearchCriteria criteria = new MembreSearchCriteria();

        if (!nomField.getText().isEmpty()) {
            criteria.setNom(nomField.getText());
        }

        if (!contactField.getText().isEmpty()) {
            criteria.setContact(contactField.getText());
        }

        if (statutCombo.getSelectedItem() != null) {
            criteria.setStatut((StatutMembre) statutCombo.getSelectedItem());
        }

        if (dateFromChooser.getDate() != null) {
            criteria.setDateInscriptionFrom(dateFromChooser.getDate());
        }

        if (dateToChooser.getDate() != null) {
            criteria.setDateInscriptionTo(dateToChooser.getDate());
        }

        return criteria;
    }
}