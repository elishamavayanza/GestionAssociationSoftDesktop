package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.dto.MembreSearchCriteria;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import com.association.view.components.IconManager;
import com.association.view.components.common.AdvancedSearchDialog;
import com.association.view.components.common.EditableTableModel;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.event.*;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
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
    private JSplitPane splitPane;
    private static final int DIVIDER_SIZE = 0; // Diviseur invisible au démarrage
    private MemberDetailsPanel currentDetailsPanel = null;
    private Long currentlyDisplayedMemberId = null;
    private JButton saveButton;
    private JButton cancelButton;

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

        // Nouveau bouton personnalisé
        JButton customButton = new JButton();
        customButton.setIcon(IconManager.getIcon("person_add.svg", 16)); // Remplacez par votre icône
        customButton.setToolTipText("Description du bouton");
        customButton.setFocusPainted(false);
        customButton.setContentAreaFilled(false);
        customButton.setBorder(roundedBorder);
        customButton.setOpaque(false);
        customButton.setMargin(new Insets(0, 0, 0, 0));
        customButton.addActionListener(e -> showAjouterMembreDialog());

// Panel pour les boutons à droite
        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightButtonsPanel.setBackground(Colors.BACKGROUND);
        rightButtonsPanel.add(advancedSearchButton);
        rightButtonsPanel.add(customButton); // Ajout du nouveau bouton

// Modifiez l'ajout des composants au panel d'outils
        toolsPanel.add(searchPanel, BorderLayout.CENTER);
        toolsPanel.add(rightButtonsPanel, BorderLayout.EAST);

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
        tableModel = new EditableTableModel(columnNames, 0);

// Création de la table avec les personnalisations
        memberTable = new JTable(tableModel) {
            @Override
            public int getAutoResizeMode() {
                return JTable.AUTO_RESIZE_OFF; // Retourne un int, pas un boolean
            }
        };

        // Ajouter un écouteur pour le double-clic
        memberTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = memberTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        Long membreId = (Long) memberTable.getValueAt(row, 0);
                        showMemberDetailsPanel(membreId);
                    }
                }
            }
        });
        memberTable.setRowMargin(0); // Réduit l'espace entre les lignes
        memberTable.setShowGrid(false); // Cache les lignes de la grille

        // Ajouter un écouteur pour gérer la suppression de ligne
        memberTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = memberTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        memberTable.setRowSelectionInterval(row, row);

                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

                        // Style pour le menu contextuel
                        UIManager.put("PopupMenu.background", Colors.BACKGROUND);
                        UIManager.put("MenuItem.background", Colors.BACKGROUND);
                        UIManager.put("MenuItem.foreground", Colors.TEXT);
                        UIManager.put("MenuItem.selectionBackground", Colors.PRIMARY_LIGHT);
                        UIManager.put("MenuItem.selectionForeground", Colors.TEXT);
                        SwingUtilities.updateComponentTreeUI(popupMenu);

                        JMenuItem deleteItem = new JMenuItem("Supprimer", IconManager.getIcon("delete.svg", 16));
                        deleteItem.setHorizontalTextPosition(SwingConstants.RIGHT);
                        deleteItem.setIconTextGap(8);
                        deleteItem.setFont(Fonts.tableFont());
                        deleteItem.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                        deleteItem.addActionListener(ev -> {
                            // Créer un JDialog personnalisé
                            JDialog confirmDialog = new JDialog(parentFrame, "Confirmation de suppression", true);
                            confirmDialog.setLayout(new BorderLayout());
                            confirmDialog.setSize(400, 200);
                            confirmDialog.setLocationRelativeTo(MemberListPanel.this);
                            confirmDialog.getContentPane().setBackground(Colors.BACKGROUND);

                            // Panel pour le message et l'icône
                            JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
                            messagePanel.setBackground(Colors.BACKGROUND);
                            messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                            // Icône d'avertissement
                            JLabel iconLabel = new JLabel(IconManager.getIcon("warning.svg", 48));
                            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            messagePanel.add(iconLabel, BorderLayout.WEST);

                            // Message de confirmation
                            JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>"
                                    + "Êtes-vous sûr de vouloir supprimer ce membre ?<br>"
                                    + "Cette action est irréversible.</div></html>");
                            messageLabel.setFont(Fonts.textFieldFont());
                            messageLabel.setForeground(Colors.TEXT);
                            messagePanel.add(messageLabel, BorderLayout.CENTER);

                            confirmDialog.add(messagePanel, BorderLayout.CENTER);

                            // Panel pour les boutons
                            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
                            buttonPanel.setBackground(Colors.BACKGROUND);

                            // Bouton Oui
                            JButton yesButton = new JButton("Oui", IconManager.getIcon("yes.svg", 16));
                            yesButton.setFont(Fonts.buttonFont());
                            yesButton.setBackground(Colors.DANGER); // Rouge pour indiquer une action critique
                            yesButton.setForeground(Color.WHITE);
                            yesButton.setFocusPainted(false);
                            yesButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
                            yesButton.addActionListener(ey -> {
                                ((EditableTableModel) tableModel).removeRow(row);
                                confirmDialog.dispose();
                            });

                            // Bouton Non
                            JButton noButton = new JButton("Non", IconManager.getIcon("no.svg", 16));
                            noButton.setFont(Fonts.buttonFont());
                            noButton.setBackground(Colors.SECONDARY); // Gris pour une action neutre
                            noButton.setForeground(Color.WHITE);
                            noButton.setFocusPainted(false);
                            noButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
                            noButton.addActionListener(ey -> confirmDialog.dispose());

                            buttonPanel.add(yesButton);
                            buttonPanel.add(noButton);
                            confirmDialog.add(buttonPanel, BorderLayout.SOUTH);

                            confirmDialog.setVisible(true);
                        });

                        popupMenu.add(deleteItem);
                        popupMenu.show(memberTable, e.getX(), e.getY());
                    }
                }
            }
        });

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

        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

        customizeScrollBar(verticalScrollBar);
        customizeScrollBar(horizontalScrollBar);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(Colors.BACKGROUND);

        JButton exportButton = new JButton("Exporter", IconManager.getIcon("export.svg", 16));
        JButton printButton = new JButton("Imprimer", IconManager.getIcon("printer.svg", 16));


