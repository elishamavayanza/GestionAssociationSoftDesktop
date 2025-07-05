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
import com.association.util.constants.DatePattern;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeeklyCalendarPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(WeeklyCalendarPanel.class);
    private static final int MAX_CONTRIBUTIONS_PER_DAY = 5;
    private static final int DAYS_IN_WEEK = 7;

    private LocalDate currentDate;
    private YearMonth currentYearMonth;
    private JLabel monthYearLabel;
    private JLabel weekRangeLabel;
    private JPanel daysPanel;
    private JPanel calendarGridPanel;
    private JTextField[][] contributionFields;
    private ContributionManager contributionManager;
    private Long membreId;
    private JButton saveButton;

    public WeeklyCalendarPanel(Long membreId) {
        this.currentDate = LocalDate.now();
        this.currentYearMonth = YearMonth.from(currentDate);
        this.contributionFields = new JTextField[DAYS_IN_WEEK][MAX_CONTRIBUTIONS_PER_DAY];
        this.membreId = membreId;

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

        // Header avec mois/année, plage de dates et boutons de navigation
        JPanel headerPanel = createHeaderPanel();

        // Panel pour les jours de la semaine
        daysPanel = createDaysOfWeekPanel();

        // Panel pour la grille du calendrier
        calendarGridPanel = createCalendarGridPanel();

        // Bouton Enregistrer
        saveButton = createSaveButton();

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

    private JButton createSaveButton() {
        JButton button = new JButton("Enregistrer les contributions");
        button.setFont(Fonts.buttonFont());
        button.setFocusPainted(false);
        button.setBackground(Colors.SUCCESS);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.SUCCESS.darker()),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.addActionListener(e -> enregistrerContributions());
        return button;
    }

    private void enregistrerContributions() {
        double[][] contributions = getContributions();
        LocalDate startOfWeek = getStartOfWeek();
        boolean hasError = false;
        boolean hasWarning = false;

        for (int day = 0; day < DAYS_IN_WEEK; day++) {
            LocalDate contributionDate = startOfWeek.plusDays(day);

            for (int cont = 0; cont < MAX_CONTRIBUTIONS_PER_DAY; cont++) {
                double montant = contributions[day][cont];
                if (montant > 0) {
                    try {
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

        showSaveResultMessages(hasError, hasWarning);
    }

    private void showSaveResultMessages(boolean hasError, boolean hasWarning) {
        if (hasError) {
            JOptionPane.showMessageDialog(this,
                    "Certaines contributions n'ont pas pu être enregistrées",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (hasWarning) {
            JOptionPane.showMessageDialog(this,
                    String.format("Certaines contributions sont inférieures au minimum (%s)",
                            AppConstants.MIN_CONTRIBUTION),
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

        // Panel pour les labels
        JPanel labelsPanel = new JPanel(new GridLayout(2, 1));
        labelsPanel.setBackground(Colors.CARD_BACKGROUND);

        // Label pour afficher le mois et l'année
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(Fonts.labelFontBold());
        monthYearLabel.setForeground(Colors.TEXT);

        // Label pour afficher la plage de dates de la semaine
        weekRangeLabel = new JLabel("", SwingConstants.CENTER);
        weekRangeLabel.setFont(Fonts.labelFont());
        weekRangeLabel.setForeground(Colors.TEXT_SECONDARY);

        labelsPanel.add(monthYearLabel);
        labelsPanel.add(weekRangeLabel);
        headerPanel.add(labelsPanel, BorderLayout.CENTER);

        // Panel pour les boutons de navigation
        JPanel navPanel = createNavigationPanel();
        headerPanel.add(navPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navPanel.setBackground(Colors.CARD_BACKGROUND);

        // Bouton mois précédent
        JButton prevMonthButton = createNavButton("<", e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendarForMonthView();
        });

        // Bouton semaine précédente
        JButton prevWeekButton = createNavButton("<<", e -> {
            currentDate = currentDate.minusWeeks(1);
            updateCalendar();
        });

        // Bouton aujourd'hui
        JButton todayButton = createNavButton("Aujourd'hui", e -> {
            currentDate = LocalDate.now();
            currentYearMonth = YearMonth.from(currentDate);
            updateCalendar();
        });

        // Bouton semaine suivante
        JButton nextWeekButton = createNavButton(">>", e -> {
            currentDate = currentDate.plusWeeks(1);
            updateCalendar();
        });

        // Bouton mois suivant
        JButton nextMonthButton = createNavButton(">", e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendarForMonthView();
        });

        navPanel.add(prevMonthButton);
        navPanel.add(prevWeekButton);
        navPanel.add(todayButton);
        navPanel.add(nextWeekButton);
        navPanel.add(nextMonthButton);

        return navPanel;
    }

    private JButton createNavButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(Fonts.buttonFont());
        button.setFocusPainted(false);
        button.setBackground(Colors.CURRENT_DANGER);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_DANGER.darker()),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        button.addActionListener(action);
        return button;
    }

    private JPanel createDaysOfWeekPanel() {
        JPanel daysPanel = new JPanel(new GridLayout(1, DAYS_IN_WEEK));
        daysPanel.setBackground(Colors.CARD_BACKGROUND);

        DayOfWeek[] daysOfWeek = DayOfWeek.values();
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            DayOfWeek day = daysOfWeek[i];
            JLabel dayLabel = createDayOfWeekLabel(day);
            daysPanel.add(dayLabel);
        }

        return daysPanel;
    }

    private JLabel createDayOfWeekLabel(DayOfWeek day) {
        String dayName = day.getDisplayName(TextStyle.SHORT, Locale.FRENCH);
        JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
        dayLabel.setFont(Fonts.labelFontBold());
        dayLabel.setForeground(Colors.TEXT);
        dayLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.BORDER),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        // Dimanche en rouge
        if (day == DayOfWeek.SUNDAY) {
            dayLabel.setForeground(Colors.DANGER);
        }

        return dayLabel;
    }

    private JPanel createCalendarGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(1, DAYS_IN_WEEK));
        gridPanel.setBackground(Colors.CARD_BACKGROUND);
        return gridPanel;
    }

    private void loadContributionsForWeek() {
        if (membreId == null) {
            return;
        }

        LocalDate startOfWeek = getStartOfWeek();
        LocalDate endOfWeek = startOfWeek.plusDays(DAYS_IN_WEEK - 1);

        try {
            List<Contribution> contributions = contributionManager.getContributionsBetweenDates(
                    java.sql.Date.valueOf(startOfWeek),
                    java.sql.Date.valueOf(endOfWeek)
            );

            resetContributionFields();
            processContributions(contributions);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des contributions", e);
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des contributions",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate getStartOfWeek() {
        return currentDate.with(DayOfWeek.MONDAY);
    }

    private void resetContributionFields() {
        for (int day = 0; day < DAYS_IN_WEEK; day++) {
            for (int cont = 0; cont < MAX_CONTRIBUTIONS_PER_DAY; cont++) {
                JTextField field = contributionFields[day][cont];
                if (field != null) {
                    field.setText("");
                    field.setBackground(Color.WHITE);
                    field.setEditable(true);
                    field.setToolTipText(null);
                }
            }
        }
    }

    private void processContributions(List<Contribution> contributions) {
        for (Contribution contribution : contributions) {
            try {
                if (isValidContribution(contribution)) {
                    LocalDate contributionDate = convertToLocalDate(contribution.getDateTransaction());
                    int dayOfWeek = contributionDate.getDayOfWeek().getValue() - 1; // 0=lundi, 6=dimanche
                    addContributionToField(dayOfWeek, contribution);
                }
            } catch (Exception e) {
                logger.warn("Contribution invalide ignorée: " + contribution, e);
            }
        }
    }

    private boolean isValidContribution(Contribution contribution) {
        return contribution != null
                && contribution.getDateTransaction() != null
                && contribution.getMembre() != null
                && contribution.getMembre().getId() != null
                && contribution.getMembre().getId().equals(membreId)
                && contribution.getMontant() != null;
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("La date ne peut pas être null");
        }

        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void addContributionToField(int dayOfWeek, Contribution contribution) {
        if (contribution.getMontant().compareTo(AppConstants.MIN_CONTRIBUTION) < 0) {
            logger.warn("Contribution trop faible ignorée: " + contribution.getMontant());
            return;
        }

        for (int i = 0; i < MAX_CONTRIBUTIONS_PER_DAY; i++) {
            if (contributionFields[dayOfWeek][i].getText().isEmpty()) {
                JTextField field = contributionFields[dayOfWeek][i];
                field.setText(formatAmount(contribution.getMontant()));
                field.setBackground(Colors.SUCCESS.brighter());
                field.setEditable(false);
                field.setToolTipText(createContributionTooltip(contribution));
                break;
            }
        }
    }

    private String createContributionTooltip(Contribution contribution) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append("Contribution du ").append(formatDate(contribution.getDateTransaction()));
        tooltip.append("\nMontant: ").append(contribution.getMontant());

        if (contribution.getDescription() != null && !contribution.getDescription().isEmpty()) {
            tooltip.append("\nDescription: ").append(contribution.getDescription());
        }

        return tooltip.toString();
    }

    private String formatAmount(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    private String formatDate(Date date) {
        return DateUtil.formatDate(date, DatePattern.SHORT_DATE);
    }

    public void updateCalendar() {
        currentYearMonth = YearMonth.from(currentDate);
        updateHeaderLabels();
        rebuildCalendarGrid();
        loadContributionsForWeek();
    }

    private void updateCalendarForMonthView() {
        // Garder le même jour du mois si possible
        int dayOfMonth = Math.min(currentDate.getDayOfMonth(), currentYearMonth.lengthOfMonth());
        currentDate = currentYearMonth.atDay(dayOfMonth);
        updateCalendar();
    }

    private void updateHeaderLabels() {
        // Mettre à jour le label du mois/année
        String monthYear = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH)
                + " " + currentYearMonth.getYear();
        monthYearLabel.setText(monthYear.toUpperCase());

        // Mettre à jour la plage de dates de la semaine
        LocalDate startOfWeek = getStartOfWeek();
        LocalDate endOfWeek = startOfWeek.plusDays(DAYS_IN_WEEK - 1);

        // Nouveau code (corrigé)
        String weekRange = String.format("Semaine du %s au %s",
                formatLocalDate(startOfWeek),
                formatLocalDate(endOfWeek));

        weekRangeLabel.setText(weekRange);
    }
    private String formatLocalDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(DatePattern.SHORT_DATE.getPattern(), Locale.FRENCH));
    }

    private void rebuildCalendarGrid() {
        calendarGridPanel.removeAll();
        LocalDate startOfWeek = getStartOfWeek();

        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            JPanel dayPanel = createDayPanel(day, i);
            calendarGridPanel.add(dayPanel);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }

    private JPanel createDayPanel(LocalDate date, int dayIndex) {
        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Colors.BORDER),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Appliquer un style différent pour les jours du mois précédent/suivant
        if (date.getMonthValue() != currentYearMonth.getMonthValue()) {
            dayPanel.setBackground(Colors.CARD_BACKGROUND.darker());
        } else {
            dayPanel.setBackground(Colors.CARD_BACKGROUND);
        }

        // Label pour le numéro du jour
        JLabel dayNumberLabel = createDayNumberLabel(date);
        dayPanel.add(dayNumberLabel, BorderLayout.NORTH);

        // Panel pour les contributions
        JPanel contributionsPanel = createContributionsPanel(dayIndex);
        dayPanel.add(contributionsPanel, BorderLayout.CENTER);

        return dayPanel;
    }

    private JLabel createDayNumberLabel(LocalDate date) {
        JLabel label = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        label.setFont(Fonts.labelFont());

        if (date.equals(LocalDate.now())) {
            label.setForeground(Colors.CURRENT_DANGER);
            label.setFont(Fonts.labelFontBold());
        } else if (date.getMonthValue() != currentYearMonth.getMonthValue()) {
            label.setForeground(Colors.TEXT_SECONDARY);
        } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            label.setForeground(Colors.DANGER);
        } else {
            label.setForeground(Colors.TEXT);
        }

        return label;
    }

    private JPanel createContributionsPanel(int dayIndex) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0, 0, 0, 0)); // Transparent

        for (int i = 0; i < MAX_CONTRIBUTIONS_PER_DAY; i++) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

            JTextField field = createContributionTextField();
            contributionFields[dayIndex][i] = field;
            row.add(field, BorderLayout.CENTER);
            panel.add(row);
        }

        return panel;
    }

    private JTextField createContributionTextField() {
        JTextField field = new JTextField();
        field.setFont(Fonts.textFieldFont());
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        // Créer le filtre avec une référence au champ texte
        ((AbstractDocument)field.getDocument()).setDocumentFilter(new NumericDocumentFilter(field));
        return field;
    }

    public double[][] getContributions() {
        double[][] contributions = new double[DAYS_IN_WEEK][MAX_CONTRIBUTIONS_PER_DAY];

        for (int day = 0; day < DAYS_IN_WEEK; day++) {
            for (int cont = 0; cont < MAX_CONTRIBUTIONS_PER_DAY; cont++) {
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

    public void setContributions(double[][] contributions) {
        for (int day = 0; day < DAYS_IN_WEEK && day < contributions.length; day++) {
            for (int cont = 0; cont < MAX_CONTRIBUTIONS_PER_DAY && cont < contributions[day].length; cont++) {
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
        this.currentYearMonth = YearMonth.from(currentDate);
        updateCalendar();
    }

    public void setMembreId(Long membreId) {
        this.membreId = membreId;
        this.contributionManager = new ContributionManager(
                DAOFactory.getInstance(ContributionDao.class),
                new MembreManager(DAOFactory.getInstance(MembreDao.class), null)
        );
        updateCalendar();
    }

    private class NumericDocumentFilter extends DocumentFilter {
        private final JTextField textField;

        public NumericDocumentFilter(JTextField textField) {
            this.textField = textField;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;

            try {
                if (!newText.isEmpty()) {
                    double value = Double.parseDouble(newText);
                    if (value < AppConstants.MIN_CONTRIBUTION.doubleValue()) {
                        textField.setBackground(Colors.WARNING.brighter());
                    } else {
                        textField.setBackground(Color.WHITE);
                    }
                }
            } catch (NumberFormatException e) {
                // Ignorer les saisies non numériques
            }

            super.replace(fb, offset, length, text, attrs);
        }
    }
}