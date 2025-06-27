package com.association.view;

import com.association.model.access.Utilisateur;
import com.association.util.file.RealFileStorageService;
import com.association.view.interfaces.InterfaceFactory;
import com.association.view.interfaces.RoleInterface;
import com.association.view.styles.Colors;
import com.association.view.components.*;
import com.association.security.SecurityManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class UserProfilePanel extends JPanel {
    private final LoginFrame loginFrame;
    private final Utilisateur utilisateur;
    private final SecurityManager securityManager = SecurityManager.getInstance();
    private LoadingSpinner loadingSpinner;

    public UserProfilePanel(LoginFrame loginFrame, Utilisateur utilisateur) {
        this.loginFrame = loginFrame;
        this.utilisateur = utilisateur;
        this.utilisateur.setFileStorageService(new RealFileStorageService());

        initComponents();
        startLoadingProcess();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.CURRENT_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Colors.CURRENT_BACKGROUND);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Photo de profil
        ImageIcon avatarIcon = loadAvatarImage();
        Image scaledImage = avatarIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);

        RoundedImagePanel imagePanel = new RoundedImagePanel(new ImageIcon(scaledImage));
        imagePanel.setPreferredSize(new Dimension(120, 120));
        imagePanel.setMaximumSize(new Dimension(120, 120));
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel imageWrapper = new JPanel();
        imageWrapper.setLayout(new FlowLayout(FlowLayout.CENTER));
        imageWrapper.setBackground(Colors.CURRENT_BACKGROUND);
        imageWrapper.add(imagePanel);

        // Nom et email
        JLabel nameLabel = new JLabel(utilisateur.getUsername(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(Colors.CURRENT_TEXT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLabel = new JLabel(utilisateur.getEmail(), SwingConstants.CENTER);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setForeground(Colors.CURRENT_TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Spinner de chargement
        loadingSpinner = new LoadingSpinner();
        loadingSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingSpinner.setVisible(false);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(imageWrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(emailLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(loadingSpinner);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    private ImageIcon loadAvatarImage() {
        try {
            // Essayer de charger l'avatar personnalisé
            byte[] avatarData = utilisateur.loadAvatarData();
            if (avatarData != null && avatarData.length > 0) {
                return new ImageIcon(avatarData);
            }

            // Fallback 1: Image par défaut du package
            URL defaultAvatarUrl = getClass().getResource("/images/avantar.jpg");
            if (defaultAvatarUrl != null) {
                return new ImageIcon(defaultAvatarUrl);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'avatar: " + e.getMessage());
        }

        // Fallback 2: Avatar généré
        return generateDefaultAvatar();
    }

    private ImageIcon generateDefaultAvatar() {
        BufferedImage image = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Fond
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, 120, 120);

        // Cercle
        g.setColor(new Color(200, 200, 200));
        g.fillOval(10, 10, 100, 100);

        // Initiales
        String initials = getInitials(utilisateur.getUsername());
        g.setColor(new Color(100, 100, 100));
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        int x = (120 - fm.stringWidth(initials)) / 2;
        int y = ((120 - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(initials, x, y);

        g.dispose();
        return new ImageIcon(image);
    }

    private String getInitials(String username) {
        if (username == null || username.isEmpty()) {
            return "??";
        }

        String[] parts = username.split(" ");
        if (parts.length == 0) return "??";
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();

        return (parts[0].substring(0, 1) + parts[parts.length-1].substring(0, 1)).toUpperCase();
    }

    private void startLoadingProcess() {
        loadingSpinner.setVisible(true);

        Timer loadingTimer = new Timer(3000, e -> {
            openRoleInterface();
            loadingSpinner.setVisible(false);
        });
        loadingTimer.setRepeats(false);
        loadingTimer.start();
    }

    private void openRoleInterface() {
        RoleInterface roleInterface = InterfaceFactory.createInterface(utilisateur);
        JFrame userFrame = roleInterface.createInterface();
        userFrame.setVisible(true);
        loginFrame.dispose();
    }
}