package com.association.util;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.text.MessageFormat;

public class PrintUtils {
    public enum PageSize { A4, LETTER }
    public enum Orientation { PORTRAIT, LANDSCAPE }

    public static void printTable(JTable table, String title, PageSize size,
                                  Orientation orientation, boolean showPrintDialog, JFrame parent) {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName(title);

            // Configurer le format de page
            PageFormat pageFormat = job.defaultPage();
            Paper paper = new Paper();

            double width = 0;
            double height = 0;
            double margin = 18; // marge en points (1/4 inch)

            switch (size) {
                case A4:
                    width = 595; // A4 en points (210mm)
                    height = 842; // (297mm)
                    break;
                case LETTER:
                    width = 612; // Letter en points (8.5 inch)
                    height = 792; // (11 inch)
                    break;
            }

            if (orientation == Orientation.LANDSCAPE) {
                double temp = width;
                width = height;
                height = temp;
                pageFormat.setOrientation(PageFormat.LANDSCAPE);
            } else {
                pageFormat.setOrientation(PageFormat.PORTRAIT);
            }

            paper.setSize(width, height);
            // Marges plus larges pour éviter le découpage
            paper.setImageableArea(margin, margin, width - margin * 2, height - margin * 2);
            pageFormat.setPaper(paper);

            // Créer le Printable avec le titre
            Printable printable = new TablePrintable(table, title, pageFormat);

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

    private static class TablePrintable implements Printable {
        private final JTable table;
        private final String title;
        private final PageFormat pageFormat;

        public TablePrintable(JTable table, String title, PageFormat pageFormat) {
            this.table = table;
            this.title = title;
            this.pageFormat = pageFormat;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.setColor(Color.BLACK);

            // Traduire les coordonnées pour tenir compte des marges
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Calculer la zone imprimable
            double pageWidth = pageFormat.getImageableWidth();
            double pageHeight = pageFormat.getImageableHeight();

            // Vérifier si le tableau est trop large
            int tableWidth = 0;
            for (int col = 0; col < table.getColumnCount(); col++) {
                tableWidth += table.getColumnModel().getColumn(col).getWidth();
            }

            // Si le tableau est trop large, ajuster l'échelle
            if (tableWidth > pageWidth) {
                double scale = pageWidth / tableWidth;
                g2d.scale(scale, scale);
                pageWidth = pageWidth / scale;
                pageHeight = pageHeight / scale;
            }

            // Dessiner le titre
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics titleMetrics = g2d.getFontMetrics();
            int titleWidth = titleMetrics.stringWidth(title);
            g2d.drawString(title, (float)((pageWidth - titleWidth) / 2), 20);

            // Dessiner le tableau
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            int rowHeight = table.getRowHeight() + 2; // Ajouter un peu d'espace
            int headerHeight = rowHeight + 10; // Espace supplémentaire pour l'en-tête

            int rowsPerPage = (int) Math.floor((pageHeight - 50) / rowHeight); // Réserver de l'espace pour le titre et le numéro de page
            int totalPages = (int) Math.ceil((double) table.getRowCount() / rowsPerPage);

            if (pageIndex >= totalPages) {
                return NO_SUCH_PAGE;
            }

            // Dessiner le numéro de page
            g2d.drawString("Page " + (pageIndex + 1) + "/" + totalPages, (float)(pageWidth - 50), (float)(pageHeight - 10));

            // Dessiner les lignes
            int y = 40; // Commencer après le titre
            int startRow = pageIndex * rowsPerPage;
            int endRow = Math.min(startRow + rowsPerPage, table.getRowCount());

            // Dessiner les en-têtes de colonnes
            int x = 0;
            for (int col = 0; col < table.getColumnCount(); col++) {
                int colWidth = table.getColumnModel().getColumn(col).getWidth();
                String header = table.getColumnName(col);

                // Dessiner le fond de l'en-tête
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRect(x, y, colWidth, headerHeight);
                g2d.setColor(Color.BLACK);

                // Dessiner le texte de l'en-tête
                g2d.drawString(header, x + 5, y + 15);
                g2d.drawRect(x, y, colWidth, headerHeight);
                x += colWidth;
            }
            y += headerHeight;

            // Dessiner les données
            for (int row = startRow; row < endRow; row++) {
                x = 0;
                for (int col = 0; col < table.getColumnCount(); col++) {
                    int colWidth = table.getColumnModel().getColumn(col).getWidth();
                    Object value = table.getValueAt(row, col);

                    // Alterner les couleurs de fond pour une meilleure lisibilité
                    if (row % 2 == 0) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(new Color(248, 248, 248));
                    }
                    g2d.fillRect(x, y, colWidth, rowHeight);
                    g2d.setColor(Color.BLACK);

                    // Dessiner le texte (tronqué si nécessaire)
                    String text = value != null ? value.toString() : "";
                    FontMetrics metrics = g2d.getFontMetrics();
                    if (metrics.stringWidth(text) > colWidth - 10) {
                        text = truncateText(text, metrics, colWidth - 10);
                    }
                    g2d.drawString(text, x + 5, y + 15);
                    g2d.drawRect(x, y, colWidth, rowHeight);
                    x += colWidth;
                }
                y += rowHeight;
            }

            return PAGE_EXISTS;
        }

        private String truncateText(String text, FontMetrics metrics, int maxWidth) {
            if (metrics.stringWidth(text) <= maxWidth) {
                return text;
            }

            String ellipsis = "...";
            int ellipsisWidth = metrics.stringWidth(ellipsis);

            int low = 0;
            int high = text.length();

            while (low < high) {
                int mid = (low + high) / 2;
                String subStr = text.substring(0, mid) + ellipsis;
                if (metrics.stringWidth(subStr) < maxWidth) {
                    low = mid + 1;
                } else {
                    high = mid;
                }
            }

            return text.substring(0, low - 1) + ellipsis;
        }
    }
}