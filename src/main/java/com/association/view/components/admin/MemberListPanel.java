package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.dto.MembreSearchCriteria;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import com.association.view.components.common.AdvancedSearchDialog;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class MemberListPanel extends JPanel {
    private final JFrame parentFrame;
    private final MembreDao membreDao;
    private JTable memberTable;
    private DefaultTableModel tableModel;
    private JButton advancedSearchButton;
    private JTextField searchField;

    public MemberListPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.membreDao = DAOFactory.getInstance(MembreDao.class);
        initComponents();
        loadMemberData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        // Titre
        JLabel titleLabel = new JLabel("Liste des Membres", SwingConstants.CENTER);
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Panel principal
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        contentPanel.setBackground(Colors.BACKGROUND);

        // Panel de recherche
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Colors.BACKGROUND);

        // Panel pour la recherche simple
        JPanel simpleSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        simpleSearchPanel.setBackground(Colors.BACKGROUND);

        // Composants de recherche simple
        JLabel searchLabel = new JLabel("Rechercher par nom:");
        searchLabel.setFont(Fonts.labelFont());

        searchField = new JTextField(20);
        searchField.setFont(Fonts.textFieldFont());
        searchField.addActionListener(this::performSearch);

        JButton searchButton = new JButton("Rechercher");
        searchButton.setFont(Fonts.buttonFont());
        searchButton.setBackground(Colors.PRIMARY);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(this::performSearch);

        simpleSearchPanel.add(searchLabel);
        simpleSearchPanel.add(searchField);
        simpleSearchPanel.add(searchButton);

        // Bouton de recherche avancée
        advancedSearchButton = new JButton("Recherche Avancée");
        advancedSearchButton.setFont(Fonts.buttonFont());
        advancedSearchButton.setBackground(Colors.SECONDARY);
        advancedSearchButton.setForeground(Color.WHITE);
        advancedSearchButton.setFocusPainted(false);
        advancedSearchButton.addActionListener(e -> showAdvancedSearchDialog());

        // Ajout des composants au panel de recherche
        searchPanel.add(simpleSearchPanel, BorderLayout.CENTER);
        searchPanel.add(advancedSearchButton, BorderLayout.EAST);
        contentPanel.add(searchPanel, BorderLayout.NORTH);

        // Modèle de tableau
        String[] columnNames = {"ID", "Nom", "Contact", "Date Inscription", "Statut"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setRowHeight(30);
        memberTable.setFont(Fonts.tableFont());
        memberTable.getTableHeader().setFont(Fonts.tableHeaderFont());

        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBackground(Colors.BACKGROUND);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(Colors.BACKGROUND);

        JButton refreshButton = new JButton("Actualiser");
        JButton exportButton = new JButton("Exporter");
        JButton printButton = new JButton("Imprimer");
        JButton closeButton = new JButton("Fermer");

        // Style des boutons
        for (JButton button : new JButton[]{refreshButton, exportButton, printButton, closeButton}) {
            button.setFont(Fonts.buttonFont());
            button.setBackground(Colors.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
        }

        // Actions des boutons
        refreshButton.addActionListener(e -> loadMemberData());
        closeButton.addActionListener(e -> {
            parentFrame.getContentPane().remove(this);
            parentFrame.revalidate();
            parentFrame.repaint();
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void performSearch(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<Membre> membres = membreDao.findByNameContaining(searchTerm);
            updateTable(membres);
        } else {
            loadMemberData();
        }
    }

    private void showAdvancedSearchDialog() {
        AdvancedSearchDialog searchDialog = new AdvancedSearchDialog(parentFrame);
        searchDialog.setVisible(true);

        if (searchDialog.isSearchPerformed()) {
            MembreSearchCriteria criteria = searchDialog.getSearchCriteria();
            List<Membre> membres = membreDao.search(criteria);
            updateTable(membres);
        }
    }

    private void loadMemberData() {
        List<Membre> membres = membreDao.findAll();
        updateTable(membres);
    }

    private void updateTable(List<Membre> membres) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (Membre membre : membres) {
            Object[] rowData = {
                    membre.getId(),
                    membre.getNom(),
                    membre.getContact(),
                    membre.getDateInscription() != null ? dateFormat.format(membre.getDateInscription()) : "N/A",
                    getStatusText(membre.getStatut())
            };
            tableModel.addRow(rowData);
        }
    }

    private String getStatusText(StatutMembre statut) {
        if (statut == null) return "INCONNU";

        switch (statut) {
            case ACTIF: return "Actif";
            case INACTIF: return "Inactif";
            case SUSPENDU: return "Suspendu";
            default: return statut.name();
        }
    }
}