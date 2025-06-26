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

import javax.swing.JLabel;              // pour forgotPasswordLabel
import java.awt.event.MouseAdapter;    // pour MouseAdapter
import java.awt.event.MouseEvent;

public class AuthFrame extends JFrame {
    private final SecurityManager securityManager;

    private JPanel mainPanel;
    private CustomTextField usernameField;
    private CustomPasswordField passwordField;
    private CustomButton loginButton;
    private JCheckBox rememberMeCheckbox;
    private JLabel forgotPasswordLabel;


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


        // Lien "Mot de passe oublié"
        forgotPasswordLabel = new JLabel("<html><u>Mot de passe oublié ?</u></html>", SwingConstants.CENTER);
        forgotPasswordLabel.setForeground(Colors.CURRENT_PRIMARY);
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(forgotPasswordLabel, gbc);

        // Gestionnaire d'événements
        loginButton.addActionListener(new LoginAction());
        passwordField.addActionListener(new LoginAction());
    }

    private void showForgotPasswordDialog() {
        JTextField emailField = new JTextField(20);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Entrez votre email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Mot de passe oublié",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            // Ici vous pouvez implémenter la logique de réinitialisation
            JOptionPane.showMessageDialog(this,
                    "Un lien de réinitialisation a été envoyé à " + emailField.getText(),
                    "Email envoyé", JOptionPane.INFORMATION_MESSAGE);
        }
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

                    // Afficher d'abord le profil utilisateur
                    UserProfileFrame profileFrame = new UserProfileFrame(utilisateur, securityManager );
                    profileFrame.setVisible(true);
                    // Création de l'interface appropriée
//                    RoleInterface roleInterface = InterfaceFactory.createInterface(utilisateur);
//                    JFrame userFrame = roleInterface.createInterface();
//                    userFrame.setVisible(true);

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