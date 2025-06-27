package com.association.view;

import com.association.model.access.Utilisateur;
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
        ImageIcon originalIcon;
        byte[] avatarData = utilisateur.loadAvatarData();

        if (avatarData != null && avatarData.length > 0) {
            originalIcon = new ImageIcon(avatarData);
        } else {
            try {
                URL defaultAvatarUrl = getClass().getResource("/images/avantarm.jpg");
                originalIcon = defaultAvatarUrl != null ? new ImageIcon(defaultAvatarUrl)
                        : new ImageIcon(new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB));
            } catch (Exception e) {
                e.printStackTrace();
                originalIcon = new ImageIcon(new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB));
            }
        }

        Image scaledImage = originalIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
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

    private void startLoadingProcess() {
        loadingSpinner.setVisible(true);

        // Simulation d'un temps de chargement
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