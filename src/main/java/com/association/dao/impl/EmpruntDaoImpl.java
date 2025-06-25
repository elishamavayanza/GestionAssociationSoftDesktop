package com.association.dao.impl;

import com.association.dao.EmpruntDao;
import com.association.model.transaction.Emprunt;
import com.association.model.enums.StatutEmprunt;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpruntDaoImpl extends GenericDaoImpl<Emprunt> implements EmpruntDao {
    public EmpruntDaoImpl() {
        super("emprunts");
    }

    @Override
    protected Emprunt mapResultSetToEntity(ResultSet rs) throws SQLException {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(rs.getLong("id"));
        // Remplir les autres propriétés
        return emprunt;
    }

    @Override
    public List<Emprunt> findByMembre(Long membreId) {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT e.* FROM emprunts e " +
                "JOIN transactions t ON e.id = t.id " +
                "WHERE t.membre_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, membreId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                emprunts.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emprunts;
    }

    @Override
    public List<Emprunt> findByStatut(StatutEmprunt statut) {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT * FROM emprunts WHERE statut = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                emprunts.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emprunts;
    }

    @Override
    public BigDecimal calculerSoldeRestant(Long empruntId) {
        String sql = "SELECT (t.montant - e.montant_rembourse) AS solde " +
                "FROM transactions t " +
                "JOIN emprunts e ON t.id = e.id " +
                "WHERE e.id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, empruntId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("solde");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean verifierEligibilite(Long membreId) {
        // Implémentation de la logique de vérification d'éligibilité
        return true;
    }

    // Implémentations des autres méthodes de GenericDao
    @Override
    public boolean create(Emprunt t) { return false; }
    @Override
    public boolean update(Emprunt t) { return false; }
    @Override
    public boolean delete(Long id) { return false; }
    @Override
    public boolean saveAll(Iterable<Emprunt> entities) { return false; }
}