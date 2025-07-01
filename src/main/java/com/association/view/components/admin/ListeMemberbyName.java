package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

public class ListeMemberbyName extends JPanel implements Observer {
    private final JList<String> membreList;
    private final DefaultListModel<String> listModel;
    private final JTextField searchField;
    private List<Membre> allMembres; // Garde une copie de tous les membres

    public ListeMemberbyName() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Membres existants"));


        // S'enregistrer comme observateur
        DAOFactory.getInstance(MembreDao.class).addObserver(this);
        // Création du modèle et de la liste
        listModel = new DefaultListModel<>();
        membreList = new JList<>(listModel);

        // Personnalisation de la liste
        membreList.setFont(Fonts.tableFont());
        membreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membreList.setBackground(Colors.CURRENT_INPUT_BACKGROUND);
        membreList.setForeground(Colors.CURRENT_TEXT);

        JScrollPane scrollPane = new JScrollPane(membreList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

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

        // Création de la barre de recherche
        searchField = new JTextField();
        searchField.setFont(Fonts.textFieldFont());
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setBackground(Colors.CURRENT_INPUT_BACKGROUND);
        searchField.setForeground(Colors.CURRENT_TEXT);
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.setBorder(roundedBorder);


        // Ajout d'un placeholder
        searchField.setText("Rechercher un membre...");
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Rechercher un membre...")) {
                    searchField.setText("");
                    searchField.setForeground(Colors.CURRENT_TEXT);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Rechercher un membre...");
                }
            }
        });

        // Écouteur pour la recherche en temps réel
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterList();
            }
        });

        // Panel pour la barre de recherche
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Ajout des composants au panel principal
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        chargerMembres();
    }
    @Override
    public void update(Observable o, Object arg) {
        SwingUtilities.invokeLater(() -> {
            if (arg instanceof Membre) {
                // Membre modifié
                chargerMembres();
            } else if (arg instanceof Long) {
                // Membre supprimé
                chargerMembres();
            } else {
                // Autre changement
                chargerMembres();
            }
        });
    }

    private void chargerMembres() {
        MembreDao membreDao = DAOFactory.getInstance(MembreDao.class);
        allMembres = membreDao.findAll();
        updateListModel(allMembres);
    }

    private void updateListModel(List<Membre> membres) {
        listModel.clear();
        for (Membre membre : membres) {
            listModel.addElement(membre.getNom()); // Affiche seulement le nom
        }
    }

    private void filterList() {
        String searchText = searchField.getText().toLowerCase();

        // Ne pas filtrer si c'est le texte par défaut
        if (searchText.equals("rechercher un membre...")) {
            updateListModel(allMembres);
            return;
        }

        List<Membre> filteredList = allMembres.stream()
                .filter(membre ->
                        membre.getNom().toLowerCase().contains(searchText) ||
                                membre.getNom().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        updateListModel(filteredList);
    }

    // Méthode pour obtenir le membre sélectionné
    public Membre getSelectedMembre() {
        int selectedIndex = membreList.getSelectedIndex();
        if (selectedIndex != -1) {
            String selectedName = listModel.getElementAt(selectedIndex);
            return allMembres.stream()
                    .filter(m -> (m.getNom() + " " + m.getNom()).equals(selectedName))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

}