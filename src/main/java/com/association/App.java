package com.association;

import com.association.dao.DAOFactory;
import com.association.dao.UtilisateurDao;
import com.association.util.file.FileStorageService;
import com.association.util.file.FileStorageServiceAdapter;
import com.association.view.AuthPanel;
import com.association.view.LoginFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Initialisation des dépendances

        UtilisateurDao utilisateurDao = DAOFactory.getInstance(UtilisateurDao.class);
        FileStorageService fileStorageService = new FileStorageServiceAdapter() {
        };

        // Afficher l'interface de connexion
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            AuthPanel authPanel = new AuthPanel(loginFrame);
            loginFrame.switchView(authPanel, "Authentification");
            loginFrame.setVisible(true);
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