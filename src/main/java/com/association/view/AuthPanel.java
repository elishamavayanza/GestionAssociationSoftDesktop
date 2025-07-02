package com.association.view;

import com.association.security.SecurityManager;
import com.association.manager.dto.LoginRequest;
import com.association.model.access.Utilisateur;
import com.association.view.components.*;
import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AuthPanel extends JPanel {
    private final SecurityManager securityManager = SecurityManager.getInstance();
    private final LoginFrame loginFrame;

    private CustomTextField usernameField;
    private CustomPasswordField passwordField;
    private CustomButton loginButton;
    private JCheckBox rememberMeCheckbox;
    private JLabel forgotPasswordLabel;

    public AuthPanel(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
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

// Effet de survol
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                titleLabel.setForeground(Colors.PRIMARY.darker());
                titleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                titleLabel.setForeground(Colors.PRIMARY);
                titleLabel.setCursor(Cursor.getDefaultCursor());
            }
        });

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
        usernameField.setText(System.getenv("APP_USERNAME") != null ? System.getenv("APP_USERNAME") : "Isaac");
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

// Effet de survol
        usernameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.PRIMARY),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.BORDER),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });

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
        passwordField.setText(System.getenv("APP_PASSWORD") != null ? System.getenv("APP_PASSWORD") : "Isaac@1234");
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

// Effet de survol
        passwordField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.PRIMARY),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colors.BORDER),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(passwordField, gbc);

        // Case "Se souvenir de moi"
        rememberMeCheckbox = new JCheckBox("Se souvenir de moi");
        rememberMeCheckbox.setBackground(Colors.BACKGROUND);
        rememberMeCheckbox.setForeground(Colors.TEXT);
        rememberMeCheckbox.setFocusPainted(false);

// Effets de survol
        rememberMeCheckbox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                rememberMeCheckbox.setForeground(Colors.PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rememberMeCheckbox.setForeground(Colors.TEXT);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(rememberMeCheckbox, gbc);

        // Bouton de connexion
        loginButton = new CustomButton("Se connecter");
        loginButton.setBackground(Colors.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.PRIMARY.darker()),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));

// Effets de survol
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(Colors.PRIMARY.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(Colors.PRIMARY);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // Lien "Mot de passe oublié"
        forgotPasswordLabel = new JLabel("<html><u>Mot de passe oublié ?</u></html>", SwingConstants.CENTER);
        forgotPasswordLabel.setForeground(Colors.PRIMARY);
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

// Effets de survol
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPasswordLabel.setForeground(Colors.PRIMARY.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotPasswordLabel.setForeground(Colors.PRIMARY);
            }

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

        int result = JOptionPane.showConfirmDialog(loginFrame, panel, "Mot de passe oublié",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(loginFrame,
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
                    UserProfilePanel profilePanel = new UserProfilePanel(loginFrame, utilisateur);
                    loginFrame.switchView(profilePanel, "Profil Utilisateur");
                } else {
                    showError("Identifiants incorrects");
                }
            } catch (Exception ex) {
                showError("Erreur lors de l'authentification: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void showError(String message) {
            // Création du panel personnalisé
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(Colors.ERROR_BACKGROUND);
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Configuration du message
            JLabel messageLabel = new JLabel("<html><div style='width: 250px;'>" + message + "</div></html>");
            messageLabel.setFont(Fonts.labelFont());
            messageLabel.setForeground(Colors.CURRENT_TEXT);

            // Ajout de l'icône d'erreur
            Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
            if (errorIcon != null) {
                JLabel iconLabel = new JLabel(errorIcon);
                panel.add(iconLabel, BorderLayout.WEST);
            }

            panel.add(messageLabel, BorderLayout.CENTER);

            // Création de la boîte de dialogue
            JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE);
            JDialog dialog = pane.createDialog(loginFrame, "Erreur");

            // Configuration de l'icône de la fenêtre
            if (errorIcon instanceof ImageIcon) {
                dialog.setIconImage(((ImageIcon)errorIcon).getImage());
            }

            // Affichage de la boîte de dialogue
            dialog.setVisible(true);

            // Réinitialisation du champ mot de passe après fermeture
            passwordField.setText("");
        }
    }


}