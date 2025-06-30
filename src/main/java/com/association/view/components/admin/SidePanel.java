package com.association.view.components.admin;

import com.association.view.components.IconManager;
import com.association.view.interfaces.AdminInterface;
import com.association.view.styles.Colors;
import com.association.view.styles.HoverButton;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class SidePanel extends JPanel {
    private final JFrame parentFrame;
    private AdminInterface adminInterface; // Ajoutez cette référence


    public SidePanel(JFrame parentFrame,  AdminInterface adminInterface) {
        this.parentFrame = parentFrame;
        this.adminInterface = adminInterface;

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.SECONDARY);
        setPreferredSize(new Dimension(220, parentFrame.getHeight()));

        JPanel sidePanel = createSidePanelContent();
        JScrollPane scrollPane = createScrollPane(sidePanel);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSidePanelContent() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(Colors.SECONDARY);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));

        // Création d'un panel pour le titre AVEC avec icône
        JPanel titlePanel = new JPanel(new BorderLayout(5, 0));
        titlePanel.setBackground(Colors.SECONDARY);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Ajout de l'icône à gauche
        Icon avecIcon = IconManager.getIcon("avec.svg", 30);
        JLabel iconLabel = new JLabel(avecIcon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));;

        // Personnalisation du label AVEC
        JLabel titleLabel = new JLabel("AVEC");
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 24));
        titleLabel.setForeground(Colors.INPUT_BACKGROUND);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 10));  // Couleur or

        // Ajout des composants au panel titre
        titlePanel.add(iconLabel, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Création de la ligne horizontale
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(Colors.DARK_PRIMARY); // Même couleur que le texte
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        separator.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));  // Couleur or


        // Bouton Dashboard
        JButton dashboard = createMenuButton("dashboard.svg", "Dashboard", 24);
        dashboard.addActionListener(e -> {
            adminInterface.setContentPanel(new DashboardPanel(parentFrame,
                    ((AdminInterface)adminInterface).getUtilisateur().getUsername()));
        });

        // Menus déroulants
        DropDownMenu gestionMembres = new DropDownMenu("Gestion des Membres", "groups.svg");
        HoverButton listeMembresBtn = gestionMembres.addSubMenuItem("Liste Membres", "list.svg");
        listeMembresBtn.setDoubleClickAction(() -> {
            // Créer et afficher le panel de liste des membres
            MemberListPanel memberListPanel = new MemberListPanel(parentFrame);
            adminInterface.setContentPanel(memberListPanel);
        });

        gestionMembres.addSubMenuItem("Ajouter Membre", "person_add.svg");
        gestionMembres.addSubMenuItem("Statuts Membres", "verified_user.svg");

        DropDownMenu gestionContributions = new DropDownMenu("Gestion Contributions", "payments.svg");
        gestionContributions.addSubMenuItem("Enregistrer Contribution", "attach_money.svg");
        gestionContributions.addSubMenuItem("Historique Contributions", "acute.svg");
        gestionContributions.addSubMenuItem("Rapports Contributions", "finance.svg");

        DropDownMenu gestionEmprunts = new DropDownMenu("Gestion Emprunts", "account_balance.svg");
        gestionEmprunts.addSubMenuItem("Demander Emprunt", "request_quote.svg");
        gestionEmprunts.addSubMenuItem("Approuver Emprunt", "approval.svg");
        gestionEmprunts.addSubMenuItem("Remboursements", "paid.svg");
        gestionEmprunts.addSubMenuItem("Suivi Emprunts", "track_changes.svg");

        DropDownMenu rapports = new DropDownMenu("Rapports", "analytics.svg");
        rapports.addSubMenuItem("Générer Rapport", "summarize.svg");
        rapports.addSubMenuItem("Exporter Données", "cloud_download.svg");

        DropDownMenu administration = new DropDownMenu("Administration", "admin_panel_settings.svg");
        administration.addSubMenuItem("Gestion Utilisateurs", "manage_accounts.svg");
        administration.addSubMenuItem("Paramètres Système", "tune.svg");

        JButton parametresBtn = createMenuButton("settings.svg", "Paramètres", 24);

        // Ajout des composants
        sidePanel.add(titlePanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(separator);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));;
        sidePanel.add(dashboard);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        sidePanel.add(gestionMembres);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        sidePanel.add(gestionContributions);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        sidePanel.add(gestionEmprunts);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        sidePanel.add(rapports);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        sidePanel.add(administration);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(parametresBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 2)));

        return sidePanel;
    }

    private JScrollPane createScrollPane(JPanel contentPanel) {
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Colors.SECONDARY);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
        verticalScrollBar.setUI(new CustomScrollBarUI());

        return scrollPane;
    }

    private JButton createMenuButton(String iconPath, String text, int size) {
        Icon icon = IconManager.getIcon(iconPath, size);
        HoverButton button = new HoverButton(
                text,
                icon,
                Colors.SECONDARY,
                Colors.SECONDARY.darker()
        );

        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setMaximumSize(new Dimension(200, 50));

        return button;
    }

    private static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = Colors.PRIMARY;
            this.trackColor = Colors.SECONDARY.darker();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(thumbColor);
            g2.fillRoundRect(
                    thumbBounds.x + 2,
                    thumbBounds.y,
                    thumbBounds.width - 4,
                    thumbBounds.height,
                    10,
                    10
            );
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(trackColor);
            g2.fillRoundRect(
                    trackBounds.x + 2,
                    trackBounds.y,
                    trackBounds.width - 4,
                    trackBounds.height,
                    10,
                    10
            );
        }
    }
}