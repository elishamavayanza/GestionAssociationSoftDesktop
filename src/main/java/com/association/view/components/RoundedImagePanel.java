package com.association.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
public class RoundedImagePanel extends JPanel {
    private ImageIcon imageIcon;

    public RoundedImagePanel(ImageIcon imageIcon) {
        this.imageIcon = imageIcon;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int diameter = Math.min(getWidth(), getHeight());

        // Dessiner une ombre légère (optionnel)
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(1, 2, diameter-2, diameter-2);

        // Créer le masque circulaire
        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, diameter, diameter);
        g2d.setClip(circle);

        // Dessiner l'image
        g2d.drawImage(imageIcon.getImage(), 0, 0, diameter, diameter, this);

        // Dessiner une bordure blanche fine
        g2d.setClip(null);
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(Color.WHITE);
        g2d.drawOval(1, 1, diameter-3, diameter-3);

        g2d.dispose();
    }
}