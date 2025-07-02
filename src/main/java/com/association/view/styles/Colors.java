package com.association.view.styles;

import java.awt.*;

public class Colors {
    // Light Theme (default)
    public static final Color PRIMARY = new Color(0, 123, 255);
    public static final Color PRIMARY_DARK = new Color(0, 86, 179);
    public static final Color SECONDARY = new Color(108, 117, 125);
    public static final Color SUCCESS = new Color(40, 167, 69);
    public static final Color DANGER = new Color(220, 53, 69);
    public static final Color WARNING = new Color(255, 193, 7);
    public static final Color INFO = new Color(23, 162, 184);

    public static final Color BACKGROUND = Color.WHITE;
    public static final Color CARD_BACKGROUND = new Color(248, 249, 250);
    public static final Color INPUT_BACKGROUND = new Color(240, 240, 240);
    public static final Color TEXT = new Color(51, 51, 51);
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    public static final Color BORDER = new Color(206, 212, 218);

    // Dark Theme
    public static final Color DARK_PRIMARY = new Color(10, 132, 255);
    public static final Color DARK_PRIMARY_DARK = new Color(0, 96, 189);
    public static final Color DARK_SECONDARY = new Color(152, 161, 169);
    public static final Color DARK_SUCCESS = new Color(48, 175, 79);
    public static final Color DARK_DANGER = new Color(230, 63, 79);
    public static final Color DARK_WARNING = new Color(255, 203, 27);
    public static final Color DARK_INFO = new Color(33, 172, 194);

    public static final Color DARK_BACKGROUND = new Color(36, 36, 36);
    public static final Color DARK_CARD_BACKGROUND = new Color(50, 50, 50);
    public static final Color DARK_INPUT_BACKGROUND = new Color(60, 60, 60);
    public static final Color DARK_TEXT = new Color(240, 240, 240);
    public static final Color DARK_TEXT_SECONDARY = new Color(180, 180, 180);
    public static final Color DARK_BORDER = new Color(80, 80, 80);

    public static final Color ERROR_BACKGROUND = new Color(255, 235, 238);
    public static final Color WARNING_BACKGROUND = new Color(255, 249, 196);
    public static final Color SUCCESS_BACKGROUND = new Color(237, 247, 237);

    // Dans la classe Colors
    public static final Color UNREAD_NOTIFICATION = new Color(0, 100, 200); // Bleu pour les non-lues
    public static Color CURRENT_UNREAD_NOTIFICATION = UNREAD_NOTIFICATION; // Par défaut
    // Dans Colors.java
    public static final Color SELECTION_BACKGROUND = new Color(0, 123, 255, 50);
    // Utility method to toggle between themes
    public static void setDarkTheme(boolean enabled) {
        if (enabled) {
            CURRENT_UNREAD_NOTIFICATION = new Color(100, 180, 255); // Bleu clair pour thème sombre
            CURRENT_PRIMARY = DARK_PRIMARY;
            CURRENT_PRIMARY_DARK = DARK_PRIMARY_DARK;
            CURRENT_SECONDARY = DARK_SECONDARY;
            CURRENT_SUCCESS = DARK_SUCCESS;
            CURRENT_DANGER = DARK_DANGER;
            CURRENT_WARNING = DARK_WARNING;
            CURRENT_INFO = DARK_INFO;
            CURRENT_BACKGROUND = DARK_BACKGROUND;
            CURRENT_CARD_BACKGROUND = DARK_CARD_BACKGROUND;
            CURRENT_INPUT_BACKGROUND = DARK_INPUT_BACKGROUND;
            CURRENT_TEXT = DARK_TEXT;
            CURRENT_TEXT_SECONDARY = DARK_TEXT_SECONDARY;
            CURRENT_BORDER = DARK_BORDER;
        } else {
            CURRENT_UNREAD_NOTIFICATION = UNREAD_NOTIFICATION;

            CURRENT_PRIMARY = PRIMARY;
            CURRENT_PRIMARY_DARK = PRIMARY_DARK;
            CURRENT_SECONDARY = SECONDARY;
            CURRENT_SUCCESS = SUCCESS;
            CURRENT_DANGER = DANGER;
            CURRENT_WARNING = WARNING;
            CURRENT_INFO = INFO;
            CURRENT_BACKGROUND = BACKGROUND;
            CURRENT_CARD_BACKGROUND = CARD_BACKGROUND;
            CURRENT_INPUT_BACKGROUND = INPUT_BACKGROUND;
            CURRENT_TEXT = TEXT;
            CURRENT_TEXT_SECONDARY = TEXT_SECONDARY;
            CURRENT_BORDER = BORDER;
        }
    }

    // Current theme colors (default to light theme)
    public static Color CURRENT_PRIMARY = PRIMARY;
    public static Color CURRENT_PRIMARY_DARK = PRIMARY_DARK;
    public static Color CURRENT_SECONDARY = SECONDARY;
    public static Color CURRENT_SUCCESS = SUCCESS;
    public static Color CURRENT_DANGER = DANGER;
    public static Color CURRENT_WARNING = WARNING;
    public static Color CURRENT_INFO = INFO;
    public static Color CURRENT_BACKGROUND = BACKGROUND;
    public static Color CURRENT_CARD_BACKGROUND = CARD_BACKGROUND;
    public static Color CURRENT_INPUT_BACKGROUND = INPUT_BACKGROUND;
    public static Color CURRENT_TEXT = TEXT;
    public static Color CURRENT_TEXT_SECONDARY = TEXT_SECONDARY;
    public static Color CURRENT_BORDER = BORDER;
}