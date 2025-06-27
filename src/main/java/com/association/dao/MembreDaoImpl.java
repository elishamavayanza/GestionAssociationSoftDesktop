package com.association.dao;

import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class MembreDaoImpl extends GenericDaoImpl<Membre> implements MembreDao {
    public MembreDaoImpl() {
        super("membres");
    }

    @Override
    public boolean create(Membre membre) {
        // Implémentation pour créer un membre
        return false;
    }

    @Override
    public boolean update(Membre membre) {
        // Implémentation pour mettre à jour un membre
        return false;
    }

    @Override
    public boolean delete(Long id) {
        // Implémentation pour supprimer un membre
        return false;
    }

    @Override
    public boolean saveAll(Iterable<Membre> entities) {
        // Implémentation pour sauvegarder plusieurs membres
        return false;
    }

    @Override
    protected Membre mapResultSetToEntity(ResultSet rs) throws SQLException {
        Membre membre = new Membre();
        membre.setId(rs.getLong("id"));
        // Remplir les autres propriétés
        return membre;
    }

    @Override
    public List<Membre> findByNom(String nom) {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT m.* FROM membres m JOIN personnes p ON m.id = p.id WHERE p.nom LIKE ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                membres.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membres;
    }

    @Override
    public List<Membre> findByDateInscription(Date date) {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT * FROM membres WHERE date_inscription = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                membres.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membres;
    }

    @Override
    public boolean updatePhoto(Long membreId, String photoPath) {
        String sql = "UPDATE personnes SET photo_path = ? WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, photoPath);
            stmt.setLong(2, membreId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public long countByStatut(StatutMembre statut) {
        String sql = "SELECT COUNT(*) FROM membres WHERE statut = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut.name());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Membre> findTopContributors(int limit) {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT m.* FROM membres m " +
                "JOIN transactions t ON m.id = t.membre_id " +
                "WHERE t.transaction_type = 'CONTRIBUTION' " +
                "GROUP BY m.id " +
                "ORDER BY SUM(t.montant) DESC " +
                "LIMIT ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                membres.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membres;
    }
}