package com.association.view;

import com.association.manager.SecurityManager;
import com.association.manager.dto.LoginRequest;
import com.association.security.model.Utilisateur;
import com.association.util.utils.ValidationUtil;
import com.association.view.components.*;
import com.association.view.interfaces.InterfaceFactory;
import com.association.view.interfaces.RoleInterface;
import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class AuthFrame extends JFrame {
    private final SecurityManager securityManager;

    private JPanel mainPanel;
    private CustomTextField usernameField;
    private CustomPasswordField passwordField;
    private CustomButton loginButton;
    private JCheckBox rememberMeCheckbox;

    public AuthFrame(SecurityManager securityManager) {
        this.securityManager = securityManager;
        initComponents();
        setupFrame();
    }

    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Colors.BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo ou titre
        JLabel titleLabel = new JLabel("Gestion Association", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Colors.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Champ username
        JLabel usernameLabel = new JLabel("Nom d'utilisateur(email) :");
        usernameLabel.setForeground(Colors.TEXT);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new CustomTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(usernameField, gbc);

        // Champ password
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setForeground(Colors.TEXT);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new CustomPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);

        // Case "Se souvenir de moi"
        rememberMeCheckbox = new JCheckBox("Se souvenir de moi");
        rememberMeCheckbox.setBackground(Colors.BACKGROUND);
        rememberMeCheckbox.setForeground(Colors.TEXT);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(rememberMeCheckbox, gbc);

        // Bouton de connexion
        loginButton = new CustomButton("Se connecter");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);

        // Gestionnaire d'événements
        loginButton.addActionListener(new LoginAction());
        passwordField.addActionListener(new LoginAction());
    }

    private void setupFrame() {
        setContentPane(mainPanel);
        setTitle("Authentification");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Validation
            if (username.isEmpty() || password.isEmpty()) {
                showError("Veuillez remplir tous les champs");
                return;
            }

            try {
                // Création de la requête de connexion
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(username);
                loginRequest.setPassword(password);

                // Authentification via SecurityManager
                Utilisateur utilisateur = securityManager.authenticate(loginRequest);

                if (utilisateur != null) {
                 //Mise à jour du dernier login
                    utilisateur.setLastLogin(new Date());

                    showSuccess("Connexion réussie! Bienvenue " + utilisateur.getUsername());

                    // Création de l'interface appropriée
                    RoleInterface roleInterface = InterfaceFactory.createInterface(utilisateur);
                    JFrame userFrame = roleInterface.createInterface();
                    userFrame.setVisible(true);

                    dispose();
                } else {
                    showError("Identifiants incorrects");
                }
            } catch (Exception ex) {
                showError("Erreur lors de l'authentification: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void showError(String message) {
            JOptionPane.showMessageDialog(AuthFrame.this,
                    message,
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }

        private void showSuccess(String message) {
            JOptionPane.showMessageDialog(AuthFrame.this,
                    message,
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }



}