package com.association.view.interfaces;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.model.access.Utilisateur;
import com.association.view.AuthPanel;
import com.association.view.LoginFrame;
import com.association.view.components.*;
import com.association.view.components.admin.DashboardPanel;
import com.association.view.components.admin.SidePanel;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.List;
import java.util.ArrayList;


public class AdminInterface implements RoleInterface, Observer {
    private final Utilisateur utilisateur;
    private JFrame frame;
    private JPanel currentContentPanel;
    private JButton notificationButton;
    private int notificationCount = 0;
    private List<String> notifications = new ArrayList<>(); // ← point-virgule ici

    public AdminInterface(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;

        DAOFactory.getInstance(MembreDao.class).addObserver(this);

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

        // Bouton de notification
        notificationButton = IconManager.createIconButton("notifications.svg", "Notifications", 30);
        notificationButton.addActionListener(e -> showNotifications());
        updateNotificationBadge();
        userPanel.add(notificationButton);

        JButton profileButton = IconManager.createIconButton("profile.svg", "Mon profil", 30);
        profileButton.addActionListener(e -> showProfileMenu(profileButton));
        userPanel.add(profileButton);

        headerPanel.add(userPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private void updateNotificationBadge() {
        if (notificationCount > 0) {
            notificationButton.setIcon(IconManager.createBadgedIcon(
                    "notifications.svg",
                    String.valueOf(notificationCount),
                    30,
                    Colors.DANGER
            ));
        } else {
            notificationButton.setIcon(IconManager.getIcon("notifications.svg", 30));
        }
    }

//    private void showNotifications() {
//        // Ici vous pouvez afficher une liste des notifications
//        // Pour l'exemple, nous affichons juste une notification test
//        NotificationDialog.showNotification(frame,
//                "Vous avez " + notificationCount + " nouvelles notifications",
//                "info");
//
//        // Réinitialiser le compteur après visualisation
//        notificationCount = 0;
//        updateNotificationBadge();
//    }

    // Méthode pour ajouter une notification
    public void addNotification(String message, String type) {
        notifications.add(0, message); // Ajoute en tête de liste
        notificationCount++;
        updateNotificationBadge();

        // Afficher une notification toast
        showToastNotification(message, type);
    }
    private void showToastNotification(String message, String type) {
        SwingUtilities.invokeLater(() -> {
            NotificationDialog.showNotification(frame, message, type);
        });
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

    public void updateTheme(boolean darkMode) {
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Membre) {
            Membre membre = (Membre) arg;
            String message = "Membre modifié: " + membre.getNom();
            addNotification(message, "info");
        } else if (arg instanceof Long) {
            String message = "Membre supprimé (ID: " + arg + ")";
            addNotification(message, "warning");
        } else {
            addNotification("Changement dans la base de données", "info");
        }
    }

    private void showNotifications() {
        if (notifications.isEmpty()) {
            NotificationDialog.showNotification(frame,
                    "Aucune nouvelle notification", "info");
            return;
        }

        // Créer un JDialog personnalisé pour afficher toutes les notifications
        JDialog dialog = new JDialog(frame, "Notifications", false);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(frame);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Notifications (" + notifications.size() + ")");
        titleLabel.setFont(Fonts.titleFont());
        panel.add(titleLabel, BorderLayout.NORTH);

        JList<String> list = new JList<>(notifications.toArray(new String[0]));
        list.setCellRenderer(new NotificationListRenderer());
        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearButton = new JButton("Tout effacer");
        clearButton.addActionListener(e -> {
            notifications.clear();
            notificationCount = 0;
            updateNotificationBadge();
            dialog.dispose();
        });
        panel.add(clearButton, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);

        // Réinitialiser le compteur après visualisation
        notificationCount = 0;
        updateNotificationBadge();
    }

    // Renderer personnalisé pour la liste de notifications
    private static class NotificationListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            label.setIcon(IconManager.getIcon("info.svg", 16));

            if (isSelected) {
                label.setBackground(Colors.CARD_BACKGROUND);
            } else {
                label.setBackground(index % 2 == 0 ?
                        Colors.BACKGROUND : Colors.CARD_BACKGROUND);
            }

            return label;
        }
    }

}