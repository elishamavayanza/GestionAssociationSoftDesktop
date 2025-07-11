package com.association.dao;

import com.association.model.Membre;
import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Contribution;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

class ContributionDaoImpl extends GenericDaoImpl<Contribution> implements ContributionDao {

    private static final Logger logger = Logger.getLogger(ContributionDaoImpl.class.getName());

    public ContributionDaoImpl() {
        super("contributions");
    }

    @Override
    protected Contribution mapResultSetToEntity(ResultSet rs) throws SQLException {
        Contribution contribution = new Contribution();
        contribution.setId(rs.getLong("id"));
        contribution.setDateTransaction(rs.getDate("date_transaction"));
        contribution.setMontant(rs.getBigDecimal("montant"));
        contribution.setDescription(rs.getString("description"));

        String typeStr = rs.getString("type_contribution");
        if (typeStr != null) {
            contribution.setTypeContribution(TypeContribution.valueOf(typeStr));
        }

        // Mapper le membre
        Membre membre = new Membre();
        membre.setId(rs.getLong("membre_id"));
        contribution.setMembre(membre);

        return contribution;
    }

    @Override
    public List<Contribution> findByMembre(Long membreId) {
        List<Contribution> contributions = new ArrayList<>();
        String sql = "SELECT c.*, t.date_transaction, t.montant, t.description, t.membre_id " +
                "FROM contributions c " +
                "JOIN transactions t ON c.id = t.id " +
                "WHERE t.membre_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, membreId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                contributions.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche des contributions par membre", e);
        }
        return contributions;
    }

    @Override
    public List<Contribution> findByDateBetween(Date start, Date end) {
        List<Contribution> contributions = new ArrayList<>();
        String sql = "SELECT DISTINCT c.id, c.type_contribution, " +
                "t.id as transaction_id, t.membre_id, t.date_transaction, t.montant, t.description " +
                "FROM contributions c " +
                "JOIN transactions t ON c.id = t.id " +
                "WHERE t.date_transaction BETWEEN ? AND ? " +
                "GROUP BY t.membre_id, t.date_transaction, t.montant, c.type_contribution " +
                "ORDER BY t.date_transaction, t.montant " +
                "LIMIT 1000";

        for (int attempt = 0; attempt < 3; attempt++) {
            try (Connection conn = databaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDate(1, new java.sql.Date(start.getTime()));
                stmt.setDate(2, new java.sql.Date(end.getTime()));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        contributions.add(mapResultSetToEntity(rs));
                    }
                    return contributions;
                }
            } catch (SQLException e) {
                if (attempt == 2) {
                    logger.log(Level.SEVERE, "Échec après 3 tentatives de récupération des contributions", e);
                    throw new RuntimeException("Failed to get contributions after 3 attempts", e);
                }
                try {
                    Thread.sleep(1000 * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted", ie);
                }
            }
        }
        return contributions;
    }

    @Override
    public BigDecimal calculerTotalContributions() {
        String sql = "SELECT SUM(t.montant) FROM transactions t " +
                "WHERE t.transaction_type = 'CONTRIBUTION'";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du calcul du total des contributions", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculerTotalContributionsMembre(Long membreId) {
        String sql = "SELECT SUM(t.montant) as total FROM transactions t " +
                "WHERE t.transaction_type = 'CONTRIBUTION' AND t.membre_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, membreId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal("total");
                System.out.println("DB returned: " + result); // Debug
                return result != null ? result : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur SQL", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<Membre> findTopContributors(Date startDate, Date endDate, int limit) {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT v.* FROM vue_membres_complets v " +
                "JOIN transactions t ON v.id = t.membre_id " +
                "WHERE t.transaction_type = 'CONTRIBUTION' " +
                "AND t.date_transaction BETWEEN ? AND ? " +
                "GROUP BY v.id, v.nom, v.contact, v.photo_path, v.date_inscription, v.statut, v.date_creation " +
                "ORDER BY SUM(t.montant) DESC " +
                "LIMIT ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(startDate.getTime()));
            stmt.setDate(2, new java.sql.Date(endDate.getTime()));
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                membres.add(new MembreDaoImpl().mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche des meilleurs contributeurs", e);
        }
        return membres;
    }

    @Override
    public boolean create(Contribution contribution) {
        String sql = "INSERT INTO entities (date_creation, entity_type) VALUES (?, 'TRANSACTION')";
        String sqlTransaction = "INSERT INTO transactions (id, membre_id, date_transaction, montant, description, transaction_type) " +
                "VALUES (?, ?, ?, ?, ?, 'CONTRIBUTION')";
        String sqlContribution = "INSERT INTO contributions (id, type_contribution) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = databaseConfig.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setTimestamp(1, new Timestamp(new Date().getTime()));

                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        contribution.setId(generatedKeys.getLong(1));

                        try (PreparedStatement tStmt = conn.prepareStatement(sqlTransaction)) {
                            tStmt.setLong(1, contribution.getId());
                            tStmt.setLong(2, contribution.getMembre().getId());
                            tStmt.setTimestamp(3, new Timestamp(contribution.getDateTransaction().getTime()));
                            tStmt.setBigDecimal(4, contribution.getMontant());
                            tStmt.setString(5, contribution.getDescription());
                            tStmt.executeUpdate();
                        }

                        try (PreparedStatement cStmt = conn.prepareStatement(sqlContribution)) {
                            cStmt.setLong(1, contribution.getId());
                            cStmt.setString(2, contribution.getTypeContribution().toString());
                            cStmt.executeUpdate();
                        }

                        conn.commit();
                        notifyObservers(contribution); // Notification après création réussie
                        return true;
                    }
                }
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }
            logger.log(Level.SEVERE, "Erreur lors de la création de la contribution", e);
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", e);
            }
        }
    }

    @Override
    public boolean update(Contribution contribution) {
        String sqlTransaction = "UPDATE transactions SET " +
                "membre_id = ?, date_transaction = ?, montant = ?, description = ? " +
                "WHERE id = ? AND transaction_type = 'CONTRIBUTION'";
        String sqlContribution = "UPDATE contributions SET type_contribution = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = databaseConfig.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement tStmt = conn.prepareStatement(sqlTransaction)) {
                tStmt.setLong(1, contribution.getMembre().getId());
                tStmt.setTimestamp(2, new Timestamp(contribution.getDateTransaction().getTime()));
                tStmt.setBigDecimal(3, contribution.getMontant());
                tStmt.setString(4, contribution.getDescription());
                tStmt.setLong(5, contribution.getId());

                if (tStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement cStmt = conn.prepareStatement(sqlContribution)) {
                    // Gérer le cas où typeContribution est null
                    if (contribution.getTypeContribution() != null) {
                        cStmt.setString(1, contribution.getTypeContribution().toString());
                    } else {
                        cStmt.setNull(1, Types.VARCHAR);
                    }
                    cStmt.setLong(2, contribution.getId());
                    cStmt.executeUpdate();
                }

                conn.commit();
                notifyObservers(contribution);
                return true;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }
            logger.log(Level.SEVERE, "Erreur lors de la mise à jour de la contribution", e);
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", e);
            }
        }
    }


    @Override
    public boolean delete(Long id) {
        Optional<Contribution> contributionOpt = findById(id);

        String sql = "DELETE FROM entities WHERE id = ?";

        Connection conn = null;
        try {
            conn = databaseConfig.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);

                if (stmt.executeUpdate() > 0) {
                    conn.commit();
                    notifyObservers(id); // Notification après suppression réussie
                    return true;
                }

                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Erreur lors du rollback", ex);
            }
            logger.log(Level.SEVERE, "Erreur lors de la suppression de la contribution", e);
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", e);
            }
        }
    }

    @Override
    public List<Contribution> findByMembreAndType(Long membreId, TypeContribution type) {
        List<Contribution> contributions = new ArrayList<>();
        String sql = "SELECT * FROM vue_transactions_completes " +
                "WHERE membre_id = ? AND type_contribution = ? " +
                "ORDER BY date_transaction DESC";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, membreId);
            stmt.setString(2, type.toString());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                contributions.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche des contributions par membre et type", e);
        }
        return contributions;
    }

    @Override
    public Map<String, Object> getContributionStats() {
        Map<String, Object> stats = new HashMap<>();

        String sql = "SELECT " +
                "COALESCE(SUM(transactions.montant), 0) as total, " +
                "COALESCE(AVG(transactions.montant), 0) as average, " +
                "(SELECT COALESCE(SUM(t2.montant), 0) FROM transactions t2 " +
                "WHERE t2.transaction_type = 'CONTRIBUTION' " +
                "AND YEAR(t2.date_transaction) = YEAR(CURRENT_DATE)) as annualTotal, " +
                "(SELECT p.nom FROM personnes p " +
                "JOIN membres m ON p.id = m.id " +
                "JOIN transactions t3 ON m.id = t3.membre_id " +
                "WHERE t3.transaction_type = 'CONTRIBUTION' " +
                "GROUP BY m.id, p.nom ORDER BY SUM(t3.montant) DESC LIMIT 1) as topContributor " +
                "FROM transactions " +
                "WHERE transactions.transaction_type = 'CONTRIBUTION'";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                stats.put("total", rs.getBigDecimal("total"));
                stats.put("monthlyAverage", rs.getBigDecimal("average"));
                stats.put("annualTotal", rs.getBigDecimal("annualTotal"));
                stats.put("topContributor", rs.getString("topContributor"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du calcul des statistiques des contributions", e);
            // Valeurs par défaut en cas d'erreur
            stats.put("total", BigDecimal.ZERO);
            stats.put("monthlyAverage", BigDecimal.ZERO);
            stats.put("annualTotal", BigDecimal.ZERO);
            stats.put("topContributor", "N/A");
        }

        return stats;
    }

    @Override
    public Map<TypeContribution, BigDecimal> getContributionsByType() {
        Map<TypeContribution, BigDecimal> typeStats = new EnumMap<>(TypeContribution.class);

        String sql = "SELECT c.type_contribution, SUM(t.montant) as total " +
                "FROM contributions c " +
                "JOIN transactions t ON c.id = t.id " +
                "WHERE t.transaction_type = 'CONTRIBUTION' " +
                "GROUP BY c.type_contribution";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TypeContribution type = TypeContribution.valueOf(rs.getString("type_contribution"));
                BigDecimal total = rs.getBigDecimal("total");
                typeStats.put(type, total);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la récupération des contributions par type", e);
        }

        return typeStats;
    }

    @Override
    public Map<String, BigDecimal> getMonthlyContributions(int months) {
        Map<String, BigDecimal> monthlyData = new LinkedHashMap<>();

        String sql = "SELECT DATE_FORMAT(t.date_transaction, '%Y-%m') as month, " +
                "SUM(t.montant) as total " +
                "FROM transactions t " +
                "WHERE t.transaction_type = 'CONTRIBUTION' " +
                "AND t.date_transaction >= DATE_SUB(CURRENT_DATE, INTERVAL ? MONTH) " +
                "GROUP BY month " +
                "ORDER BY month";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, months);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String month = rs.getString("month");
                BigDecimal total = rs.getBigDecimal("total");
                monthlyData.put(month, total);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la récupération des contributions mensuelles", e);
        }

        return monthlyData;
    }

    @Override
    public boolean saveAll(Iterable<Contribution> entities) {
        // Implémentation optionnelle pour sauvegarder plusieurs entités
        boolean allSuccess = true;
        for (Contribution contribution : entities) {
            if (!create(contribution)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
}