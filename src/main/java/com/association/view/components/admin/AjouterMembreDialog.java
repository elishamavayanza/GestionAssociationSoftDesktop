package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.MembreManager;
import com.association.model.enums.StatutMembre;
import com.association.util.file.FileStorageService;
import com.association.util.file.RealFileStorageService;
import com.association.view.components.IconManager;
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
import java.net.URL;
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
        setDefaultPhoto();

        setResizable(false);
    }
    private void setDefaultPhoto() {
        ImageIcon defaultIcon = loadDefaultPhoto();
        if (defaultIcon != null) {
            photoPreviewLabel.setIcon(defaultIcon);
            circlePanel.repaint();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Colors.CURRENT_BACKGROUND);
        ((JComponent)getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.CURRENT_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel principal avec BoxLayout pour organiser verticalement
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CURRENT_BACKGROUND);
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));

        // Titre centré en haut
        JLabel titleLabel = new JLabel("Ajouter un nouveau membre");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setForeground(Colors.CURRENT_TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

// Effet de survol pour le titre
        titleLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                titleLabel.setForeground(Colors.CURRENT_PRIMARY);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                titleLabel.setForeground(Colors.CURRENT_TEXT);
            }
        });

        contentPanel.add(titleLabel);
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
        // Bouton Photo
        photoButton = new JButton("Ajouter une photo");

// Style avec icône et fond rouge
        photoButton.setFont(Fonts.buttonFont());
        photoButton.setFocusPainted(false);
        photoButton.setBackground(Colors.CURRENT_DANGER);
        photoButton.setForeground(Color.WHITE);

// Ajout de l'icône
        ImageIcon photoIcon = IconManager.getScaledIcon("photo_icon.svg", 20, 20);
        if (photoIcon != null) {
            photoButton.setIcon(photoIcon);
            photoButton.setIconTextGap(10);
        }

        photoButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_DANGER.darker()),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

// Effet de survol
        photoButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                photoButton.setBackground(Colors.CURRENT_DANGER.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                photoButton.setBackground(Colors.CURRENT_DANGER);
            }
        });

        photoButton.addActionListener(this::choisirPhoto);
        photoButton.setPreferredSize(new Dimension(180, 40));
        photoButton.setMaximumSize(new Dimension(180, 40));
        photoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoPanel.add(Box.createVerticalStrut(15));
        photoPanel.add(photoButton);

        contentPanel.add(photoPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Panel pour les champs de formulaire (disposés verticalement)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Colors.CURRENT_BACKGROUND);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Champ Nom
        JPanel nomPanel = new JPanel(new BorderLayout(5, 5));
        nomPanel.setBackground(Colors.CURRENT_BACKGROUND);
        nomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nomPanel.setMaximumSize(new Dimension(300, 60));
        JLabel nomLabel = new JLabel("Nom:");
        nomLabel.setFont(Fonts.labelFont());
        nomLabel.setForeground(Colors.CURRENT_TEXT);
        styleLabel(nomLabel, true); // true pour activer l'effet de survol
        nomPanel.add(nomLabel, BorderLayout.NORTH);
        nomField = createStyledTextField();
        nomField.setPreferredSize(new Dimension(200, 30));
        nomPanel.add(nomField, BorderLayout.CENTER);
        formPanel.add(nomPanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Champ Contact
        JPanel contactPanel = new JPanel(new BorderLayout(5, 5));
        contactPanel.setBackground(Colors.CURRENT_BACKGROUND);
        contactPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contactPanel.setMaximumSize(new Dimension(300, 60));
        JLabel contactLabel = new JLabel("Email :");
        contactLabel.setFont(Fonts.labelFont());
        contactLabel.setForeground(Colors.CURRENT_TEXT);
        styleLabel(contactLabel, true);
        nomPanel.add(nomLabel, BorderLayout.NORTH);

        contactPanel.add(contactLabel, BorderLayout.NORTH);
        contactField = createStyledTextField();
        contactField.setPreferredSize(new Dimension(200, 30));
        contactPanel.add(contactField, BorderLayout.CENTER);
        formPanel.add(contactPanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Champ Statut
        JPanel statutPanel = new JPanel(new BorderLayout(5, 5));
        statutPanel.setBackground(Colors.CURRENT_BACKGROUND);
        statutPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statutPanel.setMaximumSize(new Dimension(300, 60));
        JLabel statutLabel = new JLabel("Statut:");
        statutLabel.setFont(Fonts.labelFont());
        statutLabel.setForeground(Colors.CURRENT_TEXT);
        statutPanel.add(statutLabel, BorderLayout.NORTH);
        styleLabel(statutLabel, true);

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

            // Effet de survol pour le bouton primaire
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(Colors.CURRENT_PRIMARY.darker());
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(Colors.CURRENT_PRIMARY);
                }
            });
        } else {
            button.setBackground(Colors.CURRENT_CARD_BACKGROUND);
            button.setForeground(Colors.CURRENT_TEXT);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));

            // Effet de survol pour le bouton secondaire
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(Colors.CURRENT_BORDER);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(Colors.CURRENT_CARD_BACKGROUND);
                }
            });
        }
    }
    private void styleLabel(JLabel label, boolean withHover) {
        label.setFont(Fonts.labelFont());
        label.setForeground(Colors.CURRENT_TEXT);

        if (withHover) {
            label.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    label.setForeground(Colors.CURRENT_PRIMARY);
                    label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    label.setForeground(Colors.CURRENT_TEXT);
                    label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
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

                // Modification clé ici - vérifier si l'utilisateur a annulé
                byte[] editedPhotoData = editor.getEditedImageData();
                if (editedPhotoData == null) {
                    // L'utilisateur a annulé, ne rien faire
                    return;
                }

                // Seulement mettre à jour si l'utilisateur a confirmé
                photoData = editedPhotoData;
                ImageIcon icon = new ImageIcon(photoData);
                ImageIcon circularIcon = CircularImageUtil.createCircularIcon(icon.getImage(), 150);
                photoPreviewLabel.setIcon(circularIcon);
                photoButton.setText("Changer de photo");
                circlePanel.repaint();

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
//                showSuccess("Membre ajouté avec succès", "Succès");
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
        setDefaultPhoto(); // Au lieu de photoPreviewLabel.setIcon(null);
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
    private ImageIcon loadDefaultPhoto() {
        try {
            URL defaultAvatarUrl = getClass().getResource("/images/avantar.jpg");
            if (defaultAvatarUrl == null) {
                System.err.println("Image par défaut non trouvée dans les ressources");
                return null;
            }

            ImageIcon defaultIcon = new ImageIcon(defaultAvatarUrl);
            if (defaultIcon.getImage() == null) {
                System.err.println("Impossible de charger l'image à partir de l'URL");
                return null;
            }

            return CircularImageUtil.createCircularIcon(defaultIcon.getImage(), 150);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la photo par défaut: " + e.getMessage());
            return null;
        }
    }
}