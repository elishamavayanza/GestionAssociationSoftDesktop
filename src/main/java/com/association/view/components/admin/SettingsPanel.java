package com.association.view.components.admin;

import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
import com.association.view.styles.ThemeManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final ThemeManager themeManager;

    public SettingsPanel() {
        themeManager = ThemeManager.getInstance();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.CURRENT_BACKGROUND);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Colors.CURRENT_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Paramètres de l'application",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                Fonts.buttonFont(),
                Colors.CURRENT_PRIMARY
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Option Mode Sombre
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel themeLabel = new JLabel("Mode d'affichage:");
        themeLabel.setFont(Fonts.labelFont());
        themeLabel.setForeground(Colors.CURRENT_TEXT);
        contentPanel.add(themeLabel, gbc);

        gbc.gridx = 1;
        JToggleButton themeToggle = new JToggleButton("Mode Sombre");
        themeToggle.setFont(Fonts.buttonFont());
        themeToggle.setSelected(themeManager.isDarkMode());
        themeToggle.addActionListener(e -> {
            themeManager.toggleTheme();
            updateTheme();
        });
        contentPanel.add(themeToggle, gbc);

        add(contentPanel, BorderLayout.NORTH);
    }

    private void updateTheme() {
        setBackground(Colors.CURRENT_BACKGROUND);
        repaint();
    }
}