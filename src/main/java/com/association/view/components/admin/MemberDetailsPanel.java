package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.util.file.FileStorageService;
import com.association.util.file.RealFileStorageService;
import com.association.view.components.IconManager;
import com.association.view.components.admin.Photo.PhotoEditorDialog;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Observable;
import java.util.Observer;

public class MemberDetailsPanel extends JPanel implements Observer {
    private final JFrame parentFrame;
    private Long membreId;
    private final MembreDao membreDao;
    private final FileStorageService fileStorageService;
    private JLabel nameLabel;
    private JLabel photoLabel;
    private String currentPhotoPath;
    private JLabel footerEmailLabel; // Nouveau champ pour l'email dans le footer

    public MemberDetailsPanel(JFrame parentFrame, Long membreId) {
        this.parentFrame = parentFrame;
        this.membreId = membreId;
        this.membreDao = DAOFactory.getInstance(MembreDao.class);
        this.fileStorageService = new RealFileStorageService();

        membreDao.addObserver(this);

        initComponents();
        initFooter(); // Ajoutez cette ligne

        loadMemberData();

    }

    private void initFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Colors.CARD_BACKGROUND);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Colors.BORDER),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        footerEmailLabel = new JLabel();
        footerEmailLabel.setFont(Fonts.textFieldFont()); // Utilisez la même police que les champs de texte
        footerEmailLabel.setForeground(Colors.TEXT);

        // Aligner à droite avec une icône d'email
        JPanel emailContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        emailContainer.setBackground(Colors.CARD_BACKGROUND);

        // Ajouter une icône d'email avant le texte
        ImageIcon emailIcon = IconManager.getIcon("gmail.svg", 16);
        if (emailIcon != null) {
            JLabel iconLabel = new JLabel(emailIcon);
            emailContainer.add(iconLabel);
        }

        emailContainer.add(footerEmailLabel);
        footerPanel.add(emailContainer, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }


    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel principal avec défilement
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Colors.CARD_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Section nom
        nameLabel = new JLabel("", SwingConstants.CENTER);
        nameLabel.setFont(Fonts.titleFont());
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(nameLabel);
        mainPanel.add(Box.createVerticalStrut(20));


        // Section photo
        photoLabel = new JLabel();
        photoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(photoLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Bouton modifier photo
        JButton editPhotoButton = new JButton("Modifier photo");

        editPhotoButton.setFont(Fonts.buttonFont());
        editPhotoButton.setFocusPainted(false);
        editPhotoButton.setBackground(Colors.CURRENT_DANGER);
        editPhotoButton.setForeground(Color.WHITE);

        ImageIcon photoIcon = IconManager.getScaledIcon("photo_icon.svg", 20, 20);
        if (photoIcon != null) {
            editPhotoButton.setIcon(photoIcon);
            editPhotoButton.setIconTextGap(10);
        }

        editPhotoButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_DANGER.darker()),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

