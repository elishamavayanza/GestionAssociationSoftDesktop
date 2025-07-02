package com.association.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private String action;
    private String message;
    private String type;
    private String date;
    private boolean read;


    public Notification(String action, String message, String type) {
        this.action = action;
        this.message = message;
        this.type = type;
        this.date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        this.read = false;

    }

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public String getDisplayText() {
        String displayText = action + " - " + date + " : " + message;
        if (!read) {
            displayText = "● " + displayText; // Ajoute un point pour les non lues
        }
        return displayText;
    }

    @Override
    public String toString() {
        return getDisplayText();
    }
    public boolean isRead() {
        return read;
    }

    public void markAsRead() {
        this.read = true;
    }

}
