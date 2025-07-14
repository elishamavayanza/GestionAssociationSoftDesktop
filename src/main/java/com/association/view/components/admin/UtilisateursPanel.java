package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.UtilisateurDao;
import com.association.model.access.Utilisateur;
import com.association.model.enums.UserRole;
import com.association.util.ExportUtils;
import com.association.util.PrintUtils;
import com.association.view.components.IconManager;
import com.association.view.components.common.AdvancedSearchDialog;
import com.association.view.components.common.EditableTableModel;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

public class UtilisateursPanel extends JPanel implements Observer {
    private final JFrame parentFrame;
    private final UtilisateurDao utilisateurDao;
    private JTable utilisateursTable;
    private DefaultTableModel tableModel;
    private JButton advancedSearchButton;
    private JTextField searchField;
    private javax.swing.Timer refreshTimer;
    private JSplitPane splitPane;
    private static final int DIVIDER_SIZE = 0;
    private UserDetailsPanel currentDetailsPanel = null;
    private Long currentlyDisplayedUserId = null;
    private JButton saveButton;
    private JButton cancelButton;

    public UtilisateursPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.utilisateurDao = DAOFactory.getInstance(UtilisateurDao.class);
        initComponents();
        loadUserData();
        this.utilisateurDao.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        SwingUtilities.invokeLater(() -> {
            if (arg instanceof Utilisateur) {
                loadUserData();
            } else if (arg instanceof Long) {
                loadUserData();
            } else {
                loadUserData();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        refreshTimer = new Timer(30000, e -> loadUserData());
        refreshTimer.start();

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colors.BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Gestion des Utilisateurs");
        titleLabel.setFont(Fonts.titleFont());
        titleLabel.setForeground(Colors.PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Tools Panel
        JPanel toolsPanel = new JPanel(new BorderLayout(10, 0));
        toolsPanel.setBackground(Colors.BACKGROUND);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.setBackground(Colors.BACKGROUND);

        Border roundedBorder = createRoundedBorder();

        searchField = new JTextField(15);
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

        JButton searchButton = new JButton();
        searchButton.setIcon(IconManager.getIcon("search.svg", 18));
        searchButton.setToolTipText("Rechercher");
        searchButton.setFocusPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setBorder(roundedBorder);
        searchButton.setOpaque(false);
        searchButton.setMargin(new Insets(0, 0, 0, 0));
        searchButton.addActionListener(this::performSearch);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        advancedSearchButton = new JButton();
        advancedSearchButton.setIcon(IconManager.getIcon("advanced_search.svg", 16));
        advancedSearchButton.setToolTipText("Recherche Avancée");
        advancedSearchButton.setFocusPainted(false);
        advancedSearchButton.setContentAreaFilled(false);
        advancedSearchButton.setBorder(roundedBorder);
        advancedSearchButton.setOpaque(false);
        advancedSearchButton.setMargin(new Insets(0, 0, 0, 0));
        advancedSearchButton.addActionListener(e -> showAdvancedSearchDialog());

        JButton addUserButton = new JButton();
        addUserButton.setIcon(IconManager.getIcon("person_add.svg", 16));
        addUserButton.setToolTipText("Ajouter un utilisateur");
        addUserButton.setFocusPainted(false);
        addUserButton.setContentAreaFilled(false);
        addUserButton.setBorder(roundedBorder);
        addUserButton.setOpaque(false);
        addUserButton.setMargin(new Insets(0, 0, 0, 0));
        addUserButton.addActionListener(e -> showAjouterUtilisateurDialog());

        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightButtonsPanel.setBackground(Colors.BACKGROUND);
        rightButtonsPanel.add(advancedSearchButton);
        rightButtonsPanel.add(addUserButton);

        toolsPanel.add(searchPanel, BorderLayout.CENTER);
        toolsPanel.add(rightButtonsPanel, BorderLayout.EAST);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(toolsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        contentPanel.setBackground(Colors.BACKGROUND);

        String[] columnNames = {"ID", "Nom d'utilisateur", "Email", "Rôles", "Actif"};
        tableModel = new EditableTableModel(columnNames, 0);

        utilisateursTable = new JTable(tableModel) {
            @Override
            public int getAutoResizeMode() {
                return JTable.AUTO_RESIZE_OFF;
            }
        };

        utilisateursTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = utilisateursTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        Long userId = (Long) utilisateursTable.getValueAt(row, 0);
                        showUserDetailsPanel(userId);
                    }
                }
            }
        });

