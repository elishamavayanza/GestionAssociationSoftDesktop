package com.association.view.components.admin.Photo;

import com.association.view.components.IconManager;
import com.association.view.styles.Colors;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PhotoEditorDialog extends JDialog {
    private BufferedImage originalImage;
    private BufferedImage displayedImage;
    private JLabel imageLabel;
    private JSlider zoomSlider;
    private JSlider rotateSlider;
    private JButton cropButton;
    private JButton resetButton;
    private JButton flipHorizontalButton;
    private JButton flipVerticalButton;
    private double rotationAngle = 0;
    private double scaleFactor = 1.0;
    private Rectangle cropRect;
    private Point dragStart;
    private boolean isCropping = false;
    private boolean isFlippedHorizontal = false;
    private boolean isFlippedVertical = false;
    private Point lastDragPoint;
    private JPanel imagePanel; // Déplacez cette déclaration ici



    public PhotoEditorDialog(JFrame parent, byte[] photoData) {
        super(parent, "Éditer la photo", true);
        setSize(850, 750);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Colors.CURRENT_BACKGROUND);

        originalImage = toBufferedImage(new ImageIcon(photoData).getImage());
        displayedImage = originalImage;

        initUI();
        applyTheme();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Panel pour l'image avec MouseListener pour le recadrage et le glissement
        imagePanel = new JPanel() { // Pas besoin de BorderLayout ici
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (displayedImage != null) {
                    // Centre l'image dans le panel
                    int x = (getWidth() - displayedImage.getWidth()) / 2;
                    int y = (getHeight() - displayedImage.getHeight()) / 2;
                    g.drawImage(displayedImage, x, y, this);

                    // Dessin du rectangle de recadrage
                    if (isCropping && cropRect != null) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setColor(Colors.CURRENT_PRIMARY);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawRect(x + cropRect.x, y + cropRect.y,
                                cropRect.width, cropRect.height);

                        // Zones assombries
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                        g2d.setColor(new Color(0, 0, 0, 150));
                        g2d.fillRect(0, 0, getWidth(), y + cropRect.y);
                        g2d.fillRect(0, y + cropRect.y, x + cropRect.x, cropRect.height);
                        g2d.fillRect(x + cropRect.x + cropRect.width, y + cropRect.y,
                                getWidth() - (x + cropRect.x + cropRect.width), cropRect.height);
                        g2d.fillRect(0, y + cropRect.y + cropRect.height,
                                getWidth(), getHeight() - (y + cropRect.y + cropRect.height));
                        g2d.dispose();
                    }
                }
            }

            @Override
            public Dimension getPreferredSize() {
                if (displayedImage == null) {
                    return new Dimension(0, 0);
                }
                return new Dimension(displayedImage.getWidth(), displayedImage.getHeight());
            }
        };
        imagePanel.setBackground(Colors.CURRENT_BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.getViewport().setBackground(Colors.CURRENT_BACKGROUND);
//        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // Gestion des événements de souris
        MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    lastDragPoint = e.getPoint();
                    if (isCropping) {
                        dragStart = e.getPoint();
                        cropRect = new Rectangle(dragStart);
                    }
                }


                @Override
            public void mouseReleased(MouseEvent e) {
                if (isCropping && cropRect != null) {
                    if (cropRect.width < 0) {
                        cropRect.x += cropRect.width;
                        cropRect.width = -cropRect.width;
                    }
                    if (cropRect.height < 0) {
                        cropRect.y += cropRect.height;
                        cropRect.height = -cropRect.height;
                    }
                    imagePanel.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isCropping) {
                    if (dragStart != null) {
                        int x = Math.min(dragStart.x, e.getX());
                        int y = Math.min(dragStart.y, e.getY());
                        int width = Math.abs(e.getX() - dragStart.x);
                        int height = Math.abs(e.getY() - dragStart.y);

                        // Utilisez les dimensions de l'image affichée
                        width = Math.min(width, displayedImage.getWidth() - x);
                        height = Math.min(height, displayedImage.getHeight() - y);

                        cropRect.setBounds(x, y, width, height);
                        imagePanel.repaint();
                    }
                } else {
                    // Glissement pour zoom/rotation
                    if (lastDragPoint != null) {
                        int dx = e.getX() - lastDragPoint.x;
                        int dy = e.getY() - lastDragPoint.y;

                        // Contrôle + glissement horizontal = zoom
                        if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                            int newZoom = zoomSlider.getValue() + dx;
                            zoomSlider.setValue(Math.max(10, Math.min(200, newZoom)));
                        }
                        // Maj + glissement horizontal = rotation
                        else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                            int newRotation = rotateSlider.getValue() + dx;
                            rotateSlider.setValue(Math.max(-180, Math.min(180, newRotation)));
                        }

                        lastDragPoint = e.getPoint();
                    }
                }
            }
        };

        imagePanel.addMouseListener(mouseAdapter);
        imagePanel.addMouseMotionListener(mouseAdapter);

        add(scrollPane, BorderLayout.CENTER);

        // Panel de contrôle principal
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Colors.CURRENT_CARD_BACKGROUND);
        controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Colors.CURRENT_BORDER));

        // Panel pour les sliders
        JPanel sliderPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        sliderPanel.setBackground(Colors.CURRENT_CARD_BACKGROUND);
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Slider pour le zoom
        JPanel zoomPanel = new JPanel(new BorderLayout(5, 0));
        zoomPanel.setBackground(Colors.CURRENT_CARD_BACKGROUND);
        zoomSlider = new JSlider(10, 200, 100);
        customizeSlider(zoomSlider);
        zoomSlider.addChangeListener(e -> {
            scaleFactor = zoomSlider.getValue() / 100.0;
            updateImage();
            if (isCropping) {
                cropRect = null;
                imagePanel.repaint();
            }
        });
        zoomPanel.add(new JLabel("Zoom:"), BorderLayout.WEST);
        zoomPanel.add(zoomSlider, BorderLayout.CENTER);
        JLabel zoomValueLabel = new JLabel("100%");
        zoomValueLabel.setForeground(Colors.CURRENT_TEXT_SECONDARY);
        zoomPanel.add(zoomValueLabel, BorderLayout.EAST);
        zoomSlider.addChangeListener(e -> zoomValueLabel.setText(zoomSlider.getValue() + "%"));

        // Slider pour la rotation
        JPanel rotatePanel = new JPanel(new BorderLayout(5, 0));
        rotatePanel.setBackground(Colors.CURRENT_CARD_BACKGROUND);
        rotateSlider = new JSlider(-180, 180, 0);
        customizeSlider(rotateSlider);
        rotateSlider.setMajorTickSpacing(45);
        rotateSlider.setMinorTickSpacing(15);
        rotateSlider.setPaintTicks(true);
        rotateSlider.setPaintLabels(true);
        rotateSlider.setMajorTickSpacing(90);  // Marques tous les 90°
        rotateSlider.setMinorTickSpacing(30);  // Marques mineures tous les 30°
        rotateSlider.addChangeListener(e -> {
            rotationAngle = Math.toRadians(rotateSlider.getValue());
            updateImage();
            if (isCropping) {
                cropRect = null;
                imagePanel.repaint();
            }
        });
        rotatePanel.add(new JLabel("Rotation:"), BorderLayout.WEST);
        rotatePanel.add(rotateSlider, BorderLayout.CENTER);
        JLabel rotateValueLabel = new JLabel("0°");
        rotateValueLabel.setForeground(Colors.CURRENT_TEXT_SECONDARY);
        rotatePanel.add(rotateValueLabel, BorderLayout.EAST);
        rotateSlider.addChangeListener(e -> rotateValueLabel.setText(rotateSlider.getValue() + "°"));

        sliderPanel.add(zoomPanel);
        sliderPanel.add(rotatePanel);
        controlPanel.add(sliderPanel, BorderLayout.NORTH);

        // Panel pour les boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Colors.CURRENT_CARD_BACKGROUND);

        // Bouton de retournement horizontal
        flipHorizontalButton = new JButton();
        flipHorizontalButton.setIcon(IconManager.getIcon("flip-horizontal.svg", 20));
        flipHorizontalButton.setToolTipText("Retournement horizontal");
        customizeButton(flipHorizontalButton);
        flipHorizontalButton.addActionListener(e -> {
            isFlippedHorizontal = !isFlippedHorizontal;
            updateImage();
            if (isCropping) {
                cropRect = null;
                imagePanel.repaint();
            }
        });

        // Bouton de retournement vertical
        flipVerticalButton = new JButton();
        flipVerticalButton.setIcon(IconManager.getIcon("flip-vertical.svg", 20));
        flipVerticalButton.setToolTipText("Retournement vertical");
        customizeButton(flipVerticalButton);
        flipVerticalButton.addActionListener(e -> {
            isFlippedVertical = !isFlippedVertical;
            updateImage();
            if (isCropping) {
                cropRect = null;
                imagePanel.repaint();
            }
        });

        // Bouton de recadrage
        cropButton = new JButton();
        cropButton.setIcon(IconManager.getIcon("crop.svg", 20));
        cropButton.setToolTipText("Recadrer");
        customizeButton(cropButton);
        cropButton.addActionListener(e -> {
            isCropping = !isCropping;
            cropButton.setToolTipText(isCropping ? "Valider recadrage" : "Recadrer");
            if (!isCropping && cropRect != null) {
                applyCrop();
            }
            cropRect = null;
            imagePanel.repaint();
        });


        // Bouton de réinitialisation
        resetButton = new JButton();
        resetButton.setIcon(IconManager.getIcon("reset.svg", 20));
        resetButton.setToolTipText("Réinitialiser");
        customizeButton(resetButton);
        resetButton.addActionListener(e -> {
            rotationAngle = 0;
            scaleFactor = 1.0;
            isFlippedHorizontal = false;
            isFlippedVertical = false;
            zoomSlider.setValue(100);
            rotateSlider.setValue(0);
            displayedImage = originalImage;
            isCropping = false;
            cropButton.setToolTipText("Recadrer");
            cropRect = null;
            imagePanel.revalidate();
            imagePanel.repaint();
        });

        // Boutons OK/Annuler
        JButton okButton = new JButton();
        okButton.setIcon(IconManager.getIcon("check.svg", 20));
        okButton.setToolTipText("OK");
        customizePrimaryButton(okButton);
        okButton.addActionListener(e -> dispose());

        JButton cancelButton = new JButton();
        cancelButton.setIcon(IconManager.getIcon("close.svg", 20));
        cancelButton.setToolTipText("Annuler");
        customizeButton(cancelButton);
        cancelButton.addActionListener(e -> {
            displayedImage = originalImage;
            dispose();
        });


        buttonPanel.add(flipHorizontalButton);
        buttonPanel.add(flipVerticalButton);
        buttonPanel.add(cropButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.SOUTH);
    }


    private void customizeButton(JButton button) {
        button.setBackground(Colors.CURRENT_CARD_BACKGROUND);
        button.setForeground(Colors.CURRENT_TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_BORDER, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Colors.CURRENT_INPUT_BACKGROUND);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Colors.CURRENT_CARD_BACKGROUND);
            }
        });
    }

    private void customizePrimaryButton(JButton button) {
        button.setBackground(Colors.CURRENT_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.CURRENT_PRIMARY_DARK, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Colors.CURRENT_PRIMARY_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Colors.CURRENT_PRIMARY);
            }
        });
    }

    private void customizeSlider(JSlider slider) {
        slider.setBackground(Colors.CURRENT_CARD_BACKGROUND);
        slider.setForeground(Colors.CURRENT_TEXT);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);

        // Personnalisation des couleurs du slider
        slider.setUI(new javax.swing.plaf.basic.BasicSliderUI(slider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Rectangle trackBounds = trackRect;
                int cy = (trackBounds.height / 2) - 2;

                // Fond de la piste
                g2d.setColor(Colors.CURRENT_INPUT_BACKGROUND);
                g2d.fillRoundRect(trackBounds.x, trackBounds.y + cy - 2,
                        trackBounds.width, 4, 2, 2);

                // Partie remplie de la piste
                g2d.setColor(Colors.CURRENT_PRIMARY);
                g2d.fillRoundRect(trackBounds.x, trackBounds.y + cy - 2,
                        thumbRect.x - trackBounds.x, 4, 2, 2);
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(Colors.CURRENT_PRIMARY);
                g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);

                g2d.setColor(Colors.CURRENT_PRIMARY_DARK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            }

            @Override
            public void paintFocus(Graphics g) {}
        });
    }

    private void applyTheme() {
        // Appliquer le thème à tous les composants
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                scrollPane.getViewport().setBackground(Colors.CURRENT_BACKGROUND);
            }
        }
    }

    private void updateImage() {
        // Calculer la nouvelle taille
        int newWidth = (int)(originalImage.getWidth() * scaleFactor);
        int newHeight = (int)(originalImage.getHeight() * scaleFactor);

        BufferedImage transformedImage = new BufferedImage(
                newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = transformedImage.createGraphics();
        AffineTransform transform = new AffineTransform();

        // Appliquer les transformations
        transform.scale(scaleFactor, scaleFactor);

        if (isFlippedHorizontal || isFlippedVertical) {
            double flipX = isFlippedHorizontal ? -1 : 1;
            double flipY = isFlippedVertical ? -1 : 1;
            double px = isFlippedHorizontal ? originalImage.getWidth() / 2.0 : 0;
            double py = isFlippedVertical ? originalImage.getHeight() / 2.0 : 0;

            transform.translate(px, py);
            transform.scale(flipX, flipY);
            transform.translate(-px, -py);
        }

        transform.translate(newWidth/2.0, newHeight/2.0);
        transform.rotate(rotationAngle);
        transform.translate(-originalImage.getWidth()/2.0, -originalImage.getHeight()/2.0);

        g2d.drawImage(originalImage, transform, null);
        g2d.dispose();

        displayedImage = transformedImage;
        imagePanel.revalidate(); // Important pour mettre à jour la taille
        imagePanel.repaint();
    }

    private void applyCrop() {
        if (cropRect == null || cropRect.width <= 0 || cropRect.height <= 0) return;

        try {
            BufferedImage croppedImage = new BufferedImage(
                    cropRect.width, cropRect.height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = croppedImage.createGraphics();
            g.drawImage(displayedImage,
                    0, 0, cropRect.width, cropRect.height,
                    cropRect.x, cropRect.y, cropRect.x + cropRect.width, cropRect.y + cropRect.height,
                    null);
            g.dispose();

            displayedImage = croppedImage;
            originalImage = displayedImage;
            rotationAngle = 0;
            scaleFactor = 1.0;
            zoomSlider.setValue(100);
            rotateSlider.setValue(0);
            imagePanel.revalidate();
            imagePanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du recadrage: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public byte[] getEditedImageData() {
        return imageToBytes(displayedImage);
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    private byte[] imageToBytes(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}