package com.association.view.components.admin;

import com.association.manager.ContributionManager;
import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Contribution;
import com.association.util.utils.DateUtil;
import com.association.util.utils.MoneyUtil;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public class AnnualContributionPanel extends JPanel implements Refreshable {
    private Long membreId;
    private final ContributionManager contributionManager;

    private JTextField amountField;
    private JFormattedTextField dateField;
    private JButton submitButton;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JTextField searchField;
    private JButton exportButton;
    private JButton printButton;
    private Contribution contributionEnEdition; // Nouveau champ pour suivre l'édition

    public AnnualContributionPanel(Long membreId, ContributionManager contributionManager) {
        this.membreId = membreId;
        this.contributionManager = contributionManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel d'en-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colors.BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Titre
        JLabel titleLabel = new JLabel("Contributions Annuelles");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setForeground(Colors.TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Panel de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.setBackground(Colors.BACKGROUND);

        // Barre de recherche
        Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );

        searchField = new JTextField(20);
        searchField.setFont(Fonts.textFieldFont());
        searchField.setBorder(roundedBorder);
        searchField.setText("Rechercher...");
        searchField.setForeground(Color.GRAY);

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Rechercher...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Rechercher...");
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterContributions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterContributions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterContributions();
            }
        });

        JButton searchButton = new JButton(IconManager.getIcon("search.svg", 16));
        searchButton.setToolTipText("Rechercher");
        searchButton.setFocusPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setBorder(roundedBorder);
        searchButton.addActionListener(e -> filterContributions());

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Panel principal avec formulaire et tableau
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Colors.BACKGROUND);

        // Panel de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Colors.CARD_BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Montant
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel amountLabel = new JLabel("Montant:");
        amountLabel.setFont(Fonts.labelFont());
        formPanel.add(amountLabel, gbc);

        gbc.gridx = 1;
        amountField = new JTextField(15);
        amountField.setFont(Fonts.textFieldFont());
        formPanel.add(amountField, gbc);

        // Date
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(Fonts.labelFont());
        formPanel.add(dateLabel, gbc);

        gbc.gridx = 1;
        dateField = new JFormattedTextField(java.text.DateFormat.getDateInstance());
        dateField.setValue(java.sql.Date.valueOf(LocalDate.now()));
        dateField.setFont(Fonts.textFieldFont());
        dateField.setColumns(15);
        formPanel.add(dateField, gbc);

        // Bouton de soumission
        gbc.gridy++;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        submitButton = new JButton("Enregistrer", IconManager.getIcon("save.svg", 16));
        submitButton.setFont(Fonts.buttonFont());
        submitButton.setBackground(Colors.PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(this::submitContribution);
        formPanel.add(submitButton, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Panel de tableau
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Colors.BACKGROUND);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Modèle de table
        historyTableModel = new DefaultTableModel(
                new Object[]{"Date", "Montant", "Type", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Seule la colonne Actions est éditable
            }
        };

        historyTable = new JTable(historyTableModel);
        customizeTableAppearance();

        JScrollPane scrollPane = new JScrollPane(historyTable);
        customizeScrollBar(scrollPane.getVerticalScrollBar());
        customizeScrollBar(scrollPane.getHorizontalScrollBar());

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Panel de boutons en bas
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.setBackground(Colors.BACKGROUND);

        exportButton = new JButton("Exporter", IconManager.getIcon("export.svg", 16));
        printButton = new JButton("Imprimer", IconManager.getIcon("printer.svg", 16));

        for (JButton button : new JButton[]{exportButton, printButton}) {
            button.setFont(Fonts.buttonFont());
            button.setBackground(Colors.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        }

        buttonPanel.add(exportButton);
        buttonPanel.add(printButton);

        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Charger l'historique initial
        loadContributionHistory();
    }

    private void customizeTableAppearance() {
        historyTable.setRowHeight(30);
        historyTable.setFont(Fonts.tableFont());
        historyTable.getTableHeader().setFont(Fonts.tableHeaderFont());
        historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new Dimension(0, 1));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Personnalisation de l'en-tête
        historyTable.getTableHeader().setOpaque(false);
        historyTable.getTableHeader().setBackground(Colors.PRIMARY);
        historyTable.getTableHeader().setForeground(Color.WHITE);

        // Centrer le contenu des colonnes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Personnalisation de la colonne Actions
        historyTable.getColumnModel().getColumn(3).setCellRenderer(new TableActionCellRenderer());
        historyTable.getColumnModel().getColumn(3).setCellEditor(new TableActionCellEditor());

        // Largeur des colonnes
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Date
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Montant
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Type
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Actions
    }

    private void customizeScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Colors.SECONDARY;
                this.trackColor = Colors.CARD_BACKGROUND;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    private void loadContributionHistory() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<Contribution> contributions = contributionManager.findByMembreAndType(
                        membreId,
                        TypeContribution.ANNUELLE
                );

                SwingUtilities.invokeLater(() -> {
                    historyTableModel.setRowCount(0);
                    for (Contribution contribution : contributions) {
                        historyTableModel.addRow(new Object[]{
                                contribution.getDateTransaction(),
                                MoneyUtil.format(contribution.getMontant(), Locale.FRANCE),
                                contribution.getTypeContribution().toString(),
                                "Modifier|Supprimer" // Texte pour les boutons d'action
                        });
                    }
                });
                return null;
            }
        };
        worker.execute();
    }

    private void filterContributions() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty() || searchTerm.equals("Rechercher...")) {
            loadContributionHistory();
            return;
        }

        // Filtrer les contributions en fonction du terme de recherche
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<Contribution> contributions = contributionManager.findByMembreAndType(
                        membreId,
                        TypeContribution.ANNUELLE
                );

                contributions.removeIf(c ->
                        !MoneyUtil.format(c.getMontant(), Locale.FRANCE).contains(searchTerm) &&
                                !c.getDateTransaction().toString().contains(searchTerm)
                );

                SwingUtilities.invokeLater(() -> {
                    historyTableModel.setRowCount(0);
                    for (Contribution contribution : contributions) {
                        historyTableModel.addRow(new Object[]{
                                contribution.getDateTransaction(),
                                MoneyUtil.format(contribution.getMontant(), Locale.FRANCE),
                                contribution.getTypeContribution().toString(),
                                "Modifier|Supprimer"
                        });
                    }
                });
                return null;
            }
        };
        worker.execute();
    }

    private void submitContribution(ActionEvent e) {
        try {
            BigDecimal amount = MoneyUtil.parse(amountField.getText(), Locale.FRANCE)
                    .orElseThrow(() -> new IllegalArgumentException("Montant invalide"));

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit être positif");
            }

            java.util.Date utilDate = (java.util.Date) dateField.getValue();
            if (utilDate == null) {
                throw new IllegalArgumentException("Date invalide");
            }
            LocalDate date = new java.sql.Date(utilDate.getTime()).toLocalDate();

            boolean success;

            if (contributionEnEdition != null) {
                // Mode édition - mise à jour
                contributionEnEdition.setMontant(amount);
                contributionEnEdition.setDateTransaction(java.sql.Date.valueOf(date));
                success = contributionManager.update(contributionEnEdition);
            } else {
                // Mode création - nouvel enregistrement
                success = contributionManager.enregistrerContribution(
                        membreId,
                        amount,
                        date,
                        TypeContribution.ANNUELLE.name()
                );
            }

            if (success) {
                String message = contributionEnEdition != null ?
                        "Contribution mise à jour avec succès" :
                        "Contribution enregistrée avec succès";

                JOptionPane.showMessageDialog(this,
                        message,
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);

                // Réinitialiser après l'opération
                contributionEnEdition = null;
                submitButton.setText("Enregistrer");
                resetForm();
                loadContributionHistory();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'opération",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erreur de validation",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        contributionEnEdition = null;
        amountField.setText("");
        dateField.setValue(java.sql.Date.valueOf(LocalDate.now()));
        submitButton.setText("Enregistrer");
    }

    @Override
    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        resetForm();
        loadContributionHistory();
    }

    // Classe interne pour le rendu des cellules d'action
    private static class TableActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setBackground(isSelected ? Colors.PRIMARY_LIGHT : Colors.BACKGROUND);

            String[] actions = value.toString().split("\\|");
            for (String action : actions) {
                JButton button = new JButton(action);
                button.setFont(Fonts.tableFont());
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                if (action.equals("Modifier")) {
                    button.setBackground(Colors.WARNING);
                } else {
                    button.setBackground(Colors.DANGER);
                }
                button.setForeground(Color.WHITE);
                panel.add(button);
            }
            return panel;
        }
    }

    // Classe interne pour l'édition des cellules d'action
    private class TableActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton editButton;
        private JButton deleteButton;
        private int currentRow;

        public TableActionCellEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

            editButton = new JButton("Modifier");
            editButton.setFont(Fonts.tableFont());
            editButton.setBackground(Colors.WARNING);
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            editButton.addActionListener(e -> {
                editContribution(currentRow);
                fireEditingStopped();
            });

            deleteButton = new JButton("Supprimer");
            deleteButton.setFont(Fonts.tableFont());
            deleteButton.setBackground(Colors.DANGER);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            deleteButton.addActionListener(e -> {
                deleteContribution(currentRow);
                fireEditingStopped();
            });

            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Modifier|Supprimer";
        }
    }

    private void editContribution(int row) {
        try {
            // Récupérer la contribution correspondante à la ligne
            List<Contribution> contributions = contributionManager.findByMembreAndType(
                    membreId, TypeContribution.ANNUELLE);

            if (row >= 0 && row < contributions.size()) {
                contributionEnEdition = contributions.get(row);

                // Remplir les champs avec les valeurs de la contribution
                amountField.setText(MoneyUtil.format(contributionEnEdition.getMontant(), Locale.FRANCE));
                dateField.setValue(contributionEnEdition.getDateTransaction());

                // Changer le texte du bouton pour indiquer qu'on est en mode édition
                submitButton.setText("Mettre à jour");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'édition: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteContribution(int row) {
        try {
            // Récupération de la date sous forme de java.sql.Date
            java.sql.Date sqlDate = (java.sql.Date) historyTable.getValueAt(row, 0);
            LocalDate date = sqlDate.toLocalDate();  // Conversion correcte en LocalDate

            String amountStr = (String) historyTable.getValueAt(row, 1);
            BigDecimal amount = MoneyUtil.parse(amountStr, Locale.FRANCE).orElse(BigDecimal.ZERO);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Êtes-vous sûr de vouloir supprimer cette contribution du " + date + " ?",
                    "Confirmation de suppression",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = contributionManager.supprimerContribution(membreId, amount, date, TypeContribution.ANNUELLE.name());
                if (success) {
                    // Si on supprime la contribution en cours d'édition, réinitialiser
                    if (contributionEnEdition != null &&
                            DateUtil.toLocalDate(contributionEnEdition.getDateTransaction()).equals(date)
                            &&
                            contributionEnEdition.getMontant().compareTo(amount) == 0) {
                        resetForm();
                    }
                    historyTableModel.removeRow(row);
                    JOptionPane.showMessageDialog(this, "Contribution supprimée avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}