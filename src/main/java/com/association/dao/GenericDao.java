package com.association.dao;

import com.association.model.Entity;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T extends Entity> extends ObservableDao<T> {
    boolean create(T t);
    boolean update(T t);
    boolean delete(Long id);
    Optional<T> findById(Long id);
    List<T> findAll();
    long count();
    boolean existsById(Long id);
    boolean saveAll(Iterable<T> entities);
}