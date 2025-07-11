package com.association.dao;

import com.association.model.Membre;
import com.association.model.transaction.Emprunt;
import com.association.model.enums.StatutEmprunt;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

class EmpruntDaoImpl extends GenericDaoImpl<Emprunt> implements EmpruntDao {
    public EmpruntDaoImpl() {
        super("emprunts");
    }

    @Override
    protected Emprunt mapResultSetToEntity(ResultSet rs) throws SQLException {
        Emprunt emprunt = new Emprunt();

        // ID - toujours présent
        emprunt.setId(rs.getLong("id"));

        Long membreId = rs.getLong("membre_id");
        if (!rs.wasNull() && membreId > 0) {
            MembreDao membreDao = DAOFactory.getInstance(MembreDao.class);
            Optional<Membre> membre = membreDao.findById(membreId);
            membre.ifPresent(emprunt::setMembre);
        }

        // Dates avec gestion des alias
        emprunt.setDateCreation(getTimestampFromResultSet(rs,
                "ent.date_creation", "date_creation", "e.date_creation", "entities.date_creation"));
        emprunt.setDateTransaction(getTimestampFromResultSet(rs,
                "t.date_transaction", "date_transaction", "transactions.date_transaction"));

        // Montants avec gestion des alias
        emprunt.setMontant(getBigDecimalFromResultSet(rs,
                "t.montant", "montant", "transactions.montant"));
        emprunt.setMontantRembourse(getBigDecimalFromResultSet(rs,
                "e.montant_rembourse", "montant_rembourse", "emprunts.montant_rembourse"));

        // Description avec gestion des alias
        emprunt.setDescription(getStringFromResultSet(rs,
                "t.description", "description", "transactions.description"));

        // Date de remboursement avec gestion des alias
        emprunt.setDateRemboursement(getDateFromResultSet(rs,
                "e.date_remboursement", "date_remboursement", "emprunts.date_remboursement"));

        // Statut avec valeur par défaut
        try {
            String statutStr = getStringFromResultSet(rs,
                    "e.statut", "statut", "emprunts.statut");
            emprunt.setStatut(statutStr != null ? StatutEmprunt.valueOf(statutStr) : StatutEmprunt.EN_COURS);
        } catch (IllegalArgumentException e) {
            emprunt.setStatut(StatutEmprunt.EN_COURS);
        }

        return emprunt;
    }

