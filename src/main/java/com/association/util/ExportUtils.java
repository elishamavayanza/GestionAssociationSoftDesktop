package com.association.util;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExportUtils {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

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
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont(); // Notez le type complet
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);

                // Créer la ligne d'en-tête
                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(model.getColumnName(col));
                    cell.setCellStyle(headerStyle);
                }

                // Remplir les données
                for (int row = 0; row < model.getRowCount(); row++) {
                    Row dataRow = sheet.createRow(row + 1);
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Object value = model.getValueAt(row, col);
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

            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Ajouter le titre avec style similaire à PrintUtils
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                Paragraph docTitle = new Paragraph(title, titleFont);
                docTitle.setAlignment(Element.ALIGN_CENTER);
                docTitle.setSpacingAfter(20f);
                document.add(docTitle);

                // Créer la table PDF avec des styles similaires à PrintUtils
                PdfPTable pdfTable = new PdfPTable(model.getColumnCount());
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10f);
                pdfTable.setSpacingAfter(10f);

                // Style pour l'en-tête (similaire à PrintUtils)
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

                // Couleurs
                BaseColor headerBgColor = new BaseColor(240, 240, 240);
                BaseColor evenRowColor = new BaseColor(255, 255, 255);
                BaseColor oddRowColor = new BaseColor(248, 248, 248);

                // Ajouter les en-têtes avec style
                for (int col = 0; col < model.getColumnCount(); col++) {
                    PdfPCell headerCell = new PdfPCell(new Phrase(model.getColumnName(col), headerFont));
                    headerCell.setBackgroundColor(headerBgColor);
                    headerCell.setBorderColor(BaseColor.BLACK);
                    headerCell.setPadding(5);
                    headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    pdfTable.addCell(headerCell);
                }

                // Ajouter les données avec alternance de couleurs
                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Object value = model.getValueAt(row, col);
                        PdfPCell dataCell = new PdfPCell(new Phrase(value != null ? value.toString() : "", dataFont));
                        dataCell.setBackgroundColor(row % 2 == 0 ? evenRowColor : oddRowColor);
                        dataCell.setBorderColor(BaseColor.BLACK);
                        dataCell.setPadding(5);
                        dataCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        pdfTable.addCell(dataCell);
                    }
                }

                document.add(pdfTable);

                // Ajouter le numéro de page
                document.add(new Paragraph("\n"));
                Paragraph footer = new Paragraph("Page 1", dataFont); // Vous devrez implémenter la pagination si nécessaire
                footer.setAlignment(Element.ALIGN_RIGHT);
                document.add(footer);

                document.close();

                JOptionPane.showMessageDialog(parent,
                        "Export PDF réussi!\nFichier enregistré sous:\n" + file.getAbsolutePath(),
                        "Export réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (DocumentException | IOException e) {
                showExportError(parent, "PDF", e);
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