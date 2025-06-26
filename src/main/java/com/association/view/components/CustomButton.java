package com.association.view.components;

import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CustomButton extends JButton {
    private Color normalBackground;
    private Color hoverBackground;

    public CustomButton(String text) {
        super(text);
        setPreferredSize(new Dimension(150, 35));
        setFont(new Font("Arial", Font.BOLD, 14));

        this.normalBackground = Colors.PRIMARY;
        this.hoverBackground = Colors.PRIMARY_DARK;

        updateButtonStyle();

        // Effet hover
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverBackground);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalBackground);
            }
        });
    }

    public void setHoverBackground(Color hoverColor) {
        this.hoverBackground = hoverColor;
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (normalBackground == null || !bg.equals(hoverBackground)) {
            this.normalBackground = bg;
        }
    }

    private void updateButtonStyle() {
        setBackground(normalBackground);
        setForeground(Color.WHITE);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);
    }
}