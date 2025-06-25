package com.association.util.file;

import java.io.InputStream;

public interface FileStorageService {
    String storeFile(byte[] file, String directory);
    InputStream loadFile(String filePath);
    boolean deleteFile(String filePath);
    String getFileExtension(String filename);
    String generateUniqueFilename(String originalName);
}