// Après avoir créé les boutons
        saveButton = new JButton("Save", IconManager.getIcon("save.svg", 16));
        cancelButton = new JButton("Cancel", IconManager.getIcon("undo.svg", 16));

// Désactiver initialement
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);

// Ajouter l'écouteur de modifications
        ((EditableTableModel)tableModel).addPropertyChangeListener(evt -> {
            if ("pendingChanges".equals(evt.getPropertyName())) {
                boolean hasChanges = !((Map<?, ?>)evt.getNewValue()).isEmpty();
                saveButton.setEnabled(hasChanges);
                cancelButton.setEnabled(hasChanges);
            }
        });

// Modifier les actions des boutons pour désactiver après usage
        saveButton.addActionListener(e -> {
            if (((EditableTableModel)memberTable.getModel()).commitChanges()) {
                JOptionPane.showMessageDialog(this, "Modifications enregistrées avec succès");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'enregistrement",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            ((EditableTableModel)memberTable.getModel()).rollbackChanges();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);


        for (JButton button : new JButton[]{ exportButton, printButton, cancelButton, saveButton}) {
            button.setFont(Fonts.buttonFont());
            button.setBackground(Colors.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
        }
        buttonPanel.add(exportButton);
        buttonPanel.add(printButton);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(contentPanel); // Le panel avec la table
        splitPane.setRightComponent(new JPanel()); // Panel vide initialement
        splitPane.setDividerLocation(1.0); // Tout l'espace pour la table (diviseur à droite)
        splitPane.setDividerSize(DIVIDER_SIZE); // Diviseur invisible
        splitPane.setResizeWeight(1.0); // Tout l'espace pour la partie gauche
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOneTouchExpandable(false); // Désactive le bouton d'expansion

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(splitPane, BorderLayout.CENTER);
        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private void showMemberDetailsPanel(Long membreId) {
        // Si le panneau existe déjà, simplement mettre à jour son contenu
        if (membreId.equals(currentlyDisplayedMemberId)) {
            return;
        }

        currentlyDisplayedMemberId = membreId;

        if (currentDetailsPanel != null) {
            currentDetailsPanel.updateMemberData(membreId);
            return;
        }

        currentDetailsPanel = new MemberDetailsPanel(parentFrame, membreId);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Fermer");
        for (JButton button : new JButton[]{ closeButton}) {
            button.setFont(Fonts.buttonFont());
            button.setBackground(Colors.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
        }
        closeButton.addActionListener(e -> {
            splitPane.setRightComponent(new JPanel());
            splitPane.setDividerSize(DIVIDER_SIZE);
            splitPane.setDividerLocation(1.0);
            currentDetailsPanel = null;
            currentlyDisplayedMemberId = null;
        });
        closePanel.add(closeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(closePanel, BorderLayout.NORTH);
        container.add(currentDetailsPanel, BorderLayout.CENTER);

        // Afficher dans le splitPane
        if (splitPane.getRightComponent() == null || splitPane.getDividerSize() == DIVIDER_SIZE) {
            splitPane.setRightComponent(container);
            splitPane.setDividerSize(5);

            // Calcul dynamique de la position du diviseur
            int totalWidth = splitPane.getWidth();
            int tableWidth = (int)(totalWidth * 0.6); // 60% pour la table
            splitPane.setDividerLocation(tableWidth);

            // Ajouter un écouteur de redimensionnement
            splitPane.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // Maintenir le ratio lors du redimensionnement
                    int newTotalWidth = splitPane.getWidth();
                    int newTableWidth = (int)(newTotalWidth * 0.6);
                    splitPane.setDividerLocation(newTableWidth);
                }
            });
        } else {
            splitPane.setRightComponent(container);
        }

        // Animation fluide seulement si le panneau n'était pas déjà visible
        if (splitPane.getDividerSize() == DIVIDER_SIZE) {
            Timer timer = new Timer(15, new ActionListener() {
                int currentDividerLocation = splitPane.getWidth();
                int targetLocation = (int)(splitPane.getSize().width * 0.8);

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentDividerLocation > targetLocation) {
                        currentDividerLocation -= 20;
                        splitPane.setDividerLocation(currentDividerLocation);
                        if (currentDividerLocation <= targetLocation) {
                            ((Timer)e.getSource()).stop();
                        }
                    }
                }
            });
            timer.start();
        }
    }

    private void showAjouterMembreDialog() {
        AjouterMembreDialog ajouterMembreDialog = new AjouterMembreDialog(parentFrame);
        ajouterMembreDialog.setVisible(true);

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
        ((EditableTableModel)tableModel).loadMemberData(membres);
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

        // Personnalisation des cellules avec effet de survol et mise en évidence des modifications
        memberTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Récupérer l'ID du membre et le modèle
                Long membreId = (Long) table.getValueAt(row, 0);
                EditableTableModel model = (EditableTableModel) table.getModel();

                // Mettre en évidence les cellules modifiées
                if (model.pendingChanges.containsKey(membreId) &&
                        model.pendingChanges.get(membreId).containsKey(column)) {
                    c.setBackground(Colors.WARNING_LIGHT); // Couleur pour les modifications
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    // Style normal pour les cellules non modifiées
                    if (table.isRowSelected(row)) {
                        // Si la ligne est sélectionnée
                        c.setBackground(Colors.PRIMARY_LIGHT);
                        c.setForeground(Colors.TEXT);
                    } else if (isMouseOverRow(table, row)) {
                        // Si la souris survole la ligne
                        c.setBackground(Colors.HOVER);
                        c.setForeground(Colors.TEXT);
                    } else {
                        // Couleur de fond alternée pour les lignes normales
                        c.setBackground(row % 2 == 0 ? Colors.BACKGROUND : Colors.CARD_BACKGROUND);
                        c.setForeground(Colors.TEXT);
                    }
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
                }

                // Centrer le texte dans toutes les cellules
                setHorizontalAlignment(SwingConstants.CENTER);

                return c;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        memberTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Ajouter un MouseMotionListener pour détecter le survol
        memberTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = memberTable.rowAtPoint(e.getPoint());
                memberTable.repaint(); // Redessiner la table pour mettre à jour l'affichage
            }
        });

        // Largeur des colonnes
        memberTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        memberTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Nom
        memberTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Contact
        memberTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Date
        memberTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Statut
    }

    // Méthode pour vérifier si la souris survole une ligne
    private boolean isMouseOverRow(JTable table, int row) {
        Point mousePos = table.getMousePosition();
        if (mousePos != null) {
            return table.rowAtPoint(mousePos) == row;
        }
        return false;
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