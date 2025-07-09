package com.association.util.utils;

import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomDialog {
    // Icônes personnalisées pour chaque type de message
    private static final Icon INFO_ICON = IconManager.getIcon("info.svg", 32);
    private static final Icon WARNING_ICON = IconManager.getIcon("warning.svg", 32);
    private static final Icon ERROR_ICON = IconManager.getIcon("error.svg", 32);
    private static final Icon QUESTION_ICON = IconManager.getIcon("question.svg", 32);
    private static final Icon SUCCESS_ICON = IconManager.getIcon("success.svg", 32);

    private static final Icon YES_ICON = IconManager.getIcon("yes.svg", 24);
    private static final Icon NO_ICON = IconManager.getIcon("no.svg", 24);
    private static final Icon CANCEL_ICON = IconManager.getIcon("cancel.svg", 24);
    private static final Icon OK_ICON = IconManager.getIcon("ok.svg", 24);

    public static void customizeDialogs() {
        // Personnalisation de base pour tous les JOptionPane
        UIManager.put("OptionPane.background", Colors.CARD_BACKGROUND);
        UIManager.put("Panel.background", Colors.CARD_BACKGROUND);
        UIManager.put("OptionPane.messageFont", Fonts.labelFont());
        UIManager.put("OptionPane.buttonFont", Fonts.buttonFont());
        UIManager.put("OptionPane.messageForeground", Colors.TEXT);
        UIManager.put("OptionPane.border", BorderFactory.createLineBorder(Colors.BORDER));

        // Styles spécifiques par type de message
        UIManager.put("OptionPane.informationBackground", Colors.UNREAD_NOTIFICATION);
        UIManager.put("OptionPane.warningBackground", Colors.WARNING_BACKGROUND);
        UIManager.put("OptionPane.errorBackground", Colors.ERROR_BACKGROUND);
        UIManager.put("OptionPane.questionBackground", Colors.UNREAD_NOTIFICATION);

        // Boutons
        UIManager.put("Button.background", Colors.PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", Fonts.buttonFont());
        UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.PRIMARY_DARK),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        // Pour les boutons dans JOptionPane
        UIManager.put("OptionPane.buttonAreaBorder", new EmptyBorder(10, 10, 10, 10));
    }

    // Méthodes pour afficher différents types de dialogues

    public static void showInfoDialog(Component parent, String message, String title) {
        showCustomMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarningDialog(Component parent, String message, String title) {
        showCustomMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showErrorDialog(Component parent, String message, String title) {
        showCustomMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccessDialog(Component parent, String message, String title) {
        showCustomDialog(parent, message, title, SUCCESS_ICON, Colors.SUCCESS_BACKGROUND);
    }

    public static int showConfirmDialog(Component parent, String message, String title) {
        return showCustomConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
    }

    public static int showCustomConfirmDialog(Component parent, String message, String title, int optionType) {
        // Créer des boutons personnalisés avec icônes
        JButton yesButton = createButton("Oui", YES_ICON, Colors.SUCCESS);
        JButton noButton = createButton("Non", NO_ICON, Colors.DANGER);
        JButton cancelButton = createButton("Annuler", CANCEL_ICON, Colors.WARNING);

        Object[] options;
        if (optionType == JOptionPane.YES_NO_OPTION) {
            options = new Object[]{yesButton, noButton};
        } else {
            options = new Object[]{yesButton, noButton, cancelButton};
        }

        // Créer le panneau de message
        JPanel messagePanel = createMessagePanel(message);

        // Créer le JOptionPane personnalisé
        JOptionPane pane = new JOptionPane(
                messagePanel,
                JOptionPane.QUESTION_MESSAGE,
                optionType,
                QUESTION_ICON,
                options,
                options[0]
        );

        // Créer et configurer la boîte de dialogue
        JDialog dialog = pane.createDialog(parent, title);
        dialog.getContentPane().setBackground(Colors.UNREAD_NOTIFICATION);

        // Ajouter les ActionListeners aux boutons
        yesButton.addActionListener(e -> {
            pane.setValue(JOptionPane.YES_OPTION);
            dialog.dispose();
        });

        noButton.addActionListener(e -> {
            pane.setValue(JOptionPane.NO_OPTION);
            dialog.dispose();
        });

        if (cancelButton != null) {
            cancelButton.addActionListener(e -> {
                pane.setValue(JOptionPane.CANCEL_OPTION);
                dialog.dispose();
            });
        }

        dialog.setVisible(true);
        Object selectedValue = pane.getValue();

        if (selectedValue == null || selectedValue.equals(JOptionPane.UNINITIALIZED_VALUE)) {
            return JOptionPane.CLOSED_OPTION;
        }

        // Retourner la valeur correspondante
        if (selectedValue.equals(yesButton)) {
            return JOptionPane.YES_OPTION;
        } else if (selectedValue.equals(noButton)) {
            return JOptionPane.NO_OPTION;
        } else {
            return JOptionPane.CANCEL_OPTION;
        }
    }

    private static JButton createButton(String text, Icon icon, Color bgColor) {

        JButton button = new JButton(text, icon);
        button.setFont(Fonts.buttonFont());
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker()),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setIconTextGap(10);
        return button;
    }

    public static String showInputDialog(Component parent, String message, String title) {
        // Créer un panneau avec le message et le champ de saisie
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Colors.CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel(message);
        label.setFont(Fonts.labelFont());
        label.setForeground(Colors.TEXT);
        panel.add(label, BorderLayout.NORTH);

        JTextField textField = new JTextField(20);
        textField.setFont(Fonts.textFieldFont());
        panel.add(textField, BorderLayout.CENTER);

        // Créer les boutons personnalisés
        JButton okButton = createButton("OK", OK_ICON, Colors.PRIMARY);
        JButton cancelButton = createButton("Annuler", CANCEL_ICON, Colors.DANGER);

        Object[] options = {okButton, cancelButton};

        // Créer le JOptionPane
        JOptionPane pane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                QUESTION_ICON,
                options,
                okButton
        );

        JDialog dialog = pane.createDialog(parent, title);
        dialog.getContentPane().setBackground(Colors.UNREAD_NOTIFICATION);

        // Gérer les actions des boutons
        okButton.addActionListener(e -> {
            pane.setValue(okButton);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            pane.setValue(cancelButton);
            dialog.dispose();
        });

        textField.addActionListener(e -> {
            pane.setValue(okButton);
            dialog.dispose();
        });

        dialog.setVisible(true);

        Object result = pane.getValue();
        if (result == okButton) {
            return textField.getText();
        }
        return null;
    }

    public static void showCustomMessageDialog(Component parent, String message, String title, int messageType) {
        Icon icon = null;
        Color bgColor = Colors.CARD_BACKGROUND;

        switch (messageType) {
            case JOptionPane.ERROR_MESSAGE:
                icon = ERROR_ICON;
                bgColor = Colors.ERROR_BACKGROUND;
                break;
            case JOptionPane.WARNING_MESSAGE:
                icon = WARNING_ICON;
                bgColor = Colors.WARNING_BACKGROUND;
                break;
            case JOptionPane.INFORMATION_MESSAGE:
                icon = INFO_ICON;
                bgColor = Colors.UNREAD_NOTIFICATION;
                break;
            case JOptionPane.QUESTION_MESSAGE:
                icon = QUESTION_ICON;
                bgColor = Colors.UNREAD_NOTIFICATION;
                break;
        }

        JOptionPane pane = new JOptionPane(
                createMessagePanel(message),
                messageType,
                JOptionPane.DEFAULT_OPTION,
                icon
        );

        JDialog dialog = pane.createDialog(parent, title);
        dialog.getContentPane().setBackground(bgColor);
        dialog.setVisible(true);
    }

    public static void showCustomDialog(Component parent, String message, String title, Icon icon, Color bgColor) {
        JOptionPane pane = new JOptionPane(
                createMessagePanel(message),
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                icon
        );

        JDialog dialog = pane.createDialog(parent, title);
        dialog.getContentPane().setBackground(bgColor);
        dialog.setVisible(true);
    }

    private static JPanel createMessagePanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Colors.CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel(message);
        label.setFont(Fonts.labelFont());
        label.setForeground(Colors.TEXT);

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private static Icon loadIcon(String path) {
        try {
            return new ImageIcon(CustomDialog.class.getResource(path));
        } catch (Exception e) {
            // Retourner l'icône par défaut si le chargement échoue
            return UIManager.getIcon("OptionPane." +
                    (path.contains("error") ? "errorIcon" :
                            path.contains("warning") ? "warningIcon" :
                                    path.contains("question") ? "questionIcon" : "informationIcon"));
        }
    }

    // Méthode pour afficher un dialogue avec des options personnalisées
    public static int showOptionsDialog(Component parent, String message, String title, String[] options, String defaultOption) {
        // Créer des boutons personnalisés
        JButton[] buttons = new JButton[options.length];
        for (int i = 0; i < options.length; i++) {
            Icon icon = options[i].equalsIgnoreCase("Oui") ? YES_ICON :
                    options[i].equalsIgnoreCase("Non") ? NO_ICON :
                            options[i].equalsIgnoreCase("Annuler") ? CANCEL_ICON : OK_ICON;

            Color color = options[i].equalsIgnoreCase("Oui") ? Colors.SUCCESS :
                    options[i].equalsIgnoreCase("Non") ? Colors.DANGER :
                            options[i].equalsIgnoreCase("Annuler") ? Colors.WARNING : Colors.PRIMARY;

            buttons[i] = createButton(options[i], icon, color);
        }

        JOptionPane pane = new JOptionPane(
                createMessagePanel(message),
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                QUESTION_ICON,
                buttons,
                buttons[0]
        );

        JDialog dialog = pane.createDialog(parent, title);
        dialog.getContentPane().setBackground(Colors.UNREAD_NOTIFICATION);

        // Ajouter les ActionListeners
        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].addActionListener(e -> {
                pane.setValue(buttons[index]);
                dialog.dispose();
            });
        }

        dialog.setVisible(true);

        Object result = pane.getValue();
        if (result == null) {
            return JOptionPane.CLOSED_OPTION;
        }

        for (int i = 0; i < buttons.length; i++) {
            if (result.equals(buttons[i])) {
                return i;
            }
        }

        return JOptionPane.CLOSED_OPTION;
    }

    // Méthode pour afficher un dialogue de saisie avec validation
    public static String showValidatedInputDialog(Component parent, String message, String title, InputValidator validator) {
        String input = null;
        boolean valid = false;

        while (!valid) {
            input = showInputDialog(parent, message, title);

            if (input == null) {
                return null; // L'utilisateur a annulé
            }

            try {
                validator.validate(input);
                valid = true;
            } catch (ValidationException e) {
                showErrorDialog(parent, e.getMessage(), "Erreur de validation");
            }
        }

        return input;
    }

    public interface InputValidator {
        void validate(String input) throws ValidationException;
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}