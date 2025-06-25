package com.association.dao.impl;

import com.association.dao.RapportDao;
import com.association.model.Rapport;
import com.association.model.enums.TypeRapport;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RapportDaoImpl extends GenericDaoImpl<Rapport> implements RapportDao {
    public RapportDaoImpl() {
        super("rapports");
    }

    @Override
    protected Rapport mapResultSetToEntity(ResultSet rs) throws SQLException {
        Rapport rapport = new Rapport();
        rapport.setId(rs.getLong("id"));
        // Remplir les autres propriétés
        return rapport;
    }

    @Override
    public List<Rapport> findByType(TypeRapport type) {
        List<Rapport> rapports = new ArrayList<>();
        String sql = "SELECT * FROM rapports WHERE type = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rapports.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rapports;
    }

    @Override
    public List<Rapport> findByDate(Date date) {
        List<Rapport> rapports = new ArrayList<>();
        String sql = "SELECT * FROM rapports WHERE DATE(date_generation) = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rapports.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rapports;
    }

    // Implémentations des autres méthodes de GenericDao
    @Override
    public boolean create(Rapport t) { return false; }
    @Override
    public boolean update(Rapport t) { return false; }
    @Override
    public boolean delete(Long id) { return false; }
    @Override
    public boolean saveAll(Iterable<Rapport> entities) { return false; }
}