// FileExportUtil.java
package com.association.util.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileExportUtil {

    public static boolean exportContent(String content, String fileName, FileType fileType) {
        try {
            String extension = getExtension(fileType);
            String fullFileName = fileName + extension;

            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get("exports"));

            File file = new File("exports/" + fullFileName);

            switch (fileType) {
                case PDF:
                    // For PDF, you would typically use a library like iText or Apache PDFBox
                    // This is a simplified version that just creates a text file
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(content);
                    }
                    break;

                case EXCEL:
                    // For Excel, you would typically use Apache POI
                    // This is a simplified version that just creates a CSV file
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(content);
                    }
                    break;

                default:
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(content);
                    }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getExtension(FileType fileType) {
        switch (fileType) {
            case PDF: return ".pdf";
            case EXCEL: return ".xlsx";
            case CSV: return ".csv";
            case TXT: return ".txt";
            default: return ".txt";
        }
    }
}