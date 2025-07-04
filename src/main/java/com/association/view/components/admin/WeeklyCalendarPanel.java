package com.association.view.components.admin;

import com.association.dao.ContributionDao;
import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.ContributionManager;
import com.association.manager.MembreManager;
import com.association.model.transaction.Contribution;
import com.association.util.constants.AppConstants;
import com.association.util.utils.DateUtil;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;

import com.association.util.constants.DatePattern; // si tu gères les formats dans un enum

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

import java.time.format.TextStyle;
import java.util.Locale;


public class WeeklyCalendarPanel extends JPanel {
    private LocalDate currentDate;
    private JLabel monthYearLabel;
    private JPanel daysPanel;
    private JPanel calendarGridPanel;
    private JTextField[][] contributionFields;
    private ContributionManager contributionManager;
    private Long membreId;
    private JButton saveButton;
    private static final Logger logger = LoggerFactory.getLogger(WeeklyCalendarPanel.class);


    public WeeklyCalendarPanel(Long membreId) {  // Ajoutez le paramètre membreId
        this.currentDate = LocalDate.now();
        this.contributionFields = new JTextField[7][5];
        this.membreId = membreId;  // Initialisez membreId avec la valeur passée en paramètre

        // Initialiser le ContributionManager via DAOFactory
        this.contributionManager = new ContributionManager(
                DAOFactory.getInstance(ContributionDao.class),
                new MembreManager(DAOFactory.getInstance(MembreDao.class), null)
        );

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.CARD_BACKGROUND);
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Header avec mois/année et boutons de navigation
        JPanel headerPanel = createHeaderPanel();

        // Panel pour les jours de la semaine
        daysPanel = createDaysOfWeekPanel();

        // Panel pour la grille du calendrier
        calendarGridPanel = createCalendarGridPanel();