// Effet de survol
        editPhotoButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                editPhotoButton.setBackground(Colors.CURRENT_DANGER.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                editPhotoButton.setBackground(Colors.CURRENT_DANGER);
            }
        });


        editPhotoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editPhotoButton.addActionListener(this::editPhotoAction);
        mainPanel.add(editPhotoButton);
        mainPanel.add(Box.createVerticalStrut(30));

        // Panel pour les 4 cartes
        JPanel cardsPanel = new JPanel(new GridBagLayout());
        cardsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardsPanel.setMaximumSize(new Dimension(600, 400)); // Ajustez la hauteur si nécessaire
        cardsPanel.setBackground(Colors.CARD_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Marge entre les composants
        gbc.fill = GridBagConstraints.BOTH; // Remplir l'espace disponible
        gbc.weightx = 1.0; // Permet l'expansion horizontale
        gbc.weighty = 1.0; // Permet l'expansion verticale

        // Première ligne (2 cartes)
        // Première ligne (2 cartes)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        cardsPanel.add(createInfoCard("Contribution", "0 FCFA", "Contribution"), gbc);

        gbc.gridx = 1;
        cardsPanel.add(createInfoCard("Emprunt", "0 FCFA", "Emprunt"), gbc);

// Deuxième ligne (2 cartes)
        gbc.gridx = 0;
        gbc.gridy = 1;
        cardsPanel.add(createInfoCard("Rempourcement", "0 FCFA", "Rempourcement"), gbc);

        gbc.gridx = 1;
        cardsPanel.add(createInfoCard("Total", "0 FCFA", null), gbc);

// Troisième ligne (1 carte qui prend toute la largeur)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        cardsPanel.add(createInfoCard("Bénéfice", "0 FCFA", null), gbc);

        mainPanel.add(cardsPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Section d'informations supplémentaires
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBackground(Colors.CARD_BACKGROUND);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gbcs = new GridBagConstraints();
        gbcs.insets = new Insets(5, 5, 5, 5);
        gbcs.anchor = GridBagConstraints.WEST;
        gbcs.fill = GridBagConstraints.HORIZONTAL;


        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

// Personnalisation du style du JScrollPane
        scrollPane.setBackground(Colors.CARD_BACKGROUND);
        scrollPane.getViewport().setBackground(Colors.CARD_BACKGROUND);

// Personnalisation des barres de défilement
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setBackground(Colors.CARD_BACKGROUND);
        verticalScrollBar.setForeground(Colors.BORDER);

// Réduire l'épaisseur de la barre de défilement (8px ici)
        verticalScrollBar.setPreferredSize(new Dimension(8, 0)); // Largeur réduite à 8px

        verticalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Colors.BORDER;
                this.trackColor = Colors.CARD_BACKGROUND;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                // Optionnel: Personnaliser le rendu du curseur
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                // Arrondir les coins du curseur
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 4, 4);
                g2.dispose();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createInfoCard(String title, String value, String actionCommand) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Colors.CARD_BACKGROUND);

        // Contenu centré
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CARD_BACKGROUND);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(Fonts.labelFont());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(Fonts.titleFont());
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(valueLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        // Bouton en haut à droite
        JButton cornerButton = new JButton();
        cornerButton.setIcon(IconManager.getIcon("kebab-menu.svg", 20));
        cornerButton.setBorder(BorderFactory.createEmptyBorder());
        cornerButton.setContentAreaFilled(false);

        // Ajouter l'action directement au bouton
        if (actionCommand != null) {
            cornerButton.setActionCommand(actionCommand);
            cornerButton.addActionListener(e -> handleCardAction(actionCommand));
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent
        buttonPanel.add(cornerButton);

        card.add(buttonPanel, BorderLayout.NORTH);

        return card;
    }

    private void handleCardAction(String actionCommand) {
        switch (actionCommand) {
            case "Contribution":
                contributionAction();
                break;
            case "Emprunt":
                empruntAction();
                break;
            case "Rempourcement":
                rempourcementAction();
                break;
        }
    }

    private void contributionAction() {
        JOptionPane.showMessageDialog(this,
                "Ajout de contribution pour le membre " + membreId,
                "Contribution", JOptionPane.INFORMATION_MESSAGE);
    }

    private void empruntAction() {
        JOptionPane.showMessageDialog(this,
                "Ajout d'emprunt pour le membre " + membreId,
                "Emprunt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void rempourcementAction() {
        JOptionPane.showMessageDialog(this,
                "Ajout de rempourcement pour le membre " + membreId,
                "Rempourcement", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadMemberData() {
        Membre membre = membreDao.findById(membreId).orElse(null);
        if (membre != null) {
            nameLabel.setText(membre.getNom());
            currentPhotoPath = membre.getPhoto();
            updatePhotoDisplay();

            // Mettre à jour l'email dans le footer avec formatage HTML
            if (footerEmailLabel != null) {
                String email = membre.getContact();
                if (email != null && !email.isEmpty()) {
                    footerEmailLabel.setText("<html><span style='color:" + toHex(Colors.TEXT_SECONDARY) + "'>Contact: </span>" + email + "</html>");
                } else {
                    footerEmailLabel.setText("");
                }
            }
        } else {
            nameLabel.setText("Membre non trouvé");
            photoLabel.setIcon(null);
            if (footerEmailLabel != null) {
                footerEmailLabel.setText("");
            }
        }
    }

    // Méthode utilitaire pour convertir Color en hex
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    private void updatePhotoDisplay() {
        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            try (InputStream is = fileStorageService.loadFile(currentPhotoPath)) {
                if (is != null) {
                    ImageIcon icon = new ImageIcon(ImageIO.read(is));
                    ImageIcon circularIcon = CircularImageUtil.createCircularIcon(icon.getImage(), 200);
                    photoLabel.setIcon(circularIcon);
                } else {
                    photoLabel.setIcon(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                photoLabel.setIcon(null);
            }
        } else {
            // Image par défaut si aucune photo n'est disponible
            URL defaultAvatarUrl = getClass().getResource("/images/avantar.jpg");
            if (defaultAvatarUrl != null) {
                ImageIcon defaultIcon = new ImageIcon(defaultAvatarUrl);
                ImageIcon circularDefaultIcon = CircularImageUtil.createCircularIcon(defaultIcon.getImage(), 200);
                photoLabel.setIcon(circularDefaultIcon);
            } else {
                // Gérer le cas où l'image par défaut n'est pas trouvée
                photoLabel.setIcon(null);
            }
        }
    }

    private void editPhotoAction(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner une nouvelle photo");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));

        int returnValue = fileChooser.showOpenDialog(parentFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                long maxSize = 5 * 1024 * 1024; // 5MB

                // Vérification de la taille du fichier
                if (selectedFile.length() > maxSize) {
                    JOptionPane.showMessageDialog(this,
                            "La photo est trop grande (max 5MB)",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                byte[] photoBytes = Files.readAllBytes(selectedFile.toPath());

                // Ouvrir l'éditeur de photo
                PhotoEditorDialog editor = new PhotoEditorDialog(parentFrame, photoBytes);
                editor.setVisible(true);

                // Récupérer la photo modifiée
                byte[] editedPhotoBytes = editor.getEditedImageData();
                if (editedPhotoBytes == null) {
                    return; // L'utilisateur a annulé, ne rien faire
                }

                // Stocker la nouvelle photo modifiée
                String newPhotoPath = fileStorageService.storeFile(editedPhotoBytes, "membres/photos");
                if (newPhotoPath != null) {
                    // Mettre à jour dans la base de données
                    if (membreDao.updatePhoto(membreId, newPhotoPath)) {
                        // Supprimer l'ancienne photo si elle existe
                        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                            fileStorageService.deleteFile(currentPhotoPath);
                        }

                        currentPhotoPath = newPhotoPath;
                        updatePhotoDisplay();
                        JOptionPane.showMessageDialog(this, "Photo mise à jour avec succès");
                    } else {
                        // Si l'update échoue, supprimer la nouvelle photo stockée
                        fileStorageService.deleteFile(newPhotoPath);
                        JOptionPane.showMessageDialog(this,
                                "Échec de la mise à jour de la photo",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la lecture du fichier",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public void updateMemberData(Long newMembreId) {
        this.membreId = newMembreId;
        loadMemberData();
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Membre) {
            Membre updatedMembre = (Membre) arg;
            if (updatedMembre.getId().equals(membreId)) {
                SwingUtilities.invokeLater(() -> {
                    nameLabel.setText(updatedMembre.getNom());
                    currentPhotoPath = updatedMembre.getPhoto();
                    updatePhotoDisplay();

                    // Animation fluide pour le changement d'email
                    if (footerEmailLabel != null) {
                        String newEmail = updatedMembre.getContact();
                        fadeTextTransition(footerEmailLabel,
                                "Contact: " + (newEmail != null ? newEmail : ""),
                                300); // Durée de 300ms
                    }
                });
            }
        } else if (arg instanceof Long) {
            // ... gestion des updates par ID ...
        }
    }

    private void fadeTextTransition(JLabel label, String newText, int duration) {
        Timer fadeOutTimer = new Timer(20, null);
        fadeOutTimer.setInitialDelay(0);
        fadeOutTimer.addActionListener(new ActionListener() {
            private float opacity = 1.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.05f;
                if (opacity <= 0) {
                    opacity = 0;
                    label.setText(newText);
                    fadeOutTimer.stop();

                    // Fade in
                    Timer fadeInTimer = new Timer(20, null);
                    fadeInTimer.addActionListener(new ActionListener() {
                        private float fadeInOpacity = 0f;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            fadeInOpacity += 0.05f;
                            if (fadeInOpacity >= 1) {
                                fadeInOpacity = 1;
                                fadeInTimer.stop();
                            }
                            label.setForeground(new Color(
                                    label.getForeground().getRed(),
                                    label.getForeground().getGreen(),
                                    label.getForeground().getBlue(),
                                    (int)(fadeInOpacity * 255)
                            ));
                        }
                    });
                    fadeInTimer.start();
                }
                label.setForeground(new Color(
                        label.getForeground().getRed(),
                        label.getForeground().getGreen(),
                        label.getForeground().getBlue(),
                        (int)(opacity * 255)
                ));
            }
        });
        fadeOutTimer.start();
    }

}