package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.dto.MembreSearchCriteria;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import com.association.view.components.IconManager;
import com.association.view.components.common.AdvancedSearchDialog;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class MemberListPanel extends JPanel {
    private final JFrame parentFrame;
    private final MembreDao membreDao;
    private JTable memberTable;
    private DefaultTableModel tableModel;
    private JButton advancedSearchButton;
    private JTextField searchField;
    private javax.swing.Timer refreshTimer;

    public MemberListPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.membreDao = DAOFactory.getInstance(MembreDao.class);
        initComponents();
        loadMemberData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        // Créer un timer qui se déclenche toutes les 30 secondes (30000 ms)
        refreshTimer = new Timer(30000, e -> loadMemberData());
        refreshTimer.start(); // Démarrer le timer
        // Panel d'en-tête avec le titre et les outils de recherche
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colors.BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Titre à gauche
        JLabel titleLabel = new JLabel("Liste des Membres");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Panel pour les outils de recherche à droite
        JPanel toolsPanel = new JPanel(new BorderLayout(10, 0));
        toolsPanel.setBackground(Colors.BACKGROUND);

        // Panel pour la recherche simple
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.setBackground(Colors.BACKGROUND);

        // Barre de recherche


        Border roundedBorder = new Border() {

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (c instanceof AbstractButton button) {
                    if (button.getModel().isRollover()) {
                        g2.setColor(new Color(100, 150, 255)); // Couleur au survol
                    } else {
                        g2.setColor(Color.GRAY); // Couleur normale
                    }
                } else {
                    g2.setColor(Color.GRAY);
                }

                int radius = 10;
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
                g2.dispose();
            }

            @Contract(value = "_ -> new", pure = true)
            @Override
            public @NotNull Insets getBorderInsets(Component c) {
                return new Insets(4, 8, 4, 8);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        };

        searchField = new JTextField(15);
        searchField.setFont(Fonts.textFieldFont());
        searchField.addActionListener(this::performSearch);
        searchField.setBorder(roundedBorder);

// Ajouter le texte par défaut (placeholder)
        searchField.setText("Rechercher...");
        searchField.setForeground(Color.GRAY); // Couleur du texte de placeholder

// Gestion de l'effacement du texte au clic
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Rechercher...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK); // Couleur normale du texte
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Rechercher...");
                }
            }
        });// Appliquer la bordure arrondie


        // Bouton de recherche simple avec icône
        JButton searchButton = new JButton();
        searchButton.setIcon(IconManager.getIcon("search.svg", 18));
        searchButton.setToolTipText("Rechercher");
        searchButton.setFocusPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setBorder(roundedBorder); // Appliquer la bordure arrondie
        searchButton.setOpaque(false);
        searchButton.setMargin(new Insets(0, 0, 0, 0));
        searchButton.addActionListener(this::performSearch);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Bouton de recherche avancée avec icône seulement
        advancedSearchButton = new JButton();
        advancedSearchButton.setIcon(IconManager.getIcon("advanced_search.svg", 16));
        advancedSearchButton.setToolTipText("Recherche Avancée");
        advancedSearchButton.setFocusPainted(false);
        advancedSearchButton.setContentAreaFilled(false);
        advancedSearchButton.setBorder(roundedBorder); // Appliquer la même bordure
        advancedSearchButton.setOpaque(false);
        advancedSearchButton.setMargin(new Insets(0, 0, 0, 0));
        advancedSearchButton.addActionListener(e -> showAdvancedSearchDialog());

        // Ajout des composants au panel d'outils
        toolsPanel.add(searchPanel, BorderLayout.CENTER);
        toolsPanel.add(advancedSearchButton, BorderLayout.EAST);

        // Ajout des composants au panel d'en-tête
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(toolsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Panel principal pour le contenu
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        contentPanel.setBackground(Colors.BACKGROUND);

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
            refreshTimer.stop(); // Arrêter le timer
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
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<Membre> membres = membreDao.findAll();
                SwingUtilities.invokeLater(() -> updateTable(membres));
                return null;
            }
        };
        worker.execute();
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
            case ACTIF:
                return "Actif";
            case INACTIF:
                return "Inactif";
            case SUSPENDU:
                return "Suspendu";
            default:
                return statut.name();
        }
    }
}