package com.association.view.components.admin;

import com.association.manager.EmpruntManager;
import com.association.manager.MembreManager;
import com.association.model.transaction.Emprunt;
import com.association.util.constants.DatePattern;
import com.association.util.utils.DateUtil;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EmpruntPanel extends JPanel implements Refreshable {
    private static final String[] TABLE_COLUMN_NAMES = {"ID", "Date", "Montant", "Remboursé", "Solde", "Statut", "Date Remb."};
    private static final String ELIGIBLE_ICON = "✔ ";
    private static final String NOT_ELIGIBLE_ICON = "✖ ";

    private Long membreId;
    private final EmpruntManager empruntManager;
    private final MembreManager membreManager;
    private JTable empruntTable;
    private JTable historiqueTable;
    private DefaultTableModel tableModel;
    private DefaultTableModel historiqueModel;
    private JButton addButton;
    private JButton refreshButton;
    private final JTabbedPane parentTabbedPane;
    private JTabbedPane empruntTabbedPane;

    public EmpruntPanel(Long membreId, EmpruntManager empruntManager, MembreManager membreManager, JTabbedPane parentTabbedPane) {
        this.membreId = membreId;
        this.empruntManager = empruntManager;
        this.membreManager = membreManager;
        this.parentTabbedPane = parentTabbedPane;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.CARD_BACKGROUND);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
        UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabHeight", 30);


        empruntTabbedPane = createTabbedPane();
        add(createTopPanel(), BorderLayout.NORTH);
        add(empruntTabbedPane, BorderLayout.CENTER);

        configureTabbedPane(parentTabbedPane);
        configureTabbedPane(empruntTabbedPane);
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Colors.CARD_BACKGROUND);
        tabbedPane.setForeground(Colors.TEXT);
        tabbedPane.setFont(Fonts.labelFont());

        ImageIcon currentIcon = IconManager.getScaledIcon("current_loans.svg", 16, 16); // Remplacez par votre icône
        ImageIcon historyIcon = IconManager.getScaledIcon("loan_history.svg", 16, 16); // Remplacez par votre icône

        tabbedPane.addTab("Emprunts en cours", currentIcon, createTablePanel(true));
        tabbedPane.addTab("Voir historique", historyIcon, createTablePanel(false));

        return tabbedPane;
    }

    private void configureTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setUI(new MetalTabbedPaneUI() {
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {}

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}

            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return Math.max(super.calculateTabWidth(tabPlacement, tabIndex, metrics), 100);
            }
        });
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topPanel.setBackground(Colors.CARD_BACKGROUND);

        addButton = createButton("Nouvel emprunt", Colors.CURRENT_DANGER, e -> handleAddEmprunt());
        topPanel.add(addButton);

        JButton checkButton = createButton("Vérifier éligibilité", Colors.CURRENT_INFO,
                e -> showEligibilityDialog(empruntManager.verifierEligibiliteDetail(membreId)));
        topPanel.add(checkButton);

        refreshButton = createButton("Actualiser", Colors.CURRENT_INFO, e -> loadData());
        topPanel.add(refreshButton);

        return topPanel;
    }

    private JButton createButton(String text, Color bgColor, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(Fonts.buttonFont());
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    private JPanel createTablePanel(boolean isCurrent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Colors.CARD_BACKGROUND);

        DefaultTableModel model = new DefaultTableModel(TABLE_COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        configureTable(table);

        if (isCurrent) {
            tableModel = model;
            empruntTable = table;
        } else {
            historiqueModel = model;
            historiqueTable = table;
        }

        JScrollPane scrollPane = createScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Colors.CARD_BACKGROUND);
        return scrollPane;
    }

    private void configureTable(JTable table) {
        table.setFont(Fonts.tableFont());
        table.getTableHeader().setFont(Fonts.tableHeaderFont());
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Colors.CARD_BACKGROUND);
        table.setForeground(Colors.TEXT);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        switchToRemboursementTab(model, row);
                    }
                }
            }
        });
    }

    private void loadData() {
        loadEmprunts();
        loadHistorique();
    }

    private void loadEmprunts() {
        populateTable(tableModel, empruntManager.getEmpruntsNonRembourses(membreId));
    }

    private void loadHistorique() {
        populateTable(historiqueModel, empruntManager.getEmpruntsMembre(membreId));
    }

    private void populateTable(DefaultTableModel model, List<Emprunt> emprunts) {
        model.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DatePattern.DATE_TIME.getPattern());

        for (Emprunt emprunt : emprunts) {
            model.addRow(new Object[]{
                    emprunt.getId(),
                    dateFormat.format(emprunt.getDateTransaction()),
                    formatCurrency(emprunt.getMontant()),
                    formatCurrency(emprunt.getMontantRembourse()),
                    formatCurrency(emprunt.calculerSoldeRestant()),
                    emprunt.getStatut().toString(),
                    emprunt.getDateRemboursement() != null ?
                            dateFormat.format(emprunt.getDateRemboursement()) : "N/A"
            });
        }
    }

    private void handleAddEmprunt() {
        Map<String, Object> eligibility = empruntManager.verifierEligibiliteDetail(membreId);
        String message = formatEligibilityMessage(eligibility);

        if ((boolean) eligibility.get("eligible")) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    message + "\nSouhaitez-vous continuer avec la demande d'emprunt?",
                    "Vérification d'éligibilité",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                showAddEmpruntDialog();
            }
        } else {
            showEligibilityDialog(eligibility);
        }
    }

    private void showEligibilityDialog(Map<String, Object> eligibility) {
        JOptionPane.showMessageDialog(
                this,
                formatEligibilityMessage(eligibility),
                "Statut d'éligibilité",
                (boolean) eligibility.get("eligible") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private void showAddEmpruntDialog() {
        Map<String, Object> eligibility = empruntManager.verifierEligibiliteDetail(membreId);
        if (!(boolean) eligibility.get("eligible")) {
            showEligibilityDialog(eligibility);
            return;
        }

        JDialog dialog = createEmpruntDialog();
        JPanel panel = createEmpruntDialogPanel(dialog);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JDialog createEmpruntDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Nouvel emprunt");
        dialog.setModal(true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private JPanel createEmpruntDialogPanel(JDialog dialog) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colors.CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel eligibilityLabel = new JLabel(ELIGIBLE_ICON + "Membre éligible à l'emprunt");
        eligibilityLabel.setForeground(Colors.CURRENT_SUCCESS);
        eligibilityLabel.setFont(Fonts.smallBoldFont());
        panel.add(eligibilityLabel, gbc);

        addFormField(panel, gbc, 1, "Montant (FCFA):");
        JTextField montantField = addTextField(panel, gbc, 2);

        addFormField(panel, gbc, 3, "Date de remboursement:");
        JTextField dateField = addTextField(panel, gbc, 4);
        dateField.setText(DateUtil.formatDate(new Date(), DatePattern.FRENCH_DATE));

        addFormField(panel, gbc, 5, "Description:");
        JTextArea descriptionArea = addTextArea(panel, gbc, 6);

        addButtonsPanel(dialog, panel, gbc, 7, montantField, dateField, descriptionArea);

        return panel;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int y, String label) {
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
    }

    private JTextField addTextField(JPanel panel, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        JTextField textField = new JTextField();
        textField.setFont(Fonts.textFieldFont());
        panel.add(textField, gbc);
        return textField;
    }

    private JTextArea addTextArea(JPanel panel, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        JTextArea textArea = new JTextArea(3, 20);
        textArea.setFont(Fonts.textFieldFont());
        textArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, gbc);
        return textArea;
    }

    private void addButtonsPanel(JDialog dialog, JPanel panel, GridBagConstraints gbc, int y,
                                 JTextField montantField, JTextField dateField, JTextArea descriptionArea) {
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.BOTH;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Colors.CARD_BACKGROUND);

        JButton cancelButton = createButton("Annuler", Colors.CURRENT_DANGER, e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = createButton("Enregistrer", Colors.CURRENT_SUCCESS, e ->
                saveEmprunt(dialog, montantField, dateField, descriptionArea));
        buttonPanel.add(saveButton);

        panel.add(buttonPanel, gbc);
    }

    private void saveEmprunt(JDialog dialog, JTextField montantField, JTextField dateField, JTextArea descriptionArea) {
        try {
            BigDecimal montant = new BigDecimal(montantField.getText().replaceAll("[^\\d.]", ""));
            Date dateRemboursement = DateUtil.parseDate(dateField.getText(), DatePattern.FRENCH_DATE)
                    .orElseThrow(() -> new IllegalArgumentException("Date invalide"));
            String description = descriptionArea.getText();

            validateEmpruntData(montant, dateRemboursement);

            if (empruntManager.demanderEmprunt(membreId, montant, dateRemboursement, description)) {
                JOptionPane.showMessageDialog(this, "Emprunt enregistré avec succès");
                loadData();
                dialog.dispose();
            } else {
                showErrorDialog("Erreur lors de l'enregistrement de l'emprunt");
            }
        } catch (NumberFormatException ex) {
            showErrorDialog("Montant invalide. Format attendu: 50000 ou 50000.00");
        } catch (IllegalArgumentException ex) {
            showErrorDialog(ex.getMessage());
        }
    }

    private void validateEmpruntData(BigDecimal montant, Date dateRemboursement) {
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        if (dateRemboursement.before(new Date())) {
            throw new IllegalArgumentException("La date de remboursement doit être dans le futur");
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void switchToRemboursementTab(DefaultTableModel model, int row) {
        if (parentTabbedPane != null) {
            Long empruntId = (Long) model.getValueAt(row, 0);
            BigDecimal soldeRestant = empruntManager.getSoldeRestant(empruntId);

            for (int i = 0; i < parentTabbedPane.getTabCount(); i++) {
                if ("Rembt".equals(parentTabbedPane.getTitleAt(i))) {
                    Component comp = parentTabbedPane.getComponentAt(i);
                    if (comp instanceof JScrollPane) {
                        Component view = ((JScrollPane) comp).getViewport().getView();
                        if (view instanceof RemboursementPanel) {
                            ((RemboursementPanel) view).setEmpruntData(empruntId, soldeRestant);
                        }
                    }
                    parentTabbedPane.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 FCFA";
        return String.format("%,.0f FCFA", amount);
    }

    private String formatEligibilityMessage(Map<String, Object> eligibility) {
        @SuppressWarnings("unchecked")
        List<String> raisons = (List<String>) eligibility.get("raisons");
        StringBuilder message = new StringBuilder();

        if ((boolean) eligibility.get("eligible")) {
            message.append(ELIGIBLE_ICON).append("Membre éligible à l'emprunt\n\nDétails:\n")
                    .append("- Contributions totales: ").append(formatCurrency((BigDecimal) eligibility.get("contributions"))).append("\n")
                    .append("- Statut: ").append(eligibility.get("statut")).append("\n");

            if (eligibility.get("dernierEmprunt") != null) {
                message.append("- Dernier emprunt il y a ").append(eligibility.get("joursDepuisDernierEmprunt")).append(" jours\n");
            }
        } else {
            message.append(NOT_ELIGIBLE_ICON).append("Membre non éligible\n\nRaisons:\n");
            for (String raison : raisons) {
                if (!raison.contains("éligible")) {
                    message.append("- ").append(raison).append("\n");
                }
            }
        }
        return message.toString();
    }

    @Override
    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        loadData();
    }
}