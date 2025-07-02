package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.MembreManager;
import com.association.model.enums.StatutMembre;
import com.association.util.file.FileStorageService;
import com.association.util.file.FileStorageServiceAdapter;
import com.association.util.file.RealFileStorageService;
import com.association.view.components.admin.Photo.PhotoEditorDialog;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

public class AjouterMembreDialog extends JDialog {
    // Composants UI
    private JTextField nomField;
    private JTextField contactField;
    private JButton photoButton;
    private JButton validerButton;
    private JButton annulerButton;
    private JComboBox<StatutMembre> statutComboBox;
    private JLabel photoPreviewLabel;

    // Données et services
    private final MembreManager membreManager;
    private byte[] photoData;
    private JPanel circlePanel;

    public AjouterMembreDialog(JFrame parentFrame) {
        super(parentFrame, "Ajouter un nouveau membre", true);

        // Initialisation des services
        FileStorageService fileStorageService = new RealFileStorageService();
        this.membreManager = new MembreManager(DAOFactory.getInstance(MembreDao.class), fileStorageService);

        // Configuration globale des composants
        UIManager.put("ScrollBar.width", 6);

        initUI();
        pack();
        setLocationRelativeTo(parentFrame);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Colors.CURRENT_BACKGROUND);
        ((JComponent)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.CURRENT_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel principal avec BoxLayout pour organiser verticalement
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CURRENT_BACKGROUND);
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Titre centré en haut
        JLabel titleLabel = new JLabel("Ajouter un nouveau membre");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setForeground(Colors.CURRENT_TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Panel pour la photo et le bouton
        JPanel photoPanel = new JPanel();
        photoPanel.setLayout(new BoxLayout(photoPanel, BoxLayout.Y_AXIS));
        photoPanel.setBackground(Colors.CURRENT_BACKGROUND);
        photoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Aperçu de la photo dans un cercle
        photoPreviewLabel = new JLabel();
        photoPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreviewLabel.setPreferredSize(new Dimension(150, 150));

        circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (photoPreviewLabel.getIcon() != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int diameter = Math.min(getWidth(), getHeight());
                    int x = (getWidth() - diameter) / 2;
                    int y = (getHeight() - diameter) / 2;

                    Shape circle = new Ellipse2D.Double(x, y, diameter, diameter);
                    g2.setClip(circle);
                    photoPreviewLabel.getIcon().paintIcon(this, g2, x, y);

                    g2.setClip(null);
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(Colors.CURRENT_BORDER);
                    g2.draw(circle);

                    g2.dispose();
                }
            }
        };
        circlePanel.setPreferredSize(new Dimension(150, 150));
        circlePanel.setMaximumSize(new Dimension(150, 150));
        circlePanel.setBackground(Colors.CURRENT_BACKGROUND);
        circlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoPanel.add(circlePanel);

        // Bouton Photo
        photoButton = new JButton("Ajouter une photo");
        styleButton(photoButton, false);
        photoButton.addActionListener(this::choisirPhoto);
        photoButton.setPreferredSize(new Dimension(180, 40));
        photoButton.setMaximumSize(new Dimension(180, 40));
        photoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoPanel.add(Box.createVerticalStrut(15));
        photoPanel.add(photoButton);

        contentPanel.add(photoPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Panel pour les champs de formulaire (disposés horizontalement)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        formPanel.setBackground(Colors.CURRENT_BACKGROUND);
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Champ Nom
        JPanel nomPanel = new JPanel(new BorderLayout(5, 5));
        nomPanel.setBackground(Colors.CURRENT_BACKGROUND);
        JLabel nomLabel = new JLabel("Nom:");
        nomLabel.setFont(Fonts.labelFont());
        nomLabel.setForeground(Colors.CURRENT_TEXT);
        nomPanel.add(nomLabel, BorderLayout.NORTH);
        nomField = createStyledTextField();
        nomField.setPreferredSize(new Dimension(200, 30));
        nomPanel.add(nomField, BorderLayout.CENTER);
        formPanel.add(nomPanel);

        // Champ Contact
        JPanel contactPanel = new JPanel(new BorderLayout(5, 5));
        contactPanel.setBackground(Colors.CURRENT_BACKGROUND);
        JLabel contactLabel = new JLabel("Contact:");
        contactLabel.setFont(Fonts.labelFont());
        contactLabel.setForeground(Colors.CURRENT_TEXT);
        contactPanel.add(contactLabel, BorderLayout.NORTH);
        contactField = createStyledTextField();
        contactField.setPreferredSize(new Dimension(200, 30));
        contactPanel.add(contactField, BorderLayout.CENTER);
        formPanel.add(contactPanel);

        // Champ Statut
        JPanel statutPanel = new JPanel(new BorderLayout(5, 5));
        statutPanel.setBackground(Colors.CURRENT_BACKGROUND);
        JLabel statutLabel = new JLabel("Statut:");
        statutLabel.setFont(Fonts.labelFont());
        statutLabel.setForeground(Colors.CURRENT_TEXT);
        statutPanel.add(statutLabel, BorderLayout.NORTH);
        statutComboBox = new JComboBox<>(StatutMembre.values());
        statutComboBox.setSelectedItem(StatutMembre.ACTIF);
        styleComboBox(statutComboBox);
        statutComboBox.setPreferredSize(new Dimension(200, 30));
        statutPanel.add(statutComboBox, BorderLayout.CENTER);
        formPanel.add(statutPanel);

        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Panel des boutons Valider/Annuler
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Colors.CURRENT_BACKGROUND);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        validerButton = new JButton("Valider");
        styleButton(validerButton, true);
        validerButton.addActionListener(e -> validerFormulaire());
        buttonPanel.add(validerButton);

        annulerButton = new JButton("Annuler");
        styleButton(annulerButton, false);
        annulerButton.addActionListener(e -> dispose());
        buttonPanel.add(annulerButton);

        contentPanel.add(buttonPanel);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
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
        fileChooser.setDialogTitle("Sélectionner une photo de profil");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Images"));

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] originalPhotoData = Files.readAllBytes(selectedFile.toPath());
                long maxSize = 5 * 1024 * 1024; // 5MB

                if (selectedFile.length() > maxSize) {
                    JOptionPane.showMessageDialog(this,
                            "La photo est trop grande (max 5MB)",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                PhotoEditorDialog editor = new PhotoEditorDialog((JFrame) this.getParent(), originalPhotoData);
                editor.setVisible(true);
                photoData = editor.getEditedImageData();

                if (photoData != null) {
                    ImageIcon icon = new ImageIcon(photoData);
                    // Créer une version circulaire de l'image
                    ImageIcon circularIcon = CircularImageUtil.createCircularIcon(icon.getImage(), 150);
                    photoPreviewLabel.setIcon(circularIcon);
                    photoButton.setText("Changer de photo");

                    // Rafraîchir directement le circlePanel
                    circlePanel.repaint();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la lecture de la photo: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
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
                resetForm();
            } else {
                if (photoData != null && photoData.length > 0) {
                    showError("Erreur lors de l'enregistrement de la photo ou du membre", "Erreur");
                } else {
                    showError("Erreur lors de l'ajout du membre", "Erreur");
                }
            }
        } catch (Exception ex) {
            showError("Erreur technique: " + ex.getMessage(), "Erreur");
            ex.printStackTrace();
        }
    }

    private void resetForm() {
        nomField.setText("");
        contactField.setText("");
        statutComboBox.setSelectedItem(StatutMembre.ACTIF);
        photoData = null;
        photoButton.setText("Ajouter une photo");
        photoPreviewLabel.setIcon(null);
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