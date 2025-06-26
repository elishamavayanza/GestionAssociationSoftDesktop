package com.association.view.components;

import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomButton extends JButton {
    public CustomButton(String text) {
        super(text);
        setPreferredSize(new Dimension(150, 35));
        setFont(new Font("Arial", Font.BOLD, 14));

        setBackground(Colors.PRIMARY);
        setForeground(Color.WHITE);
        setBorderPainted(false);
        setFocusPainted(false);

        // Effet hover
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(Colors.PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Colors.PRIMARY);
            }
        });
    }
}