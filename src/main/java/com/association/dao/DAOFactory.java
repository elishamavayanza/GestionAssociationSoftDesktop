package com.association.dao;

import com.association.model.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class DAOFactory {

    private static DAOFactory instance;
    private final Map<String, ObservableDao<?>> interfaces = new HashMap<>();


    private DAOFactory () {

    }


    public static DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    public static  <T extends ObservableDao<H>, H extends Entity> T getInstance(Class<?> clazz) {
        return getInstance().get(clazz);
    }

    public synchronized <T extends ObservableDao<H>, H extends Entity> T get (Class<?> clazz) {
        String className = clazz.getSimpleName();

        ObservableDao<?> observableDao = interfaces.get(className);
        if (observableDao == null) {
            String implementation = String.format("%sImpl", clazz.getName());
            try {
                Class<?> impl = Class.forName(implementation);
                Constructor<?> constructor = impl.getConstructor();

                T ins = (T) constructor.newInstance();
                interfaces.put(className, ins);
                return ins;
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return (T) observableDao;
    }
}
