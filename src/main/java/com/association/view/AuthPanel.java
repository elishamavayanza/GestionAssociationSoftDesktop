package com.association.view;

import com.association.manager.SecurityManager;
import com.association.manager.dto.LoginRequest;
import com.association.security.model.Utilisateur;
import com.association.view.components.*;
import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AuthPanel extends JPanel {
    private final SecurityManager securityManager;
    private final MainFrame mainFrame;

    private CustomTextField usernameField;
    private CustomPasswordField passwordField;
    private CustomButton loginButton;
    private JCheckBox rememberMeCheckbox;
    private JLabel forgotPasswordLabel;

    public AuthPanel(MainFrame mainFrame, SecurityManager securityManager) {
        this.mainFrame = mainFrame;
        this.securityManager = securityManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(Colors.BACKGROUND);

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
        add(titleLabel, gbc);

        // Champ username
        JLabel usernameLabel = new JLabel("Nom d'utilisateur(email) :");
        usernameLabel.setForeground(Colors.TEXT);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(usernameLabel, gbc);

        usernameField = new CustomTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(usernameField, gbc);

        // Champ password
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setForeground(Colors.TEXT);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passwordLabel, gbc);

        passwordField = new CustomPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(passwordField, gbc);

        // Case "Se souvenir de moi"
        rememberMeCheckbox = new JCheckBox("Se souvenir de moi");
        rememberMeCheckbox.setBackground(Colors.BACKGROUND);
        rememberMeCheckbox.setForeground(Colors.TEXT);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(rememberMeCheckbox, gbc);

        // Bouton de connexion
        loginButton = new CustomButton("Se connecter");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

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
        add(forgotPasswordLabel, gbc);

        // Gestionnaire d'événements
        loginButton.addActionListener(new LoginAction());
        passwordField.addActionListener(new LoginAction());
    }

    private void showForgotPasswordDialog() {
        JTextField emailField = new JTextField(20);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Entrez votre email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Mot de passe oublié",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Un lien de réinitialisation a été envoyé à " + emailField.getText(),
                    "Email envoyé", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                showError("Veuillez remplir tous les champs");
                return;
            }

            try {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(username);
                loginRequest.setPassword(password);

                Utilisateur utilisateur = securityManager.authenticate(loginRequest);

                if (utilisateur != null) {
                    utilisateur.setLastLogin(new Date());
                    UserProfilePanel profilePanel = new UserProfilePanel(mainFrame, utilisateur, securityManager);
                    mainFrame.switchView(profilePanel, "Profil Utilisateur");
                } else {
                    showError("Identifiants incorrects");
                }
            } catch (Exception ex) {
                showError("Erreur lors de l'authentification: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void showError(String message) {
            JOptionPane.showMessageDialog(mainFrame,
                    message,
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}