package com.association.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class IconTextToggleManager {
    private final JPanel sidePanel;
    private boolean isTextVisible = false;
    private final List<JButton> menuButtons = new ArrayList<>();
    private final List<String> buttonTexts = new ArrayList<>();

    public IconTextToggleManager(JPanel sidePanel) {
        this.sidePanel = sidePanel;
    }

    public JButton createToggleButton() {
        JButton toggleButton = IconManager.createIconButton("menu.svg", "Afficher/Masquer les textes", 30);
        toggleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        toggleButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Utiliser les mêmes dimensions fixes que les autres boutons
        if (isTextVisible) {
            toggleButton.setPreferredSize(new Dimension(180, 50));
            toggleButton.setMinimumSize(new Dimension(180, 50));
            toggleButton.setMaximumSize(new Dimension(180, 50));
        } else {
            toggleButton.setPreferredSize(new Dimension(60, 50));
            toggleButton.setMinimumSize(new Dimension(60, 50));
            toggleButton.setMaximumSize(new Dimension(60, 50));
        }

        toggleButton.addActionListener(e -> toggleTextVisibility());

        return toggleButton;
    }
    public void addMenuButton(JButton button, String text) {
        menuButtons.add(button);
        buttonTexts.add(text);
        configureButton(button);
    }

    private void configureButton(JButton button) {
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setText(isTextVisible ? buttonTexts.get(menuButtons.indexOf(button)) : "");

        // Ajuster la taille pour accommoder le texte si nécessaire
        if (isTextVisible) {
            button.setPreferredSize(new Dimension(180, 50));
            button.setMinimumSize(new Dimension(180, 50));
            button.setMaximumSize(new Dimension(180, 50));
        } else {
            button.setPreferredSize(new Dimension(60, 50));
            button.setMinimumSize(new Dimension(60, 50));
            button.setMaximumSize(new Dimension(60, 50));
        }
    }

    private void toggleTextVisibility() {
        isTextVisible = !isTextVisible;

        Dimension originalSize = sidePanel.getPreferredSize();

        // Mettre à jour tous les boutons y compris le toggle
        for (int i = 0; i < menuButtons.size(); i++) {
            JButton button = menuButtons.get(i);
            updateButtonSizeAndText(button, i);
        }

        // Mettre à jour la taille du panel latéral
        if (isTextVisible) {
            sidePanel.setPreferredSize(new Dimension(200, originalSize.height));
        } else {
            sidePanel.setPreferredSize(new Dimension(80, originalSize.height));
        }

        sidePanel.revalidate();
        sidePanel.repaint();
    }

    private void updateButtonSizeAndText(JButton button, int index) {
        if (isTextVisible) {
            button.setText(buttonTexts.get(index));
            button.setPreferredSize(new Dimension(180, 50));
            button.setMinimumSize(new Dimension(180, 50));
            button.setMaximumSize(new Dimension(180, 50));
        } else {
            button.setText("");
            button.setPreferredSize(new Dimension(60, 50));
            button.setMinimumSize(new Dimension(60, 50));
            button.setMaximumSize(new Dimension(60, 50));
        }
    }
}