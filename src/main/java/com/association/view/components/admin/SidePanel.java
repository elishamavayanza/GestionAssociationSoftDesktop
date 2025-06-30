package com.association.view.components;

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

        JLabel titleLabel = new JLabel("AVEC", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        // Bouton Dashboard
        JButton dashboard = createMenuButton("dashboard.svg", "Dashboard", 24);
        dashboard.addActionListener(e -> {
            adminInterface.setContentPanel(new DashboardPanel(parentFrame,
                    ((AdminInterface)adminInterface).getUtilisateur().getUsername()));
        });

        // Menus déroulants
        DropDownMenu gestionMembres = new DropDownMenu("Gestion des Membres", "groups.svg");
        gestionMembres.addSubMenuItem("Liste Membres", "list.svg");
        gestionMembres.addSubMenuItem("Ajouter Membre", "person_add.svg");
        gestionMembres.addSubMenuItem("Statuts Membres", "verified_user.svg");

        DropDownMenu gestionContributions = new DropDownMenu("Gestion Contributions", "payments.svg");
        gestionContributions.addSubMenuItem("Enregistrer Contribution", "add_circle.svg");
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
        sidePanel.add(titleLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
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