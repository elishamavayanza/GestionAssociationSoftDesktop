package com.association.dao;

import com.association.manager.dto.MembreSearchCriteria;
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
        Connection conn = null;
        try {
            conn = databaseConfig.getConnection();
            conn.setAutoCommit(false); // Désactive l'auto-commit

            // 1. Insérer dans entities
            String entitySql = "INSERT INTO entities(date_creation, entity_type) VALUES(?, ?)";
            PreparedStatement entityStmt = conn.prepareStatement(entitySql, Statement.RETURN_GENERATED_KEYS);
            entityStmt.setTimestamp(1, new Timestamp(membre.getDateCreation().getTime()));
            entityStmt.setString(2, "MEMBRE");
            entityStmt.executeUpdate();

            // Récupérer l'ID généré
            ResultSet rs = entityStmt.getGeneratedKeys();
            if (!rs.next()) {
                conn.rollback();
                return false;
            }
            long id = rs.getLong(1);
            membre.setId(id);

            // 2. Insérer dans personnes
            String personneSql = "INSERT INTO personnes(id, nom, contact, photo_path) VALUES(?, ?, ?, ?)";
            PreparedStatement personneStmt = conn.prepareStatement(personneSql);
            personneStmt.setLong(1, id);
            personneStmt.setString(2, membre.getNom());
            personneStmt.setString(3, membre.getContact());
            personneStmt.setString(4, membre.getPhoto());
            personneStmt.executeUpdate();

            // 3. Insérer dans membres
            String membreSql = "INSERT INTO membres(id, date_inscription, statut) VALUES(?, ?, ?)";
            PreparedStatement membreStmt = conn.prepareStatement(membreSql);
            membreStmt.setLong(1, id);
            membreStmt.setDate(2, new java.sql.Date(membre.getDateInscription().getTime()));
            membreStmt.setString(3, membre.getStatut().name());
            membreStmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
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
        try {
            membre.setDateCreation(new Date(rs.getTimestamp("date_creation").getTime()));
        } catch (SQLException e) {
            membre.setDateCreation(new Date()); // ou null selon votre besoin
        }
        membre.setNom(rs.getString("nom"));
        membre.setContact(rs.getString("contact"));
        membre.setPhoto(rs.getString("photo_path"));
        membre.setDateInscription(rs.getDate("date_inscription"));
        membre.setStatut(StatutMembre.valueOf(rs.getString("statut")));
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
            return false;
        }
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
    @Override
    public List<Membre> findAll() {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT * FROM vue_membres_complets";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
    public List<Membre> search(MembreSearchCriteria criteria) {
        List<Membre> membres = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.date_creation, m.id, m.date_inscription, m.statut, " +
                        "p.nom, p.contact, p.photo_path " +
                        "FROM membres m " +
                        "JOIN personnes p ON m.id = p.id " +
                        "JOIN entities e ON e.id = m.id " +
                        "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (criteria.getNom() != null && !criteria.getNom().isEmpty()) {
            sql.append(" AND p.nom LIKE ?");
            params.add("%" + criteria.getNom() + "%");
        }

        if (criteria.getContact() != null && !criteria.getContact().isEmpty()) {
            sql.append(" AND p.contact LIKE ?");
            params.add("%" + criteria.getContact() + "%");
        }

        if (criteria.getStatut() != null) {
            sql.append(" AND m.statut = ?");
            params.add(criteria.getStatut().name());
        }

        if (criteria.getDateInscriptionFrom() != null) {
            sql.append(" AND m.date_inscription >= ?");
            params.add(new java.sql.Date(criteria.getDateInscriptionFrom().getTime()));
        }

        if (criteria.getDateInscriptionTo() != null) {
            sql.append(" AND m.date_inscription <= ?");
            params.add(new java.sql.Date(criteria.getDateInscriptionTo().getTime()));
        }

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

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
    public List<Membre> findByNameContaining(String name) {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT m.id, m.date_inscription, m.statut, p.nom, p.contact, p.photo_path " +
                "FROM membres m JOIN personnes p ON m.id = p.id " +
                "WHERE LOWER(p.nom) LIKE LOWER(?)";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");
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