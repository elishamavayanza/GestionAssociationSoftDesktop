package com.association.util.file;

import java.io.InputStream;

public class FileStorageServiceAdapter implements FileStorageService {
    @Override
    public String storeFile(byte[] file, String directory) {
        return "";
    }

    @Override
    public InputStream loadFile(String filePath) {
        return null;
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
