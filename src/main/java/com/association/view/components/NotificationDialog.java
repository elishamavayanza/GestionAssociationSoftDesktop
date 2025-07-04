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

    // Méthode existante (gardée pour compatibilité)
    public static void showNotification(JFrame parent, String message, String type) {
        showNotification(parent, message, type, null);
    }

    // Nouvelle méthode avec paramètre d'icône
    public static void showNotification(JFrame parent, String message, String type, String iconName) {
        // Créer une icône basée sur le type de notification
        Icon icon;
        Color bgColor;
        Color borderColor;
        playBeepSound(type);


        if (iconName != null) {
            icon = IconManager.getIcon(iconName, 24);
        } else {
            switch (type.toLowerCase()) {
                case "success":
                    icon = IconManager.getIcon("check_circles.svg", 24);
                    break;
                case "warning":
                    icon = IconManager.getIcon("warning.svg", 24);
                    break;
                case "error":
                    icon = IconManager.getIcon("error.svg", 24);
                    break;
                default:
                    icon = IconManager.getIcon("info.svg", 24);
            }
        }

        // Couleurs selon le type
        switch (type.toLowerCase()) {
            case "success":
                bgColor = Colors.SUCCESS;
                borderColor = Colors.SUCCESS.darker();
                break;
            case "warning":
                bgColor = Colors.WARNING;
                borderColor = Colors.WARNING.darker();
                break;
            case "error":
                bgColor = Colors.DANGER;
                borderColor = Colors.DANGER.darker();
                break;
            default:
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
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor.brighter(), 2),
                        new EmptyBorder(10, 15, 10, 15)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        new EmptyBorder(10, 15, 10, 15)
                ));
            }
        });

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
                float opacity = 0;
                while (opacity < 1) {
                    opacity += 0.05f;
                    if (opacity > 1) opacity = 1;

                    final float finalOpacity = opacity;
                    SwingUtilities.invokeLater(() -> dialog.setOpacity(finalOpacity));
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private static void playBeepSound(String type) {
        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioFormat format = new AudioFormat(44100, 8, 1, true, false);

                byte[] data;
                switch (type.toLowerCase()) {
                    case "success":
                        data = generateTone(800, 300, format);
                        break;
                    case "warning":
                        data = generateTone(600, 400, format);
                        break;
                    case "error":
                        data = generateTone(400, 600, format);
                        break;
                    default:
                        data = generateTone(500, 200, format);
                }

                clip.open(format, data, 0, data.length);
                clip.start();
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep(); // Fallback
            }
        }).start();
    }

    private static byte[] generateTone(int freq, int durationMs, AudioFormat format) {
        int samples = (int) (durationMs * format.getSampleRate() / 1000);
        byte[] data = new byte[samples];

        for (int i = 0; i < samples; i++) {
            double angle = 2.0 * Math.PI * freq * i / format.getSampleRate();
            data[i] = (byte) (Math.sin(angle) * 127.0);
        }

        return data;
    }


}