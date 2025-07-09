package com.association.dao;

import com.association.manager.dto.MembreSearchCriteria;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import java.sql.*;
import java.util.*;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MembreDaoImpl extends GenericDaoImpl<Membre> implements MembreDao {
    public MembreDaoImpl() {
        super("membres");
    }
    private static final Logger logger = LoggerFactory.getLogger(MembreDaoImpl.class);

    @Override
    public boolean create(Membre membre) {
        // Validation préalable
        if (membre == null || membre.getNom() == null || membre.getNom().trim().isEmpty()) {
            logger.error("Tentative de création d'un membre invalide");
            return false;
        }


        Connection conn = null;
        try {
            conn = databaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Insérer dans entities
            String entitySql = "INSERT INTO entities(date_creation, entity_type) VALUES(?, ?)";
            try (PreparedStatement entityStmt = conn.prepareStatement(entitySql, Statement.RETURN_GENERATED_KEYS)) {
                entityStmt.setTimestamp(1, new Timestamp(membre.getDateCreation().getTime()));
                entityStmt.setString(2, "MEMBRE");

                if (entityStmt.executeUpdate() == 0) {
                    conn.rollback();
                    logger.error("Échec de l'insertion dans entities");
                    return false;
                }

                try (ResultSet rs = entityStmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        conn.rollback();
                        logger.error("Aucune clé générée pour l'entité");
                        return false;
                    }
                    long id = rs.getLong(1);
                    membre.setId(id);
                }
            }

            // 2. Insérer dans personnes
            String personneSql = "INSERT INTO personnes(id, nom, contact, photo_path) VALUES(?, ?, ?, ?)";
            try (PreparedStatement personneStmt = conn.prepareStatement(personneSql)) {
                personneStmt.setLong(1, membre.getId());
                personneStmt.setString(2, membre.getNom());
                personneStmt.setString(3, membre.getContact() != null ? membre.getContact() : "");
                personneStmt.setString(4, membre.getPhoto()); // Peut être null

                if (personneStmt.executeUpdate() == 0) {
                    conn.rollback();
                    logger.error("Échec de l'insertion dans personnes");
                    return false;
                }
            }

            // 3. Insérer dans membres
            String membreSql = "INSERT INTO membres(id, date_inscription, statut) VALUES(?, ?, ?)";
            try (PreparedStatement membreStmt = conn.prepareStatement(membreSql)) {
                membreStmt.setLong(1, membre.getId());
                membreStmt.setDate(2, new java.sql.Date(membre.getDateInscription().getTime()));
                membreStmt.setString(3, membre.getStatut() != null ? membre.getStatut().name() : StatutMembre.ACTIF.name());

                if (membreStmt.executeUpdate() == 0) {
                    conn.rollback();
                    logger.error("Échec de l'insertion dans membres");
                    return false;
                }
            }

            conn.commit();
            logger.info("Membre créé avec succès - ID: {}", membre.getId());
            notifyObservers(membre);
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.error("Erreur lors du rollback", ex);
            }
            logger.error("Erreur SQL lors de la création du membre", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warn("Erreur lors de la fermeture de la connexion", e);
                }
            }
        }
    }
    @Override
    public boolean update(Membre membre) {
        String sql = "UPDATE personnes SET nom = ?, contact = ?, photo_path = ? WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, membre.getNom());
            stmt.setString(2, membre.getContact());
            stmt.setString(3, membre.getPhoto());
            stmt.setLong(4, membre.getId());

            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                notifyObservers(membre);
            }
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean delete(Long id) {
        Connection conn = null;
        try {
            conn = databaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Supprimer de la table membres
            String membreSql = "DELETE FROM membres WHERE id = ?";
            PreparedStatement membreStmt = conn.prepareStatement(membreSql);
            membreStmt.setLong(1, id);
            membreStmt.executeUpdate();

            // Supprimer de la table personnes
            String personneSql = "DELETE FROM personnes WHERE id = ?";
            PreparedStatement personneStmt = conn.prepareStatement(personneSql);
            personneStmt.setLong(1, id);
            personneStmt.executeUpdate();

            // Supprimer de la table entities
            String entitySql = "DELETE FROM entities WHERE id = ?";
            PreparedStatement entityStmt = conn.prepareStatement(entitySql);
            entityStmt.setLong(1, id);
            entityStmt.executeUpdate();

            conn.commit();
            notifyObservers(id);
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
    public boolean saveAll(Iterable<Membre> entities) {
        // Implémentation pour sauvegarder plusieurs membres
        return false;
    }

    @Override
    protected Membre mapResultSetToEntity(ResultSet rs) throws SQLException {
        Membre membre = new Membre();
        try {
            membre.setId(rs.getLong("id"));
            membre.setDateCreation(new Date(rs.getTimestamp("date_creation").getTime()));
            membre.setNom(rs.getString("nom"));
            membre.setContact(rs.getString("contact"));
            membre.setPhoto(rs.getString("photo_path"));
            membre.setDateInscription(rs.getDate("date_inscription"));

            String statutStr = rs.getString("statut");
            if (statutStr != null) {
                membre.setStatut(StatutMembre.valueOf(statutStr));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du mapping ResultSet vers Membre", e);
            throw e;
        }
        return membre;
    }

    @Override
    public List<Membre> findByNom(String nom) {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT e.id, e.date_creation, p.nom, p.contact, p.photo_path, "
                + "m.date_inscription, m.statut "
                + "FROM entities e "
                + "JOIN personnes p ON e.id = p.id "
                + "JOIN membres m ON p.id = m.id "
                + "WHERE p.nom LIKE ?";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nom + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                membres.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par nom", e);
        }
        return membres;
    }

    @Override
    public List<Membre> findByDateInscription(Date date) {
        List<Membre> membres = new ArrayList<>();
        String sql = """
        SELECT e.id, e.date_creation, p.nom, p.contact, p.photo_path, 
               m.date_inscription, m.statut 
        FROM entities e 
        JOIN personnes p ON e.id = p.id 
        JOIN membres m ON p.id = m.id 
        WHERE m.date_inscription = ?""";

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

            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                notifyObservers("UPDATE_PHOTO:" + membreId);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de la photo du membre ID: " + membreId, e);
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
        String sql = """
        SELECT e.id, e.date_creation, p.nom, p.contact, p.photo_path, 
               m.date_inscription, m.statut 
        FROM membres m 
        JOIN entities e ON m.id = e.id
        JOIN personnes p ON m.id = p.id
        JOIN transactions t ON m.id = t.membre_id 
        WHERE t.transaction_type = 'CONTRIBUTION' 
        GROUP BY m.id 
        ORDER BY SUM(t.montant) DESC 
        LIMIT ?""";

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
        String sql = "SELECT e.id, e.date_creation, p.nom, p.contact, p.photo_path, "
                + "m.date_inscription, m.statut "
                + "FROM entities e "
                + "JOIN personnes p ON e.id = p.id "
                + "JOIN membres m ON p.id = m.id";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                membres.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les membres", e);
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
        String sql = "SELECT e.date_creation, m.id, m.date_inscription, m.statut, p.nom, p.contact, p.photo_path " +
                "FROM membres m " +
                "JOIN personnes p ON m.id = p.id " +
                "JOIN entities e ON m.id = e.id " + // ← jointure manquante
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

    @Override
    public Map<String, Integer> getMonthlyRegistrations() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = """
    SELECT DATE_FORMAT(date_inscription, '%Y-%m') AS month, 
           COUNT(*) AS count 
    FROM membres 
    GROUP BY DATE_FORMAT(date_inscription, '%Y-%m') 
    ORDER BY month""";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("month"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des inscriptions mensuelles", e);
        }
        return result;
    }

    @Override
    public Map<String, Integer> getMonthlyRegistrations(int months) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(date_inscription, '%Y-%m') AS month, " +
                "COUNT(*) AS count " +
                "FROM membres ";

        if (months > 0) {
            sql += "WHERE date_inscription >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) ";
        }

        sql += "GROUP BY DATE_FORMAT(date_inscription, '%Y-%m') " +
                "ORDER BY month";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (months > 0) {
                stmt.setInt(1, months);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("month"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des inscriptions mensuelles", e);
        }
        return result;
    }

    @Override
    public Map<String, Integer> getMembersByAgeGroup() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = """
        SELECT 
            CASE 
                WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) < 20 THEN '<20'
                WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 20 AND 29 THEN '20-29'
                WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 30 AND 39 THEN '30-39'
                WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 40 AND 49 THEN '40-49'
                WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) >= 50 THEN '50+'
                ELSE 'Inconnu'
            END AS age_group,
            COUNT(*) AS count
        FROM personnes p
        JOIN membres m ON p.id = m.id
        WHERE date_naissance IS NOT NULL
        GROUP BY age_group
        ORDER BY 
            CASE age_group
                WHEN '<20' THEN 1
                WHEN '20-29' THEN 2
                WHEN '30-39' THEN 3
                WHEN '40-49' THEN 4
                WHEN '50+' THEN 5
                ELSE 6
            END""";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("age_group"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des membres par tranche d'âge", e);
        }
        return result;
    }

}