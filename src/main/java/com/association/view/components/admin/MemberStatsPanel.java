package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.MembreManager;
import com.association.model.enums.StatutMembre;
import com.association.util.file.FileStorageService;
import com.association.util.file.RealFileStorageService;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import com.association.view.styles.HoverButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MemberStatsPanel extends JPanel {
    private final MembreManager membreManager;

    public MemberStatsPanel() {
        this.membreManager = new MembreManager(DAOFactory.getInstance(MembreDao.class), new RealFileStorageService());
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        // Panel principal avec padding
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // Changement ici
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Colors.BACKGROUND);

        // Titre
        JLabel titleLabel = new JLabel("Statistiques des Membres");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Colors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel pour les cartes de statistiques (hauteur fixe)
        JPanel statsCardsPanel = createStatsCardsPanel();
        statsCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel pour les graphiques (hauteur flexible)
        JPanel chartsPanel = createChartsPanel();
        chartsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Ajout des composants
        contentPanel.add(titleLabel);
        contentPanel.add(statsCardsPanel);
        contentPanel.add(Box.createVerticalStrut(20)); // Espacement
        contentPanel.add(chartsPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.setBackground(Colors.BACKGROUND);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150)); // Hauteur fixe

        // Récupérer les statistiques
        Map<String, Object> stats = membreManager.getMembreStats();

        // Nouveaux styles de couleurs plus claires et uniformes
        Color primaryCardColor = Colors.lighter(Colors.PRIMARY, 0.7f);
        Color successCardColor = Colors.lighter(Colors.SUCCESS, 0.7f);
        Color warningCardColor = Colors.lighter(Colors.WARNING, 0.7f);
        Color dangerCardColor = Colors.lighter(Colors.DANGER, 0.7f);

        // Carte pour le total des membres (hauteur réduite)
        panel.add(createStatCard(
                "Total Membres",
                stats.get("total").toString(),
                IconManager.getIcon("groups.svg", 30), // Icône plus petite
                primaryCardColor,
                Colors.PRIMARY
        ));

        // Carte pour les membres actifs
        panel.add(createStatCard(
                "Membres Actifs",
                stats.get("actifs").toString(),
                IconManager.getIcon("active_user.svg", 30),
                successCardColor,
                Colors.SUCCESS
        ));

        // Carte pour les membres inactifs
        panel.add(createStatCard(
                "Membres Inactifs",
                stats.get("inactifs").toString(),
                IconManager.getIcon("inactive_user.svg", 30),
                warningCardColor,
                Colors.WARNING
        ));

        // Carte pour les membres suspendus
        panel.add(createStatCard(
                "Membres Suspendus",
                stats.get("suspendus").toString(),
                IconManager.getIcon("blocked_user.svg", 30),
                dangerCardColor,
                Colors.DANGER
        ));

        return panel;
    }

    private JPanel createStatCard(String title, String value, Icon icon, Color bgColor, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(bgColor);

        // Définir une taille préférée avec hauteur fixe à 140px
        card.setPreferredSize(new Dimension(card.getPreferredSize().width, 140));

        // Empêcher le redimensionnement
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setMinimumSize(new Dimension(0, 140));

        // Icone
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Titre
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.smallBoldFont());
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Colors.TEXT_DARK);

        // Valeur
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setForeground(Colors.TEXT_DARK);

        // Panel pour le contenu
        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(valueLabel, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Colors.BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Graphique 1 - Répartition par statut
        JFreeChart pieChart = createPieChart();
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setMinimumSize(new Dimension(400, 300));
        pieChartPanel.setPreferredSize(new Dimension(400, 400));
        pieChartPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        panel.add(wrapChartPanel(pieChartPanel, "Répartition par Statut"));

        panel.add(Box.createHorizontalStrut(15)); // Espacement entre les graphiques

        // Graphique 2 - Evolution des inscriptions
        JFreeChart lineChart = createLineChart();
        ChartPanel lineChartPanel = new ChartPanel(lineChart);
        lineChartPanel.setMinimumSize(new Dimension(400, 300));
        lineChartPanel.setPreferredSize(new Dimension(400, 400));
        lineChartPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        panel.add(wrapChartPanel(lineChartPanel, "Evolution des Inscriptions"));

        return panel;
    }

    private JPanel wrapChartPanel(ChartPanel chartPanel, String title) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.SECONDARY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        wrapper.setBackground(Color.WHITE);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Titre
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        wrapper.add(titleLabel, BorderLayout.NORTH);
        wrapper.add(chartPanel, BorderLayout.CENTER);

        // Permettre l'expansion verticale
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        return wrapper;
    }

    private JFreeChart createPieChart() {
        Map<String, Object> stats = membreManager.getMembreStats();

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Actifs", (Number) stats.get("actifs"));
        dataset.setValue("Inactifs", (Number) stats.get("inactifs"));
        dataset.setValue("Suspendus", (Number) stats.get("suspendus"));

        JFreeChart chart = ChartFactory.createPieChart(
                "",
                dataset,
                true,
                true,
                false
        );

        // Personnalisation améliorée du graphique
        PiePlot plot = (PiePlot) chart.getPlot();

        // Couleurs plus claires et harmonieuses
        plot.setSectionPaint("Actifs", Colors.lighter(Colors.SUCCESS, 0.3f));
        plot.setSectionPaint("Inactifs", Colors.lighter(Colors.WARNING, 0.3f));
        plot.setSectionPaint("Suspendus", Colors.lighter(Colors.DANGER, 0.3f));

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelShadowPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelLinkPaint(Colors.TEXT_SECONDARY);

        // Amélioration de la légende
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 12));
        chart.getLegend().setBackgroundPaint(Color.WHITE);

        return chart;
    }


    private JFreeChart createLineChart() {
        Map<String, Integer> monthlyData = membreManager.getMonthlyRegistrations();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        monthlyData.forEach((month, count) -> {
            dataset.addValue(count, "Inscriptions", month);
        });

        JFreeChart chart = ChartFactory.createLineChart(
                "",
                "Mois",
                "Nombre d'inscriptions",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Personnalisation améliorée du graphique
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Colors.SECONDARY);

        // Couleur de ligne plus visible
        plot.getRenderer().setSeriesPaint(0, Colors.PRIMARY);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.5f));

        // Amélioration des axes
        plot.getDomainAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 10));

        // Amélioration de la légende
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 12));
        chart.getLegend().setBackgroundPaint(Color.WHITE);

        return chart;
    }
}