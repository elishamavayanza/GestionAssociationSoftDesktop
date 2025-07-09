package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.MembreManager;
import com.association.model.enums.StatutMembre;
import com.association.util.file.FileStorageService;
import com.association.util.file.RealFileStorageService;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
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
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Colors.BACKGROUND);

        // Titre
        JLabel titleLabel = new JLabel("Statistiques des Membres");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Colors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Panel pour les cartes de statistiques
        JPanel statsCardsPanel = createStatsCardsPanel();

        // Panel pour les graphiques
        JPanel chartsPanel = createChartsPanel();

        // Ajout des composants
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(statsCardsPanel, BorderLayout.CENTER);
        contentPanel.add(chartsPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.setBackground(Colors.BACKGROUND);

        // Récupérer les statistiques
        Map<String, Object> stats = membreManager.getMembreStats();

        // Carte pour le total des membres
        panel.add(createStatCard(
                "Total Membres",
                stats.get("total").toString(),
                IconManager.getIcon("groups.svg", 40),
                Colors.PRIMARY
        ));

        // Carte pour les membres actifs
        panel.add(createStatCard(
                "Membres Actifs",
                stats.get("actifs").toString(),
                IconManager.getIcon("active_user.svg", 40),
                Colors.SUCCESS
        ));

        // Carte pour les membres inactifs
        panel.add(createStatCard(
                "Membres Inactifs",
                stats.get("inactifs").toString(),
                IconManager.getIcon("inactive_user.svg", 40),
                Colors.WARNING
        ));

        // Carte pour les membres suspendus
        panel.add(createStatCard(
                "Membres Suspendus",
                stats.get("suspendus").toString(),
                IconManager.getIcon("blocked_user.svg", 40),
                Colors.DANGER
        ));

        return panel;
    }

    private JPanel createStatCard(String title, String value, Icon icon, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(color.brighter().brighter());

        // Icone
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Titre
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Color.BLACK);

        // Valeur
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setForeground(Color.BLACK);

        // Panel pour le contenu
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(valueLabel, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(Colors.BACKGROUND);

        // Graphique 1 - Répartition par statut
        JFreeChart pieChart = createPieChart();
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 300));
        panel.add(wrapChartPanel(pieChartPanel, "Répartition par Statut"));

        // Graphique 2 - Evolution des inscriptions
        JFreeChart lineChart = createLineChart();
        ChartPanel lineChartPanel = new ChartPanel(lineChart);
        lineChartPanel.setPreferredSize(new Dimension(400, 300));
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

        // Titre
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        wrapper.add(titleLabel, BorderLayout.NORTH);
        wrapper.add(chartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JFreeChart createPieChart() {
        Map<String, Object> stats = membreManager.getMembreStats();

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Actifs", (Number) stats.get("actifs"));
        dataset.setValue("Inactifs", (Number) stats.get("inactifs"));
        dataset.setValue("Suspendus", (Number) stats.get("suspendus"));

        JFreeChart chart = ChartFactory.createPieChart(
                "", // titre déjà affiché dans le wrapper
                dataset,
                true, // légende
                true, // tooltips
                false // URLs
        );

        // Personnalisation du graphique
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Actifs", Colors.SUCCESS);
        plot.setSectionPaint("Inactifs", Colors.WARNING);
        plot.setSectionPaint("Suspendus", Colors.DANGER);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        return chart;
    }

    private JFreeChart createLineChart() {
        // Ici vous devriez implémenter une méthode dans MembreManager pour récupérer
        // les données d'évolution mensuelle des inscriptions
        Map<String, Integer> monthlyData = membreManager.getMonthlyRegistrations();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        monthlyData.forEach((month, count) -> {
            dataset.addValue(count, "Inscriptions", month);
        });

        JFreeChart chart = ChartFactory.createLineChart(
                "", // titre déjà affiché dans le wrapper
                "Mois", // axe X
                "Nombre d'inscriptions", // axe Y
                dataset,
                PlotOrientation.VERTICAL,
                true, // légende
                true, // tooltips
                false // URLs
        );

        // Personnalisation du graphique
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Colors.SECONDARY);
        plot.getRenderer().setSeriesPaint(0, Colors.PRIMARY);

        return chart;
    }
}