package com.association.view.login;

import javax.swing.*;

public class LoginFrame extends JFrame {
    private JPanel currentView;

    public LoginFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void switchView(JPanel newView, String authentification) {
        if (currentView != null) {
            remove(currentView);
        }
        currentView = newView;
        setContentPane(newView);
        revalidate();
        repaint();

        // Ajuster la taille si nécessaire
        pack();
        setSize(400, 350); // Conserver la taille fixe
    }
}