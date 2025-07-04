package com.association.view.components.admin;

import com.association.dao.ContributionDao;
import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.manager.ContributionManager;
import com.association.manager.MembreManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mysql.cj.conf.PropertyKey.logger;

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

        for (int day = 0; day < 7; day++) {
            LocalDate contributionDate = startOfWeek.plusDays(day);

            for (int cont = 0; cont < 5; cont++) {
                double montant = contributions[day][cont];
                if (montant > 0) {
                    try {
                        boolean success = contributionManager.enregistrerContribution(membreId, BigDecimal.valueOf(montant), contributionDate);

                        if (success) {
                            contributionFields[day][cont].setBackground(Colors.SUCCESS.brighter());
                        } else {
                            contributionFields[day][cont].setBackground(Colors.SUCCESS.brighter());
                            hasError = true;
                        }
                    } catch (Exception e) {
                        contributionFields[day][cont].setBackground(Colors.SUCCESS.brighter());
                        hasError = true;
                        logger.error("Erreur lors de l'enregistrement", e);
                    }
                }
            }
        }

        if (hasError) {
            JOptionPane.showMessageDialog(this, "Certaines contributions n'ont pas pu être enregistrées", "Erreur", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Toutes les contributions ont été enregistrées avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
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

            // Créer un champ de texte pour la contribution
            JTextField contributionField = new JTextField();
            contributionField.setHorizontalAlignment(JTextField.CENTER);
            contributionField.setFont(Fonts.textFieldFont());
            contributionField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Colors.BORDER), BorderFactory.createEmptyBorder(3, 3, 3, 3)));

            // Stocker la référence au champ de texte
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
        updateCalendar();
    }
}