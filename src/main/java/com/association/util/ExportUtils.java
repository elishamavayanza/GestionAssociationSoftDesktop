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

            // Marges : gauche, droite, haut, bas (en points, 72 points = 1 pouce = 2.54 cm)
            // Marges standard pour document administratif : 2.5 cm de chaque côté
            float marginLeft = 72;    // 2.54 cm
            float marginRight = 72;   // 2.54 cm
            float marginTop = 72;     // 2.54 cm
            float marginBottom = 72;  // 2.54 cm

            Document document = new Document(PageSize.A4.rotate(), marginLeft, marginRight, marginTop, marginBottom);
            try {
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
                writer.setPageEvent(new PdfHeaderFooter());
                document.open();

                // ========= NOUVEL EN-TÊTE ========= //
                Font headerFontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                Font headerFontRegular = FontFactory.getFont(FontFactory.HELVETICA, 9);

                PdfPTable associationHeaderTable = new PdfPTable(1);
                associationHeaderTable.setWidthPercentage(80);
                associationHeaderTable.setHorizontalAlignment(Element.ALIGN_CENTER);

                Paragraph associationInfo = new Paragraph();
                associationInfo.add(new Phrase("ASSOCIATION AVEC\n\n", headerFontBold));
                associationInfo.add(new Phrase("Siège social : .................................................................\n", headerFontRegular));
                associationInfo.add(new Phrase("Email : ..........................| Tél : +243...............................\n", headerFontRegular));
                associationInfo.add(new Phrase("SIRET : .........................| RNA : ....................................\n\n", headerFontRegular));
                associationInfo.add(new Phrase("LISTE DES MEMBRES \n", headerFontBold));
                associationInfo.add(new Phrase("Année " + LocalDateTime.now().getYear(), headerFontRegular));
                associationInfo.setAlignment(Element.ALIGN_CENTER);

                PdfPCell headerCells = new PdfPCell(associationInfo);
                headerCells.setBorder(Rectangle.NO_BORDER);
                associationHeaderTable.addCell(headerCells);

                document.add(associationHeaderTable);

                // Polices
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                Font adminFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

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
        private boolean firstPage = true;
        private Image signatureImage;
        private Image stampImage;

        public PdfHeaderFooter() {
            try {
                // Charger les logos
                try (InputStream is = getClass().getResourceAsStream(LOGO_PATH)) {
                    if (is != null) logoRight = Image.getInstance(IOUtils.toByteArray(is));
                }
                try (InputStream is = getClass().getResourceAsStream(LOGO_LEFT_PATH)) {
                    if (is != null) logoLeft = Image.getInstance(IOUtils.toByteArray(is));
                }

                // Charger les images de signature et cachet (si disponibles)
                try (InputStream is = getClass().getResourceAsStream("/images/signature.png")) {
                    if (is != null) signatureImage = Image.getInstance(IOUtils.toByteArray(is));
                }
                try (InputStream is = getClass().getResourceAsStream("/images/stamp.png")) {
                    if (is != null) stampImage = Image.getInstance(IOUtils.toByteArray(is));
                }

                // Redimensionner les images
                if (logoRight != null) logoRight.scaleToFit(80, 50);
                if (logoLeft != null) logoLeft.scaleToFit(80, 50);
                if (signatureImage != null) signatureImage.scaleToFit(100, 40);
                if (stampImage != null) stampImage.scaleToFit(80, 80);
            } catch (Exception e) {
                System.err.println("Failed to load images: " + e.getMessage());
            }
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // En-tête administratif - seulement sur la première page
            if (firstPage) {
                try {
                    // Logo à gauche
                    if (logoLeft != null) {
                        logoLeft.setAbsolutePosition(
                                document.left(),
                                document.top() + 10
                        );
                        cb.addImage(logoLeft);
                    }

                    // Logo à droite
                    if (logoRight != null) {
                        logoRight.setAbsolutePosition(
                                document.right() - logoRight.getScaledWidth(),
                                document.top() + 10
                        );
                        cb.addImage(logoRight);
                    }

                    // Ajouter le titre "LISTE DES MEMBRES" entre les deux logos
//                    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
//                    float titleY = document.top() + 30; // Ajustez cette valeur pour aligner verticalement avec les logos
//                    float titleX = (document.right() - document.left()) / 2 + document.leftMargin();
//
//                    ColumnText.showTextAligned(
//                            cb,
//                            Element.ALIGN_CENTER,
//                            new Phrase("AVEC", titleFont),
//                            titleX,
//                            titleY,
//                            0
//                    );

                    // Informations administratives
                    float yPos = document.top() - 30; // Position sous les logos et le titre

                    // Tableau pour les infos administratives
                    PdfPTable adminTable = new PdfPTable(2);
                    adminTable.setTotalWidth(document.right() - document.left());
                    adminTable.setWidths(new float[]{1, 1});
                    adminTable.setLockedWidth(true);

                    // Cellule de gauche : Nom et Téléphone
                    PdfPCell leftCell = new PdfPCell();
                    leftCell.setBorder(Rectangle.NO_BORDER);


                    // Cellule de droite : Date
                    PdfPCell rightCell = new PdfPCell();
                    rightCell.setBorder(Rectangle.NO_BORDER);
                    rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);


                    // Positionner le tableau
                    adminTable.writeSelectedRows(0, -1, document.left(), yPos, cb);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                firstPage = false;
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Pied de page avec signature et cachet
            float footerY = document.bottom() - 20;

            // Tableau pour le pied de page
            PdfPTable footerTable = new PdfPTable(3);
            footerTable.setTotalWidth(document.right() - document.left());
            try {
                footerTable.setWidths(new float[]{2, 1, 2});
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
            footerTable.setLockedWidth(true);

            // Cellule de gauche (vide)
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(leftCell);

            // Cellule centrale (signature)
            PdfPCell centerCell = new PdfPCell();
            centerCell.setBorder(Rectangle.NO_BORDER);
            centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            if (signatureImage != null) {
                centerCell.addElement(new Chunk(signatureImage, 0, 0));
            }
            centerCell.addElement(new Paragraph("Signature",
                    FontFactory.getFont(FontFactory.HELVETICA, 10)));
            footerTable.addCell(centerCell);

            // Cellule de droite (cachet)
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            if (stampImage != null) {
                rightCell.addElement(new Chunk(stampImage, 0, 0));
                rightCell.addElement(new Paragraph("Cachet",
                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
            }
            footerTable.addCell(rightCell);

            // Positionner le tableau en bas de page
            footerTable.writeSelectedRows(0, -1, document.left(), footerY, cb);

            // Numéro de page
            Phrase footer = new Phrase(
                    "Page " + writer.getPageNumber(),
                    FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)
            );
            ColumnText.showTextAligned(
                    cb, Element.ALIGN_CENTER, footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0
            );
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