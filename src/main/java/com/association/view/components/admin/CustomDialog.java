package com.association.view.components.admin;

import com.association.view.styles.Fonts;

import javax.swing.*;
import java.awt.*;

public class CustomDialog {
    public static int showCustomOptionDialog(Component parent, String title, String message,
                                             String[] options, String[] buttonTexts,
                                             Icon[] icons, Color[] colors) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Message
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(Fonts.labelFont());
        panel.add(messageLabel, BorderLayout.CENTER);

        // Options
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton[] buttons = new JButton[options.length];

        for (int i = 0; i < options.length; i++) {
            buttons[i] = new JButton(buttonTexts[i]);
            buttons[i].setFont(Fonts.buttonFont());
            buttons[i].setBackground(colors[i]);
            buttons[i].setForeground(Color.WHITE);
            buttons[i].setFocusPainted(false);
            buttons[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(colors[i].darker()),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)));

            if (icons[i] != null) {
                buttons[i].setIcon(icons[i]);
            }

            final int option = i;
            buttons[i].addActionListener(e -> {
                ((Window)SwingUtilities.getRoot((Component)e.getSource())).dispose();
                ((JOptionPane)SwingUtilities.getAncestorOfClass(JOptionPane.class, (Component)e.getSource()))
                        .setValue(option);
            });

            optionsPanel.add(buttons[i]);
        }

        panel.add(optionsPanel, BorderLayout.SOUTH);

        return JOptionPane.showOptionDialog(parent, panel, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
    }
}