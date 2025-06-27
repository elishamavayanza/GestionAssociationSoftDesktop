package com.association;

import com.association.dao.DAOFactory;
import com.association.dao.UtilisateurDao;
import com.association.util.file.FileStorageService;
import com.association.util.file.FileStorageServiceAdapter;
import com.association.view.AuthPanel;
import com.association.view.LoginFrame;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
//        String rawPassword = "Isaac@1234";
//        String hashedPassword = encoder.encode(rawPassword);
//
//        System.out.println("Mot de passe brut : " + rawPassword);
//        System.out.println("Mot de passe haché : " + hashedPassword);
    }

    //    Isaac : Isaac@1234 = admin
    //    Eli :  Eli@1234 = gestionnaire
    //    Elisahama : Eli@2004 = membre
    //    Exaucee  : Ex@12345 = visiteur


}