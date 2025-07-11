package com.association.view.components.rapport;

import com.association.dao.DAOFactory;
import com.association.dao.RapportDao;
import com.association.manager.RapportManager;
import com.association.model.Rapport;
import com.association.model.enums.TypeRapport;
import com.association.util.file.FileExportUtil;
import com.association.util.file.FileType;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Panel pour la génération et l'export de rapports
 */
public class RapportPanel extends JPanel {
    // Services
    private final RapportManager rapportManager;
    private final ExecutorService executorService;

    // Composants UI
    private JComboBox<TypeRapport> typeRapportCombo;
    private JTextArea rapportContentArea;
    private JButton generateButton;
    private JButton exportPdfButton;
    private JButton exportExcelButton;
    private JButton clearButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JCheckBox includeDetailsCheckbox;

    // Format de date
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public RapportPanel() {
        this.rapportManager = new RapportManager(DAOFactory.getInstance(RapportDao.class));
        this.executorService = Executors.newSingleThreadExecutor();

        initUI();
        setupListeners();
    }

    /**
     * Initialise l'interface utilisateur
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel de configuration
        JPanel configPanel = createConfigPanel();
        add(configPanel, BorderLayout.NORTH);

        // Zone de contenu principale
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        // Panel de statut et actions
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
    }

    /**
     * Crée le panel de configuration
     */
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Configuration du rapport",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sélection du type de rapport
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Type de rapport:"), gbc);

        gbc.gridx = 1;
        typeRapportCombo = new JComboBox<>(TypeRapport.values());
        typeRapportCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(typeRapportCombo, gbc);

        // Option pour inclure les détails
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        includeDetailsCheckbox = new JCheckBox("Inclure les détails complets");
        includeDetailsCheckbox.setSelected(true);
        panel.add(includeDetailsCheckbox, gbc);

