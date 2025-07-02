package com.association.view.interfaces;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.model.Notification;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.List;


public class AdminInterface implements RoleInterface, Observer {
    private final Utilisateur utilisateur;
    private JFrame frame;
    private JPanel currentContentPanel;
    private JButton notificationButton;
    private int notificationCount = 0;
    private List<Notification> notifications = new ArrayList<>();
    private int unreadNotificationCount = 0;


    private final String NOTIFICATION_FILE = "data/notifications.ser"; // Tu peux choisir un .json, .txt, .ser selon ton format

    public AdminInterface(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;

        DAOFactory.getInstance(MembreDao.class).addObserver(this);

        purgeOldNotificationsFile();

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

        loadNotifications(); // ← Charger les anciennes notifications au démarrage

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
        if (unreadNotificationCount > 0) {
            notificationButton.setIcon(IconManager.createBadgedIcon(
                    "notification.svg",
                    String.valueOf(unreadNotificationCount),
                    32,
                    Colors.DANGER
            ));
        } else {
            notificationButton.setIcon(IconManager.getIcon("notifications.svg", 30));
        }
    }


    // Méthode pour ajouter une notification
    public void addNotification(String action, String message, String type) {
        Notification notif = new Notification(action, message, type);
        notifications.add(0, notif);
        unreadNotificationCount++; // Incrémente seulement les non lues
        updateNotificationBadge();
        showToastNotification(message, type);
        saveNotifications();
    }

    private void showToastNotification(String message, String type) {
        SwingUtilities.invokeLater(() -> {
            String title = switch (type) {
                case "warning" -> "Suppression";
                case "info" -> "Ajout/Modification";
                default -> "Notification";
            };
            String dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            NotificationDialog.showNotification(frame,
                    "<html><b>" + title + "</b> - " + dateTime + "<br>" + message + "</html>",
                    type);
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
            String message = "Membre: " + membre.getNom();
            addNotification("AJOUT", message, "info");
        } else if (arg instanceof Long) {
            String message = "Membre supprimé (ID: " + arg + ")";
            addNotification("SUPPRESSION", message, "warning");
        } else {
            addNotification("MODIFICATION", "Changement dans la base de données", "info");
        }
    }

    private void showNotifications() {
        System.out.println("Nombre de notifications avant affichage: " + notifications.size());

        if (notifications.isEmpty()) {
            NotificationDialog.showNotification(frame,
                    "Aucune nouvelle notification", "info");
            return;
        }

        // Marquer toutes les notifications comme lues lors de l'affichage
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.markAsRead();
                unreadNotificationCount--;
            }
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

        // Nouveau code
        DefaultListModel<Notification> model = new DefaultListModel<>();
        for (Notification n : notifications) {
            model.addElement(n);
        }
        JList<Notification> list = new JList<>(model);
        list.setCellRenderer(new NotificationListRenderer());

        // Ajouter le MouseListener après la création de la JList
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Notification notif = list.getModel().getElementAt(index);
                    if (!notif.isRead()) {
                        notif.markAsRead();
                        unreadNotificationCount--;
                        updateNotificationBadge();
                        list.repaint();
                        saveNotifications();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearButton = new JButton("Tout effacer");
        styleButton(clearButton, true);

        clearButton.addActionListener(e -> {
            notifications.clear();
            unreadNotificationCount = 0;
            updateNotificationBadge();
            saveNotifications();
            dialog.dispose();
        });
        panel.add(clearButton, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);

        updateNotificationBadge();
        saveNotifications();
    }

    // Renderer personnalisé pour la liste de notifications
    private static class NotificationListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Notification notif = (Notification) value;

            // Utiliser le rendu par défaut comme base
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, notif.getDisplayText(), index, isSelected, cellHasFocus);

            // Style différent selon si la notification est lue ou non
            if (!notif.isRead()) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setForeground(Colors.CURRENT_UNREAD_NOTIFICATION); // Couleur spéciale pour non-lues
            } else {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
                label.setForeground(Colors.CURRENT_TEXT_SECONDARY);
            }

            // Icône selon le type de notification
            switch (notif.getType().toLowerCase()) {
                case "info":
                    label.setIcon(IconManager.getIcon("infos.svg", 16));
                    break;
                case "warning":
                    label.setIcon(IconManager.getIcon("warning.svg", 16));
                    break;
                case "error":
                    label.setIcon(IconManager.getIcon("error.svg", 16));
                    break;
                default:
                    label.setIcon(IconManager.getIcon("add.svg", 16));
                    break;
            }

            // Style de fond
            if (isSelected) {
                label.setBackground(Colors.SELECTION_BACKGROUND);
                label.setForeground(Colors.CURRENT_TEXT);
            }

            // Ajouter un indicateur visuel pour les notifications non lues
            if (!notif.isRead()) {
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, Colors.CURRENT_PRIMARY),
                        BorderFactory.createEmptyBorder(5, 8, 5, 5)
                ));
            } else {
                label.setBorder(BorderFactory.createEmptyBorder(5, 11, 5, 5));
            }

            // Tooltip avec plus de détails
            label.setToolTipText("<html><b>" + notif.getAction() + "</b><br>" +
                    notif.getMessage() + "<br>" +
                    "<i>" + notif.getDate() + "</i></html>");

            return label;
        }
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(Fonts.buttonFont());
        button.setFocusPainted(false);

        if (primary) {
            button.setBackground(Colors.CURRENT_PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.CURRENT_PRIMARY_DARK),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));

            // Effet de survol pour le bouton primaire
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(Colors.CURRENT_PRIMARY.darker());
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(Colors.CURRENT_PRIMARY);
                }
            });
        } else {
            button.setBackground(Colors.CURRENT_CARD_BACKGROUND);
            button.setForeground(Colors.CURRENT_TEXT);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));

            // Effet de survol pour le bouton secondaire
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(Colors.CURRENT_BORDER);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(Colors.CURRENT_CARD_BACKGROUND);
                }
            });
        }
    }
    private void purgeOldNotificationsFile() {
        File file = new File(NOTIFICATION_FILE);
        if (file.exists()) {
            // on peut lancer un essai pour détecter un List<String>
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object o = ois.readObject();
                if (o instanceof List<?>) {
                    List<?> l = (List<?>) o;
                    if (!l.isEmpty() && l.get(0) instanceof String) {
                        // on supprime : c’était l’ancien format
                        file.delete();
                    }
                }
            } catch (Exception e) {
                // fichier corrompu ou nouvelle version, on laisse ou on supprime
                file.delete();
            }
        }
    }

    private void loadNotifications() {
        File file = new File(NOTIFICATION_FILE);
        if (!file.exists()) {
            notifications = new ArrayList<>();
            unreadNotificationCount = 0;
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                notifications = (List<Notification>) obj;
                // Compter seulement les notifications non lues
                unreadNotificationCount = (int) notifications.stream()
                        .filter(n -> !n.isRead())
                        .count();
                updateNotificationBadge();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur de chargement des notifications : " + e.getMessage());
            notifications = new ArrayList<>();
            unreadNotificationCount = 0;
            file.renameTo(new File(NOTIFICATION_FILE + ".corrupted_" + System.currentTimeMillis()));
        }
    }


    private void saveNotifications() {
        try {
            File file = new File(NOTIFICATION_FILE);
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    System.err.println("Impossible de créer le répertoire pour les notifications");
                    return;
                }
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(notifications);
                System.out.println("Notifications sauvegardées : " + notifications.size());
            }
        } catch (IOException e) {
            System.err.println("Erreur de sauvegarde des notifications : " + e.getMessage());
            e.printStackTrace();
        }
    }


}