        // Bouton Enregistrer
        saveButton = new JButton("Enregistrer les contributions");
        styleSaveButton(saveButton);
        saveButton.addActionListener(e -> enregistrerContributions());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Colors.CARD_BACKGROUND);
        bottomPanel.add(saveButton);

        // Panel conteneur pour les éléments du bas
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(calendarGridPanel, BorderLayout.CENTER);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Ajout des composants principaux
        add(headerPanel, BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        updateCalendar();
    }

    private void styleSaveButton(JButton button) {
        button.setFont(Fonts.buttonFont());
        button.setFocusPainted(false);
        button.setBackground(Colors.SUCCESS);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Colors.SUCCESS.darker()), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
    }

    private void enregistrerContributions() {
        double[][] contributions = getContributions();
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        boolean hasError = false;
        boolean hasWarning = false;

        for (int day = 0; day < 7; day++) {
            LocalDate contributionDate = startOfWeek.plusDays(day);

            for (int cont = 0; cont < 5; cont++) {
                double montant = contributions[day][cont];
                if (montant > 0) {
                    try {
                        // Vérification du montant minimum
                        if (montant < AppConstants.MIN_CONTRIBUTION.doubleValue()) {
                            contributionFields[day][cont].setBackground(Colors.WARNING.brighter());
                            hasWarning = true;
                            continue;
                        }

                        boolean success = contributionManager.enregistrerContribution(
                                membreId,
                                BigDecimal.valueOf(montant),
                                contributionDate
                        );

                        if (success) {
                            contributionFields[day][cont].setBackground(Colors.SUCCESS.brighter());
                            contributionFields[day][cont].setEditable(false);
                        } else {
                            contributionFields[day][cont].setBackground(Colors.DANGER.brighter());
                            hasError = true;
                        }
                    } catch (Exception e) {
                        contributionFields[day][cont].setBackground(Colors.DANGER.brighter());
                        hasError = true;
                        logger.error("Erreur lors de l'enregistrement", e);
                    }
                }
            }
        }

        if (hasError) {
            JOptionPane.showMessageDialog(this,
                    "Certaines contributions n'ont pas pu être enregistrées",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (hasWarning) {
            JOptionPane.showMessageDialog(this,
                    "Certaines contributions sont inférieures au minimum (" +
                            AppConstants.MIN_CONTRIBUTION + ")",
                    "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
        }

        if (!hasError && !hasWarning) {
            JOptionPane.showMessageDialog(this,
                    "Toutes les contributions ont été enregistrées avec succès!",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colors.CARD_BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Label pour afficher le mois et l'année
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(Fonts.labelFontBold());
        monthYearLabel.setForeground(Colors.TEXT);
        headerPanel.add(monthYearLabel, BorderLayout.CENTER);

        // Panel pour les boutons de navigation
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navPanel.setBackground(Colors.CARD_BACKGROUND);

        // Bouton semaine précédente
        JButton prevButton = new JButton("<");
        styleNavButton(prevButton);
        prevButton.addActionListener(e -> {
            currentDate = currentDate.minusWeeks(1);
            updateCalendar();
        });

        // Bouton aujourd'hui
        JButton todayButton = new JButton("Aujourd'hui");
        styleNavButton(todayButton);
        todayButton.addActionListener(e -> {
            currentDate = LocalDate.now();
            updateCalendar();
        });

        // Bouton semaine suivante
        JButton nextButton = new JButton(">");
        styleNavButton(nextButton);
        nextButton.addActionListener(e -> {
            currentDate = currentDate.plusWeeks(1);
            updateCalendar();
        });

        navPanel.add(prevButton);
        navPanel.add(todayButton);
        navPanel.add(nextButton);
        headerPanel.add(navPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void styleNavButton(JButton button) {
        button.setFont(Fonts.buttonFont());
        button.setFocusPainted(false);
        button.setBackground(Colors.CURRENT_DANGER);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Colors.CURRENT_DANGER.darker()), BorderFactory.createEmptyBorder(3, 10, 3, 10)));
    }

    private JPanel createDaysOfWeekPanel() {
        JPanel daysPanel = new JPanel(new GridLayout(1, 7));
        daysPanel.setBackground(Colors.CARD_BACKGROUND);

        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = daysOfWeek[i];
            String dayName = day.getDisplayName(TextStyle.SHORT, Locale.FRENCH);

            JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
            dayLabel.setFont(Fonts.labelFontBold());
            dayLabel.setForeground(Colors.TEXT);
            dayLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.BORDER), BorderFactory.createEmptyBorder(5, 0, 5, 0)));

            daysPanel.add(dayLabel);
        }

        return daysPanel;
    }

    private JPanel createCalendarGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(1, 7));
        gridPanel.setBackground(Colors.CARD_BACKGROUND);
        return gridPanel;
    }
    private void loadContributionsForWeek() {
        // Vérification de l'ID membre
        if (membreId == null) {
            return;
        }

        // Détermination de la période (semaine courante)
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        try {
            // Récupération des contributions depuis le manager
            List<Contribution> contributions = contributionManager.getContributionsBetweenDates(
                    java.sql.Date.valueOf(startOfWeek),
                    java.sql.Date.valueOf(endOfWeek)
            );

            // Réinitialisation de l'interface
            resetContributionFields();

            // Traitement des contributions
            processContributions(contributions);
        } catch (Exception e) {
            // Gestion des erreurs
            logger.error("Erreur lors du chargement des contributions", e);
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des contributions",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Méthode pour réinitialiser les champs de contribution
    private void resetContributionFields() {
        for (int day = 0; day < 7; day++) {
            for (int cont = 0; cont < 5; cont++) {
                contributionFields[day][cont].setText("");
                contributionFields[day][cont].setBackground(Color.WHITE);
                contributionFields[day][cont].setEditable(true);
                contributionFields[day][cont].setToolTipText(null);
            }
        }
    }

    // Méthode pour traiter les contributions
    private void processContributions(List<Contribution> contributions) {
        for (Contribution contribution : contributions) {
            try {
                // Vérification de la validité de la contribution
                if (isValidContribution(contribution)) {
                    LocalDate contributionDate = convertToLocalDate(contribution.getDateTransaction());
                    int dayOfWeek = contributionDate.getDayOfWeek().getValue() - 1; // 0=lundi, 6=dimanche

                    // Ajout de la contribution à l'interface
                    addContributionToField(dayOfWeek, contribution);
                }
            } catch (Exception e) {
                logger.warn("Contribution invalide ignorée: " + contribution, e);
            }
        }
    }

    // Méthode pour vérifier si une contribution est valide
    private boolean isValidContribution(Contribution contribution) {
        return contribution != null
                && contribution.getDateTransaction() != null
                && contribution.getMembre() != null
                && contribution.getMembre().getId() != null
                && contribution.getMembre().getId().equals(membreId)
                && contribution.getMontant() != null;
    }

    // Méthode pour convertir une Date en LocalDate
    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("La date ne peut pas être null");
        }

        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Méthode pour ajouter une contribution à un champ spécifique
    private void addContributionToField(int dayOfWeek, Contribution contribution) {
        // Vérifier que le montant respecte le minimum
        if (contribution.getMontant().compareTo(AppConstants.MIN_CONTRIBUTION) < 0) {
            logger.warn("Contribution trop faible ignorée: " + contribution.getMontant());
            return;
        }

        for (int i = 0; i < 5; i++) {
            if (contributionFields[dayOfWeek][i].getText().isEmpty()) {
                contributionFields[dayOfWeek][i].setText(formatAmount(contribution.getMontant()));
                contributionFields[dayOfWeek][i].setBackground(Colors.SUCCESS.brighter());
                contributionFields[dayOfWeek][i].setEditable(false);
                contributionFields[dayOfWeek][i].setToolTipText(
                        "Contribution du " + formatDate(contribution.getDateTransaction()) +
                                "\nMontant: " + contribution.getMontant() +
                                (contribution.getDescription() != null ?
                                        "\nDescription: " + contribution.getDescription() : "")
                );
                break;
            }
        }
    }

    // Méthode utilitaire pour formater un montant
    private String formatAmount(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    // Méthode utilitaire pour formater une date
    private String formatDate(Date date) {
        return DateUtil.formatDate(date, DatePattern.SHORT_DATE);
    }
    public void updateCalendar() {
        // Mettre à jour le label du mois/année
        String monthYear = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + currentDate.getYear();
        monthYearLabel.setText(monthYear.toUpperCase());

        // Calculer le début de la semaine (lundi)
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);

        // Nettoyer la grille existante
        calendarGridPanel.removeAll();

        // Ajouter les jours de la semaine
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            JPanel dayPanel = createDayPanel(day, i);
            calendarGridPanel.add(dayPanel);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();

        // Charger les contributions pour cette semaine
        loadContributionsForWeek();
    }

    private JPanel createDayPanel(LocalDate date, int dayIndex) {
        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Colors.BORDER), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        dayPanel.setBackground(Colors.CARD_BACKGROUND);

        // Label pour le numéro du jour
        JLabel dayNumberLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        dayNumberLabel.setFont(Fonts.labelFont());

        // Mettre en évidence la date actuelle
        if (date.equals(LocalDate.now())) {
            dayNumberLabel.setForeground(Colors.CURRENT_DANGER);
            dayNumberLabel.setFont(Fonts.labelFontBold());
            dayPanel.setBackground(Colors.CARD_BACKGROUND.darker());
        } else if (date.getMonthValue() != currentDate.getMonthValue()) {
            dayNumberLabel.setForeground(Colors.TEXT_SECONDARY);
        } else {
            dayNumberLabel.setForeground(Colors.TEXT);
        }

        dayPanel.add(dayNumberLabel, BorderLayout.NORTH);

        // Panel pour les contributions
        JPanel contributionsPanel = new JPanel();
        contributionsPanel.setLayout(new BoxLayout(contributionsPanel, BoxLayout.Y_AXIS));
        contributionsPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent

        // Ajouter 5 champs de saisie pour les contributions
        for (int i = 0; i < 5; i++) {
            JPanel contributionRow = new JPanel(new BorderLayout());
            contributionRow.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

            JTextField contributionField = new JTextField();
            // ... (configuration existante)

            // Ajout d'un DocumentFilter pour valider la saisie
            ((AbstractDocument)contributionField.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                        throws BadLocationException {
                    String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;

                    try {
                        if (!newText.isEmpty()) {
                            double value = Double.parseDouble(newText);
                            if (value < AppConstants.MIN_CONTRIBUTION.doubleValue()) {
                                contributionField.setBackground(Colors.ERRER.brighter());
                            } else {
                                contributionField.setBackground(Color.WHITE);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer les saisies non numériques (seront bloquées après)
                    }

                    super.replace(fb, offset, length, text, attrs);
                }
            });

            contributionFields[dayIndex][i] = contributionField;
            contributionRow.add(contributionField, BorderLayout.CENTER);
            contributionsPanel.add(contributionRow);
        }


        dayPanel.add(contributionsPanel, BorderLayout.CENTER);

        return dayPanel;
    }

    // Méthode pour récupérer les contributions saisies
    public double[][] getContributions() {
        double[][] contributions = new double[7][5];

        for (int day = 0; day < 7; day++) {
            for (int cont = 0; cont < 5; cont++) {
                try {
                    String text = contributionFields[day][cont].getText();
                    contributions[day][cont] = text.isEmpty() ? 0 : Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    contributions[day][cont] = 0;
                }
            }
        }

        return contributions;
    }

    // Méthode pour définir les contributions
    public void setContributions(double[][] contributions) {
        for (int day = 0; day < 7 && day < contributions.length; day++) {
            for (int cont = 0; cont < 5 && cont < contributions[day].length; cont++) {
                if (contributions[day][cont] > 0) {
                    contributionFields[day][cont].setText(String.valueOf(contributions[day][cont]));
                } else {
                    contributionFields[day][cont].setText("");
                }
            }
        }
    }


    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
        updateCalendar();
    }
    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        this.contributionManager = new ContributionManager(
                DAOFactory.getInstance(ContributionDao.class),
                new MembreManager(DAOFactory.getInstance(MembreDao.class), null)
        );
        updateCalendar(); // Cela appellera loadContributionsForWeek()
    }
}