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
        return null;
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

    public static Icon getIcon(String iconPath, int size) {
        return getScaledIcon(iconPath, size, size);
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