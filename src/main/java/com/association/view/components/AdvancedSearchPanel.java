package com.association.view.components;

import com.association.manager.dto.MembreSearchCriteria;
import com.association.model.enums.StatutMembre;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;

public class AdvancedSearchPanel extends JPanel {
    private JTextField nomField;
    private JTextField contactField;
    private JComboBox<StatutMembre> statutCombo;
    private JDateChooser dateFromChooser;
    private JDateChooser dateToChooser;
    private JButton searchButton;
    private JButton resetButton;

    public AdvancedSearchPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Recherche Avancée",
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
        add(new JLabel("Nom:"), gbc);

        gbc.gridx = 1;
        nomField = new JTextField(15);
        nomField.setFont(Fonts.textFieldFont());
        add(nomField, gbc);

        // Contact
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Contact:"), gbc);

        gbc.gridx = 1;
        contactField = new JTextField(15);
        contactField.setFont(Fonts.textFieldFont());
        add(contactField, gbc);

        // Statut
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Statut:"), gbc);

        gbc.gridx = 1;
        statutCombo = new JComboBox<>(StatutMembre.values());
        statutCombo.insertItemAt(null, 0); // Option vide
        statutCombo.setSelectedIndex(0);
        statutCombo.setFont(Fonts.textFieldFont());
        add(statutCombo, gbc);

        // Date d'inscription (De)
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Date Inscription (De):"), gbc);

        gbc.gridx = 1;
        dateFromChooser = new JDateChooser();
        dateFromChooser.setDateFormatString("dd/MM/yyyy");
        dateFromChooser.setFont(Fonts.textFieldFont());
        add(dateFromChooser, gbc);

        // Date d'inscription (À)
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Date Inscription (À):"), gbc);

        gbc.gridx = 1;
        dateToChooser = new JDateChooser();
        dateToChooser.setDateFormatString("dd/MM/yyyy");
        dateToChooser.setFont(Fonts.textFieldFont());
        add(dateToChooser, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Colors.BACKGROUND);

        searchButton = new JButton("Rechercher");
        searchButton.setFont(Fonts.buttonFont());
        searchButton.setBackground(Colors.PRIMARY);
        searchButton.setForeground(Color.WHITE);
        buttonPanel.add(searchButton);

        resetButton = new JButton("Réinitialiser");
        resetButton.setFont(Fonts.buttonFont());
        resetButton.setBackground(Colors.SECONDARY);
        resetButton.setForeground(Color.WHITE);
        buttonPanel.add(resetButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        add(buttonPanel, gbc);

        // Action pour le bouton Réinitialiser
        resetButton.addActionListener(e -> resetFields());
    }

    public void resetFields() {
        nomField.setText("");
        contactField.setText("");
        statutCombo.setSelectedIndex(0);
        dateFromChooser.setDate(null);
        dateToChooser.setDate(null);
    }

    public JButton getSearchButton() {
        return searchButton;
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