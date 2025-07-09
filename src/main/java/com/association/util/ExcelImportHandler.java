package com.association.util;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import com.association.util.ImportUtils;
import com.association.view.components.admin.MemberListPanel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelImportHandler {
    private final JFrame parentFrame;
    private final MemberListPanel memberListPanel;
    private final MembreDao membreDao;

    public ExcelImportHandler(JFrame parentFrame, MemberListPanel memberListPanel) {
        this.parentFrame = parentFrame;
        this.memberListPanel = memberListPanel;
        this.membreDao = DAOFactory.getInstance(MembreDao.class);
    }

    public void importFromExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner un fichier Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers Excel (*.xlsx)", "xlsx"));

        int returnValue = fileChooser.showOpenDialog(parentFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            processExcelFile(selectedFile);
        }
    }

    private void processExcelFile(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Sauter l'en-tête
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            List<Membre> membres = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    Membre membre = parseMembreFromRow(row);
                    if (membre != null) {
                        membres.add(membre);
                    }
                } catch (Exception e) {
                    errors.add("Ligne " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                showImportErrors(errors);
            }

            if (!membres.isEmpty()) {
                confirmAndSaveMembers(membres);
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                        "Aucun membre valide trouvé dans le fichier.",
                        "Aucune donnée",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Erreur lors de la lecture du fichier Excel: " + e.getMessage(),
                    "Erreur d'import",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Membre parseMembreFromRow(Row row) {
        Membre membre = new Membre();

        // Colonne 0: ID (peut être ignoré pour une création)
        // Colonne 1: Nom complet
        membre.setNom(getCellStringValue(row.getCell(1)));

        // Colonne 2: Contact (email ou téléphone)
        membre.setContact(getCellStringValue(row.getCell(2)));

        // Colonne 3: Photo (chemin ou nom de fichier)
        membre.setPhoto(getCellStringValue(row.getCell(3)));

        // Colonne 4: Date Inscription
        membre.setDateInscription(ImportUtils.parseDate(getCellStringValue(row.getCell(4))));

        // Colonne 5: Statut
        membre.setStatut(parseStatut(getCellStringValue(row.getCell(5))));

        // Valider les champs obligatoires
        if (membre.getNom() == null || membre.getNom().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        return membre;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return ImportUtils.formatDate(cell.getDateCellValue());
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private StatutMembre parseStatut(String statutStr) {
        if (statutStr == null || statutStr.isEmpty()) {
            return StatutMembre.ACTIF; // Valeur par défaut
        }
        try {
            return StatutMembre.valueOf(statutStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return StatutMembre.ACTIF; // Valeur par défaut en cas d'erreur
        }
    }

    private void showImportErrors(List<String> errors) {
        StringBuilder message = new StringBuilder("<html>Certaines lignes n'ont pas pu être importées:<br><br>");
        for (String error : errors) {
            message.append("• ").append(error).append("<br>");
        }
        message.append("</html>");

        JOptionPane.showMessageDialog(parentFrame,
                message.toString(),
                "Erreurs d'import",
                JOptionPane.WARNING_MESSAGE);
    }

    private void confirmAndSaveMembers(List<Membre> membres) {
        int response = JOptionPane.showConfirmDialog(parentFrame,
                "Voulez-vous importer " + membres.size() + " membres ?",
                "Confirmation d'import",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            // Utiliser un SwingWorker pour éviter de bloquer l'interface
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    for (Membre membre : membres) {
                        membreDao.create(membre); // Utilisez create() au lieu de save()
                    }
                    return null;
                }

                @Override
                protected void done() {
                    memberListPanel.loadMemberData(); // Rafraîchir la table
                    JOptionPane.showMessageDialog(parentFrame,
                            "Import terminé avec succès !",
                            "Import réussi",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            };
            worker.execute();
        }
    }
}