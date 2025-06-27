package com.association.view.interfaces;

import com.association.model.access.Utilisateur;
import com.association.view.components.*;
import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;

public class MembreInterface implements RoleInterface {
    private final Utilisateur utilisateur;

    public MembreInterface(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public JFrame createInterface() {
        JFrame frame = new JFrame("Tableau de bord - Membre");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colors.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 60));

        JLabel titleLabel = new JLabel("Gestion Association - Membre", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(Colors.PRIMARY);
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel("Connecté en tant que: " + utilisateur.getUsername());
        userLabel.setForeground(Color.WHITE);
        userPanel.add(userLabel);

        headerPanel.add(userPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Menu latéral
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(Colors.SECONDARY);
        sidePanel.setPreferredSize(new Dimension(150, frame.getHeight()));

        // Boutons du menu
        CustomButton profilBtn = new CustomButton("Mon profil");
        CustomButton evenementsBtn = new CustomButton("Événements");
        CustomButton documentsBtn = new CustomButton("Documents");
        CustomButton deconnexionBtn = new CustomButton("Déconnexion");

        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(profilBtn);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(evenementsBtn);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(documentsBtn);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(deconnexionBtn);
        sidePanel.add(Box.createVerticalStrut(20));

        mainPanel.add(sidePanel, BorderLayout.WEST);

        // Contenu principal
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Bienvenue " + utilisateur.getUsername(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(welcomeLabel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        return frame;
    }

    @Override
    public String getRoleName() {
        return "MEMBRE";
    }

    @Override
    public boolean hasAccessToFeature(String featureName) {
        // Le membre a accès aux fonctionnalités de base
        return featureName.equals("profile") ||
                featureName.equals("events") ||
                featureName.equals("documents");
    }
}