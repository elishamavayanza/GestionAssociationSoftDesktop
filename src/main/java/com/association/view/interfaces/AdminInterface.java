package com.association.view.interfaces;

import com.association.model.access.Utilisateur;
import com.association.view.AuthPanel;
import com.association.view.LoginFrame;
import com.association.view.components.*;
import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;

public class AdminInterface implements RoleInterface {
    private final Utilisateur utilisateur;
    private JFrame frame;


    public AdminInterface(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public JFrame createInterface() {
        frame = new JFrame("Tableau de bord - Administrateur");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.BACKGROUND);

        // Header avec profil utilisateur
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Menu latéral avec icônes
        JPanel sidePanel = createSidePanel();
        mainPanel.add(sidePanel, BorderLayout.WEST);

        // Contenu principal
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Bienvenue dans l'interface d'administration " + utilisateur.getUsername(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(welcomeLabel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        return frame;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colors.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 60));

        JLabel titleLabel = new JLabel("Gestion Association - Administrateur", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Bouton profil avec menu déroulant
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);

        JButton profileButton = IconManager.createIconButton("users.svg", "Mon profil", 30);
        profileButton.addActionListener(e -> showProfileMenu(profileButton));
        userPanel.add(profileButton);

        headerPanel.add(userPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(Colors.SECONDARY);
        sidePanel.setPreferredSize(new Dimension(80, frame.getHeight()));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Créer le gestionnaire de bascule avec animation
        IconTextToggleManager toggleManager = new IconTextToggleManager(sidePanel);
        JButton toggleButton = toggleManager.createToggleButton();

        // Boutons du menu avec icônes
        JButton gestionUtilisateursBtn = createFixedIconButton("users.svg", "Gestion des utilisateurs", 40);
        JButton gestionAssociationsBtn = createFixedIconButton("users.svg", "Gestion des associations", 40);
        JButton statistiquesBtn = createFixedIconButton("users.svg", "Statistiques", 40);
        JButton parametresBtn = createFixedIconButton("settings.svg", "Paramètres", 40);

        // Ajouter les boutons au gestionnaire avec leurs textes
        toggleManager.addMenuButton(gestionUtilisateursBtn, "Gestion des utilisateurs");
        toggleManager.addMenuButton(gestionAssociationsBtn, "Gestion des associations");
        toggleManager.addMenuButton(statistiquesBtn, "Statistiques");
        toggleManager.addMenuButton(parametresBtn, "Paramètres");

        // Ajout des composants
        sidePanel.add(toggleButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(gestionUtilisateursBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(gestionAssociationsBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(statistiquesBtn);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(parametresBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));

        return sidePanel;
    }

    private JButton createFixedIconButton(String iconPath, String tooltip, int size) {
        JButton button = IconManager.createIconButton(iconPath, tooltip, size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        button.setPreferredSize(new Dimension(60, 50)); // Taille fixe
        button.setMinimumSize(new Dimension(60, 50));
        button.setMaximumSize(new Dimension(60, 50));
        return button;
    }

    // Nouvelle méthode pour créer des boutons avec padding
    private JButton createPaddedIconButton(String iconPath, String tooltip, int size) {
        JButton button = IconManager.createIconButton(iconPath, tooltip, size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrer horizontalement
        button.setBorder(BorderFactory.createCompoundBorder(
                button.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Padding de 5px
        ));
        return button;
    }

    private void showProfileMenu(Component invoker) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem profileItem = new JMenuItem("Mon profil");
        JMenuItem logoutItem = new JMenuItem("Déconnexion");

        logoutItem.addActionListener(e -> {
            // Ferme la fenêtre actuelle
            frame.dispose();

            // Crée une nouvelle instance de LoginFrame avec le AuthPanel
            LoginFrame loginFrame = new LoginFrame();
            AuthPanel authPanel = new AuthPanel(loginFrame);
            loginFrame.switchView(authPanel, "Connexion");
            loginFrame.setVisible(true);
        });

        popupMenu.add(profileItem);
        popupMenu.addSeparator();
        popupMenu.add(logoutItem);

        popupMenu.show(invoker, 0, invoker.getHeight());
    }

    @Override
    public String getRoleName() {
        return "ADMIN";
    }

    @Override
    public boolean hasAccessToFeature(String featureName) {
        return true;
    }
}