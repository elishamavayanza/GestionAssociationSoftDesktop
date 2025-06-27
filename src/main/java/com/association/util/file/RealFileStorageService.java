package com.association.util.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RealFileStorageService implements FileStorageService {
    private String storageDirectory = "uploads/";

    @Override
    public String storeFile(byte[] file, String directory) {
        try {
            String fileName = generateUniqueFilename("file");
            Path path = Paths.get(storageDirectory + directory, fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file);
            return path.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public InputStream loadFile(String filePath) {
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        return false;
    }

    @Override
    public String getFileExtension(String filename) {
        return "";
    }

    @Override
    public String generateUniqueFilename(String originalName) {
        return "";
    }
}
