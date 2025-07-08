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
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class InfoCardsPanel extends JPanel implements Refreshable {
    private Long membreId;
    private final ContributionManager contributionManager;
    private final EmpruntManager empruntManager;
    private final MembreManager membreManager;
    private BigDecimal totalEmprunts = BigDecimal.ZERO;
    private BigDecimal soldeRestant = BigDecimal.ZERO;
    private Timer refreshTimer;
    private JLabel remboursementDatesLabel; // Ajoutez cette ligne


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
    private BigDecimal totalRemboursements = BigDecimal.ZERO;

    public InfoCardsPanel(Long membreId,
                          ContributionManager contributionManager,
                          EmpruntManager empruntManager,
                          MembreManager membreManager) {

        if (!SwingUtilities.isEventDispatchThread()) {
        }

        this.membreId = membreId;
        this.contributionManager = contributionManager;
        this.empruntManager = empruntManager;
        this.membreManager = membreManager;


        initComponents();
        showLoadingState();
        updateCardValues();

        initRefreshTimer();

    }



    private void initComponents() {
        // Panel principal avec BorderLayout
        setLayout(new BorderLayout());
        setBackground(Colors.CARD_BACKGROUND);

        // Nouvelle hauteur pour toutes les cartes (ex: 140px au lieu de 180px)
        int reducedHeight = 140;

        // Panel pour les 4 premières cartes (2x2)
        JPanel topCardsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topCardsPanel.setBackground(Colors.CARD_BACKGROUND);

        // Panel pour la 5ème carte (en bas)
        JPanel bottomCardPanel = new JPanel(new BorderLayout());
        bottomCardPanel.setBackground(Colors.CARD_BACKGROUND);
        bottomCardPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Créer les cartes avec la nouvelle hauteur réduite
        JPanel contributionCard = createCardWithFixedSize(createContributionCard(), reducedHeight);
        JPanel empruntCard = createCardWithFixedSize(createEmpruntCard(), reducedHeight);
        JPanel remboursementCard = createCardWithFixedSize(createRemboursementCard(), reducedHeight);
        JPanel totalCard = createCardWithFixedSize(createTotalCard(), reducedHeight);
        JPanel beneficeCard = createCardWithFixedSize(createBeneficeCard(), reducedHeight);

        // Ajouter les 4 premières cartes au panel du haut
        topCardsPanel.add(contributionCard);
        topCardsPanel.add(empruntCard);
        topCardsPanel.add(remboursementCard);
        topCardsPanel.add(totalCard);

        // Ajouter la 5ème carte au panel du bas
        bottomCardPanel.add(beneficeCard, BorderLayout.CENTER);

        // Conteneur principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Colors.CARD_BACKGROUND);

        mainPanel.add(topCardsPanel);
        mainPanel.add(bottomCardPanel);

        // Ajouter un scrolling
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Colors.CARD_BACKGROUND);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createCardWithFixedSize(JPanel card, int height) {
        // Largeur fixe, hauteur personnalisable
        card.setPreferredSize(new Dimension(300, height));
        card.setMinimumSize(new Dimension(300, height));
        card.setMaximumSize(new Dimension(300, height));
        return card;
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

// Ajout d'un deuxième sous-titre pour les dates
        remboursementDatesLabel = new JLabel("", SwingConstants.CENTER);
        remboursementDatesLabel.setFont(Fonts.smallFont().deriveFont(10));
        remboursementDatesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(remboursementDatesLabel);

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


        return card;
    }

    private void showLoadingState() {
        Component[] loadingComponents = {
                new JLabel(IconManager.getIcon("loader.svg", 24)),
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

        if (refreshTimer != null) {
            refreshTimer.stop();
        }

// Test direct des managers
        BigDecimal testContrib = contributionManager.getTotalContributionsMembre(membreId);

        List<Emprunt> testEmprunts = empruntManager.getEmpruntsMembre(membreId);

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

                // Calculer l'intérêt total de 5% sur les emprunts
                BigDecimal interetTotal = totalEmprunts.multiply(new BigDecimal("0.05"));

                // Ajouter l'intérêt au total des remboursements attendus
                BigDecimal totalRemboursementsAttendus = totalEmprunts.add(interetTotal);

                return null;
            }

            @Override
            protected void done() {
                // Calcul du solde net
                BigDecimal soldeNet = totalContributions
                        .subtract(totalEmprunts)
                        .add(totalRemboursements);

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

                remboursementSubtitleLabel.setText(String.format("%d en cours | Reste: %s (incl. 5%%)",
                        empruntsEnCours, formatCurrency(soldeRestant)));
                // Tooltips détaillés
                setTooltipDetails(totalContributions, mensuelCount, annuelCount, donCount, empruntsEnCours, soldeRestant);

                String nextRepayment = getNextRepaymentDate();
                String lastRepayment = getLastRepaymentDate();
                String datesText = String.format("<html>Prochain: %s | Dernier: %s</html>",
                        formatDateWithColor(getNextRepaymentDate(), true),
                        formatDateWithColor(getLastRepaymentDate(), false));
                remboursementDatesLabel.setText(datesText);

                // Mise à jour des couleurs
                updateCardColors(soldeNet, benefice);

                if (refreshTimer != null) {
                    refreshTimer.start();
                }
            }
        }.execute();
    }
    private String formatDateWithColor(String date, boolean isNext) {
        if ("N/A".equals(date)) {
            return "<font color='gray'>" + date + "</font>";
        }
        return String.format("<font color='%s'>%s</font>",
                isNext ? "#2E7D32" : "#1565C0", // Vert pour prochain, bleu pour dernier
                date);
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
                        + "Montant emprunté: %s<br>"
                        + "Intérêt (5%%): %s<br>"
                        + "Total à rembourser: %s<br>"
                        + "Déjà remboursé: %s<br>"
                        + "Solde restant: %s<br>"
                        + "Dernier remboursement: %s<br>"
                        + "Prochain remboursement: %s</html>",
                formatCurrency(totalEmprunts),
                formatCurrency(totalEmprunts.multiply(new BigDecimal("0.05"))),
                formatCurrency(totalEmprunts.multiply(new BigDecimal("1.05"))),
                formatCurrency(totalRemboursements),
                formatCurrency(soldeRestant),
                getLastRepaymentDate(),
                getNextRepaymentDate()
        ));
    }

    private String getNextRepaymentDate() {
        // Récupérer tous les emprunts non remboursés du membre
        List<Emprunt> empruntsNonRembourses = empruntManager.getEmpruntsNonRembourses(membreId);

        if (empruntsNonRembourses.isEmpty()) {
            return "N/A";
        }

        // Trouver la prochaine date de remboursement la plus proche
        Date nextDate = null;
        Date now = new Date();

        for (Emprunt emprunt : empruntsNonRembourses) {
            Date dateRemboursement = emprunt.getDateRemboursement();
            if (dateRemboursement != null && dateRemboursement.after(now)) {
                if (nextDate == null || dateRemboursement.before(nextDate)) {
                    nextDate = dateRemboursement;
                }
            }
        }

        return nextDate != null ? formatDate(nextDate) : "N/A";
    }

    private String getLastRepaymentDate() {
        // Récupérer tous les emprunts remboursés du membre
        List<Emprunt> empruntsRembourses = empruntManager.getEmpruntsMembre(membreId).stream()
                .filter(e -> e.getStatut() == StatutEmprunt.REMBOURSE)
                .toList();

        if (empruntsRembourses.isEmpty()) {
            return "N/A";
        }

        // Trouver la date de remboursement la plus récente
        Date lastDate = null;

        for (Emprunt emprunt : empruntsRembourses) {
            Date dateRemboursement = emprunt.getDateRemboursement();
            if (dateRemboursement != null) {
                if (lastDate == null || dateRemboursement.after(lastDate)) {
                    lastDate = dateRemboursement;
                }
            }
        }

        return lastDate != null ? formatDate(lastDate) : "N/A";
    }

    private String formatDate(Date date) {
        // Utiliser SimpleDateFormat pour formater la date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
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
        return CURRENCY_FORMAT.format(amount.doubleValue()).replace("€", "FC");
    }

    private void initRefreshTimer() {
        // Créer un timer qui se déclenche toutes les 3 secondes (3000 ms)
        refreshTimer = new Timer(3000, e -> {
            // Rafraîchir les données
            updateCardValues();
        });
        refreshTimer.setRepeats(true); // Répéter indéfiniment
        refreshTimer.start(); // Démarrer le timer
    }

    @Override
    public void setMembreId(Long membreId) {

        if (refreshTimer != null) {
            refreshTimer.stop();
        }

        this.membreId = membreId;
        showLoadingState();
        updateCardValues();

        initRefreshTimer();

    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // Arrêter le timer lorsque le panel est retiré
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

}