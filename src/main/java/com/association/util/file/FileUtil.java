package com.association.util.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileUtil {
    public static Optional<String> readFile(String path) {
        try {
            return Optional.of(new String(Files.readAllBytes(Paths.get(path))));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static boolean writeFile(String path, String content) {
        try {
            Files.write(Paths.get(path), content.getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteFile(String path) {
        try {
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            return false;
        }
    }

    public static Optional<String> getExtension(String filename) {
        if (filename == null) return Optional.empty();
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) return Optional.empty();
        return Optional.of(filename.substring(dotIndex + 1));
    }

    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}