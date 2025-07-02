package com.association.view.components;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class IconManager {
    public static ImageIcon getScaledIcon(String path, int width, int height) {
        try {
            URL iconUrl = IconManager.class.getClassLoader().getResource("icons/" + path);
            if (iconUrl == null) {
                System.err.println("Icon not found: icons/" + path);
                return createFallbackIcon(width, height);
            }

            if (path.toLowerCase().endsWith(".svg")) {
                return loadSvgIcon(iconUrl, width, height);
            } else {
                ImageIcon originalIcon = new ImageIcon(iconUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
            return createFallbackIcon(width, height);
        }
    }

    private static ImageIcon createFallbackIcon(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return new ImageIcon(img);
    }

    private static ImageIcon loadSvgIcon(URL svgUrl, int width, int height) throws Exception {
        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float)width);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float)height);

        TranscoderInput input = new TranscoderInput(svgUrl.openStream());
        try {
            transcoder.transcode(input, null);
            return new ImageIcon(transcoder.getBufferedImage());
        } catch (Exception e) {
            throw new RuntimeException("Error transcoding SVG", e);
        }
    }

    public static ImageIcon getIcon(String iconPath, int size) {
        return getScaledIcon(iconPath, size, size);
    }

    public static ImageIcon createBadgedIcon(String iconName, String badgeText, int size, Color badgeColor) {
        // Charger l'icône de base
        ImageIcon baseIcon = getIcon(iconName, size);
        if (baseIcon == null) return null;

        // Créer une nouvelle image avec badge
        BufferedImage badgedImage = new BufferedImage(
                size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = badgedImage.createGraphics();

        // Dessiner l'icône de base
        baseIcon.paintIcon(null, g2d, 0, 0);

        // Dessiner le badge
        int badgeSize = size / 3;
        int badgeX = size - badgeSize;
        int badgeY = 0;

        g2d.setColor(badgeColor);
        g2d.fillOval(badgeX, badgeY, badgeSize, badgeSize);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, badgeSize / 2));

        // Centrer le texte dans le badge
        FontMetrics fm = g2d.getFontMetrics();
        int textX = badgeX + (badgeSize - fm.stringWidth(badgeText)) / 2;
        int textY = badgeY + ((badgeSize - fm.getHeight()) / 2) + fm.getAscent();

        g2d.drawString(badgeText, textX, textY);
        g2d.dispose();

        return new ImageIcon(badgedImage);
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage img;

        @Override
        public BufferedImage createImage(int w, int h) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput output) {
            this.img = img;
        }

        public BufferedImage getBufferedImage() {
            return img;
        }
    }

    public static JButton createIconButton(String iconPath, String tooltip, int size) {
        JButton button = new JButton(getScaledIcon(iconPath, size, size));
        button.setToolTipText(tooltip);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(), // Bordure extérieure
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Padding intérieur
        ));
        button.setContentAreaFilled(false);
        return button;
    }
}