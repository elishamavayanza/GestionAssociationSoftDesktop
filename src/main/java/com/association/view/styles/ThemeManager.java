package com.association.view.styles;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    public enum Theme {
        LIGHT("Clair"),
        DARK("Sombre"),
        CUSTOM("Personnalisé");

        private final String displayName;

        Theme(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static void setTheme(Theme theme) {
        try {
            switch (theme) {
                case LIGHT:
                    UIManager.setLookAndFeel(new FlatMacLightLaf());
                    applyCustomOverrides(); // Applique vos couleurs personnalisées
                    break;
                case DARK:
                    UIManager.setLookAndFeel(new FlatMacDarkLaf());
                    applyCustomOverrides(); // Applique vos couleurs personnalisées
                    break;
                case CUSTOM:
                    applyFullCustomTheme(); // Applique uniquement votre thème
                    break;
            }
            updateAllComponents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyFullCustomTheme() {
        // Désactive FlatLaf pour revenir au look par défaut
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Applique toutes vos propriétés personnalisées
        UIManager.put("Panel.background", Colors.SECONDARY);
        UIManager.put("Button.background", Colors.PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("ScrollBar.thumb", Colors.PRIMARY);
        UIManager.put("ScrollBar.track", Colors.SECONDARY.darker());
        // Ajoutez d'autres propriétés selon vos besoins
    }

    private static void applyCustomOverrides() {
        // Surcharge certaines propriétés de FlatLaf avec vos couleurs
        UIManager.put("Panel.background", Colors.CURRENT_BACKGROUND);
        UIManager.put("Button.background", Colors.CURRENT_PRIMARY);
        UIManager.put("Button.foreground", Colors.CURRENT_TEXT);
        UIManager.put("ScrollBar.thumb", Colors.CURRENT_PRIMARY);
        UIManager.put("ScrollBar.track", Colors.CURRENT_SECONDARY.darker());
        // Ajoutez d'autres surcharges selon vos besoins
    }

    private static void updateAllComponents() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}