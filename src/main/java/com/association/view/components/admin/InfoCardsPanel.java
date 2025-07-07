package com.association.view.components.admin;

import com.association.manager.ContributionManager;
import com.association.manager.EmpruntManager;
import com.association.manager.MembreManager;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class InfoCardsPanel extends JPanel {
    private final Long membreId;
    private final ContributionManager contributionManager;
    private final EmpruntManager empruntManager;
    private final MembreManager membreManager;

    public InfoCardsPanel(Long membreId,
                          ContributionManager contributionManager,
                          EmpruntManager empruntManager,
                          MembreManager membreManager) {
        this.membreId = membreId;
        this.contributionManager = contributionManager;
        this.empruntManager = empruntManager;
        this.membreManager = membreManager;

        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(Colors.CARD_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Première ligne (2 cartes)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        add(createInfoCard("Contribution", "0 FCFA", "Contribution"), gbc);

        gbc.gridx = 1;
        add(createInfoCard("Emprunt", "0 FCFA", "Emprunt"), gbc);

        // Deuxième ligne (2 cartes)
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(createInfoCard("Remboursement", "0 FCFA", "Remboursement"), gbc);

        gbc.gridx = 1;
        add(createInfoCard("Total", "0 FCFA", null), gbc);

        // Troisième ligne (1 carte qui prend toute la largeur)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(createInfoCard("Bénéfice", "0 FCFA", null), gbc);
    }

    private JPanel createInfoCard(String title, String value, String actionCommand) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Colors.CARD_BACKGROUND);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CARD_BACKGROUND);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(Fonts.labelFont());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(Fonts.titleFont());
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(valueLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        JButton cornerButton = new JButton();
        cornerButton.setIcon(IconManager.getIcon("kebab-menu.svg", 20));
        cornerButton.setBorder(BorderFactory.createEmptyBorder());
        cornerButton.setContentAreaFilled(false);

        if (actionCommand != null) {
            cornerButton.setActionCommand(actionCommand);
            cornerButton.addActionListener(this::handleCardAction);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(0, 0, 0, 0));
        buttonPanel.add(cornerButton);

        card.add(buttonPanel, BorderLayout.NORTH);

        return card;
    }

    private void handleCardAction(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case "Contribution":
                contributionAction();
                break;
            case "Emprunt":
                empruntAction();
                break;
            case "Remboursement":
                remboursementAction();
                break;
        }
    }

    private void contributionAction() {
        JOptionPane.showMessageDialog(this,
                "Ajout de contribution pour le membre " + membreId,
                "Contribution", JOptionPane.INFORMATION_MESSAGE);
    }

    private void empruntAction() {
        JOptionPane.showMessageDialog(this,
                "Ajout d'emprunt pour le membre " + membreId,
                "Emprunt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void remboursementAction() {
        JOptionPane.showMessageDialog(this,
                "Ajout de remboursement pour le membre " + membreId,
                "Remboursement", JOptionPane.INFORMATION_MESSAGE);
    }

    // Méthode pour mettre à jour les valeurs des cartes
    public void updateCardValues(String contribution, String emprunt,
                                 String remboursement, String total, String benefice) {
        // Implémentez la logique de mise à jour des cartes ici
    }
}