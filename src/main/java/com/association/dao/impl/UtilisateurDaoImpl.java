package com.association.dao.impl;

import com.association.dao.UtilisateurDao;
import com.association.security.model.Utilisateur;
import java.sql.*;
import java.util.Optional;

public class UtilisateurDaoImpl extends GenericDaoImpl<Utilisateur> implements UtilisateurDao {
    public UtilisateurDaoImpl() {
        super("utilisateurs");
    }

    @Override
    protected Utilisateur mapResultSetToEntity(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getLong("id"));
        // Remplir les autres propriétés
        return utilisateur;
    }

    @Override
    public boolean updateAvatar(Long userId, String avatarPath) {
        String sql = "UPDATE utilisateurs SET avatar_path = ? WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, avatarPath);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Implémentations des autres méthodes de GenericDao
    @Override
    public boolean create(Utilisateur t) { return false; }
    @Override
    public boolean update(Utilisateur t) { return false; }
    @Override
    public boolean delete(Long id) { return false; }
    @Override
    public boolean saveAll(Iterable<Utilisateur> entities) { return false; }
}