package com.association.view.components;

import com.association.view.styles.Colors;
import com.association.view.styles.Fonts;
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

    public static ImageIcon getMaterialIcon(String iconName, int size) {
        try {
            // Mapping des noms d'icônes personnalisés aux noms Material Icons
            String materialIconName = mapCustomIconToMaterial(iconName);

            // Chemin vers les icônes Material (doivent être dans votre resources/icons/)
            String iconPath = "material/" + materialIconName + ".svg";

            // Chargement normal de l'icône
            ImageIcon icon = getIcon(iconPath, size);

            // Si l'icône n'est pas trouvée, créer une icône de fallback
            if (icon == null || icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                System.err.println("Material icon not found: " + iconPath);
                return createFallbackIcon(size, size);
            }

            return icon;
        } catch (Exception e) {
            System.err.println("Error loading Material icon: " + e.getMessage());
            return createFallbackIcon(size, size);
        }
    }

    private static String mapCustomIconToMaterial(String customName) {
        // Mapper vos noms d'icônes personnalisés aux noms Material Icons standards
        switch (customName) {
            case "groups": return "groups";
            case "active_user": return "person";
            case "inactive_user": return "person_outline";
            case "blocked_user": return "person_off";
            default: return customName;
        }
    }

    // Version animée (optionnelle)
    public static JLabel getAnimatedMaterialIcon(String iconName, int size, String animationType) {
        ImageIcon icon = getMaterialIcon(iconName, size);
        return new AnimatedIconLabel(icon, animationType);
    }

    public static class AnimatedIconLabel extends JLabel {
        private final String animationType;
        private float angle = 0f;
        private float scale = 1f;
        private boolean running = true;
        private final Timer animationTimer;

        public AnimatedIconLabel(ImageIcon icon, String animationType) {
            super(icon);
            this.animationType = animationType;
            setHorizontalAlignment(SwingConstants.CENTER);

            // Créer le timer pour l'animation
            animationTimer = new Timer(50, e -> {
                if (!running) return;

                switch (animationType) {
                    case "spin":
                        angle += 0.1f;
                        if (angle > 2 * Math.PI) angle = 0;
                        break;
                    case "pulse":
                        scale += 0.05f;
                        if (scale > 1.2f || scale < 0.8f) scale = scale > 1.2f ? 0.8f : 1.2f;
                        break;
                }
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Centrer les transformations
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            g2d.translate(centerX, centerY);

            if ("spin".equals(animationType)) {
                g2d.rotate(angle);
            } else if ("pulse".equals(animationType)) {
                g2d.scale(scale, scale);
            }

            g2d.translate(-centerX, -centerY);
            super.paintComponent(g2d);
            g2d.dispose();
        }

        public void setRunning(boolean running) {
            this.running = running;
            if (!running) {
                angle = 0f;
                scale = 1f;
                repaint();
            }
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            animationTimer.stop(); // Arrêter l'animation quand le composant est retiré
        }
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
    public static JLabel createLoadingLabel() {
        JLabel loadingLabel = new JLabel("Chargement...", IconManager.getIcon("loader.svg", 32), SwingConstants.CENTER);
        loadingLabel.setFont(Fonts.textFieldFont());
        loadingLabel.setForeground(Colors.TEXT);
        loadingLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        loadingLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        return loadingLabel;
    }
}