        utilisateursTable.setRowMargin(0);
        utilisateursTable.setShowGrid(false);

        setupTableContextMenu();

        customizeTableAppearance();

        JScrollPane scrollPane = new JScrollPane(utilisateursTable);
        scrollPane.setBackground(Colors.BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

        customizeScrollBar(verticalScrollBar);
        customizeScrollBar(horizontalScrollBar);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        saveButton = new JButton("Enregistrer", IconManager.getIcon("save.svg", 16));
        cancelButton = new JButton("Annuler", IconManager.getIcon("undo.svg", 16));

        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);

        ((EditableTableModel)tableModel).addPropertyChangeListener(evt -> {
            if ("pendingChanges".equals(evt.getPropertyName())) {
                boolean hasChanges = !((Map<?, ?>)evt.getNewValue()).isEmpty();
                saveButton.setEnabled(hasChanges);
                cancelButton.setEnabled(hasChanges);
            }
        });

        saveButton.addActionListener(e -> {
            if (((EditableTableModel)utilisateursTable.getModel()).commitChanges()) {
                showSuccessDialog("Modifications enregistrées avec succès");
            } else {
                showErrorDialog("Erreur lors de l'enregistrement");
            }
        });

        cancelButton.addActionListener(e -> {
            ((EditableTableModel)utilisateursTable.getModel()).rollbackChanges();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(Colors.BACKGROUND);

        setupExportAndPrintButtons(buttonPanel);

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        for (JButton button : new JButton[]{cancelButton, saveButton}) {
            button.setFont(Fonts.buttonFont());
            button.setBackground(Colors.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
        }

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(contentPanel);
        splitPane.setRightComponent(new JPanel());
        splitPane.setDividerLocation(1.0);
        splitPane.setDividerSize(DIVIDER_SIZE);
        splitPane.setResizeWeight(1.0);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOneTouchExpandable(false);

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(splitPane, BorderLayout.CENTER);
        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private Border createRoundedBorder() {
        return new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (c instanceof AbstractButton button) {
                    if (button.getModel().isRollover()) {
                        g2.setColor(new Color(100, 150, 255));
                    } else {
                        g2.setColor(Color.GRAY);
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
    }

    private void setupTableContextMenu() {
        utilisateursTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = utilisateursTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        utilisateursTable.setRowSelectionInterval(row, row);

                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

                        UIManager.put("PopupMenu.background", Colors.BACKGROUND);
                        UIManager.put("MenuItem.background", Colors.BACKGROUND);
                        UIManager.put("MenuItem.foreground", Colors.TEXT);
                        UIManager.put("MenuItem.selectionBackground", Colors.PRIMARY_LIGHT);
                        UIManager.put("MenuItem.selectionForeground", Colors.TEXT);
                        SwingUtilities.updateComponentTreeUI(popupMenu);

                        JMenuItem deleteItem = new JMenuItem("Supprimer", IconManager.getIcon("delete.svg", 16));
                        deleteItem.setHorizontalTextPosition(SwingConstants.RIGHT);
                        deleteItem.setIconTextGap(8);
                        deleteItem.setFont(Fonts.tableFont().deriveFont(24f));
                        deleteItem.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                        deleteItem.addActionListener(ev -> confirmDeleteUser(row));

                        popupMenu.add(deleteItem);
                        popupMenu.show(utilisateursTable, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void confirmDeleteUser(int row) {
        JDialog confirmDialog = new JDialog(parentFrame, "Confirmation de suppression", true);
        confirmDialog.setLayout(new BorderLayout());
        confirmDialog.setSize(400, 200);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.getContentPane().setBackground(Colors.BACKGROUND);

        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        messagePanel.setBackground(Colors.BACKGROUND);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel(IconManager.getIcon("warning.svg", 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(iconLabel, BorderLayout.WEST);

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>"
                + "Êtes-vous sûr de vouloir supprimer cet utilisateur ?<br>"
                + "Cette action est irréversible.</div></html>");
        messageLabel.setFont(Fonts.textFieldFont());
        messageLabel.setForeground(Colors.TEXT);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        confirmDialog.add(messagePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Colors.BACKGROUND);

        JButton yesButton = new JButton("Oui", IconManager.getIcon("yes.svg", 16));
        yesButton.setFont(Fonts.buttonFont());
        yesButton.setBackground(Colors.DANGER);
        yesButton.setForeground(Color.WHITE);
        yesButton.setFocusPainted(false);
        yesButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        yesButton.addActionListener(ey -> {
            Long userId = (Long) utilisateursTable.getValueAt(row, 0);
            utilisateurDao.delete(userId);
            confirmDialog.dispose();
        });

        JButton noButton = new JButton("Non", IconManager.getIcon("no.svg", 16));
        noButton.setFont(Fonts.buttonFont());
        noButton.setBackground(Colors.SECONDARY);
        noButton.setForeground(Color.WHITE);
        noButton.setFocusPainted(false);
        noButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        noButton.addActionListener(ey -> confirmDialog.dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        confirmDialog.add(buttonPanel, BorderLayout.SOUTH);

        confirmDialog.setVisible(true);
    }

    private void showUserDetailsPanel(Long userId) {
        if (userId.equals(currentlyDisplayedUserId)) {
            return;
        }

        currentlyDisplayedUserId = userId;

        if (currentDetailsPanel != null) {
            currentDetailsPanel.updateUserData(userId);
            return;
        }

        currentDetailsPanel = new UserDetailsPanel(parentFrame, userId);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Fermer");
        for (JButton button : new JButton[]{closeButton}) {
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
            currentlyDisplayedUserId = null;
        });
        closePanel.add(closeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(closePanel, BorderLayout.NORTH);
        container.add(currentDetailsPanel, BorderLayout.CENTER);

        if (splitPane.getRightComponent() == null || splitPane.getDividerSize() == DIVIDER_SIZE) {
            splitPane.setRightComponent(container);
            splitPane.setDividerSize(5);

            int totalWidth = splitPane.getWidth();
            int tableWidth = (int)(totalWidth * 0.6);
            splitPane.setDividerLocation(tableWidth);

            splitPane.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int newTotalWidth = splitPane.getWidth();
                    int newTableWidth = (int)(newTotalWidth * 0.6);
                    splitPane.setDividerLocation(newTableWidth);
                }
            });
        } else {
            splitPane.setRightComponent(container);
        }

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

    private void showAjouterUtilisateurDialog() {
        // Implémentez cette méthode pour afficher un dialogue d'ajout d'utilisateur
        JOptionPane.showMessageDialog(this, "Fonctionnalité d'ajout d'utilisateur à implémenter");
    }

    private void performSearch(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<Utilisateur> utilisateurs = utilisateurDao.findByUsernameContaining(searchTerm);
            updateTable(utilisateurs);
        } else {
            loadUserData();
        }
    }

    private void showAdvancedSearchDialog() {
        // Implémentez cette méthode pour afficher un dialogue de recherche avancée
        JOptionPane.showMessageDialog(this, "Fonctionnalité de recherche avancée à implémenter");
    }

    public void loadUserData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<Utilisateur> utilisateurs = utilisateurDao.findAll();
                SwingUtilities.invokeLater(() -> updateTable(utilisateurs));
                return null;
            }
        };
        worker.execute();
    }

    private void updateTable(List<Utilisateur> utilisateurs) {
        tableModel.setRowCount(0);
        for (Utilisateur utilisateur : utilisateurs) {
            String roles = utilisateur.getRoles().stream()
                    .map(UserRole::name)
                    .collect(Collectors.joining(", "));

            tableModel.addRow(new Object[]{
                    utilisateur.getId(),
                    utilisateur.getUsername(),
                    utilisateur.getEmail(),
                    roles,
                    utilisateur.isActive() ? "Oui" : "Non"
            });
        }
    }

    private void customizeTableAppearance() {
        utilisateursTable.getTableHeader().setOpaque(false);
        utilisateursTable.getTableHeader().setBackground(Colors.PRIMARY);
        utilisateursTable.getTableHeader().setForeground(Color.WHITE);
        utilisateursTable.getTableHeader().setFont(Fonts.tableHeaderFont().deriveFont(14f));
        utilisateursTable.setRowHeight(25); // ou une valeur plus grande si nécessaire

        utilisateursTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Long userId = (Long) table.getValueAt(row, 0);
                EditableTableModel model = (EditableTableModel) table.getModel();

                if (model.pendingChanges.containsKey(userId) &&
                        model.pendingChanges.get(userId).containsKey(column)) {
                    c.setBackground(Colors.WARNING_LIGHT);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    if (table.isRowSelected(row)) {
                        c.setBackground(Colors.PRIMARY_LIGHT);
                        c.setForeground(Colors.TEXT);
                    } else if (isMouseOverRow(table, row)) {
                        c.setBackground(Colors.HOVER);
                        c.setForeground(Colors.TEXT);
                    } else {
                        c.setBackground(row % 2 == 0 ? Colors.BACKGROUND : Colors.CARD_BACKGROUND);
                        c.setForeground(Colors.TEXT);
                    }
                }

                if (column == 4) { // Colonne "Actif"
                    String status = (String) value;
                    if ("Oui".equals(status)) {
                        c.setForeground(Colors.SUCCESS);
                    } else {
                        c.setForeground(Colors.DANGER);
                    }
                }

                setHorizontalAlignment(SwingConstants.CENTER);

                return c;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        utilisateursTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        utilisateursTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = utilisateursTable.rowAtPoint(e.getPoint());
                utilisateursTable.repaint();
            }
        });

        utilisateursTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        utilisateursTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Nom d'utilisateur
        utilisateursTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Email
        utilisateursTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Rôles
        utilisateursTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Actif
    }

