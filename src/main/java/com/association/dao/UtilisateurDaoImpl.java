package com.association.dao;

import com.association.model.access.Utilisateur;
import com.association.model.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UtilisateurDaoImpl extends GenericDaoImpl<Utilisateur> implements UtilisateurDao {
    private static final Logger logger = LoggerFactory.getLogger(UtilisateurDaoImpl.class);
    private static final Map<Long, Set<UserRole>> rolesCache = new ConcurrentHashMap<>();

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
        utilisateur.setAvatar(rs.getString("avatar_path"));
        utilisateur.setLastLogin(rs.getTimestamp("last_login"));
        utilisateur.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));

        // Chargement des rôles depuis le cache ou la base
        Set<UserRole> roles = rolesCache.computeIfAbsent(utilisateur.getId(), k -> {
            try {
                return loadUserRoles(k);
            } catch (SQLException e) {
                logger.error("Erreur lors du chargement des rôles pour l'utilisateur {}", k, e);
                return new HashSet<>();
            }
        });
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
                    logger.warn("Rôle inconnu ignoré: {}", roleName);
                }
            }
        }
        return roles;
    }

    @Override
    public boolean create(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateurs (id, username, password, email, is_active, avatar_path, last_login, failed_login_attempts) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, utilisateur.getId());
            stmt.setString(2, utilisateur.getUsername());
            stmt.setString(3, utilisateur.getPassword());
            stmt.setString(4, utilisateur.getEmail());
            stmt.setBoolean(5, utilisateur.isActive());
            stmt.setString(6, utilisateur.getAvatar());
            stmt.setTimestamp(7, utilisateur.getLastLogin() != null ?
                    new Timestamp(utilisateur.getLastLogin().getTime()) : null);
            stmt.setInt(8, utilisateur.getFailedLoginAttempts());

            boolean created = stmt.executeUpdate() > 0;

            if (created && utilisateur.getRoles() != null && !utilisateur.getRoles().isEmpty()) {
                saveUserRoles(utilisateur.getId(), utilisateur.getRoles());
                rolesCache.put(utilisateur.getId(), new HashSet<>(utilisateur.getRoles()));
            }

            return created;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'utilisateur", e);
            return false;
        }
    }

    private void saveUserRoles(Long userId, Set<UserRole> roles) throws SQLException {
        // Suppression des anciens rôles
        String deleteSql = "DELETE FROM utilisateur_roles WHERE utilisateur_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setLong(1, userId);
            deleteStmt.executeUpdate();
        }

        // Ajout des nouveaux rôles
        if (!roles.isEmpty()) {
            String insertSql = "INSERT INTO utilisateur_roles (utilisateur_id, role_id) " +
                    "SELECT ?, id FROM roles WHERE name = ?";
            try (Connection conn = databaseConfig.getConnection();
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                for (UserRole role : roles) {
                    insertStmt.setLong(1, userId);
                    insertStmt.setString(2, role.name());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        }
    }

    @Override
    public boolean update(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateurs SET username = ?, password = ?, email = ?, is_active = ?, " +
                "avatar_path = ?, last_login = ?, failed_login_attempts = ? WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, utilisateur.getUsername());
            stmt.setString(2, utilisateur.getPassword());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setBoolean(4, utilisateur.isActive());
            stmt.setString(5, utilisateur.getAvatar());
            stmt.setTimestamp(6, utilisateur.getLastLogin() != null ?
                    new Timestamp(utilisateur.getLastLogin().getTime()) : null);
            stmt.setInt(7, utilisateur.getFailedLoginAttempts());
            stmt.setLong(8, utilisateur.getId());

            boolean updated = stmt.executeUpdate() > 0;

            if (updated) {
                if (utilisateur.getRoles() != null) {
                    saveUserRoles(utilisateur.getId(), utilisateur.getRoles());
                    rolesCache.put(utilisateur.getId(), new HashSet<>(utilisateur.getRoles()));
                }
            }

            return updated;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de l'utilisateur {}", utilisateur.getId(), e);
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        // Suppression des rôles associés
        String deleteRolesSql = "DELETE FROM utilisateur_roles WHERE utilisateur_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteRolesSql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();

            // Suppression du cache
            rolesCache.remove(id);
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression des rôles de l'utilisateur {}", id, e);
            return false;
        }

        // Suppression de l'utilisateur
        String deleteUserSql = "DELETE FROM utilisateurs WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteUserSql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de l'utilisateur {}", id, e);
            return false;
        }
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
            logger.error("Erreur lors de la recherche par email: {}", email, e);
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
            logger.error("Erreur lors de la recherche par username: {}", username, e);
        }
        return Optional.empty();
    }

    public List<Utilisateur> findByUsernameContaining(String usernamePart) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE username LIKE ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + usernamePart + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                utilisateurs.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par username contenant: {}", usernamePart, e);
        }
        return utilisateurs;
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
            logger.error("Erreur lors de la mise à jour de l'avatar pour l'utilisateur {}", userId, e);
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
            logger.error("Erreur lors de la récupération du chemin de l'avatar pour l'utilisateur {}", userId, e);
        }
        return null;
    }

    public boolean updateLoginInfo(Long userId, Timestamp lastLogin, int failedAttempts) {
        String sql = "UPDATE utilisateurs SET last_login = ?, failed_login_attempts = ? WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, lastLogin);
            stmt.setInt(2, failedAttempts);
            stmt.setLong(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour des informations de connexion pour l'utilisateur {}", userId, e);
            return false;
        }
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM utilisateurs WHERE username = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification de l'existence du username: {}", username, e);
            return false;
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM utilisateurs WHERE email = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification de l'existence de l'email: {}", email, e);
            return false;
        }
    }

    @Override
    public List<Utilisateur> findAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY username";

        try (Connection conn = databaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utilisateurs.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les utilisateurs", e);
        }
        return utilisateurs;
    }

    @Override
    public boolean saveAll(Iterable<Utilisateur> entities) {
        return false;
    }

    public void clearRolesCache() {
        rolesCache.clear();
    }

    public void clearUserRolesCache(Long userId) {
        rolesCache.remove(userId);
    }
}