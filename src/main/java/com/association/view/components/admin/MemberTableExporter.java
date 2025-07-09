package com.association.view.components.admin;

import com.association.model.Membre;
import com.association.util.ExportUtils;
import com.association.util.PrintUtils;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MemberTableExporter {
    private final JTable memberTable;
    private final JFrame parentFrame;
    private final List<Membre> currentMembers;

    public MemberTableExporter(JTable memberTable, JFrame parentFrame, List<Membre> currentMembers) {
        this.memberTable = memberTable;
        this.parentFrame = parentFrame;
        this.currentMembers = currentMembers;
    }

    public void exportToExcel() {
        try {
            // Obtenir le modèle de table
            TableModel model = memberTable.getModel();

            // Obtenir l'en-tête de la table
            JTableHeader header = memberTable.getTableHeader();

            // Exporter vers Excel
            ExportUtils.exportTableToExcel(
                    model,
                    header,
                    "Liste des Membres",
                    "membres_export.xlsx",
                    parentFrame
            );

            // Afficher un message de succès
            showSuccessDialog("Export réussi", "Les données ont été exportées avec succès vers le fichier membres_export.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Erreur d'export", "Une erreur est survenue lors de l'export: " + e.getMessage());
        }
    }

    public void printTable() {
        try {
            // Imprimer la table
            PrintUtils.printTable(
                    memberTable,
                    "Liste des Membres",
                    PrintUtils.PageSize.A4,  // PageSize au lieu de PageFormat
                    PrintUtils.Orientation.PORTRAIT,
                    true,
                    parentFrame
            );
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Erreur d'impression", "Une erreur est survenue lors de l'impression: " + e.getMessage());
        }
    }

    public void exportToPDF() {
        try {
            // Exporter vers PDF
            ExportUtils.exportTableToPDF(
                    memberTable.getModel(),
                    memberTable.getTableHeader(),
                    "Liste des Membres",
                    "membres_export.pdf",
                    parentFrame
            );

            showSuccessDialog("Export PDF réussi", "Les données ont été exportées avec succès vers le fichier membres_export.pdf");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Erreur d'export PDF", "Une erreur est survenue lors de l'export PDF: " + e.getMessage());
        }
    }

    public Action getExportAction() {
        return new AbstractAction("Exporter") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Créer un menu popup pour les options d'export
                JPopupMenu exportMenu = new JPopupMenu();

                JMenuItem excelItem = new JMenuItem("Exporter vers Excel");
                excelItem.addActionListener(ev -> exportToExcel());

                JMenuItem pdfItem = new JMenuItem("Exporter vers PDF");
                pdfItem.addActionListener(ev -> exportToPDF());

                exportMenu.add(excelItem);
                exportMenu.add(pdfItem);

                // Afficher le menu sous le bouton
                if (e.getSource() instanceof Component) {
                    Component source = (Component) e.getSource();
                    exportMenu.show(source, 0, source.getHeight());
                }
            }
        };
    }

    public Action getPrintAction() {
        return new AbstractAction("Imprimer") {
            @Override
            public void actionPerformed(ActionEvent e) {
                printTable();
            }
        };
    }

    private void showSuccessDialog(String title, String message) {
        JOptionPane.showMessageDialog(
                parentFrame,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(
                parentFrame,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
}