    private boolean isMouseOverRow(JTable table, int row) {
        Point mousePos = table.getMousePosition();
        if (mousePos != null) {
            return table.rowAtPoint(mousePos) == row;
        }
        return false;
    }

    private void customizeScrollBar(JScrollBar scrollBar) {
        if (scrollBar.getOrientation() == JScrollBar.VERTICAL) {
            scrollBar.setPreferredSize(new Dimension(8, 0));
        } else {
            scrollBar.setPreferredSize(new Dimension(0, 8));
        }

        scrollBar.setBackground(Colors.BACKGROUND);
        scrollBar.setForeground(Colors.SECONDARY);

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

                if (scrollBar.getOrientation() == JScrollBar.VERTICAL) {
                    width = 6;
                    thumbBounds.x += 1;
                } else {
                    height = 6;
                    thumbBounds.y += 1;
                }

                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, width, height, 3, 3);

                if (isThumbRollover()) {
                    g2.setColor(new Color(thumbColor.getRed(), thumbColor.getGreen(), thumbColor.getBlue(), 150));
                    g2.fillRoundRect(thumbBounds.x, thumbBounds.y, width, height, 3, 3);
                }
            }
        });

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

        if (searchTerm.equals("Rechercher...") || searchTerm.isEmpty()) {
            loadUserData();
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<Utilisateur> utilisateurs = utilisateurDao.findByUsernameContaining(searchTerm);
                SwingUtilities.invokeLater(() -> updateTable(utilisateurs));
                return null;
            }
        };
        worker.execute();
    }

    private void setupExportAndPrintButtons(JPanel buttonPanel) {
        JPopupMenu exportMenu = new JPopupMenu();

        JMenuItem excelItem = new JMenuItem("Excel");
        excelItem.addActionListener(e -> exportToExcel());

        JMenuItem pdfItem = new JMenuItem("PDF");
        pdfItem.addActionListener(e -> exportToPDF());

        exportMenu.add(excelItem);
        exportMenu.add(pdfItem);

        JButton importButton = new JButton("Importer");
        importButton.setIcon(IconManager.getIcon("import.svg", 16));
        importButton.addActionListener(e -> {
            // Implémentez l'importation ici
            JOptionPane.showMessageDialog(this, "Fonctionnalité d'importation à implémenter");
        });

        JButton exportButton = new JButton("Exporter");
        exportButton.setIcon(IconManager.getIcon("export.svg", 16));
        exportButton.addActionListener(e -> {
            exportMenu.show(exportButton, 0, exportButton.getHeight());
        });

        JButton printButton = new JButton("Imprimer");
        printButton.setIcon(IconManager.getIcon("printer.svg", 16));
        printButton.addActionListener(e -> printTable());

        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(printButton);

        for (JButton button : new JButton[]{importButton, exportButton, printButton}) {
            button.setFont(Fonts.buttonFont());
            button.setBackground(Colors.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }
    }

    private void exportToExcel() {
        try {
            ExportUtils.exportTableToExcel(
                    utilisateursTable.getModel(),
                    utilisateursTable.getTableHeader(),
                    "Liste des Utilisateurs",
                    "utilisateurs_export.xlsx",
                    parentFrame
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'export Excel: " + e.getMessage(),
                    "Erreur d'export",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToPDF() {
        try {
            ExportUtils.exportTableToPDF(
                    utilisateursTable.getModel(),
                    utilisateursTable.getTableHeader(),
                    "Liste des Utilisateurs",
                    "utilisateurs_export.pdf",
                    parentFrame
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'export PDF: " + e.getMessage(),
                    "Erreur d'export",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printTable() {
        try {
            PrintUtils.printTable(
                    utilisateursTable,
                    "Liste des Utilisateurs",
                    PrintUtils.PageSize.A4,
                    PrintUtils.Orientation.PORTRAIT,
                    true,
                    parentFrame
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'impression: " + e.getMessage(),
                    "Erreur d'impression",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSuccessDialog(String message) {
        JDialog successDialog = new JDialog();
        successDialog.setTitle("Succès");
        successDialog.setModal(true);
        successDialog.setLayout(new BorderLayout());
        successDialog.setSize(400, 200);
        successDialog.setLocationRelativeTo(null);
        successDialog.getContentPane().setBackground(Colors.BACKGROUND);

        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        messagePanel.setBackground(Colors.BACKGROUND);

        JLabel iconLabel = new JLabel(IconManager.getIcon("success.svg", 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(iconLabel, BorderLayout.WEST);

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(Fonts.textFieldFont());
        messageLabel.setForeground(Colors.TEXT);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        successDialog.add(messagePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Colors.BACKGROUND);

        JButton okButton = new JButton("OK", IconManager.getIcon("yes.svg", 16));
        okButton.setFont(Fonts.buttonFont());
        okButton.setBackground(Colors.PRIMARY);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.addActionListener(ev -> successDialog.dispose());

        buttonPanel.add(okButton);
        successDialog.add(buttonPanel, BorderLayout.SOUTH);

        successDialog.setVisible(true);
    }

    private void showErrorDialog(String message) {
        JDialog errorDialog = new JDialog();
        errorDialog.setTitle("Erreur");
        errorDialog.setModal(true);
        errorDialog.setLayout(new BorderLayout());
        errorDialog.setSize(400, 200);
        errorDialog.setLocationRelativeTo(null);
        errorDialog.getContentPane().setBackground(Colors.BACKGROUND);

        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        messagePanel.setBackground(Colors.BACKGROUND);

        JLabel iconLabel = new JLabel(IconManager.getIcon("error.svg", 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(iconLabel, BorderLayout.WEST);

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(Fonts.textFieldFont());
        messageLabel.setForeground(Colors.TEXT);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        errorDialog.add(messagePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Colors.BACKGROUND);

        JButton okButton = new JButton("OK", IconManager.getIcon("close.svg", 16));
        okButton.setFont(Fonts.buttonFont());
        okButton.setBackground(Colors.SECONDARY);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.addActionListener(ev -> errorDialog.dispose());

        buttonPanel.add(okButton);
        errorDialog.add(buttonPanel, BorderLayout.SOUTH);

        errorDialog.setVisible(true);
    }
}