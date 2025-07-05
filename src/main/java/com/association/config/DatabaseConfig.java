package com.association.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    // Instance unique de la classe
    private static DatabaseConfig instance;
    private final HikariDataSource dataSource;

    // Constructeur privé pour empêcher l'instanciation directe
    private DatabaseConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/gestion_association");
        config.setUsername("AvecSoft");
        config.setPassword("2004");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Paramètres optimisés
        config.setMaximumPoolSize(20); // Augmenté de 10 à 20
        config.setMinimumIdle(5); // Ajouté
        config.setConnectionTimeout(10000); // Réduit de 30000 à 10000 ms
        config.setIdleTimeout(300000); // Réduit de 600000 à 300000 ms (5 min)
        config.setMaxLifetime(900000); // Réduit de 1800000 à 900000 ms (15 min)
        config.setLeakDetectionThreshold(5000); // Ajouté pour détecter les fuites

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        this.dataSource = new HikariDataSource(config);
    }

    // Méthode statique pour obtenir l'instance unique
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    // Méthode pour obtenir une connexion
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Méthode pour fermer la connexion
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Méthode pour fermer le pool de connexions (à appeler à la fermeture de l'application)
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}