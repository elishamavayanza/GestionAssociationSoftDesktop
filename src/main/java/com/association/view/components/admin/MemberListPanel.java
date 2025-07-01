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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MemberListPanel extends JPanel implements Observer {
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
        // S'enregistrer comme observateur
        this.membreDao.addObserver(this);
    }
    @Override
    public void update(Observable o, Object arg) {
        SwingUtilities.invokeLater(() -> {
            if (arg instanceof Membre) {
                // Membre modifié
                loadMemberData();
            } else if (arg instanceof Long) {
                // Membre supprimé
                loadMemberData();
            } else {
                // Autre changement
                loadMemberData();
            }
        });
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
        searchField.setBorder(roundedBorder);

// Texte par défaut (placeholder)
        searchField.setText("Rechercher...");
        searchField.setForeground(Color.GRAY);

// Gestion du placeholder
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

// Ajout du DocumentListener pour la recherche en temps réel
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performRealTimeSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performRealTimeSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performRealTimeSearch();
            }
        });

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
        // Modèle de tableau
        String[] columnNames = {"ID", "Nom", "Contact", "Date Inscription", "Statut"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

// Création de la table avec les personnalisations
        memberTable = new JTable(tableModel) {
            @Override
            public int getAutoResizeMode() {
                return JTable.AUTO_RESIZE_OFF; // Retourne un int, pas un boolean
            }
        };

// Configuration du style de la table
        memberTable.setShowHorizontalLines(true);
        memberTable.setShowVerticalLines(false);
        memberTable.setGridColor(Colors.BORDER);
        memberTable.setIntercellSpacing(new Dimension(0, 1));
        memberTable.setRowMargin(5);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setRowHeight(30);
        memberTable.setFont(Fonts.tableFont());
        memberTable.getTableHeader().setFont(Fonts.tableHeaderFont());

        memberTable.setAutoCreateRowSorter(true);

// Empêcher le déplacement des colonnes
        memberTable.getTableHeader().setReorderingAllowed(false);

// Configuration du rendu du header pour qu'il soit cohérent avec le style
        memberTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(Colors.PRIMARY);
                setForeground(Color.WHITE);
                setHorizontalAlignment(CENTER);
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                return this;
            }
        });

        customizeTableAppearance(); // Applique les autres personnalisations

        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBackground(Colors.BACKGROUND);

// Optionnel: supprimer la bordure si vous voulez un look plus minimaliste
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

// Personnalisation de la barre de défilement
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

// Appliquer le style aux barres de défilement
        customizeScrollBar(verticalScrollBar);
        customizeScrollBar(horizontalScrollBar);

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

        return switch (statut) {
            case ACTIF -> "Actif";
            case INACTIF -> "Inactif";
            case SUSPENDU -> "Suspendu";
            default -> statut.name();
        };
    }

    private void customizeTableAppearance() {
        // Personnalisation de l'en-tête
        memberTable.getTableHeader().setOpaque(false);
        memberTable.getTableHeader().setBackground(Colors.PRIMARY);
        memberTable.getTableHeader().setForeground(Color.WHITE);
        memberTable.getTableHeader().setFont(Fonts.tableHeaderFont());

        // Personnalisation des cellules
        memberTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Couleur de fond alternée pour les lignes
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Colors.BACKGROUND : Colors.CARD_BACKGROUND);
                }

                // Personnalisation selon la colonne (ex: statut)
                if (column == 4) { // Colonne Statut
                    String status = (String) value;
                    switch (status) {
                        case "Actif":
                            c.setForeground(Colors.SUCCESS);
                            break;
                        case "Inactif":
                            c.setForeground(Colors.DANGER);
                            break;
                        case "Suspendu":
                            c.setForeground(Colors.WARNING);
                            break;
                        default:
                            c.setForeground(Colors.TEXT);
                    }
                } else {
                    c.setForeground(Colors.TEXT);
                }

                // Centrer le texte dans toutes les cellules
                setHorizontalAlignment(SwingConstants.CENTER);

                return c;
            }
        });

        // Largeur des colonnes
        memberTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        memberTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Nom
        memberTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Contact
        memberTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Date
        memberTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Statut
    }
    private void customizeScrollBar(JScrollBar scrollBar) {
        // Définir la taille préférée pour rendre la barre plus fine
        if (scrollBar.getOrientation() == JScrollBar.VERTICAL) {
            scrollBar.setPreferredSize(new Dimension(8, 0)); // Largeur réduite à 8px
        } else {
            scrollBar.setPreferredSize(new Dimension(0, 8)); // Hauteur réduite à 8px
        }

        scrollBar.setBackground(Colors.BACKGROUND);
        scrollBar.setForeground(Colors.SECONDARY);

        // Modifier l'UI de la barre de défilement
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Colors.SECONDARY;
                this.trackColor = Colors.CARD_BACKGROUND;
                this.thumbDarkShadowColor = Colors.DARK_SECONDARY;
                this.thumbHighlightColor = Colors.SECONDARY;
                this.thumbLightShadowColor = Colors.SECONDARY;
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
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (thumbBounds.isEmpty() || !scrollBar.isEnabled()) {
                    return;
                }

                int width = thumbBounds.width;
                int height = thumbBounds.height;

                // Pour une barre plus fine, on réduit encore la largeur/hauteur du thumb
                if (scrollBar.getOrientation() == JScrollBar.VERTICAL) {
                    width = 6; // Largeur du thumb encore plus réduite
                    thumbBounds.x += 1; // Centrer le thumb
                } else {
                    height = 6; // Hauteur du thumb encore plus réduite
                    thumbBounds.y += 1; // Centrer le thumb
                }

                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, width, height, 3, 3);

                // Effet de survol
                if (isThumbRollover()) {
                    g2.setColor(new Color(thumbColor.getRed(), thumbColor.getGreen(), thumbColor.getBlue(), 150));
                    g2.fillRoundRect(thumbBounds.x, thumbBounds.y, width, height, 3, 3);
                }
            }
        });

        // Ajouter un écouteur pour changer la couleur au survol
        scrollBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                scrollBar.setForeground(Colors.PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                scrollBar.setForeground(Colors.PRIMARY);
            }
        });
    }
    private void performRealTimeSearch() {
        String searchTerm = searchField.getText().trim();

        // Ne pas effectuer de recherche si c'est le texte placeholder
        if (searchTerm.equals("Rechercher...") || searchTerm.isEmpty()) {
            loadMemberData();
            return;
        }

        // Utiliser un SwingWorker pour éviter de bloquer l'interface
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<Membre> membres = membreDao.findByNameContaining(searchTerm);
                SwingUtilities.invokeLater(() -> updateTable(membres));
                return null;
            }
        };
        worker.execute();
    }
}