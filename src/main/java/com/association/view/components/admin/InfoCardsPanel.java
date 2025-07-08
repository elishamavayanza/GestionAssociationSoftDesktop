package com.association.view.components.admin;

import com.association.manager.ContributionManager;
import com.association.manager.EmpruntManager;
import com.association.manager.MembreManager;
import com.association.model.enums.StatutEmprunt;
import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Emprunt;
import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InfoCardsPanel extends JPanel implements Refreshable {
    private Long membreId;
    private final ContributionManager contributionManager;
    private final EmpruntManager empruntManager;
    private final MembreManager membreManager;
    private BigDecimal totalEmprunts = BigDecimal.ZERO;
    private BigDecimal soldeRestant = BigDecimal.ZERO;


    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("fr", "FR"));

    // Cartes
    private JLabel contributionValueLabel;
    private JLabel empruntValueLabel;
    private JLabel remboursementValueLabel;
    private JLabel totalValueLabel;
    private JLabel beneficeValueLabel;

    // Sous-titres
    private JLabel contributionSubtitleLabel;
    private JLabel empruntSubtitleLabel;
    private JLabel remboursementSubtitleLabel;

    public InfoCardsPanel(Long membreId,
                          ContributionManager contributionManager,
                          EmpruntManager empruntManager,
                          MembreManager membreManager) {

        if (!SwingUtilities.isEventDispatchThread()) {
            System.out.println("ATTENTION: Le panel est créé en dehors de l'EDT!");
        }

        this.membreId = membreId;
        this.contributionManager = contributionManager;
        this.empruntManager = empruntManager;
        this.membreManager = membreManager;

        initComponents();
        showLoadingState();
        updateCardValues();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(Colors.CARD_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Créer un conteneur intermédiaire pour les cartes
        JPanel cardsContainer = new JPanel(new GridBagLayout());
        cardsContainer.setBackground(Colors.CARD_BACKGROUND);

        // Ajout des cartes avec une largeur minimale
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JPanel contributionCard = createContributionCard();
        contributionCard.setMinimumSize(new Dimension(200, 150));
        cardsContainer.add(contributionCard, gbc);

        gbc.gridx = 1;
        JPanel empruntCard = createEmpruntCard();
        empruntCard.setMinimumSize(new Dimension(200, 150));
        cardsContainer.add(empruntCard, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JPanel remboursementCard = createRemboursementCard();
        remboursementCard.setMinimumSize(new Dimension(200, 150));
        cardsContainer.add(remboursementCard, gbc);

        gbc.gridx = 1;
        JPanel totalCard = createTotalCard();
        totalCard.setMinimumSize(new Dimension(200, 150));
        cardsContainer.add(totalCard, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel beneficeCard = createBeneficeCard();
        beneficeCard.setMinimumSize(new Dimension(200, 150));
        cardsContainer.add(beneficeCard, gbc);

        // Ajout du conteneur avec scroll si nécessaire
        add(new JScrollPane(cardsContainer), new GridBagConstraints());
    }

    private JPanel createContributionCard() {
        JPanel card = createBaseCard("Contribution", "0 FCFA", "Contribution");
        JPanel contentPanel = (JPanel) card.getComponent(0);
        contributionValueLabel = (JLabel) contentPanel.getComponent(2);
        contributionSubtitleLabel = (JLabel) contentPanel.getComponent(4);
        return card;
    }

    private JPanel createEmpruntCard() {
        JPanel card = createBaseCard("Emprunt", "0 FCFA", "Emprunt");
        JPanel contentPanel = (JPanel) card.getComponent(0);
        empruntValueLabel = (JLabel) contentPanel.getComponent(2);
        empruntSubtitleLabel = (JLabel) contentPanel.getComponent(4);
        return card;
    }

    private JPanel createRemboursementCard() {
        JPanel card = createBaseCard("Remboursement", "0 FCFA", "Remboursement");
        JPanel contentPanel = (JPanel) card.getComponent(0);
        remboursementValueLabel = (JLabel) contentPanel.getComponent(2);
        remboursementSubtitleLabel = (JLabel) contentPanel.getComponent(4);
        return card;
    }

    private JPanel createTotalCard() {
        JPanel card = createBaseCard("Solde Net", "0 FCFA", null);
        JPanel contentPanel = (JPanel) card.getComponent(0);
        totalValueLabel = (JLabel) contentPanel.getComponent(2);
        return card;
    }

    private JPanel createBeneficeCard() {
        JPanel card = createBaseCard("Bénéfice", "0 FCFA", null);
        JPanel contentPanel = (JPanel) card.getComponent(0);
        beneficeValueLabel = (JLabel) contentPanel.getComponent(2);
        return card;
    }

    private JPanel createBaseCard(String title, String value, String actionCommand) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Colors.CARD_BACKGROUND);

        // Effet au survol
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.BORDER.brighter(), 2),
                        BorderFactory.createEmptyBorder(14, 14, 14, 14)
                ));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.BORDER),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }
        });

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.CARD_BACKGROUND);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(Fonts.labelFont().deriveFont(Font.BOLD, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ajout d'icône contextuelle
        ImageIcon icon = switch(title) {
            case "Contribution" -> IconManager.getIcon("wallet.svg", 16);
            case "Emprunt" -> IconManager.getIcon("loan.svg", 16);
            case "Remboursement" -> IconManager.getIcon("repayment.svg", 16);
            default -> null;
        };
        if (icon != null) titleLabel.setIcon(icon);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(Fonts.titleFont().deriveFont(Font.BOLD, 24));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("", SwingConstants.CENTER);
        subtitleLabel.setFont(Fonts.smallFont().deriveFont(10));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(valueLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(subtitleLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        if (actionCommand != null) {
            JButton cornerButton = new JButton();
            cornerButton.setIcon(IconManager.getIcon("kebab-menu.svg", 20));
            cornerButton.setBorder(BorderFactory.createEmptyBorder());
            cornerButton.setContentAreaFilled(false);
            cornerButton.setActionCommand(actionCommand);

            // Animation du bouton
            cornerButton.addActionListener(e -> {
                cornerButton.setIcon(IconManager.getIcon("kebab-menu-pressed.svg", 20));
                Timer timer = new Timer(200, ev -> {
                    cornerButton.setIcon(IconManager.getIcon("kebab-menu.svg", 20));
                    ((Timer)ev.getSource()).stop();
                });
                timer.start();
                handleCardAction((ActionEvent)e);
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(new Color(0, 0, 0, 0));
            buttonPanel.add(cornerButton);

            card.add(buttonPanel, BorderLayout.NORTH);
        }

        System.out.println("Création carte: " + title);
        card.setName("card_" + title.replace(" ", "_"));

        return card;
    }

    private void showLoadingState() {
        Component[] loadingComponents = {
                new JLabel(IconManager.getIcon("loading-spinner.svg", 24)),
                new JLabel("Chargement...", SwingConstants.CENTER),
                new JLabel("", SwingConstants.CENTER)
        };

        setCardsContent(loadingComponents);
    }

    private void setCardsContent(Component[] components) {
        Component[] cards = getComponents();
        for (Component card : cards) {
            if (card instanceof JPanel) {
                JPanel contentPanel = (JPanel) ((JPanel) card).getComponent(0);
                contentPanel.removeAll();
                for (Component comp : components) {
                    contentPanel.add(comp);
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        }
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

    public void updateCardValues() {

        System.out.println("Debug - Membre ID: " + membreId);
        System.out.println("Debug - ContributionManager: " + contributionManager);
        System.out.println("Debug - EmpruntManager: " + empruntManager);

// Test direct des managers
        BigDecimal testContrib = contributionManager.getTotalContributionsMembre(membreId);
        System.out.println("Debug - Total contributions: " + testContrib);

        List<Emprunt> testEmprunts = empruntManager.getEmpruntsMembre(membreId);
        System.out.println("Debug - Nombre d'emprunts: " + testEmprunts.size());

        new SwingWorker<Void, Void>() {
            private BigDecimal totalContributions;
            private BigDecimal totalEmprunts;
            private BigDecimal totalRemboursements;
            private long mensuelCount;
            private long annuelCount;
            private long donCount;
            private long empruntsEnCours;
            private BigDecimal soldeRestant;

            @Override
            protected Void doInBackground() {
                // Récupérer les données depuis les managers
                totalContributions = contributionManager.getTotalContributionsMembre(membreId);
                if (totalContributions == null) {
                    totalContributions = BigDecimal.ZERO;
                }

                List<Emprunt> emprunts = empruntManager.getEmpruntsMembre(membreId);
                totalEmprunts = emprunts.stream()
                        .map(Emprunt::getMontant)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                totalRemboursements = emprunts.stream()
                        .map(Emprunt::getMontantRembourse)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Compter les contributions par type
                mensuelCount = contributionManager.findByMembreAndType(membreId, TypeContribution.MENSUEL).size();
                annuelCount = contributionManager.findByMembreAndType(membreId, TypeContribution.ANNUELLE).size();
                donCount = contributionManager.findByMembreAndType(membreId, TypeContribution.DON).size();

                // Compter les emprunts en cours
                empruntsEnCours = empruntManager.getEmpruntsNonRembourses(membreId).size();                // Calculer le solde restant
                soldeRestant = emprunts.stream()
                        .map(e -> e.getMontant().subtract(e.getMontantRembourse()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                return null;
            }

            @Override
            protected void done() {
                // Calcul du solde net
                BigDecimal soldeNet = totalContributions
                        .subtract(totalEmprunts)
                        .add(totalRemboursements);

                // Debug
                System.out.println("Debug - Valeurs calculées:");
                System.out.println("Contributions: " + totalContributions);
                System.out.println("Emprunts: " + totalEmprunts);
                System.out.println("Remboursements: " + totalRemboursements);


                // Calcul du bénéfice
                BigDecimal benefice = calculerBenefice(totalContributions, totalEmprunts, totalRemboursements);

                // Mise à jour des labels
                contributionValueLabel.setText(formatCurrency(totalContributions));
                empruntValueLabel.setText(formatCurrency(totalEmprunts));
                remboursementValueLabel.setText(formatCurrency(totalRemboursements));
                totalValueLabel.setText(formatCurrency(soldeNet));
                beneficeValueLabel.setText(formatCurrency(benefice));

                // Mise à jour des sous-titres
                contributionSubtitleLabel.setText(String.format("Mensuel: %d | Annuel: %d | Don: %d",
                        mensuelCount, annuelCount, donCount));

                empruntSubtitleLabel.setText(String.format("%d emprunt(s) | Total: %s",
                        empruntsEnCours, formatCurrency(totalEmprunts)));

                remboursementSubtitleLabel.setText(String.format("%d en cours | Reste: %s",
                        empruntsEnCours, formatCurrency(soldeRestant)));

                // Tooltips détaillés
                setTooltipDetails(totalContributions, mensuelCount, annuelCount, donCount, empruntsEnCours, soldeRestant);

                // Mise à jour des couleurs
                updateCardColors(soldeNet, benefice);
            }
        }.execute();
    }

    private void setTooltipDetails(BigDecimal totalContributions, long mensuelCount,
                                   long annuelCount, long donCount, long empruntsEnCours,
                                   BigDecimal soldeRestant) {
        long totalContributionsCount = mensuelCount + annuelCount + donCount;

        contributionValueLabel.setToolTipText(String.format(
                "<html><b>Détail des contributions</b><br>"
                        + "Mensuelles: %d (%.1f%%)<br>"
                        + "Annuelles: %d (%.1f%%)<br>"
                        + "Dons: %d (%.1f%%)</html>",
                mensuelCount, (totalContributionsCount > 0 ? mensuelCount*100.0/totalContributionsCount : 0),
                annuelCount, (totalContributionsCount > 0 ? annuelCount*100.0/totalContributionsCount : 0),
                donCount, (totalContributionsCount > 0 ? donCount*100.0/totalContributionsCount : 0)
        ));

        empruntValueLabel.setToolTipText(String.format(
                "<html><b>Détail des emprunts</b><br>"
                        + "En cours: %d<br>"
                        + "Remboursés: %d<br>"
                        + "En retard: %d</html>",
                empruntsEnCours,
                empruntManager.getEmpruntsMembre(membreId).size() - empruntsEnCours,
                empruntManager.getEmpruntsMembre(membreId).stream()
                        .filter(e -> e.getStatut() == StatutEmprunt.EN_RETARD).count()
        ));

        remboursementValueLabel.setToolTipText(String.format(
                "<html><b>Détail des remboursements</b><br>"
                        + "Prochain remboursement: %s<br>"
                        + "Dernier remboursement: %s</html>",
                getNextRepaymentDate(),
                getLastRepaymentDate()
        ));
    }

    private String getNextRepaymentDate() {
        // Implémentation pour récupérer la date du prochain remboursement
        return "N/A";
    }

    private String getLastRepaymentDate() {
        // Implémentation pour récupérer la date du dernier remboursement
        return "N/A";
    }

    private BigDecimal calculerBenefice(BigDecimal contributions, BigDecimal emprunts, BigDecimal remboursements) {
        BigDecimal baseBenefit = contributions.multiply(new BigDecimal("0.02"));

        if (contributions.compareTo(emprunts) > 0) {
            BigDecimal surplus = contributions.subtract(emprunts);
            baseBenefit = baseBenefit.add(surplus.multiply(new BigDecimal("0.05")));
        }

        long empruntsEnRetard = empruntManager.getEmpruntsMembre(membreId).stream()
                .filter(e -> e.getDateRemboursement() != null
                        && e.getDateRemboursement().before(new Date())
                        && e.getStatut() != StatutEmprunt.REMBOURSE)
                .count();

        if (empruntsEnRetard > 0) {
            baseBenefit = baseBenefit.multiply(new BigDecimal("0.9"));
        }

        return baseBenefit.max(BigDecimal.ZERO);
    }

    private void updateCardColors(BigDecimal soldeNet, BigDecimal benefice) {
        // Couleurs modernes
        Color positiveColor = new Color(34, 139, 34); // Vert
        Color warningColor = new Color(255, 193, 7);   // Orange
        Color negativeColor = new Color(220, 53, 69);  // Rouge

        // Contribution - Couleur neutre
        contributionValueLabel.setForeground(new Color(33, 37, 41));

        // Emprunt - Rouge si montant élevé (>50% des contributions)
        BigDecimal contributions = contributionManager.getTotalContributionsMembre(membreId);
        if (contributions != null && contributions.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = totalEmprunts.divide(contributions, 2, BigDecimal.ROUND_HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.5")) > 0) {
                empruntValueLabel.setForeground(negativeColor);
                empruntSubtitleLabel.setForeground(negativeColor);
            } else if (ratio.compareTo(new BigDecimal("0.3")) > 0) {
                empruntValueLabel.setForeground(warningColor);
                empruntSubtitleLabel.setForeground(warningColor);
            }
        }

        // Remboursement - Vert si complet, orange si en cours
        if (soldeRestant.compareTo(BigDecimal.ZERO) == 0) {
            remboursementValueLabel.setForeground(positiveColor);
            remboursementSubtitleLabel.setForeground(positiveColor);
        } else {
            remboursementValueLabel.setForeground(warningColor);
        }

        // Solde Net
        if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
            totalValueLabel.setForeground(negativeColor);
        } else {
            totalValueLabel.setForeground(positiveColor);
        }

        // Bénéfice
        if (benefice.compareTo(BigDecimal.ZERO) > 0) {
            beneficeValueLabel.setForeground(positiveColor);
        } else {
            beneficeValueLabel.setForeground(negativeColor);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount.doubleValue()).replace("€", "FCFA");
    }

    @Override
    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        showLoadingState();
        updateCardValues();
    }
}