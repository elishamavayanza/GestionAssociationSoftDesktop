package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import java.awt.*;

public class MemberDetailsPanel extends JPanel {
    private final JFrame parentFrame;
    private Long membreId;
    private final MembreDao membreDao;
    private JLabel nameLabel; // Ajout d'un label pour le nom

    public MemberDetailsPanel(JFrame parentFrame, Long membreId) {
        this.parentFrame = parentFrame;
        this.membreId = membreId;
        this.membreDao = DAOFactory.getInstance(MembreDao.class);

        initComponents();
        loadMemberData();
    }


    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Label pour le nom du membre
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        mainPanel.setBackground(Colors.CARD_BACKGROUND);

        // Section d'informations
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBackground(Colors.CARD_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nom
        nameLabel = new JLabel();
        nameLabel.setFont(Fonts.titleFont());
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(nameLabel, gbc);

        // Ajouter d'autres champs avec GridBagConstraints
        // Exemple :
        gbc.gridwidth = 1;
        gbc.gridy++;
        infoPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx++;
        infoPanel.add(new JLabel("email@example.com"), gbc);

        mainPanel.add(infoPanel);

        // Boutons d'action en bas
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Colors.CARD_BACKGROUND);

        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        // Stylez vos boutons...

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

    }

    private void loadMemberData() {
        Membre membre = membreDao.findById(membreId).orElse(null);
        if (membre != null) {
            // Mettre à jour le label avec le nom du membre
            nameLabel.setText(membre.getNom()); // Assurez-vous que getNom() existe dans votre classe Membre
        } else {
            nameLabel.setText("Membre non trouvé");
        }
    }

    public void updateMemberData(Long newMembreId) {
        this.membreId = newMembreId; // Mettre à jour l'ID
        loadMemberData(); // Recharger les données
    }
}