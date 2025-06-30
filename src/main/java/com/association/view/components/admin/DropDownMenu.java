package com.association.view.components;

import com.association.view.styles.Colors;
import com.association.view.styles.HoverButton;
import javax.swing.*;
import java.awt.*;

public class DropDownMenu extends JPanel {
    private final JButton mainButton;
    public final JPanel subMenuPanel;
    private boolean isExpanded = false;

    public DropDownMenu(String text, String iconPath) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Colors.SECONDARY);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Création du bouton principal avec un rendu personnalisé
        mainButton = new HoverButton(
                text,
                IconManager.getIcon(iconPath, 24),
                Colors.SECONDARY,
                Colors.SECONDARY.darker()
        ) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Dessiner la flèche à droite
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);

                int arrowSize = 8;
                int arrowX = getWidth() - 15; // Position X de la flèche
                int arrowY = getHeight() / 2;  // Position Y centrée

                if (isExpanded) {
                    // Dessiner une flèche vers le haut (▲)
                    int[] xPoints = {arrowX, arrowX + arrowSize, arrowX + arrowSize/2};
                    int[] yPoints = {arrowY + arrowSize/2, arrowY + arrowSize/2, arrowY - arrowSize/2};
                    g2.fillPolygon(xPoints, yPoints, 3);
                } else {
                    // Dessiner une flèche vers le bas (▼)
                    int[] xPoints = {arrowX, arrowX + arrowSize, arrowX + arrowSize/2};
                    int[] yPoints = {arrowY - arrowSize/2, arrowY - arrowSize/2, arrowY + arrowSize/2};
                    g2.fillPolygon(xPoints, yPoints, 3);
                }

                g2.dispose();
            }
        };

        mainButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainButton.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 20)); // Ajouter plus de padding à droite
        mainButton.setFont(new Font("Arial", Font.PLAIN, 14));
        mainButton.setForeground(Color.WHITE);
        mainButton.setMaximumSize(new Dimension(200, 50));

        // Panel pour les sous-éléments
        subMenuPanel = new JPanel();
        subMenuPanel.setLayout(new BoxLayout(subMenuPanel, BoxLayout.Y_AXIS));
        subMenuPanel.setBackground(Colors.SECONDARY.darker());
        subMenuPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subMenuPanel.setVisible(false);

        // Action pour développer/réduire
        mainButton.addActionListener(e -> toggleSubMenu());

        add(mainButton);
        add(subMenuPanel);
    }
    public JPanel getSubMenuPanel() {
        return subMenuPanel;
    }


    public void addSubMenuItem(String text, String iconPath) {
        HoverButton subButton = new HoverButton(
                text,
                IconManager.getIcon(iconPath, 20),
                Colors.SECONDARY.darker(),
                Colors.SECONDARY.darker().darker()
        );
        subButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        subButton.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 5));
        subButton.setFont(new Font("Arial", Font.PLAIN, 13));
        subButton.setForeground(Color.WHITE);
        subButton.setMaximumSize(new Dimension(200, 40));

        subMenuPanel.add(subButton);
        subMenuPanel.add(Box.createRigidArea(new Dimension(0, 2)));
    }

    private void toggleSubMenu() {
        isExpanded = !isExpanded;
        subMenuPanel.setVisible(isExpanded);
        mainButton.repaint(); // Redessiner le bouton pour mettre à jour la flèche
        revalidate();
        repaint();
    }
}