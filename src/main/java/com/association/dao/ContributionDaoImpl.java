package com.association.dao;

import com.association.model.Membre;
import com.association.model.transaction.Contribution;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class ContributionDaoImpl extends GenericDaoImpl<Contribution> implements ContributionDao {
    public ContributionDaoImpl() {
        super("contributions");
    }

    @Override
    protected Contribution mapResultSetToEntity(ResultSet rs) throws SQLException {
        Contribution contribution = new Contribution();
        contribution.setId(rs.getLong("id"));
        // Remplir les autres propriétés
        return contribution;
    }

    @Override
    public List<Contribution> findByMembre(Long membreId) {
        List<Contribution> contributions = new ArrayList<>();
        String sql = "SELECT c.* FROM contributions c " +
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
            e.printStackTrace();
        }
        return contributions;
    }

    @Override
    public List<Contribution> findByDateBetween(Date start, Date end) {
        List<Contribution> contributions = new ArrayList<>();
        String sql = "SELECT c.* FROM contributions c " +
                "JOIN transactions t ON c.id = t.id " +
                "WHERE t.date_transaction BETWEEN ? AND ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(start.getTime()));
            stmt.setDate(2, new java.sql.Date(end.getTime()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                contributions.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculerTotalContributionsMembre(Long membreId) {
        String sql = "SELECT SUM(t.montant) FROM transactions t " +
                "WHERE t.transaction_type = 'CONTRIBUTION' AND t.membre_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, membreId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<Membre> findTopContributors(Date startDate, Date endDate, int limit) {
        List<Membre> membres = new ArrayList<>();
        String sql = "SELECT m.* FROM membres m " +
                "JOIN transactions t ON m.id = t.membre_id " +
                "WHERE t.transaction_type = 'CONTRIBUTION' " +
                "AND t.date_transaction BETWEEN ? AND ? " +
                "GROUP BY m.id " +
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
            e.printStackTrace();
        }
        return membres;
    }

    // Implémentations des autres méthodes de GenericDao
    @Override
    public boolean create(Contribution t) { return false; }
    @Override
    public boolean update(Contribution t) { return false; }
    @Override
    public boolean delete(Long id) { return false; }
    @Override
    public boolean saveAll(Iterable<Contribution> entities) { return false; }
}