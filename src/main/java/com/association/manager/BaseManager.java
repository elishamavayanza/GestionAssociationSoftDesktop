package com.association.manager;

import com.association.dao.GenericDao;
import java.util.List;
import java.util.Optional;

public abstract class BaseManager<T> {
    protected GenericDao<T> dao;

    public BaseManager(GenericDao<T> dao) {
        this.dao = dao;
    }

    public boolean create(T t) {
        return dao.create(t);
    }

    public boolean update(T t) {
        return dao.update(t);
    }

    public boolean delete(Long id) {
        return dao.delete(id);
    }

    public Optional<T> findById(Long id) {
        return dao.findById(id);
    }

    public List<T> findAll() {
        return dao.findAll();
    }

    public long count() {
        return dao.count();
    }

    public boolean existsById(Long id) {
        return dao.existsById(id);
    }
}