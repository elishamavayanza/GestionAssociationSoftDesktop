package com.association;

import com.association.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        // Obtenir l'instance singleton de DatabaseConfig
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();


        try {
            // Obtenir une connexion depuis le pool
            Connection connection = dbConfig.getConnection();

            if (connection != null) {
                System.out.println("Connexion à la base de données réussie !");

                // Utilisation try-with-resources pour fermer automatiquement la connexion
                try (connection) {
                    // Ici vous pouvez exécuter vos requêtes SQL
                    System.out.println("Exécution des opérations sur la base de données...");

                    // Exemple: vérifier que la connexion est valide
                    if (connection.isValid(2)) {
                        System.out.println("La connexion est valide");
                    }
                } // La connexion sera automatiquement fermée ici
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données:");
            e.printStackTrace();
        } finally {
            // Fermer proprement le pool de connexions à la fin de l'application
            dbConfig.shutdown();
        }
    }
}