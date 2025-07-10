package com.association.util;

import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.*;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExportUtils {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String LOGO_PATH = "/images/logo.jpg"; // Si le logo est dans src/main/resources/images
    private static final String LOGO_LEFT_PATH = "/images/RDC.jpg"; // Chemin vers votre deuxième logo

    public static void exportTableToExcel(TableModel model, JTableHeader header,
                                          String title, String fileName, JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le fichier Excel");
        fileChooser.setSelectedFile(new File(generateFileName(fileName, "xlsx")));

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(title);

                // Style pour l'en-tête
                CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);

                // Créer la ligne d'en-tête - remplacer "ID" par "N°" si c'est la première colonne
                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < model.getColumnCount(); col++) {
                    String columnName = model.getColumnName(col);
                    if (col == 0 && "ID".equalsIgnoreCase(columnName)) {
                        columnName = "N°"; // Remplace "ID" par "N°"
                    }
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(columnName);
                    cell.setCellStyle(headerStyle);
                }

                // Remplir les données
                for (int row = 0; row < model.getRowCount(); row++) {
                    Row dataRow = sheet.createRow(row + 1);
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Object value = model.getValueAt(row, col);

                        // Pour la première colonne (ID), remplacer par un numéro séquentiel formaté
                        if (col == 0 && "ID".equalsIgnoreCase(model.getColumnName(col))) {
                            value = String.format("%02d", row + 1); // Format 01, 02, etc.
                        }

                        Cell cell = dataRow.createCell(col);
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    }
                }

                // Ajuster la largeur des colonnes
                for (int col = 0; col < model.getColumnCount(); col++) {
                    sheet.autoSizeColumn(col);
                }

                // Écrire le fichier
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                }

                JOptionPane.showMessageDialog(parent,
                        "Export Excel réussi!\nFichier enregistré sous:\n" + file.getAbsolutePath(),
                        "Export réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                showExportError(parent, "Excel", e);
            }
        }
    }

    public static void exportTableToPDF(TableModel model, JTableHeader header,
                                        String title, String fileName, JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le fichier PDF");
        fileChooser.setSelectedFile(new File(generateFileName(fileName, "pdf")));

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            Document document = new Document(PageSize.A4.rotate(), 36, 36, 90, 36);
            try {
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
                writer.setPageEvent(new PdfHeaderFooter());
                document.open();

                // Polices
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

                // Titre
                Paragraph docTitle = new Paragraph(title.toUpperCase(), titleFont);
                docTitle.setAlignment(Element.ALIGN_CENTER);
                docTitle.setSpacingAfter(10f);
                document.add(docTitle);

                // Date d'export
                Paragraph dateInfo = new Paragraph(
                        "Export généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                        FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
                dateInfo.setAlignment(Element.ALIGN_CENTER);
                dateInfo.setSpacingAfter(20f);
                document.add(dateInfo);

                // Création de la table PDF
                int columnCount = model.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(columnCount);
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10f);
                pdfTable.setSpacingAfter(10f);

                // Couleurs
                BaseColor headerBgColor = new BaseColor(51, 102, 153);

                // En-têtes - remplacer "ID" par "N°" si c'est la première colonne
                for (int col = 0; col < columnCount; col++) {
                    String columnName = model.getColumnName(col);
                    if (col == 0 && "ID".equalsIgnoreCase(columnName)) {
                        columnName = "N°"; // Remplace "ID" par "N°"
                    }
                    PdfPCell headerCell = new PdfPCell(new Phrase(columnName, headerFont));
                    headerCell.setBackgroundColor(headerBgColor);
                    headerCell.setBorderColor(BaseColor.WHITE);
                    headerCell.setPadding(5);
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfTable.addCell(headerCell);
                }

                // Données
                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < columnCount; col++) {
                        Object value = model.getValueAt(row, col);

                        // Pour la première colonne (ID), remplacer par un numéro séquentiel formaté
                        if (col == 0 && "ID".equalsIgnoreCase(model.getColumnName(col))) {
                            value = String.format("%02d", row + 1); // Format 01, 02, etc.
                        }

                        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value.toString() : "", dataFont));
                        cell.setPadding(5);
                        cell.setBorderColor(BaseColor.LIGHT_GRAY);

                        // Alternance des couleurs de fond
                        if (row % 2 == 0) {
                            cell.setBackgroundColor(BaseColor.WHITE);
                        } else {
                            cell.setBackgroundColor(new BaseColor(240, 240, 240));
                        }

                        pdfTable.addCell(cell);
                    }
                }

                document.add(pdfTable);
                document.close();

                JOptionPane.showMessageDialog(parent,
                        "Export PDF réussi!\nFichier enregistré sous:\n" + file.getAbsolutePath(),
                        "Export réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (DocumentException | IOException e) {
                showExportError(parent, "PDF", e);
            }
        }
    }

    private static boolean hasIdColumn(TableModel model) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if ("ID".equalsIgnoreCase(model.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    private static void styleHeaderCell(PdfPCell cell, BaseColor bgColor) {
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
    }

    private static void styleDataCell(PdfPCell cell, BaseColor bgColor) {
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
    }

    private static class PdfHeaderFooter extends PdfPageEventHelper {
        private Image logoRight;
        private Image logoLeft;

        public PdfHeaderFooter() {
            try {
                // Charger le logo de droite
                try (InputStream is = getClass().getResourceAsStream(LOGO_PATH)) {
                    if (is != null) {
                        logoRight = Image.getInstance(IOUtils.toByteArray(is));
                        logoRight.scaleToFit(80, 50);
                    } else {
                        System.err.println("Right logo not found: " + LOGO_PATH);
                    }
                }

                // Charger le logo de gauche
                try (InputStream is = getClass().getResourceAsStream(LOGO_LEFT_PATH)) {
                    if (is != null) {
                        logoLeft = Image.getInstance(IOUtils.toByteArray(is));
                        logoLeft.scaleToFit(80, 50);
                    } else {
                        System.err.println("Left logo not found: " + LOGO_LEFT_PATH);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load logos: " + e.getMessage());
                logoRight = null;
                logoLeft = null;
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                // Logo à gauche
                if (logoLeft != null) {
                    logoLeft.setAbsolutePosition(
                            document.left(),
                            document.top() + 10
                    );
                    writer.getDirectContent().addImage(logoLeft);
                }

                // Logo à droite
                if (logoRight != null) {
                    logoRight.setAbsolutePosition(
                            document.right() - logoRight.getScaledWidth() - 36,
                            document.top() + 10
                    );
                    writer.getDirectContent().addImage(logoRight);
                }

                // Pied de page (inchangé)
                PdfContentByte cb = writer.getDirectContent();
                Phrase footer = new Phrase(
                        "Document généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                " - Page " + writer.getPageNumber(),
                        FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)
                );
                ColumnText.showTextAligned(
                        cb, Element.ALIGN_CENTER, footer,
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 20, 0
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String generateFileName(String baseName, String extension) {
        return baseName.replace(".xlsx", "").replace(".pdf", "") +
                "_" + LocalDateTime.now().format(DATE_FORMATTER) + "." + extension;
    }

    private static void showExportError(JFrame parent, String format, Exception e) {
        JOptionPane.showMessageDialog(parent,
                "Erreur lors de l'export " + format + ":\n" + e.getMessage(),
                "Erreur d'export", JOptionPane.ERROR_MESSAGE);
    }
}