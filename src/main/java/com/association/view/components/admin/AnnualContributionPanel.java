package com.association.view.components.admin;

import com.association.manager.ContributionManager;
import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Contribution;
import com.association.util.utils.MoneyUtil;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    public AnnualContributionPanel(Long membreId, ContributionManager contributionManager) {
        this.membreId = membreId;
        this.contributionManager = contributionManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Colors.CARD_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titre en haut
        JLabel titleLabel = new JLabel("Contribution Annuelle");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setForeground(Colors.TEXT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Panel principal avec formulaire en haut
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Colors.CARD_BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

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
        submitButton = new JButton("Enregistrer");
        submitButton.setFont(Fonts.buttonFont());
        submitButton.setBackground(Colors.PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(this::submitContribution);
        formPanel.add(submitButton, gbc);

        add(formPanel, BorderLayout.NORTH);

        // Panel d'historique en bas
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(Colors.CARD_BACKGROUND);
        historyPanel.setBorder(BorderFactory.createTitledBorder("Historique des Contributions"));

        // Modèle de table pour l'historique
        historyTableModel = new DefaultTableModel(
                new Object[]{"Date", "Montant", "Type"}, 0);
        historyTable = new JTable(historyTableModel);
        historyTable.setFont(Fonts.tableFont());
        historyTable.getTableHeader().setFont(Fonts.tableHeaderFont());

        // Ajout du tableau avec scroll
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(0, 200)); // Hauteur fixe, largeur flexible
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        add(historyPanel, BorderLayout.CENTER);

        // Charger l'historique initial
        loadContributionHistory();
    }

    private void loadContributionHistory() {
        historyTableModel.setRowCount(0);
        List<Contribution> contributions = contributionManager.findByMembreAndType(
                membreId,
                TypeContribution.ANNUELLE
        );

        for (Contribution contribution : contributions) {
            historyTableModel.addRow(new Object[]{
                    contribution.getDateTransaction(),
                    MoneyUtil.format(contribution.getMontant(), Locale.FRANCE),
                    contribution.getTypeContribution().toString()
            });
        }
    }

    private void submitContribution(ActionEvent e) {
        try {
            // Validation du montant
            BigDecimal amount = MoneyUtil.parse(amountField.getText(), Locale.FRANCE)
                    .orElseThrow(() -> new IllegalArgumentException("Montant invalide"));

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit être positif");
            }

            // Validation de la date
            java.util.Date utilDate = (java.util.Date) dateField.getValue();
            if (utilDate == null) {
                throw new IllegalArgumentException("Date invalide");
            }
            LocalDate date = new java.sql.Date(utilDate.getTime()).toLocalDate();

            // Enregistrement
            boolean success = contributionManager.enregistrerContribution(
                    membreId,
                    amount,
                    date,
                    TypeContribution.ANNUELLE.name()
            );

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Contribution annuelle enregistrée avec succès",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                resetForm();
                loadContributionHistory(); // Rafraîchir l'historique
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'enregistrement",
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
        amountField.setText("");
        dateField.setValue(java.sql.Date.valueOf(LocalDate.now()));
    }

    @Override
    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        resetForm();
        loadContributionHistory();
    }
}