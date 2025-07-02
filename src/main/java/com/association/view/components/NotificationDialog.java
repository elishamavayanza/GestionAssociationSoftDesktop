package com.association.view.components;

import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class NotificationDialog {
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 80;
    private static final int DISPLAY_TIME = 5000; // 5 secondes

    public static void showNotification(JFrame parent, String message, String type) {
        // Créer une icône basée sur le type de notification
        Icon icon;
        Color bgColor;
        Color borderColor;
        playBeepSound(type);


        switch(type.toLowerCase()) {
            case "success":
                icon = IconManager.getIcon("check_circles.svg", 24);
                bgColor = Colors.SUCCESS;
                borderColor = Colors.SUCCESS.darker();
                break;
            case "warning":
                icon = IconManager.getIcon("warning.svg", 24);
                bgColor = Colors.WARNING;
                borderColor = Colors.WARNING.darker();
                break;
            case "error":
                icon = IconManager.getIcon("error.svg", 24);
                bgColor = Colors.DANGER;
                borderColor = Colors.DANGER.darker();
                break;
            default:
                icon = IconManager.getIcon("info.svg", 24);
                bgColor = Colors.INFO;
                borderColor = Colors.INFO.darker();
        }

        // Créer une fenêtre de notification non modale
        JDialog dialog = new JDialog(parent, "", false);
        dialog.setUndecorated(true);
        dialog.setSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);

        // Positionner en haut à droite de l'écran
        GraphicsConfiguration config = dialog.getGraphicsConfiguration();
        Rectangle bounds = config.getBounds();
        dialog.setLocation(
                bounds.x + bounds.width - NOTIFICATION_WIDTH - 20,
                bounds.y + 20
        );

        // Créer le contenu de la notification
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        panel.setBackground(bgColor);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Ajouter l'icône
        JLabel iconLabel = new JLabel(icon);
        panel.add(iconLabel, BorderLayout.WEST);

        // Ajouter le message avec un style amélioré
        JLabel messageLabel = new JLabel(message);  // message contient tes balises HTML
        messageLabel.setFont(Fonts.labelFont());
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(messageLabel, BorderLayout.CENTER);


        // Ajouter un bouton de fermeture
        JLabel closeLabel = new JLabel(IconManager.getIcon("close.svg", 16));
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
            }
        });
        panel.add(closeLabel, BorderLayout.EAST);

        // Fermeture automatique après un délai
        Timer timer = new Timer(DISPLAY_TIME, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();

        // Fermer au clic
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                timer.stop();
                dialog.dispose();
            }
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);

        // Animation d'apparition
        new Thread(() -> {
            try {
                for (float opacity = 0; opacity <= 1; opacity += 0.1f) {
                    final float finalOpacity = opacity;
                    SwingUtilities.invokeLater(() -> dialog.setOpacity(finalOpacity));
                    Thread.sleep(30);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private static void playBeepSound(String type) {
        new Thread(() -> {
            try {
                int frequency = 500;
                int duration = 500;

                switch(type.toLowerCase()) {
                    case "success":
                        frequency = 800;
                        duration = 300;
                        break;
                    case "warning":
                        frequency = 600;
                        duration = 400;
                        break;
                    case "error":
                        frequency = 400;
                        duration = 600;
                        break;
                    default:
                        frequency = 500;
                        duration = 200;
                }

                Toolkit.getDefaultToolkit().beep(); // Son système
                // Si tu veux un son généré (par onde sinusoïdale), il faut coder ou utiliser un fichier WAV.

            } catch (Exception e) {
                System.err.println("Erreur sonore: " + e.getMessage());
            }
        }).start();
    }


}