package com.association.view.components.admin;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final JFrame parentFrame;
    private final String username;

    public DashboardPanel(JFrame parentFrame, String username) {
        this.parentFrame = parentFrame;
        this.username = username;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Bienvenue dans l'interface d'administration " + username, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.CENTER);

        // Ici vous pouvez ajouter d'autres composants spécifiques au dashboard
        // comme des statistiques, des graphiques, etc.
    }
}