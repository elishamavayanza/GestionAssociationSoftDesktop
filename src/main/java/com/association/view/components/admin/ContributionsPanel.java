package com.association.view.components.admin;

import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import java.awt.*;

public class ContributionsPanel extends JPanel {
    private JTabbedPane contributionsTabbedPane;
    private Long membreId;

    public ContributionsPanel(Long membreId) {
        this.membreId = membreId;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.CARD_BACKGROUND); // Même fond que le panneau principal

        contributionsTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        contributionsTabbedPane.setFont(Fonts.labelFont());
        contributionsTabbedPane.setBackground(Colors.CARD_BACKGROUND);
        contributionsTabbedPane.setForeground(Colors.TEXT);

        // Personnalisation de l'apparence des onglets
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
                // Force une largeur égale pour tous les onglets
                int width = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
                return Math.max(width, 100); // 100px de largeur minimale par onglet
            }
        });

        // Style supplémentaire pour les onglets
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
        UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabHeight", 30);

        // Création des icônes pour chaque onglet
        ImageIcon monthlyIcon = IconManager.getScaledIcon("calendar_monthly.svg", 16, 16);
        ImageIcon yearlyIcon = IconManager.getScaledIcon("calendar_yearly.svg", 16, 16);
        ImageIcon donationIcon = IconManager.getScaledIcon("donation_icon.svg", 16, 16);

        // Onglet pour les contributions mensuelles
        WeeklyCalendarPanel monthlyPanel = new WeeklyCalendarPanel(membreId, "MENSUEL");
        contributionsTabbedPane.addTab("Mensuel", monthlyIcon, monthlyPanel);

        // Onglet pour les contributions annuelles
        WeeklyCalendarPanel yearlyPanel = new WeeklyCalendarPanel(membreId, "ANNUEL");
        contributionsTabbedPane.addTab("Annuel", yearlyIcon, yearlyPanel);

        // Onglet pour les dons
        DonationPanel donationPanel = new DonationPanel(membreId);
        contributionsTabbedPane.addTab("Dons", donationIcon, donationPanel);

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
