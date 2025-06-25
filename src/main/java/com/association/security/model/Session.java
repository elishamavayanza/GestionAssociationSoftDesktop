package com.association.security.model;

import java.util.Date;

public class Session {
    private String id; // UUID
    private Utilisateur utilisateur;
    private Date startTime;
    private Date lastAccess;
    private String ipAddress;
    private boolean isExpired = false;

    public boolean isValid() {
        return !isExpired && (new Date().getTime() - lastAccess.getTime()) < 30 * 60 * 1000;
    }

    public void refresh() {
        this.lastAccess = new Date();
    }

    // Getters et Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }
}
