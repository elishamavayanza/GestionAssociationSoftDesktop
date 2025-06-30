package com.association.view.components;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

public class JDateChooser extends JPanel {
    private JTextField dateField;
    private JButton chooseButton;
    private Date selectedDate;

    public JDateChooser() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        dateField = new JTextField(10);
        dateField.setEditable(false);
        add(dateField, BorderLayout.CENTER);

        chooseButton = new JButton("...");
        chooseButton.addActionListener(e -> showDatePicker());
        add(chooseButton, BorderLayout.EAST);
    }

    private void showDatePicker() {
        // Implémentation basique - vous pouvez utiliser JCalendar ou autre bibliothèque ici
        // Pour l'exemple, nous utilisons une boîte de dialogue simple
        String dateStr = JOptionPane.showInputDialog(this, "Entrez la date (jj/mm/aaaa):");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                String[] parts = dateStr.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int year = Integer.parseInt(parts[2]);

                // Note: Cette implémentation est simplifiée, utilisez une vraie date picker en production
                setDate(new Date(year - 1900, month, day));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Format de date invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setDateFormatString(String format) {
        // Format non implémenté dans cette version simplifiée
    }

    public Date getDate() {
        return selectedDate;
    }

    public void setDate(Date date) {
        Date oldValue = this.selectedDate;
        this.selectedDate = date;

        if (date != null) {
            dateField.setText(String.format("%02d/%02d/%04d",
                    date.getDate(), date.getMonth() + 1, date.getYear() + 1900));
        } else {
            dateField.setText("");
        }

        firePropertyChange("date", oldValue, date);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
    }
}