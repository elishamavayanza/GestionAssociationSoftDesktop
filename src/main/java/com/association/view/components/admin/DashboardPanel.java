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
        // Création des séries temporelles
        TimeSeries contributionsSeries = new TimeSeries("Contributions");
        TimeSeries membresSeries = new TimeSeries("Membres");
        TimeSeries empruntsSeries = new TimeSeries("Emprunts");

        // Remplissage des données (exemple avec 12 derniers mois)
        Map<String, BigDecimal> monthlyContribs = contributionManager.getMonthlyContributions(12);
        Map<String, Integer> monthlyMembres = membreManager.getMonthlyRegistrations(12);
        Map<String, Integer> monthlyEmprunts = empruntManager.getMonthlyEmprunts(12);

        monthlyContribs.forEach((month, amount) ->
                contributionsSeries.add(new Month(Integer.parseInt(month.split("-")[1]),
                                Integer.parseInt(month.split("-")[0])),
                        amount.doubleValue()));

        monthlyMembres.forEach((month, count) ->
                membresSeries.add(new Month(Integer.parseInt(month.split("-")[1]),
                                Integer.parseInt(month.split("-")[0])),
                        count.doubleValue()));

        monthlyEmprunts.forEach((month, count) ->
                empruntsSeries.add(new Month(Integer.parseInt(month.split("-")[1]),
                                Integer.parseInt(month.split("-")[0])),
                        count.doubleValue()));

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(contributionsSeries);
        dataset.addSeries(membresSeries);
        dataset.addSeries(empruntsSeries);

        // Création du graphique
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "", // titre
                "Mois", // axe x
                "Quantité", // axe y
                dataset, // données
                true, // légende
                true, // infobulles
                false // urls
        );

        // Personnalisation du graphique
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        XYAreaRenderer renderer = new XYAreaRenderer();
        renderer.setSeriesPaint(0, new Color(63, 81, 181, 150)); // Contributions - bleu
        renderer.setSeriesPaint(1, new Color(76, 175, 80, 150));  // Membres - vert
        renderer.setSeriesPaint(2, new Color(255, 152, 0, 150));  // Emprunts - orange
        plot.setRenderer(renderer);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM yyyy"));

        return chart;
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