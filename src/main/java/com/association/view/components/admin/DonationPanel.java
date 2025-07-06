package com.association.view.components.admin;

import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;

public class DonationPanel extends JPanel implements Refreshable {
    private Long membreId;
    private JTable donationsTable;
    private JButton addDonationButton;

    public DonationPanel(Long membreId) {
        this.membreId = membreId;
        initComponents();
        loadDonations();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        // Tableau pour afficher les dons
        donationsTable = new JTable();
        add(new JScrollPane(donationsTable), BorderLayout.CENTER);

        // Bouton pour ajouter un don
        addDonationButton = new JButton("Ajouter un don");
        addDonationButton.addActionListener(e -> showAddDonationDialog());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addDonationButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadDonations() {
        // Implémentez le chargement des dons depuis la base de données
    }

    private void showAddDonationDialog() {
        // Implémentez une boîte de dialogue pour ajouter un don
    }

    @Override
    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        loadDonations();
    }
}
