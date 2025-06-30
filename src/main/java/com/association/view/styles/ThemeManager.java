package com.association.view.styles;

import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    private boolean darkMode = false;

    public void toggleTheme() {
        darkMode = !darkMode;
        Colors.setDarkTheme(darkMode);
    }

    public void applyTheme(Component component) {
        if (component instanceof JFrame) {
            ((JFrame) component).getContentPane().setBackground(Colors.CURRENT_BACKGROUND);
        }

        // Parcourir tous les composants pour appliquer le thème
        applyThemeToComponent(component);
    }

    private void applyThemeToComponent(Component component) {
        if (component instanceof JPanel) {
            component.setBackground(Colors.CURRENT_BACKGROUND);
            ((JPanel) component).setOpaque(true);
        } else if (component instanceof JLabel) {
            ((JLabel) component).setForeground(Colors.CURRENT_TEXT);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setBackground(Colors.CURRENT_PRIMARY);
            button.setForeground(Color.WHITE);
        }

        // Appliquer récursivement aux enfants
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyThemeToComponent(child);
            }
        }
    }
}