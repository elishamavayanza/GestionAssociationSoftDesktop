package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.MembreManager;
import com.association.util.file.FileStorageService;
import com.association.util.file.RealFileStorageService;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartUtils;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Map;

public class MemberStatsPanel extends JPanel {
    private final MembreManager membreManager;
    private ChartPanel pieChartPanel;
    private ChartPanel lineChartPanel;
    private ChartPanel barChartPanel;

    public MemberStatsPanel() {
        this.membreManager = new MembreManager(DAOFactory.getInstance(MembreDao.class), new RealFileStorageService());
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        // Panel principal avec padding
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Colors.BACKGROUND);

        // Titre
        JLabel titleLabel = new JLabel("Statistiques des Membres");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Colors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel pour les cartes de statistiques
        JPanel statsCardsPanel = createStatsCardsPanel();
        statsCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel pour les graphiques avec indicateurs de chargement
        JPanel chartsPanel = createChartsPanel();
        chartsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Ajout des composants
        contentPanel.add(titleLabel);
        contentPanel.add(statsCardsPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(chartsPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.setBackground(Colors.BACKGROUND);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        Map<String, Object> stats = membreManager.getMembreStats();

        Color primaryCardColor = Colors.lighter(Colors.PRIMARY, 0.7f);
        Color successCardColor = Colors.lighter(Colors.SUCCESS, 0.7f);
        Color warningCardColor = Colors.lighter(Colors.WARNING, 0.7f);
        Color dangerCardColor = Colors.lighter(Colors.DANGER, 0.7f);

        panel.add(createStatCard(
                "Total Membres",
                stats.get("total").toString(),
                IconManager.getIcon("groups.svg", 30),
                primaryCardColor,
                Colors.PRIMARY
        ));

        panel.add(createStatCard(
                "Membres Actifs",
                stats.get("actifs").toString(),
                IconManager.getIcon("active_user.svg", 30),
                successCardColor,
                Colors.SUCCESS
        ));

        panel.add(createStatCard(
                "Membres Inactifs",
                stats.get("inactifs").toString(),
                IconManager.getIcon("inactive_user.svg", 30),
                warningCardColor,
                Colors.WARNING
        ));

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
        card.setPreferredSize(new Dimension(card.getPreferredSize().width, 140));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setMinimumSize(new Dimension(0, 140));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.smallBoldFont());
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Colors.TEXT_DARK);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setForeground(Colors.TEXT_DARK);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(valueLabel, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.setBackground(Colors.BACKGROUND);

        // Graphique circulaire (à gauche)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2; // Prendra 2 lignes de hauteur
        gbc.weightx = 0.5;  // Moitié de la largeur
        gbc.weighty = 1.0;
        JPanel pieChartWrapper = createChartWithLoading("Répartition par Statut", () -> {
            JFreeChart pieChart = createPieChart();
            pieChartPanel = new ChartPanel(pieChart);
            configurePieChartInteractions(pieChartPanel);
            return pieChartPanel;
        });
        panel.add(pieChartWrapper, gbc);

        // Réinitialisation des paramètres pour les graphiques de droite
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;

        // Graphique linéaire (en haut à droite)
        gbc.gridx = 1;
        gbc.gridy = 0;
        JPanel lineChartWrapper = createChartWithLoading("Évolution des Inscriptions", () -> {
            JFreeChart lineChart = createLineChart(12);
            lineChartPanel = new ChartPanel(lineChart);
            configureLineChartInteractions(lineChartPanel);
            return lineChartPanel;
        });
        panel.add(lineChartWrapper, gbc);

        // Graphique à barres (en bas à droite)
        gbc.gridx = 1;
        gbc.gridy = 1;
        JPanel barChartWrapper = createChartWithLoading("Répartition par Âge", () -> {
            JFreeChart barChart = createAgeGroupChart();
            barChartPanel = new ChartPanel(barChart);
            configureBarChartInteractions(barChartPanel);
            return barChartPanel;
        });
        panel.add(barChartWrapper, gbc);

        return panel;
    }

    private JPanel createChartWithLoading(String title, ChartLoader chartLoader) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        wrapper.setBackground(Color.WHITE);

        // Header avec titre et bouton d'export
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        titleLabel.setForeground(new Color(66, 66, 66));

        JButton exportButton = new JButton("Exporter");
        exportButton.setFont(Fonts.normalFont());
        exportButton.setForeground(Color.WHITE);
        exportButton.setBackground(Colors.PRIMARY);
        exportButton.setOpaque(true);
        exportButton.setBorderPainted(false);
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

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(exportButton, BorderLayout.EAST);

