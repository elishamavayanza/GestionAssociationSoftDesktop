package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.MembreManager;
import com.association.model.enums.StatutMembre;
import com.association.util.file.FileStorageService;
import com.association.util.file.FileStorageServiceAdapter;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.util.Date;

public class AjouterMembrePanel extends JPanel {
    // Composants UI
    private JTextField nomField;
    private JTextField contactField;
    private JButton photoButton;
    private JButton validerButton;
    private JButton annulerButton;
    private JComboBox<StatutMembre> statutComboBox;

    // Données et services
    private final MembreManager membreManager;
    private byte[] photoData;
    private final JFrame parentFrame;

    public AjouterMembrePanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        // Initialisation des services
        FileStorageService fileStorageService = new FileStorageServiceAdapter();
        this.membreManager = new MembreManager(DAOFactory.getInstance(MembreDao.class), fileStorageService);

        // Configuration du layout et style
        setLayout(new BorderLayout(15, 15));
        setBackground(Colors.CURRENT_BACKGROUND);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Ajout de la liste des membres à gauche
        add(createListPanel(), BorderLayout.WEST);

        // Panel principal avec le formulaire
        add(createMainPanel(), BorderLayout.CENTER);

        // Panel des boutons en bas
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createListPanel() {
        ListeMembresPanel listeMembresPanel = new ListeMembresPanel();
        listeMembresPanel.setPreferredSize(new Dimension(250, getHeight()));
        return listeMembresPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Colors.CURRENT_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titleLabel = new JLabel("Ajouter un nouveau membre");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setForeground(Colors.CURRENT_TEXT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        // Réinitialisation des contraintes pour les champs
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;

        // Champ Nom
        addFormField(mainPanel, gbc, 1, "Nom:", nomField = createStyledTextField());

        // Champ Contact
        addFormField(mainPanel, gbc, 2, "Contact:", contactField = createStyledTextField());

        // Champ Statut
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel statutLabel = new JLabel("Statut:");
        statutLabel.setFont(Fonts.labelFont());
        statutLabel.setForeground(Colors.CURRENT_TEXT);
        mainPanel.add(statutLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        statutComboBox = new JComboBox<>(StatutMembre.values());
        statutComboBox.setSelectedItem(StatutMembre.ACTIF);
        styleComboBox(statutComboBox);
        mainPanel.add(statutComboBox, gbc);

        // Bouton Photo
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel photoLabel = new JLabel("Photo:");
        photoLabel.setFont(Fonts.labelFont());
        photoLabel.setForeground(Colors.CURRENT_TEXT);
        mainPanel.add(photoLabel, gbc);

        gbc.gridx = 1;
        photoButton = new JButton("Choisir une photo");
        styleButton(photoButton, false);
        photoButton.addActionListener(this::choisirPhoto);
        mainPanel.add(photoButton, gbc);

        return mainPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Colors.CURRENT_BACKGROUND);

        validerButton = new JButton("Valider");
        styleButton(validerButton, true);
        validerButton.addActionListener(e -> validerFormulaire());
        buttonPanel.add(validerButton);

        annulerButton = new JButton("Annuler");
        styleButton(annulerButton, false);
        annulerButton.addActionListener(e -> parentFrame.dispose());
        buttonPanel.add(annulerButton);

        return buttonPanel;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(Fonts.labelFont());
        label.setForeground(Colors.CURRENT_TEXT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(field, gbc);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(25);
        field.setFont(Fonts.textFieldFont());
        field.setBackground(Colors.CURRENT_INPUT_BACKGROUND);
        field.setForeground(Colors.CURRENT_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(Fonts.buttonFont());
        button.setFocusPainted(false);

        if (primary) {
            button.setBackground(Colors.CURRENT_PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.CURRENT_PRIMARY_DARK),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
        } else {
            button.setBackground(Colors.CURRENT_CARD_BACKGROUND);
            button.setForeground(Colors.CURRENT_TEXT);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
        }
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(Fonts.textFieldFont());
        comboBox.setBackground(Colors.CURRENT_INPUT_BACKGROUND);
        comboBox.setForeground(Colors.CURRENT_TEXT);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void choisirPhoto(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner une photo");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "jpeg", "png", "gif"));

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                photoData = Files.readAllBytes(fileChooser.getSelectedFile().toPath());
                photoButton.setText("Photo sélectionnée");
                photoButton.setIcon(new ImageIcon(
                        new ImageIcon(photoData).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
                ));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la lecture de la photo: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void validerFormulaire() {
        String nom = nomField.getText().trim();
        String contact = contactField.getText().trim();
        StatutMembre statut = (StatutMembre) statutComboBox.getSelectedItem();

        if (nom.isEmpty() || contact.isEmpty()) {
            showWarning("Veuillez remplir tous les champs obligatoires", "Champs manquants");
            return;
        }

        try {
            boolean success = membreManager.ajouterMembre(
                    nom,
                    contact,
                    photoData,
                    new Date(),
                    statut
            );

            if (success) {
                showSuccess("Membre ajouté avec succès", "Succès");
                // parentFrame.dispose(); // Retirer cette ligne
                // Optionnel : Réinitialiser le formulaire
                nomField.setText("");
                contactField.setText("");
                statutComboBox.setSelectedItem(StatutMembre.ACTIF);
                photoData = null;
                photoButton.setText("Choisir une photo");
                photoButton.setIcon(null);
            } else {
                showError("Erreur lors de l'ajout du membre", "Erreur");
            }
        } catch (Exception ex) {
            showError("Erreur technique: " + ex.getMessage(), "Erreur");
            ex.printStackTrace();
        }
    }

    private void showError(String message, String title) {
        showCustomDialog(message, title, Colors.ERROR_BACKGROUND, UIManager.getIcon("OptionPane.errorIcon"));
    }

    private void showWarning(String message, String title) {
        showCustomDialog(message, title, Colors.WARNING_BACKGROUND, UIManager.getIcon("OptionPane.warningIcon"));
    }

    private void showSuccess(String message, String title) {
        showCustomDialog(message, title, Colors.SUCCESS_BACKGROUND, UIManager.getIcon("OptionPane.informationIcon"));
    }

    private void showCustomDialog(String message, String title, Color bgColor, Icon icon) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel messageLabel = new JLabel("<html><div style='width: 250px;'>" + message + "</div></html>");
        messageLabel.setFont(Fonts.labelFont());
        messageLabel.setForeground(Colors.CURRENT_TEXT);

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            panel.add(iconLabel, BorderLayout.WEST);
        }

        panel.add(messageLabel, BorderLayout.CENTER);

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE);
        JDialog dialog = pane.createDialog(this, title);
        dialog.setIconImage(((ImageIcon)icon).getImage());
        dialog.setVisible(true);
    }
}