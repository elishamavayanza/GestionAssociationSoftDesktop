package com.association.view;

import com.association.security.model.Utilisateur;
import com.association.view.interfaces.InterfaceFactory;
import com.association.view.interfaces.RoleInterface;
import com.association.view.styles.Colors;
import com.association.view.components.*;
import com.association.manager.SecurityManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;

public class UserProfileFrame extends JFrame {
    private Utilisateur utilisateur;
    private SecurityManager securityManager;

    public UserProfileFrame(Utilisateur utilisateur, SecurityManager securityManager) {
        this.utilisateur = utilisateur;
        this.securityManager = securityManager;
        initComponents();
        setupFrame();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.CURRENT_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel principal pour centrer le contenu
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Colors.CURRENT_BACKGROUND);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Photo de profil
        ImageIcon originalIcon;
        if (utilisateur.getAvatar() != null && !utilisateur.getAvatar().isEmpty()) {
            originalIcon = new ImageIcon(utilisateur.getAvatar());
        } else {
            try {
                URL defaultAvatarUrl = getClass().getResource("/images/avantar.jpg");
                originalIcon = defaultAvatarUrl != null ? new ImageIcon(defaultAvatarUrl)
                        : new ImageIcon(new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB));
            } catch (Exception e) {
                e.printStackTrace();
                originalIcon = new ImageIcon(new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB));
            }
        }

        // Redimensionner et créer le panel d'image
        Image scaledImage = originalIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        RoundedImagePanel imagePanel = new RoundedImagePanel(new ImageIcon(scaledImage));
        imagePanel.setPreferredSize(new Dimension(120, 120));
        imagePanel.setMaximumSize(new Dimension(120, 120));
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Conteneur pour centrer l'image
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

        // Ajout des composants avec des espacements
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(imageWrapper);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(emailLabel);
        centerPanel.add(Box.createVerticalGlue());

        // Boutons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Colors.CURRENT_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        CustomButton continueButton = new CustomButton("Continuer");
        continueButton.addActionListener(e -> openRoleInterface());

        CustomButton logoutButton = new CustomButton("Déconnexion");
        logoutButton.setBackground(Colors.CURRENT_DANGER);
        logoutButton.setHoverBackground(new Color(Colors.CURRENT_DANGER.getRGB()).darker());
        logoutButton.addActionListener(e -> {
            dispose();
            EventQueue.invokeLater(() -> new AuthFrame(securityManager).setVisible(true));
        });

        buttonPanel.add(continueButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }
    private void setupFrame() {
        setTitle("Profil Utilisateur");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void openRoleInterface() {
        RoleInterface roleInterface = InterfaceFactory.createInterface(utilisateur);
        JFrame userFrame = roleInterface.createInterface();
        userFrame.setVisible(true);
        dispose();
    }
}