        // Panel de chargement
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        JLabel loadingLabel = new JLabel("Chargement des données...", SwingConstants.CENTER);
        loadingLabel.setFont(Fonts.normalFont());
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);

        wrapper.add(headerPanel, BorderLayout.NORTH);
        wrapper.add(loadingPanel, BorderLayout.CENTER);

        // Chargement asynchrone du graphique
        new SwingWorker<ChartPanel, Void>() {
            @Override
            protected ChartPanel doInBackground() throws Exception {
                Thread.sleep(800); // Simulation de chargement
                return chartLoader.loadChart();
            }

            @Override
            protected void done() {
                try {
                    ChartPanel chartPanel = get();
                    exportButton.addActionListener(e -> exportChart(chartPanel.getChart()));
                    wrapper.remove(loadingPanel);
                    wrapper.add(chartPanel, BorderLayout.CENTER);
                    wrapper.revalidate();
                    wrapper.repaint();
                } catch (Exception e) {
                    loadingLabel.setText("Erreur de chargement des données");
                    e.printStackTrace();
                }
            }
        }.execute();

        return wrapper;
    }

    private void configurePieChartInteractions(ChartPanel chartPanel) {
        RingPlot plot = (RingPlot) chartPanel.getChart().getPlot();

        // Animation au survol
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity instanceof PieSectionEntity) {
                    PieSectionEntity sectionEntity = (PieSectionEntity) entity;
                    String sectionKey = (String) sectionEntity.getSectionKey();
                    double explodePercent = plot.getExplodePercent(sectionKey);
                    plot.setExplodePercent(sectionKey, explodePercent == 0.0 ? 0.1 : 0.0);
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity instanceof PieSectionEntity) {
                    PieSectionEntity sectionEntity = (PieSectionEntity) entity;
                    String sectionKey = (String) sectionEntity.getSectionKey();
                    plot.setExplodePercent(sectionKey, 0.05);
                } else {
                    for (Object key : plot.getDataset().getKeys()) {
                        plot.setExplodePercent((Comparable<?>) key, 0.0);
                    }
                }
            }
        });
    }

    private void configureLineChartInteractions(ChartPanel chartPanel) {
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);

        // Animation de la ligne
        CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

        Timer timer = new Timer(50, new ActionListener() {
            float alpha = 0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (alpha < 1f) {
                    alpha += 0.05f;
                    renderer.setSeriesPaint(0, new Color(
                            33, 150, 243, (int)(255 * alpha))
                    );
                    chartPanel.repaint();
                } else {
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    private void configureBarChartInteractions(ChartPanel chartPanel) {
        CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Animation des barres
        Timer timer = new Timer(50, new ActionListener() {
            float alpha = 0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (alpha < 1f) {
                    alpha += 0.05f;
                    renderer.setSeriesPaint(0, new Color(
                            63, 81, 181, (int)(255 * alpha))
                    );
                    chartPanel.repaint();
                } else {
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    private JFreeChart createPieChart() {
        Map<String, Object> stats = membreManager.getMembreStats();
        int totalMembres = ((Number) stats.get("total")).intValue();

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Actifs", (Number) stats.get("actifs"));
        dataset.setValue("Inactifs", (Number) stats.get("inactifs"));
        dataset.setValue("Suspendus", (Number) stats.get("suspendus"));

        JFreeChart chart = ChartFactory.createRingChart("", dataset, true, true, false);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setCenterText(generateCenterText(totalMembres));
        plot.setCenterTextFont(new Font("SansSerif", Font.BOLD, 24));
        plot.setCenterTextColor(Colors.PRIMARY);
        plot.setSectionDepth(0.35);
        plot.setInnerSeparatorExtension(0.05);
        plot.setOuterSeparatorExtension(0.05);
        plot.setSectionPaint("Actifs", Colors.lighter(Colors.SUCCESS, 0.7f));
        plot.setSectionPaint("Inactifs", Colors.lighter(Colors.WARNING, 0.7f));
        plot.setSectionPaint("Suspendus", Colors.lighter(Colors.DANGER, 0.7f));

        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})",
                NumberFormat.getNumberInstance(),
                NumberFormat.getPercentInstance()
        ));
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);

        plot.setToolTipGenerator(new StandardPieToolTipGenerator(
                "<html><div style='padding:5px;background:#ffffff;border:1px solid #cccccc;'>"
                        + "<b>{0}</b><br>"
                        + "Nombre: <b>{1}</b><br>"
                        + "Pourcentage: <b>{2}</b>"
                        + "</div></html>",
                NumberFormat.getNumberInstance(),
                NumberFormat.getPercentInstance()
        ));

        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);

        LegendTitle legend = chart.getLegend();
        legend.setBackgroundPaint(null);

        return chart;
    }

    private JFreeChart createLineChart(int months) {
        Map<String, Integer> monthlyData = membreManager.getMonthlyRegistrations(months);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        monthlyData.forEach((month, count) -> dataset.addValue(count, "Inscriptions", month));

        JFreeChart chart = ChartFactory.createLineChart(
                "", "Mois", "Nombre d'inscriptions",
                dataset, PlotOrientation.VERTICAL, true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinePaint(new Color(230, 230, 230));

        LineAndShapeRenderer renderer = new LineAndShapeRenderer(true, true) {
            @Override
            public Paint getItemPaint(int row, int column) {
                return plot.isDomainGridlinesVisible()
                        ? new Color(33, 150, 243)
                        : new Color(33, 150, 243, 100);
            }
        };
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShape(new Ellipse2D.Double(-4, -4, 8, 8));
        renderer.setDefaultShapesFilled(true);
        renderer.setDefaultStroke(new BasicStroke(3f));
        plot.setRenderer(renderer);

        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);

        LegendTitle legend = chart.getLegend();
        legend.setItemFont(new Font("SansSerif", Font.PLAIN, 12));
        legend.setBackgroundPaint(null);

        return chart;
    }

    private JFreeChart createAgeGroupChart() {
        Map<String, Integer> ageData = membreManager.getMembersByAgeGroup();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        ageData.forEach((ageGroup, count) -> dataset.addValue(count, "Membres", ageGroup));

        JFreeChart chart = ChartFactory.createBarChart(
                "", "Tranche d'âge", "Nombre de membres",
                dataset, PlotOrientation.VERTICAL, true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setDomainGridlinePaint(new Color(230, 230, 230));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new GradientPaint(
                0, 0, new Color(63, 81, 181),
                0, 0, new Color(100, 181, 246)
        ));
        renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator(
                "<html><div style='padding:5px'>"
                        + "<b>{1}</b><br>"
                        + "Membres: <b>{2}</b>"
                        + "</div></html>",
                NumberFormat.getInstance()
        ));

        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);

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

    @FunctionalInterface
    private interface ChartLoader {
        ChartPanel loadChart();
    }
}