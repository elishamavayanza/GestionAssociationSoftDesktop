package com.association.view.components.admin;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class CircularImageUtil {
    public static ImageIcon createCircularIcon(Image image, int diameter) {
        // Convertir l'image en BufferedImage si ce n'est pas déjà le cas
        BufferedImage bufferedImage;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage) image;
        } else {
            bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
        }

        // Créer une image avec un masque circulaire
        BufferedImage circularImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circularImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Créer un masque circulaire
        Shape circle = new Ellipse2D.Double(0, 0, diameter, diameter);
        g2.setClip(circle);

        // Dessiner l'image redimensionnée dans le cercle
        g2.drawImage(bufferedImage, 0, 0, diameter, diameter, null);

        // Optionnel: ajouter une bordure
        g2.setClip(null);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.LIGHT_GRAY);
        g2.draw(circle);

        g2.dispose();

        return new ImageIcon(circularImage);
    }
}