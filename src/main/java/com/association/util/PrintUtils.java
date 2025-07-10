package com.association.util;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.IOException;

public class PrintUtils {
    public enum PageSize { A4, LETTER }
    public enum Orientation { PORTRAIT, LANDSCAPE }

    private static final String LOGO_PATH = "/images/logo.jpg";
    private static final String LOGO_LEFT_PATH = "/images/RDC.jpg";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    public static void printTable(JTable table, String title, PageSize size,
                                  Orientation orientation, boolean showPrintDialog, JFrame parent) {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName(title);

            PageFormat pageFormat = job.defaultPage();
            Paper paper = new Paper();

            // Configuration du format de page
            double width = 842; // A4 par défaut (297mm en hauteur)
            double height = 595; // (210mm en largeur)

            if (size == PageSize.LETTER) {
                width = 792;
                height = 612;
            }

            if (orientation == Orientation.PORTRAIT) {
                double temp = width;
                width = height;
                height = temp;
                pageFormat.setOrientation(PageFormat.PORTRAIT);
            } else {
                pageFormat.setOrientation(PageFormat.LANDSCAPE);
            }

            // Marges (en points) - mêmes que dans ExportUtils
            float margin = 72; // 2.54 cm
            paper.setSize(width, height);
            paper.setImageableArea(margin, margin,
                    width - margin * 2,
                    height - margin * 2);
            pageFormat.setPaper(paper);

            // Créer le Printable avec le style du PDF
            Printable printable = new PdfStylePrintable(table, title, pageFormat);

            job.setPrintable(printable, pageFormat);

            if (!showPrintDialog || job.printDialog()) {
                job.print();
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(parent,
                    "Erreur lors de l'impression:\n" + e.getMessage(),
                    "Erreur d'impression", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class PdfStylePrintable implements Printable {
        private final JTable table;
        private final String title;
        private final PageFormat pageFormat;
        private BufferedImage logoLeft;
        private BufferedImage logoRight;

        public PdfStylePrintable(JTable table, String title, PageFormat pageFormat) {
            this.table = table;
            this.title = title;
            this.pageFormat = pageFormat;

            // Charger les logos
            try {
                try (InputStream is = getClass().getResourceAsStream(LOGO_LEFT_PATH)) {
                    if (is != null) logoLeft = ImageIO.read(is);
                }
                try (InputStream is = getClass().getResourceAsStream(LOGO_PATH)) {
                    if (is != null) logoRight = ImageIO.read(is);
                }
            } catch (IOException e) {
                System.err.println("Erreur de chargement des logos: " + e.getMessage());
            }
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Zone imprimable
            double pageWidth = pageFormat.getImageableWidth();
            double pageHeight = pageFormat.getImageableHeight();

            // Couleurs (mêmes que dans ExportUtils)
            Color headerBgColor = new Color(51, 102, 153); // Bleu foncé
            Color evenRowColor = new Color(240, 240, 240); // Gris clair

            int yPos = 0;

            // ===== EN-TÊTE (uniquement sur la première page) ===== //
            if (pageIndex == 0) {
                // Logo gauche
                if (logoLeft != null) {
                    int logoWidth = 80;
                    int logoHeight = 50;
                    g2d.drawImage(logoLeft, 0, yPos, logoWidth, logoHeight, null);
                }

                // Logo droit
                if (logoRight != null) {
                    int logoWidth = 80;
                    int logoHeight = 50;
                    int xPos = (int)pageWidth - logoWidth;
                    g2d.drawImage(logoRight, xPos, yPos, logoWidth, logoHeight, null);
                }

                yPos = 60; // Position verticale après les logos

                // Titre principal "ASSOCIATION AVEC"
                g2d.setFont(new Font("Helvetica", Font.BOLD, 10));
                String associationTitle = "ASSOCIATION AVEC";
                int titleWidth = g2d.getFontMetrics().stringWidth(associationTitle);
                g2d.drawString(associationTitle, (float)(pageWidth/2 - titleWidth/2), yPos);
                yPos += 15;

                // Informations
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 9));
                String address = "Siège social : .................................................................";
                titleWidth = g2d.getFontMetrics().stringWidth(address);
                g2d.drawString(address, (float)(pageWidth/2 - titleWidth/2), yPos);
                yPos += 15;

                String contact = "Email : ..........................| Tél : +243...............................";
                titleWidth = g2d.getFontMetrics().stringWidth(contact);
                g2d.drawString(contact, (float)(pageWidth/2 - titleWidth/2), yPos);
                yPos += 15;

                String legal = "SIRET : .........................| RNA : ....................................";
                titleWidth = g2d.getFontMetrics().stringWidth(legal);
                g2d.drawString(legal, (float)(pageWidth/2 - titleWidth/2), yPos);
                yPos += 20;

                // Année
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 10));
                String year = "Année " + LocalDateTime.now().getYear();
                titleWidth = g2d.getFontMetrics().stringWidth(year);
                g2d.drawString(year, (float)(pageWidth/2 - titleWidth/2), yPos);
                yPos += 30;

                // Titre du document
                g2d.setFont(new Font("Helvetica", Font.BOLD, 14));
                g2d.setColor(Color.DARK_GRAY);
                titleWidth = g2d.getFontMetrics().stringWidth(title.toUpperCase());
                g2d.drawString(title.toUpperCase(), (float)(pageWidth/2 - titleWidth/2), yPos);
                yPos += 20;

                // Date d'export
                g2d.setFont(new Font("Helvetica", Font.PLAIN, 10));
                g2d.setColor(Color.GRAY);
                String exportDate = "Export généré le " + LocalDateTime.now().format(DATE_FORMATTER);
                int dateWidth = g2d.getFontMetrics().stringWidth(exportDate);
                g2d.drawString(exportDate, (float)(pageWidth/2 - dateWidth/2), yPos);
                yPos += 30;
            } else {
                // Pour les autres pages, on commence plus haut
                yPos = 30;
            }


            // ===== TABLEAU ===== //
            int tableWidth = 0;
            int[] colWidths = new int[table.getColumnCount()];

            // Calculer les largeurs de colonnes
            for (int col = 0; col < table.getColumnCount(); col++) {
                int colWidth = 0;
                // Largeur de l'en-tête
                String header = table.getColumnName(col);
                if (col == 0 && "ID".equalsIgnoreCase(header)) {
                    header = "N°"; // Remplace "ID" par "N°" comme dans ExportUtils
                }
                colWidth = Math.max(colWidth, g2d.getFontMetrics().stringWidth(header) + 20);

                // Largeur des données
                for (int row = 0; row < table.getRowCount(); row++) {
                    Object value = table.getValueAt(row, col);
                    if (col == 0 && "ID".equalsIgnoreCase(table.getColumnName(col))) {
                        value = String.format("%02d", row + 1); // Format 01, 02, etc.
                    }
                    String text = (value != null) ? value.toString() : "";
                    colWidth = Math.max(colWidth, g2d.getFontMetrics().stringWidth(text) + 10);
                }

                colWidths[col] = colWidth;
                tableWidth += colWidth;
            }

            // Ajuster l'échelle si le tableau est trop large
            double scale = 1.0;
            if (tableWidth > pageWidth) {
                scale = pageWidth / tableWidth;
                g2d.scale(scale, 1.0);
            }

            // En-têtes du tableau
            g2d.setFont(new Font("Helvetica", Font.BOLD, 10));
            g2d.setColor(Color.WHITE);

            int xPos = 0;
            for (int col = 0; col < table.getColumnCount(); col++) {
                String header = table.getColumnName(col);
                if (col == 0 && "ID".equalsIgnoreCase(header)) {
                    header = "N°";
                }

                // Dessiner le fond de l'en-tête
                g2d.setColor(headerBgColor);
                g2d.fillRect(xPos, yPos, colWidths[col], 25);
                g2d.setColor(Color.WHITE);

                // Dessiner le texte centré
                int textWidth = g2d.getFontMetrics().stringWidth(header);
                g2d.drawString(header, xPos + (colWidths[col] - textWidth)/2, yPos + 18);

                // Dessiner les bordures
                g2d.setColor(Color.WHITE);
                g2d.drawRect(xPos, yPos, colWidths[col], 25);

                xPos += colWidths[col];
            }
            yPos += 25;

            // Données du tableau
            g2d.setFont(new Font("Helvetica", Font.PLAIN, 10));

            int rowsPerPage = (int)((pageHeight - yPos - 50) / 20); // Réserver de l'espace pour le pied de page
            int startRow = pageIndex * rowsPerPage;
            int endRow = Math.min(startRow + rowsPerPage, table.getRowCount());

            if (startRow >= table.getRowCount()) {
                return NO_SUCH_PAGE;
            }

            for (int row = startRow; row < endRow; row++) {
                xPos = 0;
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Object value = table.getValueAt(row, col);
                    if (col == 0 && "ID".equalsIgnoreCase(table.getColumnName(col))) {
                        value = String.format("%02d", row + 1);
                    }
                    String text = (value != null) ? value.toString() : "";

                    // Alternance des couleurs de fond
                    if (row % 2 == 0) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(evenRowColor);
                    }
                    g2d.fillRect(xPos, yPos, colWidths[col], 20);

                    // Dessiner le texte
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(text, xPos + 5, yPos + 15);

                    // Bordures
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawRect(xPos, yPos, colWidths[col], 20);

                    xPos += colWidths[col];
                }
                yPos += 20;
            }

            // Pied de page (numéro de page)
            g2d.setFont(new Font("Helvetica", Font.PLAIN, 8));
            g2d.setColor(Color.GRAY);
            String footer = "Page " + (pageIndex + 1);
            int footerWidth = g2d.getFontMetrics().stringWidth(footer);
            g2d.drawString(footer, (int)(pageWidth/2 - footerWidth/2), (int)(pageHeight - 10));

            return PAGE_EXISTS;
        }
    }
}