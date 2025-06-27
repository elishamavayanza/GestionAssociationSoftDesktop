package com.association.dao;

import com.association.model.Entity;

import java.util.Observer;

public interface ObservableDao<T extends Entity> {
    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}