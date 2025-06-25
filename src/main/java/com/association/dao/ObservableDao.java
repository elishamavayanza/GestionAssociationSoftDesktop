package com.association.dao;

import java.util.Observer;

public interface ObservableDao<T> {
    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}