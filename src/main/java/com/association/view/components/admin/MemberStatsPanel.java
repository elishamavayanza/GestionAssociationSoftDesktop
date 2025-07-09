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
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartUtils;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
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
        panel.setLayout(new GridLayout(1, 3, 15, 0)); // 3 colonnes au lieu de 2
        panel.setBackground(Colors.BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Graphique circulaire
        JFreeChart pieChart = createPieChart();
        ChartPanel pieChartPanel = new ChartPanel(pieChart);

        panel.add(wrapChartPanel(pieChartPanel, "Répartition par Statut"));

        // Graphique linéaire
        JFreeChart lineChart = createLineChart(12);
        ChartPanel lineChartPanel = new ChartPanel(lineChart);
        panel.add(wrapChartPanel(lineChartPanel, "Évolution des Inscriptions"));

        // Nouveau graphique à barres
        JFreeChart barChart = createAgeGroupChart();
        ChartPanel barChartPanel = new ChartPanel(barChart);
        panel.add(wrapChartPanel(barChartPanel, "Répartition par Âge"));

        return panel;
    }

    private JPanel wrapChartPanel(ChartPanel chartPanel, String title) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        wrapper.setBackground(Color.WHITE);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Titre avec style amélioré
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        titleLabel.setForeground(new Color(66, 66, 66));

        // Bouton d'export
        JButton exportButton = new JButton("Exporter");
        exportButton.setFont(Fonts.normalFont());
        exportButton.setForeground(Color.WHITE);
        exportButton.setBackground(Colors.PRIMARY);
        exportButton.setOpaque(true);
        exportButton.setBorderPainted(false);

