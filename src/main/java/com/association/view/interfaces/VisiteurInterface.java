package com.association.view.interfaces;

import com.association.security.model.Utilisateur;
import com.association.view.components.*;
import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;

public class VisiteurInterface implements RoleInterface {
    private final Utilisateur utilisateur;

    public VisiteurInterface(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public JFrame createInterface() {
        JFrame frame = new JFrame("Tableau de bord - Visiteur");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colors.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 60));

        JLabel titleLabel = new JLabel("Gestion Association - Visiteur", SwingConstants.CENTER);
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

        // Contenu principal
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Bienvenue visiteur", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        CustomButton devenirMembreBtn = new CustomButton("Devenir membre");
        buttonPanel.add(devenirMembreBtn);

        contentPanel.add(welcomeLabel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        return frame;
    }

    @Override
    public String getRoleName() {
        return "VISITEUR";
    }

    @Override
    public boolean hasAccessToFeature(String featureName) {
        // Le visiteur a un accès très limité
        return featureName.equals("public_info") ||
                featureName.equals("become_member");
    }
}