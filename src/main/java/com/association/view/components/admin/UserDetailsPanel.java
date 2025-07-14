package com.association.view.components.admin;

import com.association.dao.UtilisateurDao;
import com.association.dao.DAOFactory;
import com.association.model.access.Utilisateur;
import com.association.view.styles.Colors;

import javax.swing.*;
import java.awt.*;

public class UserDetailsPanel extends JPanel {
    private final JFrame parentFrame;
    private final UtilisateurDao utilisateurDao;
    private Long currentUserId;
    private JLabel idLabel, usernameLabel, emailLabel;

    public UserDetailsPanel(JFrame parentFrame, Long userId) {
        this.parentFrame = parentFrame;
        this.utilisateurDao = DAOFactory.getInstance(UtilisateurDao.class);
        this.currentUserId = userId;
        initComponents();
        updateUserData(userId);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        detailsPanel.setBackground(Colors.BACKGROUND);

        idLabel = new JLabel();
        usernameLabel = new JLabel();
        emailLabel = new JLabel();

        detailsPanel.add(new JLabel("ID:"));
        detailsPanel.add(idLabel);
        detailsPanel.add(new JLabel("Nom d'utilisateur:"));
        detailsPanel.add(usernameLabel);
        detailsPanel.add(new JLabel("Email:"));
        detailsPanel.add(emailLabel);

        add(detailsPanel, BorderLayout.CENTER);
    }

    public void updateUserData(Long userId) {
        this.currentUserId = userId;
        Utilisateur user = utilisateurDao.findById(userId).orElse(null);

        if (user != null) {
            idLabel.setText(user.getId().toString());
            usernameLabel.setText(user.getUsername());
            emailLabel.setText(user.getEmail());
        } else {
            idLabel.setText("N/A");
            usernameLabel.setText("Utilisateur non trouvé");
            emailLabel.setText("N/A");
        }
    }
}