package com.association.view.components.common;

import com.association.view.styles.Fonts;
import com.association.view.styles.Colors;
import org.jdesktop.swingx.JXDatePicker;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Date;

public class JDateChooser extends JPanel {
    private JXDatePicker datePicker;
    private boolean darkMode = false;
    private Color borderColor = Colors.BORDER;
    private int borderRadius = 5;
    private Color highlightColor = Colors.PRIMARY;

    public JDateChooser() {
        initComponents();
        updateTheme();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        datePicker = new JXDatePicker();
        datePicker.setFormats("dd/MM/yyyy");
        datePicker.getEditor().setEditable(false);
        datePicker.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Style personnalisé
        updateComponentStyles();

        add(datePicker, BorderLayout.CENTER);
    }


    private void updateComponentStyles() {
        Color bg = darkMode ? Colors.DARK_INPUT_BACKGROUND : Colors.INPUT_BACKGROUND;
        Color fg = darkMode ? Colors.DARK_TEXT : Colors.TEXT;

        // Style de l'éditeur
        JComponent editor = datePicker.getEditor();
        editor.setBackground(bg);
        editor.setForeground(fg);
        editor.setFont(Fonts.textFieldFont());
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Style général
        datePicker.setBackground(bg);
        datePicker.setForeground(fg);
        datePicker.setFont(Fonts.textFieldFont());

        // Style du bouton (approche plus robuste)
        try {
            Component[] comps = datePicker.getComponents();
            for (Component comp : comps) {
                if (comp instanceof JButton button) {
                    button.setBackground(highlightColor);
                    button.setForeground(Color.WHITE);
                    button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                }
            }
        } catch (Exception e) {
            // Fallback si la structure change
            System.err.println("Could not style date picker button: " + e.getMessage());
        }
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        updateTheme();
    }

    private void updateTheme() {
        borderColor = darkMode ? Colors.DARK_BORDER : Colors.BORDER;
        highlightColor = darkMode ? Colors.DARK_PRIMARY : Colors.PRIMARY;
        updateComponentStyles();
        repaint();
    }

    public void setBorderRadius(int radius) {
        this.borderRadius = radius;
        repaint();
    }

    public void setHighlightColor(Color color) {
        this.highlightColor = color;
        updateComponentStyles();
    }

    public void setDateFormatString(String format) {
        datePicker.setFormats(format);
    }

    public Date getDate() {
        return datePicker.getDate();
    }

    public void setDate(Date date) {
        Date oldValue = getDate();
        datePicker.setDate(date);
        firePropertyChange("date", oldValue, date);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        datePicker.addPropertyChangeListener("date", listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = darkMode ? Colors.DARK_INPUT_BACKGROUND : Colors.INPUT_BACKGROUND;
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), borderRadius, borderRadius);

        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, borderRadius, borderRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}