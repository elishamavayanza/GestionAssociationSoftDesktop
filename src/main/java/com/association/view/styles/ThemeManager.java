package com.association.view.styles;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ThemeManager {
    private boolean darkMode = false;

    public void toggleTheme() {
        darkMode = !darkMode;
        Colors.setDarkTheme(darkMode);
    }

    public void applyTheme(Map<String, JComponent> components) {
        Color bgColor = darkMode ? Colors.DARK_BACKGROUND : Colors.BACKGROUND;
        Color fgColor = darkMode ? Colors.DARK_TEXT : Colors.TEXT;

        components.forEach((key, component) -> {
            component.setBackground(bgColor);
            component.setForeground(fgColor);

            if (component instanceof JPanel) {
                ((JPanel)component).setBorder(BorderFactory.createLineBorder(
                        darkMode ? Colors.DARK_BORDER : Colors.BORDER));
            }

            // Gestion spécifique pour différents composants
            if (key.equals("headerPanel")) {
                component.setBackground(darkMode ? Colors.DARK_PRIMARY : Colors.PRIMARY);
            } else if (key.equals("sidePanel")) {
                component.setBackground(darkMode ? Colors.DARK_SECONDARY : Colors.SECONDARY);
            }
        });
    }

    public boolean isDarkMode() {
        return darkMode;
    }
}