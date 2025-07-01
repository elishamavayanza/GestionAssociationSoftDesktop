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

public class AjouterMembrePanel extends JPanel {
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
    private final JFrame parentFrame;
    private JPanel circlePanel; // Ajoutez cette ligne


    public AjouterMembrePanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        // Initialisation des services
        FileStorageService fileStorageService = new RealFileStorageService();
        this.membreManager = new MembreManager(DAOFactory.getInstance(MembreDao.class), fileStorageService);

        // Configuration globale des composants
        UIManager.put("ScrollBar.width", 6);
        UIManager.put("SplitPane.dividerSize", 1);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Colors.CURRENT_BACKGROUND);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerSize(1);
        mainSplitPane.setResizeWeight(0.8);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder());
        mainSplitPane.setContinuousLayout(true);

        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplitPane.setDividerSize(1);
        topSplitPane.setResizeWeight(0.3);
        topSplitPane.setBorder(BorderFactory.createEmptyBorder());
        topSplitPane.setContinuousLayout(true);

        topSplitPane.setLeftComponent(createListPanel());
        topSplitPane.setRightComponent(createMainPanel());

        mainSplitPane.setTopComponent(topSplitPane);
        mainSplitPane.setBottomComponent(createBottomPanel());

        add(mainSplitPane, BorderLayout.CENTER);
    }

    private JPanel createListPanel() {
        ListeMemberbyName listeMemberbyName = new ListeMemberbyName();
        JScrollPane scrollPane = new JScrollPane(listeMemberbyName);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Colors.CURRENT_BACKGROUND);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setBackground(Colors.CURRENT_BACKGROUND);
        verticalScrollBar.setForeground(Colors.CURRENT_BORDER);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(Colors.CURRENT_BACKGROUND);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.setPreferredSize(new Dimension(250, getHeight()));

        return listPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.CURRENT_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel principal avec formulaire et photo
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Colors.CURRENT_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titleLabel = new JLabel("Ajouter un nouveau membre");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setForeground(Colors.CURRENT_TEXT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(titleLabel, gbc);

        // Panel pour les champs de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Colors.CURRENT_BACKGROUND);
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(8, 8, 8, 8);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.anchor = GridBagConstraints.LINE_END;

        // Champ Nom
        addFormField(formPanel, formGbc, 0, "Nom:", nomField = createStyledTextField());

        // Champ Contact
        addFormField(formPanel, formGbc, 1, "Contact:", contactField = createStyledTextField());

        // Champ Statut
        formGbc.gridy = 2;
        formGbc.gridx = 0;
        JLabel statutLabel = new JLabel("Statut:");
        statutLabel.setFont(Fonts.labelFont());
        statutLabel.setForeground(Colors.CURRENT_TEXT);
        formPanel.add(statutLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.anchor = GridBagConstraints.LINE_START;
        statutComboBox = new JComboBox<>(StatutMembre.values());
        statutComboBox.setSelectedItem(StatutMembre.ACTIF);
        styleComboBox(statutComboBox);
        formPanel.add(statutComboBox, formGbc);

        // Ajout du formulaire au contentPanel
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(formPanel, gbc);

        // Panel pour la photo
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBackground(Colors.CURRENT_BACKGROUND);
        photoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Bouton Photo
        photoButton = new JButton("Ajouter une photo");
        styleButton(photoButton, false);
        photoButton.addActionListener(this::choisirPhoto);
        photoButton.setPreferredSize(new Dimension(180, 40));

        // Aperçu de la photo - maintenant dans un cercle
        photoPreviewLabel = new JLabel();
        photoPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreviewLabel.setPreferredSize(new Dimension(150, 150));

        // Remplacer la déclaration locale par l'utilisation de la variable de classe
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

                    // Dessiner l'image
                    Shape circle = new Ellipse2D.Double(x, y, diameter, diameter);
                    g2.setClip(circle);
                    photoPreviewLabel.getIcon().paintIcon(this, g2, x, y);

                    // Dessiner la bordure
                    g2.setClip(null);
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(Colors.CURRENT_BORDER);
                    g2.draw(circle);

                    g2.dispose();
                }
            }
        };
        circlePanel.setPreferredSize(new Dimension(150, 150));
        circlePanel.setBackground(Colors.CURRENT_BACKGROUND);

        photoPanel.add(photoButton, BorderLayout.NORTH);
        photoPanel.add(circlePanel, BorderLayout.CENTER);

        // Ajout du panel photo au contentPanel
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        contentPanel.add(photoPanel, gbc);

        // Panel des boutons en bas
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        contentPanel.add(createButtonPanel(), gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
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

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Colors.CURRENT_BACKGROUND);
        return bottomPanel;
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

                PhotoEditorDialog editor = new PhotoEditorDialog(parentFrame, originalPhotoData);
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