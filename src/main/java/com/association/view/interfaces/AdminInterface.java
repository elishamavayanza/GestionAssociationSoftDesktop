package com.association.view.interfaces;

import com.association.model.access.Utilisateur;
import com.association.view.AuthPanel;
import com.association.view.LoginFrame;
import com.association.view.components.*;
import com.association.view.components.admin.DashboardPanel;
import com.association.view.components.admin.SidePanel;
import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;
public class AdminInterface implements RoleInterface {
    private final Utilisateur utilisateur;
    private JFrame frame;
    private JPanel currentContentPanel;

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

        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.setPreferredSize(new Dimension(220, frame.getHeight()));

        JPanel sidePanel = createSidePanel();
        westPanel.add(sidePanel, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Par défaut, on affiche le DashboardPanel
        currentContentPanel = new DashboardPanel(frame, utilisateur.getUsername());
        contentPanel.add(currentContentPanel, BorderLayout.CENTER);

        mainPanel.add(westPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        return frame;
    }

    // Méthode pour changer le contenu central
    public void setContentPanel(JPanel panel) {
        // Récupère le contentPanel (qui est le deuxième composant dans mainPanel)
        JPanel contentPanel = (JPanel) frame.getContentPane().getComponent(1);

        // Supprime l'ancien panel s'il existe
        if (currentContentPanel != null) {
            contentPanel.remove(currentContentPanel);
        }

        currentContentPanel = panel;
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colors.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth() - 80, 60));

        JLabel titleLabel = new JLabel("Gestion Association - Administrateur", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);

        JButton profileButton = IconManager.createIconButton("profile.svg", "Mon profil", 30);
        profileButton.addActionListener(e -> showProfileMenu(profileButton));
        userPanel.add(profileButton);

        headerPanel.add(userPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createSidePanel() {
        return new SidePanel(frame, this);
    }

    // Ajoutez cette méthode pour permettre l'accès à l'utilisateur
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    private void showProfileMenu(Component invoker) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem profileItem = new JMenuItem("Mon profil");
        JMenuItem logoutItem = new JMenuItem("Déconnexion");

        logoutItem.addActionListener(e -> {
            frame.dispose();
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