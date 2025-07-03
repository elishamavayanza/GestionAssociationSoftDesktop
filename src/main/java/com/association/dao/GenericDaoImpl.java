package com.association.dao;

import com.association.config.DatabaseConfig;
import com.association.model.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

abstract class GenericDaoImpl<T extends Entity> extends Observable implements GenericDao<T> {
    protected final DatabaseConfig databaseConfig;
    protected final String tableName;

    public GenericDaoImpl(String tableName) {
        this.databaseConfig = DatabaseConfig.getInstance();
        this.tableName = tableName;
    }

    @Override
    public void notifyObservers(Object arg) {
        setChanged();
        super.notifyObservers(arg);
    }

    public void notifyObservers(T entity) {
        notifyObservers((Object)entity);
    }

    public void notifyObservers(Long id) {
        notifyObservers((Object)id);
    }

    @Override
    public Optional<T> findById(Long id) {
        String sql = """
        SELECT e.id, e.date_creation, p.nom, p.contact, p.photo_path,
               m.date_inscription, m.statut
        FROM entities e
        JOIN personnes p ON e.id = p.id
        JOIN membres m ON p.id = m.id
        WHERE e.id = ?
    """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    @Override
    public List<T> findAll() {
        if (tableName.equals("membres")) {
            // Special handling for Membre
            List<T> entities = new ArrayList<>();
            String sql = "SELECT e.id, e.date_creation, p.nom, p.contact, p.photo_path, "
                    + "m.date_inscription, m.statut "
                    + "FROM entities e "
                    + "JOIN personnes p ON e.id = p.id "
                    + "JOIN membres m ON p.id = m.id";

            try (Connection conn = databaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapResultSetToEntity(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return entities;
        } else {
            // Default implementation for other entities
            List<T> entities = new ArrayList<>();
            String sql = "SELECT * FROM " + tableName;
            try (Connection conn = databaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapResultSetToEntity(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return entities;
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE id = ? LIMIT 1";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public void removeObserver(Observer o) {

    }
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;


}