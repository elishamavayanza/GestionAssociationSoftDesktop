package com.association.dao;

import com.association.model.transaction.Emprunt;
import com.association.model.enums.StatutEmprunt;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class EmpruntDaoImpl extends GenericDaoImpl<Emprunt> implements EmpruntDao {
    public EmpruntDaoImpl() {
        super("emprunts");
    }

    @Override
    protected Emprunt mapResultSetToEntity(ResultSet rs) throws SQLException {
        Emprunt emprunt = new Emprunt();
        emprunt.setId(rs.getLong("id"));
        emprunt.setDateCreation(rs.getTimestamp("date_creation"));
        emprunt.setDateTransaction(rs.getTimestamp("date_transaction"));
        emprunt.setMontant(rs.getBigDecimal("montant"));
        emprunt.setDescription(rs.getString("description"));
        emprunt.setMontantRembourse(rs.getBigDecimal("montant_rembourse"));
        emprunt.setDateRemboursement(rs.getDate("date_remboursement"));
        emprunt.setStatut(StatutEmprunt.valueOf(rs.getString("statut")));
        return emprunt;
    }

    @Override
    public List<Emprunt> findByMembre(Long membreId) {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT e.*, t.*, ent.date_creation FROM emprunts e " +
                "JOIN transactions t ON e.id = t.id " +
                "JOIN entities ent ON t.id = ent.id " +
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
        String sql = "SELECT e.*, t.*, ent.date_creation FROM emprunts e " +
                "JOIN transactions t ON e.id = t.id " +
                "JOIN entities ent ON t.id = ent.id " +
                "WHERE e.statut = ?";
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
    public boolean create(Emprunt emprunt) {
        String sqlEntity = "INSERT INTO entities (date_creation, entity_type) VALUES (?, ?)";
        String sqlTransaction = "INSERT INTO transactions (id, membre_id, date_transaction, montant, description, transaction_type) " +
                "VALUES (?, ?, ?, ?, ?, 'EMPRUNT')";
        String sqlEmprunt = "INSERT INTO emprunts (id, montant_rembourse, date_remboursement, statut) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmtEntity = conn.prepareStatement(sqlEntity, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtTransaction = conn.prepareStatement(sqlTransaction);
             PreparedStatement stmtEmprunt = conn.prepareStatement(sqlEmprunt)) {

            conn.setAutoCommit(false);

            // 1. Insertion dans entities pour générer l’ID
            stmtEntity.setTimestamp(1, new Timestamp(emprunt.getDateCreation().getTime()));
            stmtEntity.setString(2, "TRANSACTION");
            stmtEntity.executeUpdate();

            ResultSet generatedKeys = stmtEntity.getGeneratedKeys();
            if (generatedKeys.next()) {
                Long generatedId = generatedKeys.getLong(1);
                emprunt.setId(generatedId);
            } else {
                conn.rollback();
                throw new SQLException("Impossible de récupérer l’ID généré");
            }

            // 2. Insertion dans transactions
            stmtTransaction.setLong(1, emprunt.getId());
            stmtTransaction.setLong(2, emprunt.getMembre().getId());
            stmtTransaction.setTimestamp(3, new Timestamp(emprunt.getDateTransaction().getTime()));
            stmtTransaction.setBigDecimal(4, emprunt.getMontant());
            stmtTransaction.setString(5, emprunt.getDescription());
            stmtTransaction.executeUpdate();

            // 3. Insertion dans emprunts
            stmtEmprunt.setLong(1, emprunt.getId());
            stmtEmprunt.setBigDecimal(2, emprunt.getMontantRembourse());
            stmtEmprunt.setDate(3, emprunt.getDateRemboursement() != null ?
                    new java.sql.Date(emprunt.getDateRemboursement().getTime()) : null);
            stmtEmprunt.setString(4, emprunt.getStatut().name());
            stmtEmprunt.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean update(Emprunt t) { return false; }
    @Override
    public boolean delete(Long id) { return false; }
    @Override
    public boolean saveAll(Iterable<Emprunt> entities) { return false; }
}