package com.association.view.components.admin;

import com.association.manager.ContributionManager;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import java.awt.*;

public class ContributionsPanel extends JPanel {
    private JTabbedPane contributionsTabbedPane;
    private Long membreId;
    private final ContributionManager contributionManager;


    public ContributionsPanel(Long membreId, ContributionManager contributionManager) {
        this.membreId = membreId;
        this.contributionManager = contributionManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        contributionsTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
            @Override
            public Dimension getPreferredSize() {
                // Ne forcer la taille que si un onglet est sélectionné
                if (getSelectedComponent() != null) {
                    return getSelectedComponent().getPreferredSize();
                }
                return super.getPreferredSize();
            }
        };

        contributionsTabbedPane.setFont(Fonts.labelFont());
        contributionsTabbedPane.setBackground(Colors.CARD_BACKGROUND);
        contributionsTabbedPane.setForeground(Colors.TEXT);

        // Personnalisation de l'apparence des onglets (identique à votre code original)
        contributionsTabbedPane.setUI(new javax.swing.plaf.metal.MetalTabbedPaneUI() {
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                // Pas de bordure
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Pas de bordure autour du contenu
            }

            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                int width = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
                return Math.max(width, 100);
            }
        });

        // Style supplémentaire pour les onglets (identique)
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
        UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabHeight", 30);

        // Création des icônes pour chaque onglet (identique)
        ImageIcon monthlyIcon = IconManager.getScaledIcon("calendar_monthly.svg", 16, 16);
        ImageIcon yearlyIcon = IconManager.getScaledIcon("calendar_yearly.svg", 16, 16);
        ImageIcon donationIcon = IconManager.getScaledIcon("donation_icon.svg", 16, 16);

        // Onglet pour les contributions mensuelles avec JScrollPane
        WeeklyCalendarPanel monthlyPanel = new WeeklyCalendarPanel(membreId, "MENSUEL");
        JScrollPane monthlyScroll = new JScrollPane(monthlyPanel);
        monthlyScroll.setBorder(BorderFactory.createEmptyBorder());
        contributionsTabbedPane.addTab("Mensuel", monthlyIcon, monthlyScroll);

        // Onglet pour les contributions annuelles avec JScrollPane
        AnnualContributionPanel yearlyPanel = new AnnualContributionPanel(membreId, contributionManager);
        JScrollPane yearlyScroll = new JScrollPane(yearlyPanel);
        yearlyScroll.setBorder(BorderFactory.createEmptyBorder());
        contributionsTabbedPane.addTab("Annuel", yearlyIcon, yearlyScroll);

        // Onglet pour les dons avec JScrollPane
        DonationPanel donationPanel = new DonationPanel(membreId);
        JScrollPane donationScroll = new JScrollPane(donationPanel);
        donationScroll.setBorder(BorderFactory.createEmptyBorder());
        contributionsTabbedPane.addTab("Dons", donationIcon, donationScroll);

        // Ajouter un listener pour ajuster la taille lors du changement d'onglet
        contributionsTabbedPane.addChangeListener(e -> {
            Component selected = contributionsTabbedPane.getSelectedComponent();
            if (selected != null) {
                selected.setPreferredSize(selected.getPreferredSize());
                contributionsTabbedPane.revalidate();
                contributionsTabbedPane.repaint();
            }
        });

        add(contributionsTabbedPane, BorderLayout.CENTER);
    }


    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        // Mettre à jour tous les panels enfants
        for (int i = 0; i < contributionsTabbedPane.getTabCount(); i++) {
            Component comp = contributionsTabbedPane.getComponentAt(i);
            if (comp instanceof Refreshable) {
                ((Refreshable) comp).setMembreId(membreId);
            }
        }
    }
}

interface Refreshable {
    void setMembreId(Long membreId);
}
