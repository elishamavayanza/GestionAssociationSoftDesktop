package com.association.dao;

import com.association.model.access.Utilisateur;
import com.association.model.enums.UserRole;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class UtilisateurDaoImpl extends GenericDaoImpl<Utilisateur> implements UtilisateurDao {
    public UtilisateurDaoImpl() {
        super("utilisateurs");
    }

    @Override
    protected Utilisateur mapResultSetToEntity(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getLong("id"));
        utilisateur.setUsername(rs.getString("username"));
        utilisateur.setPassword(rs.getString("password"));
        utilisateur.setEmail(rs.getString("email"));
        utilisateur.setActive(rs.getBoolean("is_active"));
        utilisateur.setAvatar(rs.getString("avatar_path")); // Ajout de l'avatar

        Set<UserRole> roles = loadUserRoles(utilisateur.getId());
        utilisateur.setRoles(roles);

        return utilisateur;
    }
    private Set<UserRole> loadUserRoles(Long userId) throws SQLException {
        Set<UserRole> roles = new HashSet<>();
        String sql = "SELECT r.name FROM utilisateur_roles ur " +
                "JOIN roles r ON ur.role_id = r.id " +
                "WHERE ur.utilisateur_id = ?";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String roleName = rs.getString("name");
                try {
                    roles.add(UserRole.valueOf(roleName));
                } catch (IllegalArgumentException e) {
                    System.err.println("Rôle inconnu: " + roleName);
                }
            }
        }
        return roles;
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
    @Override
    public Optional<Utilisateur> findByUsername(String username) {
        String sql = "SELECT * FROM utilisateurs WHERE username = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
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
    public boolean update(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateurs SET username = ?, password = ?, email = ?, is_active = ? WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, utilisateur.getUsername());
            stmt.setString(2, utilisateur.getPassword());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setBoolean(4, utilisateur.isActive());
            stmt.setLong(5, utilisateur.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public String getAvatarPath(Long userId) {
        String sql = "SELECT avatar_path FROM utilisateurs WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("avatar_path");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean delete(Long id) { return false; }
    @Override
    public boolean saveAll(Iterable<Utilisateur> entities) { return false; }
}