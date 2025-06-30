package com.association.view.styles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HoverButton extends JButton {
    private Color normalBackground;
    private Color hoverBackground;
    private Color clickedBackground;
    private Color doubleClickedBackground; // Nouvelle couleur pour double-clic
    private Runnable doubleClickAction;
    private boolean isSelected = false;
    private boolean isDoubleClicked = false; // Nouvel état pour double-clic

    public HoverButton(String text, Icon icons, Color normalBg, Color hoverBg) {
        this(text, icons, normalBg, hoverBg, null, null);
    }

    public HoverButton(String text, Icon icons, Color normalBg, Color hoverBg, Color clickedBg) {
        this(text, icons, normalBg, hoverBg, clickedBg, null);
    }

    // Nouveau constructeur avec couleur pour double-clic
    public HoverButton(String text, Icon icons, Color normalBg, Color hoverBg,
                       Color clickedBg, Color doubleClickedBg) {
        super(text, icons);
        this.normalBackground = normalBg;
        this.hoverBackground = hoverBg;
        this.clickedBackground = clickedBg != null ? clickedBg : normalBg;
        this.doubleClickedBackground = doubleClickedBg != null ? doubleClickedBg : clickedBackground;
        setOpaque(true);
        setBorderPainted(false);
        setBackground(normalBg);
        setHorizontalAlignment(SwingConstants.LEFT);
        setHorizontalTextPosition(SwingConstants.RIGHT);
        setFocusPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (!isSelected && !isDoubleClicked) {
                    setBackground(hoverBackground);
                }
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                if (!isSelected && !isDoubleClicked) {
                    setBackground(normalBackground);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && doubleClickAction != null) {
                    setDoubleClickedState(true);
                    doubleClickAction.run();
                } else if (e.getClickCount() == 1) {
                    setSelectedState(true);
                }
            }
        });
    }

    public void setDoubleClickAction(Runnable action) {
        this.doubleClickAction = action;
    }

    public void setClickedBackground(Color color) {
        this.clickedBackground = color;
    }

    public void setDoubleClickedBackground(Color color) {
        this.doubleClickedBackground = color;
    }

    public void setSelectedState(boolean selected) {
        this.isSelected = selected;
        this.isDoubleClicked = false; // Réinitialiser l'état de double-clic
        setBackground(selected ? clickedBackground : normalBackground);
    }

    public void setDoubleClickedState(boolean doubleClicked) {
        this.isDoubleClicked = doubleClicked;
        this.isSelected = false; // Réinitialiser l'état de sélection simple
        setBackground(doubleClicked ? doubleClickedBackground : normalBackground);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isDoubleClicked() {
        return isDoubleClicked;
    }

    public void resetState() {
        this.isSelected = false;
        this.isDoubleClicked = false;
        setBackground(normalBackground);
    }

//    public void setDoubleClickAction(Runnable action) {
//        this.doubleClickAction = action;
//    }
//
//    public void setClickedBackground(Color color) {
//        this.clickedBackground = color;
//    }
//
//    // Nouvelle méthode pour définir l'état sélectionné
//    public void setSelectedState(boolean selected) {
//        this.isSelected = selected;
//        setBackground(selected ? clickedBackground : normalBackground);
//    }
//
//    // Nouvelle méthode pour vérifier l'état sélectionné
//    public boolean isSelected() {
//        return isSelected;
//    }
}