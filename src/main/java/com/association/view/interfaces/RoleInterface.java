package com.association.view.interfaces;

import com.association.view.styles.Colors;

import javax.swing.*;

public interface RoleInterface {
    JFrame createInterface();
    String getRoleName();
    boolean hasAccessToFeature(String featureName);

    default void applyTheme(JFrame frame, boolean darkMode) {
        Colors.setDarkTheme(darkMode);
        SwingUtilities.updateComponentTreeUI(frame);
    }

}