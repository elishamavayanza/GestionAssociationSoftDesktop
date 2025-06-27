package com.association.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class LoadingSpinner extends JComponent {
    private float angle = 0;
    private Timer timer;
    private final Color spinnerColor = new Color(0, 120, 215);

    public LoadingSpinner() {
        setPreferredSize(new Dimension(50, 50));
        startAnimation();
    }

    private void startAnimation() {
        timer = new Timer(15, e -> {
            angle = (angle + 5) % 360;
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height) - 10;
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        // Dessiner le fond du spinner
        g2.setColor(new Color(200, 200, 200, 50));
        g2.fillOval(x, y, size, size);

        // Dessiner la partie animée
        Arc2D.Float arc = new Arc2D.Float(x, y, size, size, angle, 90, Arc2D.PIE);
        Area area = new Area(arc);
        Area hole = new Area(new Ellipse2D.Float(x + 5, y + 5, size - 10, size - 10));
        area.subtract(hole);

        g2.setColor(spinnerColor);
        g2.fill(area);

        g2.dispose();
    }

    public void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }
}