        return panel;
    }

    /**
     * Crée le panel de contenu principal
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Contenu du rapport",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        rapportContentArea = new JTextArea(20, 60);
        rapportContentArea.setEditable(false);
        rapportContentArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        rapportContentArea.setLineWrap(true);
        rapportContentArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(rapportContentArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crée le panel d'actions et de statut
     */
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Barre de progression
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panel.add(progressBar, BorderLayout.NORTH);

        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        // Bouton Générer
        generateButton = new JButton("Générer mon rapport de membre");
        styleButton(generateButton, Colors.CURRENT_PRIMARY, Color.WHITE);

        generateButton.setIcon(IconManager.getIcon("generate.svg", 16));
        buttonPanel.add(generateButton);

        // Bouton Effacer
        clearButton = new JButton("Effacer");
        styleButton(clearButton, Colors.CURRENT_PRIMARY, Color.WHITE);

        clearButton.setIcon(IconManager.getIcon("clear.svg", 16));
        buttonPanel.add(clearButton);

        // Bouton Export PDF
        exportPdfButton = new JButton("PDF");
        styleButton(exportPdfButton, Colors.CURRENT_PRIMARY, Color.WHITE);

        exportPdfButton.setIcon(IconManager.getIcon("pdf.svg", 16));
        exportPdfButton.setEnabled(false);
        buttonPanel.add(exportPdfButton);

        // Bouton Export Excel
        exportExcelButton = new JButton("Excel");
        styleButton(exportExcelButton, Colors.CURRENT_PRIMARY, Color.WHITE);

        exportExcelButton.setIcon(IconManager.getIcon("excel.svg", 16));
        exportExcelButton.setEnabled(false);
        buttonPanel.add(exportExcelButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // Label de statut
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        panel.add(statusLabel, BorderLayout.SOUTH);



        return panel;
    }


    /**
     * Configure les écouteurs d'événements
     */
    private void setupListeners() {
        generateButton.addActionListener(e -> {
            TypeRapport selectedType = (TypeRapport) typeRapportCombo.getSelectedItem();
            JOptionPane.showMessageDialog(this,
                    "Génération du rapport " + selectedType.toString().toLowerCase() + " en cours...",
                    "Génération de rapport",
                    JOptionPane.INFORMATION_MESSAGE);
            generateReport();
        });
        clearButton.addActionListener(e -> clearReport());
        exportPdfButton.addActionListener(e -> exportReport(FileType.PDF));
        exportExcelButton.addActionListener(e -> exportReport(FileType.EXCEL));

        // Ajoutez l'écouteur pour le combo box
        typeRapportCombo.addActionListener(e -> {
            if (e.getSource() == typeRapportCombo) {
                handleReportTypeChange();
            }
        });
    }

    private void handleReportTypeChange() {
        TypeRapport selectedType = (TypeRapport) typeRapportCombo.getSelectedItem();

        generateButton.setText("Générer mon rapport " + selectedType.toString().toLowerCase());
        rapportContentArea.setText("");
        exportPdfButton.setEnabled(false);
        exportExcelButton.setEnabled(false);

        switch (selectedType) {
            case MEMBRES:
                includeDetailsCheckbox.setSelected(true);
                includeDetailsCheckbox.setEnabled(true);
                statusLabel.setText("Prêt à générer mon rapport sur les membres");
                break;
            case FINANCIER:
                includeDetailsCheckbox.setSelected(false);
                includeDetailsCheckbox.setEnabled(false);
                statusLabel.setText("Générer un rapport financier");
                break;
            case MEMBRE_CONTRIBUTION_EMPRUNT:
                includeDetailsCheckbox.setSelected(true);
                includeDetailsCheckbox.setEnabled(true);
                statusLabel.setText("Générer un rapport détaillé membre/contribution/emprunt");
                break;
            default:
                includeDetailsCheckbox.setSelected(true);
                includeDetailsCheckbox.setEnabled(true);
                statusLabel.setText("Sélectionnez un type de rapport");
        }
    }

    /**
     * Génère le rapport
     */
    private void generateReport() {
        setBusyState(true);
        statusLabel.setText("Génération de mon rapport en cours...");

        executorService.execute(() -> {
            try {
                TypeRapport selectedType = (TypeRapport) typeRapportCombo.getSelectedItem();
                boolean includeDetails = includeDetailsCheckbox.isSelected();

                Rapport rapport;
                if (selectedType == TypeRapport.MEMBRES) {
                    rapport = rapportManager.genererRapportMembres();
                } else if (selectedType == TypeRapport.MEMBRE_CONTRIBUTION_EMPRUNT) {
                    rapport = rapportManager.genererRapportMembreContributionEmprunt(includeDetails);
                } else {
                    rapport = rapportManager.genererRapport(selectedType, includeDetails);
                }

                SwingUtilities.invokeLater(() -> {
                    if (rapport != null) {
                        rapportContentArea.setText(formatRapportContent(rapport));
                        exportPdfButton.setEnabled(true);
                        exportExcelButton.setEnabled(true);
                        statusLabel.setText("Mon rapport généré le " + DATE_FORMAT.format(rapport.getDateGeneration()));
                    } else {
                        statusLabel.setText("Erreur lors de la génération de mon rapport");
                    }
                    setBusyState(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erreur: " + ex.getMessage());
                    setBusyState(false);
                });
            }
        });
    }

    /**
     * Exporte le rapport dans le format spécifié
     */
    private void exportReport(FileType fileType) {
        setBusyState(true);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Export " + fileType + " en cours...");

        executorService.execute(() -> {
            try {
                String content = rapportContentArea.getText();
                String fileName = "rapport_" + typeRapportCombo.getSelectedItem() + "_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                // Simulation de progression
                for (int i = 0; i <= 100; i += 10) {
                    final int progress = i;
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    Thread.sleep(200);
                }

                boolean success = FileExportUtil.exportContent(content, fileName, fileType);

                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        statusLabel.setText("Export " + fileType + " terminé avec succès");
                    } else {
                        statusLabel.setText("Échec de l'export " + fileType);
                    }
                    progressBar.setVisible(false);
                    setBusyState(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Erreur d'export: " + ex.getMessage());
                    progressBar.setVisible(false);
                    setBusyState(false);
                });
            }
        });
    }

    /**
     * Efface le rapport actuel
     */
    private void clearReport() {
        rapportContentArea.setText("");
        exportPdfButton.setEnabled(false);
        exportExcelButton.setEnabled(false);
        statusLabel.setText(" ");
    }

    /**
     * Formate le contenu du rapport pour l'affichage
     */
    private String formatRapportContent(Rapport rapport) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MON RAPPORT PERSONNALISÉ ===\n\n");
        sb.append("Type: ").append(rapport.getType()).append("\n");
        sb.append("Date de génération: ").append(DATE_FORMAT.format(rapport.getDateGeneration())).append("\n\n");

        // Ajoute une ligne de séparation
        sb.append("----------------------------------------\n\n");

        // Contenu du rapport
        sb.append(rapport.getContenu());

        // Ajoute un pied de page
        sb.append("\n\n----------------------------------------\n");
        sb.append("Rapport généré par l'Association - Tous droits réservés");

        return sb.toString();
    }

    /**
     * Gère l'état occupé/libre de l'interface
     */
    private void setBusyState(boolean busy) {
        generateButton.setEnabled(!busy);
        exportPdfButton.setEnabled(!busy && exportPdfButton.isEnabled());
        exportExcelButton.setEnabled(!busy && exportExcelButton.isEnabled());
        clearButton.setEnabled(!busy);
        typeRapportCombo.setEnabled(!busy);
        includeDetailsCheckbox.setEnabled(!busy);
    }

    /**
     * Nettoyage des ressources
     */
    public void cleanup() {
        executorService.shutdown();
    }

    public static void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(Fonts.buttonFont());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}