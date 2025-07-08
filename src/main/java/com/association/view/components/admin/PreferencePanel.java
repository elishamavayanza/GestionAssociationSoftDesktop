package com.association.view.components.admin;

import com.association.view.styles.Colors;
import com.association.view.styles.ThemeManager;
import javax.swing.*;
import java.awt.*;

public class PreferencePanel extends JPanel {
    public PreferencePanel(JFrame parentFrame) {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Préférences d'affichage");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel themeLabel = new JLabel("Thème:");

        JComboBox<ThemeManager.Theme> themeComboBox = new JComboBox<>(ThemeManager.Theme.values());
        themeComboBox.addActionListener(e -> {
            ThemeManager.Theme selectedTheme = (ThemeManager.Theme) themeComboBox.getSelectedItem();
            ThemeManager.setTheme(selectedTheme);
            Colors.updateCurrentColors(selectedTheme == ThemeManager.Theme.DARK);
        });

        themePanel.add(themeLabel);
        themePanel.add(themeComboBox);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(themePanel);

        add(contentPanel, BorderLayout.NORTH);
    }
}