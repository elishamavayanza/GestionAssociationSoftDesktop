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

    public Notification(String action, String message, String type) {
        this.action = action;
        this.message = message;
        this.type = type;
        this.date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
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
        return action + " - " + date + " : " + message;
    }

    @Override
    public String toString() {
        return getDisplayText();
    }

}
