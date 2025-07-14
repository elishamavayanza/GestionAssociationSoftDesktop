package com.association.view.components.admin;

import com.association.dao.ContributionDao;
import com.association.dao.DAOFactory;
import com.association.dao.EmpruntDao;
import com.association.dao.MembreDao;
import com.association.manager.ContributionManager;
import com.association.manager.EmpruntManager;
import com.association.manager.MembreManager;
import com.association.util.file.RealFileStorageService;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final JFrame parentFrame;
    private final String username;
    private final ContributionManager contributionManager;
    private final MembreManager membreManager;
    private final EmpruntManager empruntManager;

    public DashboardPanel(JFrame parentFrame, String username) {
        this.parentFrame = parentFrame;
        this.username = username;

        // Initialisation des managers
        this.contributionManager = new ContributionManager(
                DAOFactory.getInstance(ContributionDao.class),
                new MembreManager(DAOFactory.getInstance(MembreDao.class), new RealFileStorageService()));

        this.membreManager = new MembreManager(
                DAOFactory.getInstance(MembreDao.class),
                new RealFileStorageService());

        this.empruntManager = new EmpruntManager(
                DAOFactory.getInstance(EmpruntDao.class),
                this.membreManager);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel principal avec scroll
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Modifié
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel contentPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // S'adapter à la largeur du parent
                Dimension d = super.getPreferredSize();
                Container parent = getParent();
                if (parent != null) {
                    d.width = parent.getWidth() - 40; // 40 pour le padding
                }
                return d;
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.BACKGROUND);
        scrollPane.setViewportView(contentPanel);

        // Titre de bienvenue
        JLabel welcomeLabel = new JLabel("Bienvenue, " + username, SwingConstants.LEFT);
        welcomeLabel.setFont(Fonts.largeBoldFont());
        welcomeLabel.setForeground(Colors.TEXT_DARK);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(welcomeLabel);

        // Panel des cartes statistiques (4 cartes)
        JPanel statsPanel = createStatsCardsPanel();
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(statsPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Graphique Area
        JPanel chartPanel = createAreaChartPanel();
        chartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(chartPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Panel des informations essentielles
        JPanel infoPanel = createEssentialInfoPanel();
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(infoPanel);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatsCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 15, 0)); // 0 pour le nombre de colonnes = auto
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.setBackground(Colors.BACKGROUND);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Récupération des statistiques
        Map<String, Object> contribStats = contributionManager.getContributionStats();
        Map<String, Object> membreStats = membreManager.getMembreStats();
        Map<String, Object> empruntStats = empruntManager.getEmpruntStats();

        // Carte 1: Total des contributions
        panel.add(createStatCard(
                "Total Contributions",
                safeToString(contribStats.get("total")),
                IconManager.getAnimatedMaterialIcon("account_balance", 30, "pulse"),
                Colors.lighter(Colors.PRIMARY, 0.8f),
                Colors.PRIMARY
        ));

        // Carte 2: Total des membres
        panel.add(createStatCard(
                "Total Membres",
                safeToString(membreStats.get("total")),
                IconManager.getAnimatedMaterialIcon("people", 30, "spin"),
                Colors.lighter(Colors.SUCCESS, 0.8f),
                Colors.SUCCESS
        ));

        // Carte 3: Emprunts actifs
        panel.add(createStatCard(
                "Emprunts Actifs",
                safeToString(empruntStats.get("actifs")),
                IconManager.getAnimatedMaterialIcon("credit_card", 30, "pulse"),
                Colors.lighter(Colors.WARNING, 0.8f),
                Colors.WARNING
        ));

        // Carte 4: Contributions ce mois
        panel.add(createStatCard(
                "Contrib. ce mois",
                safeToString(contribStats.get("monthly")),
                IconManager.getAnimatedMaterialIcon("trending_ups", 30, "pulse"),
                Colors.lighter(Colors.DANGER, 0.8f),
                Colors.DANGER
        ));


        return panel;
    }

    // Méthode utilitaire pour gérer les valeurs nulles
    private String safeToString(Object obj) {
        return obj != null ? obj.toString() : "0"; // ou "N/A" selon ce que vous préférez
    }

    private JPanel createStatCard(String title, String value, JLabel iconLabel, Color bgColor, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(bgColor);
        card.setPreferredSize(new Dimension(250, 140));

        // Effet de survol
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor.brighter(), 2),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14)
                ));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }
        });

        // Contenu de la carte
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.smallBoldFont());
        titleLabel.setForeground(Colors.TEXT_DARK);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Colors.TEXT_DARK);

        iconLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(valueLabel, BorderLayout.CENTER);
        contentPanel.add(iconLabel, BorderLayout.EAST);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAreaChartPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JLabel titleLabel = new JLabel("Évolution des Contributions, Membres et Emprunts");
        titleLabel.setFont(Fonts.mediumBoldFont());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Création du graphique Area
        JFreeChart chart = createAreaChart();
        ChartPanel chartPanel = new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                // Limiter la largeur préférée pour éviter le débordement
                Dimension d = super.getPreferredSize();
                int width = Math.min(d.width, panel.getParent().getWidth() - 50); // 50 pour le padding
                return new Dimension(width, 300);
            }
        };

        chartPanel.setMaximumDrawWidth(2000); // Limite maximale
        chartPanel.setMinimumDrawWidth(300);  // Largeur minimale
        chartPanel.setPreferredSize(new Dimension(panel.getWidth(), 300));

        // Activer le redimensionnement et le défilement si nécessaire
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setMouseWheelEnabled(true);

        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JFreeChart createAreaChart() {
        TimeSeries contributionsSeries = new TimeSeries("Contributions (FCFA)");
        TimeSeries membresSeries = new TimeSeries("Membres");
        TimeSeries empruntsSeries = new TimeSeries("Emprunts");

        // Récupérer et ajouter les données
        addDataToSeries(contributionsSeries, contributionManager.getMonthlyContributions(12), "Contributions");
        addDataToSeries(membresSeries, membreManager.getMonthlyRegistrations(12), "Members");
        addDataToSeries(empruntsSeries, empruntManager.getMonthlyEmprunts(12), "Loans");

        // Créer des datasets séparés pour chaque axe
        TimeSeriesCollection datasetContributions = new TimeSeriesCollection();
        datasetContributions.addSeries(contributionsSeries);

        TimeSeriesCollection datasetOthers = new TimeSeriesCollection();
        datasetOthers.addSeries(membresSeries);
        datasetOthers.addSeries(empruntsSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",
                "Mois",
                "Valeurs",
                datasetContributions, // Dataset principal (contributions)
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        // Configurer le second axe Y pour les petites valeurs
        NumberAxis axis2 = new NumberAxis("Membres/Emprunts");
        plot.setRangeAxis(1, axis2);
        plot.setDataset(1, datasetOthers);

        // Associer les séries aux axes
        plot.mapDatasetToRangeAxis(0, 0); // Contributions sur axe principal
        plot.mapDatasetToRangeAxis(1, 1); // Membres et emprunts sur second axe

        // Personnalisation des rendus
        XYAreaRenderer renderer1 = new XYAreaRenderer();
        renderer1.setSeriesPaint(0, new Color(63, 81, 181)); // Bleu pour contributions
        plot.setRenderer(0, renderer1);

        XYAreaRenderer renderer2 = new XYAreaRenderer();
        renderer2.setSeriesPaint(0, new Color(76, 175, 80)); // Vert pour membres
        renderer2.setSeriesPaint(1, new Color(255, 152, 0)); // Orange pour emprunts
        plot.setRenderer(1, renderer2);

        // Améliorer l'axe des dates
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM yyyy", Locale.FRENCH));
        axis.setVerticalTickLabels(true);

        // Configurer les axes Y
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false);

        NumberAxis rangeAxis2 = (NumberAxis) plot.getRangeAxis(1);
        rangeAxis2.setAutoRangeIncludesZero(false);

        return chart;
    }

    private void addDataToSeries(TimeSeries series, Map<String, ?> data, String seriesName) {

        System.out.println("=== Données pour " + seriesName + " ===");
        if (data == null) {
            System.out.println("Aucune donnée (data est null)");
        } else {
            System.out.println("Nombre d'entrées: " + data.size());
            data.forEach((k, v) -> System.out.println(k + " => " + v));
        }
        System.out.println("Adding data for " + seriesName);
        if (data == null || data.isEmpty()) {
            System.err.println("Aucune donnée disponible pour " + seriesName);
            // Ajouter un point zéro pour assurer la visibilité
            int year = LocalDate.now().getYear();
            int month = LocalDate.now().getMonthValue();
            series.add(new Month(month, year), 0);
            return;
        }

        data.forEach((monthStr, value) -> {
            try {
                String[] parts = monthStr.split("-");
                if (parts.length != 2) {
                    System.err.println("Format de mois invalide: " + monthStr);
                    return;
                }

                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);

                double val = 0;
                if (value instanceof BigDecimal) {
                    val = ((BigDecimal)value).doubleValue();
                } else if (value instanceof Number) {
                    val = ((Number)value).doubleValue();
                }

                // Supprimez la multiplication par 1000 ou ajustez le facteur
                System.out.printf("Adding %s: %s = %.2f%n", seriesName, monthStr, val);

                series.add(new Month(month, year), val);
            } catch (Exception e) {
                System.err.println("Error adding data point for " + seriesName + ", month: " + monthStr);
                e.printStackTrace();
            }
        });
    }

    private TimeSeries createDemoSeries() {
        System.out.println("Creating demo data series");
        TimeSeries demoSeries = new TimeSeries("Demo Data");
        for (int i = 1; i <= 12; i++) {
            demoSeries.add(new Month(i, 2023), 100 + (i * 10));
        }
        return demoSeries;
    }

    private JPanel createEssentialInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Informations Essentielles");
        titleLabel.setFont(Fonts.mediumBoldFont());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel infoContent = new JPanel(new GridLayout(2, 2, 15, 15));
        infoContent.setBackground(Color.WHITE);

        // Top contributeurs
        infoContent.add(createInfoBox(
                "Top Contributeurs",
                contributionManager.getTopContributors(5),
                new JLabel(IconManager.getMaterialIcon("star", 24)) {{
                    setForeground(Colors.WARNING);
                }}));

        // Derniers membres inscrits
        infoContent.add(createInfoBox(
                "Derniers Membres",
                membreManager.getLatestRegistrationsGroupedByDate(5),
                new JLabel(IconManager.getMaterialIcon("person_add", 24)) {{
                    setForeground(Colors.PRIMARY);
                }}));

        // Emprunts en retard
        infoContent.add(createInfoBox(
                "Emprunts en Retard",
                empruntManager.getLateLoans(5),
                new JLabel(IconManager.getMaterialIcon("warning", 24)) {{
                    setForeground(Colors.DANGER);
                }}));

        // Contributions récentes
        infoContent.add(createInfoBox(
                "Contributions Récentes",
                contributionManager.getRecentContributions(5),
                new JLabel(IconManager.getMaterialIcon("attach_money", 24)) {{
                    setForeground(Colors.SUCCESS);
                }}));

        panel.add(infoContent, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInfoBox(String title, Map<String, ?> data, JLabel icon) {
        JPanel box = new JPanel(new BorderLayout(10, 5));
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        box.setBackground(Colors.BACKGROUND);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.smallBoldFont());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(icon, BorderLayout.EAST);

        box.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        data.forEach((key, value) -> {
            JLabel item = new JLabel(key + ": " + value);
            item.setFont(Fonts.smallFont());
            item.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            contentPanel.add(item);
        });

        box.add(contentPanel, BorderLayout.CENTER);
        return box;
    }
}