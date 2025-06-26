package com.association;

import com.association.dao.UtilisateurDao;
import com.association.dao.impl.UtilisateurDaoImpl;
import com.association.manager.SecurityManager;
import com.association.view.AuthFrame;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Initialisation des dépendances

        UtilisateurDao utilisateurDao = new UtilisateurDaoImpl();
        SecurityManager securityManager = new SecurityManager(utilisateurDao);

        // Afficher l'interface de connexion
        javax.swing.SwingUtilities.invokeLater(() -> {
            AuthFrame authFrame = new AuthFrame(securityManager);
            authFrame.setVisible(true);
        });


//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
//        String rawPassword = "Membre#456";
//        String hashedPassword = encoder.encode(rawPassword);
//
//        System.out.println("Mot de passe brut : " + rawPassword);
//        System.out.println("Mot de passe haché : " + hashedPassword);
    }

    //    Mot de passe brut : Admin@1234
    //    Mot de passe haché : $2a$10$IIAmiiRLr5XJfJO9rgK.7O3Sac7hReajHOrdKWFhMmlf7oFfRncTq

  //    Mot de passe brut : Gest@2023!
  //    Mot de passe haché : $2a$10$.crHETh9Vh0z87earkzaaO2vYP1SqlUaWbUlngEeZTu6E/miGCBum

//    Mot de passe brut : Membre#456
//    Mot de passe haché : $2a$10$tJJJyX5JPDA.9evwsjY2RuZYZj8LtVlHTMdMxc44CFiMujHTWOBBi
}