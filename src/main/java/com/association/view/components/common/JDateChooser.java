package com.association.view.components.common;

import com.association.view.styles.Fonts;
import org.jdesktop.swingx.JXDatePicker;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Date;

public class JDateChooser extends JPanel {
    private JXDatePicker datePicker;

    public JDateChooser() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        datePicker = new JXDatePicker();
        datePicker.setFormats("dd/MM/yyyy");
        datePicker.getEditor().setEditable(false); // Empêche la saisie manuelle

        // Style optionnel
        datePicker.getEditor().setBackground(Color.WHITE);
        datePicker.getEditor().setFont(Fonts.textFieldFont());

        add(datePicker, BorderLayout.CENTER);
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
}