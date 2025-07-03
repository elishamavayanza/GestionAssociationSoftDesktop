package com.association.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private String action;
    private String message;
    private String type;
    private Date date;  // Stocké comme Date
    private boolean read;

    public Notification(String action, String message, String type) {
        this.action = action;
        this.message = message;
        this.type = type;
        this.date = new Date();
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

    // Changé pour retourner Date au lieu de String
    public Date getDate() {
        return date;
    }

    public String getDisplayText() {
        String prefix = switch (action) {
            case "AJOUT" -> "[Ajout] ";
            case "SUPPRESSION" -> "[Suppression] ";
            case "MODIFICATION" -> "[Modification] ";
            default -> "";
        };

        return prefix + message + " - " + new SimpleDateFormat("HH:mm").format(date);
    }

    // Pour obtenir la date formatée en String
    public String getFormattedDate() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
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