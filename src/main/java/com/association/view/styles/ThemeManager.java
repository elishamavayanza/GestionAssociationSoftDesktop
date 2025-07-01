package com.association.view.styles;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static ThemeManager instance;
    private boolean darkMode = false;
    private Map<JFrame, Map<String, JComponent>> registeredComponents = new HashMap<>();

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void toggleTheme() {
        darkMode = !darkMode;
        Colors.setDarkTheme(darkMode);
        applyThemeToAllRegisteredFrames();
    }

    public void registerFrame(JFrame frame, Map<String, JComponent> components) {
        registeredComponents.put(frame, components);
        applyTheme(frame);
    }

    public void unregisterFrame(JFrame frame) {
        registeredComponents.remove(frame);
    }

    private void applyThemeToAllRegisteredFrames() {
        registeredComponents.keySet().forEach(this::applyTheme);
    }

    private void applyTheme(JFrame frame) {
        Map<String, JComponent> components = registeredComponents.get(frame);
        if (components != null) {
            applyTheme(components);
        }
    }

    public void applyTheme(Map<String, JComponent> components) {
        Color bgColor = darkMode ? Colors.DARK_BACKGROUND : Colors.BACKGROUND;
        Color fgColor = darkMode ? Colors.DARK_TEXT : Colors.TEXT;
        Color borderColor = darkMode ? Colors.DARK_BORDER : Colors.BORDER;

        components.forEach((key, component) -> {
            component.setBackground(bgColor);
            component.setForeground(fgColor);

            if (component instanceof JPanel) {
                component.setBackground(bgColor);

                // Gestion des bordures titrées
                if (component.getBorder() instanceof TitledBorder) {
                    TitledBorder border = (TitledBorder) component.getBorder();
                    border.setTitleColor(darkMode ? Colors.DARK_PRIMARY : Colors.PRIMARY);
                } else {
                    component.setBorder(BorderFactory.createLineBorder(borderColor));
                }
            }

            // Gestion spécifique pour différents composants
            if (component instanceof JTextField || component instanceof JComboBox) {
                component.setBackground(darkMode ? Colors.DARK_INPUT_BACKGROUND : Colors.INPUT_BACKGROUND);
                component.setForeground(fgColor);
            } else if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getText().equals("Rechercher")) {
                    button.setBackground(darkMode ? Colors.DARK_PRIMARY : Colors.PRIMARY);
                } else if (button.getText().equals("Réinitialiser")) {
                    button.setBackground(darkMode ? Colors.DARK_SECONDARY : Colors.SECONDARY);
                }
                button.setForeground(Color.WHITE);
            }
        });
    }

    public boolean isDarkMode() {
        return darkMode;
    }
}