    // Méthodes utilitaires pour gérer les différents types de données
    private Timestamp getTimestampFromResultSet(ResultSet rs, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            try {
                Timestamp value = rs.getTimestamp(columnName);
                if (value != null) {
                    return value;
                }
            } catch (SQLException e) {
                // Passer au nom suivant
            }
        }
        return null;
    }

    private BigDecimal getBigDecimalFromResultSet(ResultSet rs, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            try {
                BigDecimal value = rs.getBigDecimal(columnName);
                if (value != null) {
                    return value;
                }
            } catch (SQLException e) {
                // Passer au nom suivant
            }
        }
        return BigDecimal.ZERO;
    }

    private String getStringFromResultSet(ResultSet rs, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            try {
                String value = rs.getString(columnName);
                if (value != null) {
                    return value;
                }
            } catch (SQLException e) {
                // Passer au nom suivant
            }
        }
        return null;
    }

    private Date getDateFromResultSet(ResultSet rs, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            try {
                Date value = rs.getDate(columnName);
                if (value != null) {
                    return value;
                }
            } catch (SQLException e) {
                // Passer au nom suivant
            }
        }
        return null;
    }

    @Override
    public List<Emprunt> findByMembre(Long membreId) {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT e.id, e.montant_rembourse, e.date_remboursement, e.statut, " +
                "t.montant, t.date_transaction, t.description, " +
                "ent.date_creation as ent_date_creation " +
                "FROM emprunts e " +
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

    @Override
    public Map<String, Object> verifierEligibiliteDetail(Long membreId) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> raisons = new ArrayList<>();
        boolean eligible = true;

        // Paramètres configurables
        BigDecimal montantMinimumContributions = new BigDecimal("1000.00");
        int delaiDepuisDernierEmprunt = 30;

        // Initialisation
        result.put("membreId", membreId);
        result.put("eligible", true);
        result.put("raisons", raisons);

        try (Connection conn = databaseConfig.getConnection()) {
            // Requête combinée pour optimiser les appels SQL
            String sql = "SELECT "
                    + "(SELECT COUNT(*) FROM emprunts e JOIN transactions t ON e.id = t.id "
                    + "WHERE t.membre_id = ? AND e.statut != 'REMBOURSE') as dette_count, "
                    + "(SELECT SUM(t.montant) FROM transactions t "
                    + "WHERE t.membre_id = ? AND t.transaction_type = 'CONTRIBUTION') as total_contributions, "
                    + "(SELECT m.statut FROM membres m WHERE m.id = ?) as statut_membre, "
                    + "(SELECT MAX(t.date_transaction) FROM transactions t JOIN emprunts e ON t.id = e.id "
                    + "WHERE t.membre_id = ?) as dernier_emprunt";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, membreId);
                stmt.setLong(2, membreId);
                stmt.setLong(3, membreId);
                stmt.setLong(4, membreId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // 1. Vérification des dettes
                    int detteCount = rs.getInt("dette_count");
                    result.put("dettes", detteCount > 0);
                    if (detteCount > 0) {
                        eligible = false;
                        raisons.add("Le membre a des emprunts non remboursés");
                    }

                    // 2. Vérification des contributions
                    BigDecimal totalContributions = rs.getBigDecimal("total_contributions");
                    if (totalContributions == null) totalContributions = BigDecimal.ZERO;
                    result.put("contributions", totalContributions);

                    if (totalContributions.compareTo(montantMinimumContributions) < 0) {
                        eligible = false;
                        raisons.add(String.format(
                                "Contributions insuffisantes (actuelles: %s, minimum requis: %s)",
                                totalContributions, montantMinimumContributions
                        ));
                    }

                    // 3. Vérification du statut
                    String statut = rs.getString("statut_membre");
                    result.put("statut", statut != null ? statut : "NON_TROUVE");

                    if (statut == null) {
                        eligible = false;
                        raisons.add("Membre non trouvé");
                    } else if (!"ACTIF".equals(statut)) {
                        eligible = false;
                        raisons.add("Statut du membre: " + statut + " (requis: ACTIF)");
                    }

                    // 4. Vérification du délai
                    Date dernierEmprunt = rs.getDate("dernier_emprunt");
                    result.put("dernierEmprunt", dernierEmprunt);

                    if (dernierEmprunt != null) {
                        long diffDays = (System.currentTimeMillis() - dernierEmprunt.getTime()) / (24 * 60 * 60 * 1000);
                        result.put("joursDepuisDernierEmprunt", diffDays);

                        if (diffDays < delaiDepuisDernierEmprunt) {
                            eligible = false;
                            raisons.add(String.format(
                                    "Délai depuis le dernier emprunt insuffisant (%d jours, minimum requis: %d jours)",
                                    diffDays, delaiDepuisDernierEmprunt
                            ));
                        }
                    }
                } else {
                    eligible = false;
                    raisons.add("Membre non trouvé");
                }
            }

            result.put("eligible", eligible);
            if (eligible) {
                raisons.add("Le membre est éligible à un nouvel emprunt");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            result.put("eligible", false);
            raisons.add("Erreur technique lors de la vérification");
            result.put("erreur", e.getMessage());
        }

        return result;
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
            notifyObservers(emprunt); // Notification après création
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean effectuerRemboursement(Long empruntId, BigDecimal montant) {
        String sql = "UPDATE emprunts SET montant_rembourse = montant_rembourse + ? WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, montant);
            stmt.setLong(2, empruntId);
            boolean success = stmt.executeUpdate() > 0;

            if (success) {
                // Créer une map avec les données du remboursement
                Map<String, Object> rembData = new HashMap<>();
                rembData.put("remboursement", true);
                rembData.put("montant", montant);
                rembData.put("empruntId", empruntId);

                // Notifier avec les données complètes
                notifyObservers(rembData);
            }
            return success;
//            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public List<Emprunt> findByMembreAndStatutNot(Long membreId, StatutEmprunt statut) {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT e.*, t.*, ent.date_creation FROM emprunts e " +
                "JOIN transactions t ON e.id = t.id " +
                "JOIN entities ent ON t.id = ent.id " +
                "WHERE t.membre_id = ? AND e.statut != ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, membreId);
            stmt.setString(2, statut.name());
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
    public boolean updateStatut(Long empruntId, StatutEmprunt statut) {
        String sql = "UPDATE emprunts SET statut = ? WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut.name());
            stmt.setLong(2, empruntId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public Optional<Emprunt> findById(Long id) {
        String sql = "SELECT e.*, t.*, ent.date_creation FROM emprunts e " +
                "JOIN transactions t ON e.id = t.id " +
                "JOIN entities ent ON t.id = ent.id " +
                "WHERE e.id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
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
    public boolean archiverEmprunt(Long empruntId) {
        // Vérifier d'abord si l'emprunt existe et est remboursé
        Optional<Emprunt> empruntOpt = findById(empruntId);
        if (!empruntOpt.isPresent()) {
            throw new IllegalStateException("Emprunt non trouvé");
        }

        Emprunt emprunt = empruntOpt.get();
        BigDecimal soldeRestant = emprunt.calculerSoldeRestant();

        if (emprunt.getStatut() != StatutEmprunt.REMBOURSE || soldeRestant.compareTo(BigDecimal.ZERO) != 0) {
            // Au lieu de throw une exception, on pourrait juste retourner false
            return false;
        }

        // Option 2: Archivage dans une table dédiée (solution plus complète)
        String sqlArchive = "INSERT INTO emprunts_archives " +
                "SELECT e.*, NOW() as date_archivage FROM emprunts e WHERE e.id = ?";
        String sqlDelete = "DELETE FROM emprunts WHERE id = ?";

        try (Connection conn = databaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtArchive = conn.prepareStatement(sqlArchive);
                 PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {

                // Archiver l'emprunt
                stmtArchive.setLong(1, empruntId);
                stmtArchive.executeUpdate();

                // Supprimer de la table principale
                stmtDelete.setLong(1, empruntId);
                stmtDelete.executeUpdate();

                conn.commit();
                notifyObservers(empruntId); // Notifier le changement de statut
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Erreur lors de l'archivage", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    @Override
    public List<Emprunt> findArchivedByMembre(Long membreId) {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT e.*, t.*, ent.date_creation FROM emprunts_archives e " +
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
    public List<Emprunt> findAll() {
        List<Emprunt> emprunts = new ArrayList<>();
        String sql = "SELECT e.id, e.montant_rembourse, e.date_remboursement, e.statut, " +
                "t.montant, t.date_transaction, t.description, t.membre_id, " +
                "ent.date_creation as ent_date_creation " +
                "FROM emprunts e " +
                "JOIN transactions t ON e.id = t.id " +
                "JOIN entities ent ON t.id = ent.id";

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                emprunts.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emprunts;
    }

    @Override
    public boolean update(Emprunt t) { return false; }
    @Override
    public boolean delete(Long id) { return false; }
    @Override
    public boolean saveAll(Iterable<Emprunt> entities) { return false; }
}