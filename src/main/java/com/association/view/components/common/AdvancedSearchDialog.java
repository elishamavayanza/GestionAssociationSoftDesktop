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
    private boolean darkMode = false;

    public AdvancedSearchDialog(JFrame parent) {
        super(parent, "Recherche Avancée", true);
        initComponents();
        updateTheme();
        pack();
        setLocationRelativeTo(parent);
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        updateTheme();
    }

    private void updateTheme() {
        Color bg = darkMode ? Colors.DARK_BACKGROUND : Colors.BACKGROUND;
        Color fg = darkMode ? Colors.DARK_TEXT : Colors.TEXT;
        Color borderColor = darkMode ? Colors.DARK_BORDER : Colors.BORDER;

        getContentPane().setBackground(bg);

        // Mettre à jour tous les composants
        Component[] components = getContentPane().getComponents();
        for (Component component : components) {
            updateComponentTheme(component, bg, fg, borderColor);
        }

        // Mettre à jour les JDateChooser
        dateFromChooser.setDarkMode(darkMode);
        dateToChooser.setDarkMode(darkMode);
    }

    private void updateComponentTheme(Component component, Color bg, Color fg, Color borderColor) {
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            panel.setBackground(bg);
            panel.setForeground(fg);

            // Mettre à jour le bord titre si présent
            if (panel.getBorder() instanceof TitledBorder) {
                TitledBorder border = (TitledBorder) panel.getBorder();
                border.setTitleColor(darkMode ? Colors.DARK_PRIMARY : Colors.PRIMARY);
            }

            // Mettre à jour les composants enfants
            for (Component child : panel.getComponents()) {
                updateComponentTheme(child, bg, fg, borderColor);
            }
        } else if (component instanceof JLabel) {
            component.setForeground(fg);
            component.setFont(Fonts.labelFont());
        } else if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            textField.setBackground(darkMode ? Colors.DARK_INPUT_BACKGROUND : Colors.INPUT_BACKGROUND);
            textField.setForeground(fg);
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        } else if (component instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) component;
            comboBox.setBackground(darkMode ? Colors.DARK_INPUT_BACKGROUND : Colors.INPUT_BACKGROUND);
            comboBox.setForeground(fg);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            if (button == searchButton) {
                button.setBackground(darkMode ? Colors.DARK_PRIMARY : Colors.PRIMARY);
            } else if (button == resetButton) {
                button.setBackground(darkMode ? Colors.DARK_SECONDARY : Colors.SECONDARY);
            }
            button.setForeground(Color.WHITE);
        }
    }

    private void initComponents() {
        // Configurer le fond du dialog
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Colors.BACKGROUND);

        // Panel principal
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
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nom
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nomLabel = new JLabel("Nom:");
        nomLabel.setFont(Fonts.labelFont());
        contentPanel.add(nomLabel, gbc);

        gbc.gridx = 1;
        nomField = new JTextField(20);
        nomField.setFont(Fonts.textFieldFont());
        nomField = createStyledTextField();
        contentPanel.add(nomField, gbc);

        // Contact
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel contactLabel = new JLabel("Contact:");
        contactLabel.setFont(Fonts.labelFont());
        contentPanel.add(contactLabel, gbc);

        gbc.gridx = 1;
        contactField = new JTextField(20);
        contactField.setFont(Fonts.textFieldFont());
        contactField = createStyledTextField();
        contentPanel.add(contactField, gbc);

        // Statut
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel statutLabel = new JLabel("Statut:");
        statutLabel.setFont(Fonts.labelFont());
        contentPanel.add(statutLabel, gbc);

        gbc.gridx = 1;
        statutCombo = new JComboBox<>(StatutMembre.values());
        statutCombo.insertItemAt(null, 0); // Option vide
        statutCombo.setSelectedIndex(0);
        statutCombo.setFont(Fonts.textFieldFont());
        styleComboBox(statutCombo);

        contentPanel.add(statutCombo, gbc);

        // Date d'inscription (De)
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel dateFromLabel = new JLabel("Date Inscription (De):");
        dateFromLabel.setFont(Fonts.labelFont());
        contentPanel.add(dateFromLabel, gbc);

        gbc.gridx = 1;
        dateFromChooser = new JDateChooser();
        dateFromChooser.setDateFormatString("dd/MM/yyyy");
        contentPanel.add(dateFromChooser, gbc);

        // Date d'inscription (À)
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel dateToLabel = new JLabel("Date Inscription (À):");
        dateToLabel.setFont(Fonts.labelFont());
        contentPanel.add(dateToLabel, gbc);

        gbc.gridx = 1;
        dateToChooser = new JDateChooser();
        dateToChooser.setDateFormatString("dd/MM/yyyy");
        contentPanel.add(dateToChooser, gbc);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
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
        cancelButton.setBackground(darkMode ? Colors.DARK_BORDER : Colors.BORDER);
        cancelButton.setForeground(darkMode ? Colors.DARK_TEXT : Colors.TEXT);
        cancelButton.addActionListener(e -> {
            searchPerformed = false;
            setVisible(false);
        });
        buttonPanel.add(cancelButton);

        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
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

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(Fonts.textFieldFont());
        field.setBackground(Colors.CURRENT_INPUT_BACKGROUND);
        field.setForeground(Colors.CURRENT_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Effet de survol pour le champ de texte
        field.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.CURRENT_PRIMARY),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });

        return field;
    }
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(Fonts.textFieldFont());
        comboBox.setBackground(Colors.CURRENT_INPUT_BACKGROUND);
        comboBox.setForeground(Colors.CURRENT_TEXT);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Effet de survol pour la comboBox
        comboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.CURRENT_PRIMARY),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }
}