// Ajouter l'effet de survol avec MouseAdapter
        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                exportButton.setBackground(Colors.darker(Colors.PRIMARY, 0.1f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exportButton.setBackground(Colors.PRIMARY);
            }
        });
        exportButton.addActionListener(e -> exportChart(chartPanel.getChart()));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(exportButton, BorderLayout.EAST);
        headerPanel.setOpaque(false);

        wrapper.add(headerPanel, BorderLayout.NORTH);
        wrapper.add(chartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private void exportChart(JFreeChart chart) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter le graphique");

        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG Image", "png");
        FileNameExtensionFilter jpegFilter = new FileNameExtensionFilter("JPEG Image", "jpg", "jpeg");
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.addChoosableFileFilter(jpegFilter);
        fileChooser.setFileFilter(pngFilter);

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String format = ((FileNameExtensionFilter)fileChooser.getFileFilter()).getExtensions()[0];

            try {
                if ("png".equalsIgnoreCase(format)) {
                    ChartUtils.saveChartAsPNG(fileToSave, chart, 800, 600);
                } else if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
                    ChartUtils.saveChartAsJPEG(fileToSave, chart, 800, 600);
                } else {
                    throw new IOException("Format d'image non pris en charge : " + format);
                }
                JOptionPane.showMessageDialog(this,
                        "Graphique exporté avec succès!",
                        "Export réussi",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'export: " + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JFreeChart createPieChart() {
        Map<String, Object> stats = membreManager.getMembreStats();
        System.out.println("Stats: " + stats);

        int totalMembres = ((Number) stats.get("total")).intValue();
        System.out.println("Total membres: " + totalMembres);

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Actifs", (Number) stats.get("actifs"));
        dataset.setValue("Inactifs", (Number) stats.get("inactifs"));
        dataset.setValue("Suspendus", (Number) stats.get("suspendus"));

        JFreeChart chart = ChartFactory.createRingChart(
                "", // Titre vide
                dataset,
                true, // légende
                true, // tooltips
                false // URLs
        );

        // Personnalisation du graphique en couronne
        RingPlot plot = (RingPlot) chart.getPlot();

        // Configuration du texte central
        plot.setCenterText(generateCenterText(totalMembres));
        plot.setCenterTextFont(new Font("SansSerif", Font.BOLD, 24));
        plot.setCenterTextColor(Colors.PRIMARY);

        // Ajustez ces valeurs si nécessaire pour l'espacement
        plot.setSectionDepth(0.35);
        plot.setInnerSeparatorExtension(0.05);
        plot.setOuterSeparatorExtension(0.05);

        // Utilisation des mêmes couleurs claires que pour les cartes
        plot.setSectionPaint("Actifs", Colors.lighter(Colors.SUCCESS, 0.7f));
        plot.setSectionPaint("Inactifs", Colors.lighter(Colors.WARNING, 0.7f));
        plot.setSectionPaint("Suspendus", Colors.lighter(Colors.DANGER, 0.7f));

        // Labels
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})",
                NumberFormat.getNumberInstance(),
                NumberFormat.getPercentInstance()
        ));
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);

        // Tooltips
        plot.setToolTipGenerator(new StandardPieToolTipGenerator(
                "<html><b>{0}</b><br>Membres: {1}<br>{2}</html>",
                NumberFormat.getNumberInstance(),
                NumberFormat.getPercentInstance()
        ));

        // Arrière-plan
        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);

        // Légende
        LegendTitle legend = chart.getLegend();
        legend.setBackgroundPaint(null);

        return chart;
    }


    private String generateCenterText(int total) {
        return "<html><div style='text-align: center;'>"
                + "<span style='font-size: 24px; font-weight: bold;'>"
                + NumberFormat.getNumberInstance().format(total)
                + "</span><br>"
                + "<span style='font-size: 14px;'>Membres</span>"
                + "</div></html>";
    }

    private JPanel createLineChartWithControls() {
        JPanel panel = new JPanel(new BorderLayout());

        // Contrôles en haut
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsPanel.setOpaque(false);

        JComboBox<String> periodCombo = new JComboBox<>(new String[]{"12 mois", "24 mois", "36 mois", "Tout"});
        periodCombo.addActionListener(e -> {
            String selected = (String) periodCombo.getSelectedItem();
            int months = 0; // Tout par défaut
            if (selected.contains("12")) months = 12;
            else if (selected.contains("24")) months = 24;
            else if (selected.contains("36")) months = 36;

            updateLineChart(months);
        });

        controlsPanel.add(new JLabel("Période:"));
        controlsPanel.add(periodCombo);

        // Graphique - initialiser avec 12 mois par défaut
        ChartPanel chartPanel = new ChartPanel(createLineChart(12));

        panel.add(controlsPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private void updateLineChart(int months) {
        // Implémentez la logique pour mettre à jour le graphique
        ChartPanel newChartPanel = new ChartPanel(createLineChart(months));

        // en fonction de la période sélectionnée
    }


    private JFreeChart createLineChart(int months) {
//        Map<String, Integer> monthlyData = membreManager.getMonthlyRegistrations();
        Map<String, Integer> monthlyData = membreManager.getMonthlyRegistrations(months);

//        Map<String, Integer> monthlyData = membreManager.getMonthlyRegistrations(12); // 12 mois par défaut
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

        CategoryPlot plot = chart.getCategoryPlot();

        // Style moderne
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinePaint(new Color(230, 230, 230));

        // Ligne plus épaisse et colorée
        plot.getRenderer().setSeriesPaint(0, new Color(33, 150, 243)); // Bleu
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(3f));
        plot.getRenderer().setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));

        // Axes améliorés
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        // Fond transparent
        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);

        // Légende améliorée
        LegendTitle legend = chart.getLegend();
        legend.setItemFont(new Font("SansSerif", Font.PLAIN, 12));
        legend.setBackgroundPaint(null);

        return chart;
    }
    private JFreeChart createAgeGroupChart() {
        Map<String, Integer> ageData = membreManager.getMembersByAgeGroup();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        ageData.forEach((ageGroup, count) -> {
            dataset.addValue(count, "Membres", ageGroup);
        });

        JFreeChart chart = ChartFactory.createBarChart(
                "",
                "Tranche d'âge",
                "Nombre de membres",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();

        // Améliorations visuelles
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinePaint(new Color(230, 230, 230));

        // Couleur des barres
        plot.getRenderer().setSeriesPaint(0, new Color(63, 81, 181));

        // Taille des polices
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        // Transparence
        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);

        return chart;
    }
}