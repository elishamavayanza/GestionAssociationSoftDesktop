package com.association.model.media;

import java.util.Date;
import com.association.model.Entity;

public class MediaFile extends Entity {
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private Date dateUpload;
    private String entityType;
    private Long entityId;

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Date getDateUpload() {
        return dateUpload;
    }

    public void setDateUpload(Date dateUpload) {
        this.dateUpload = dateUpload;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    // Methods
    public boolean compressImage() {
        // Implémentation pour compresser l'image
        return true;
    }

    public boolean generateThumbnail() {
        // Implémentation pour générer une miniature
        return true;
    }

    public boolean deleteFile() {
        // Implémentation pour supprimer le fichier
        return true;
    }

    public String getPublicUrl() {
        return this.filePath;
    }
}