package com.association.view.components;

import com.association.view.styles.Colors;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class CustomTextField extends JTextField {
    public CustomTextField() {
        setPreferredSize(new Dimension(200, 30));
        setFont(new Font("Arial", Font.PLAIN, 14));

        // Bordure personnalisée
        Border line = new LineBorder(Colors.BORDER);
        Border margin = new EmptyBorder(5, 5, 5, 5);
        setBorder(new CompoundBorder(line, margin));

        setBackground(Colors.INPUT_BACKGROUND);
        setForeground(Colors.TEXT);
    }
}