package com.association.util.listener;

import com.association.model.Entity;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.PreRemove;
import java.util.Date;

public class AuditListener {

    @PrePersist
    public void prePersist(Entity entity) {
        entity.setDateCreation(new Date());
        System.out.println("Entity persisted: " + entity.getClass().getSimpleName());
    }

    @PreUpdate
    public void preUpdate(Entity entity) {
        System.out.println("Entity updated: " + entity.getClass().getSimpleName());
    }

    @PreRemove
    public void preRemove(Entity entity) {
        System.out.println("Entity removed: " + entity.getClass().getSimpleName());
    }
}
