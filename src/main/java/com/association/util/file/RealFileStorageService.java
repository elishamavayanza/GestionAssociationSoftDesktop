package com.association.util.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealFileStorageService implements FileStorageService {
    private final Path storageDirectory;
    private static final Logger logger = LoggerFactory.getLogger(RealFileStorageService.class);


    public RealFileStorageService() {
        this.storageDirectory = Paths.get(System.getProperty("user.dir"), "uploads").normalize();
        logger.info("Répertoire de stockage: {}", storageDirectory);

        logger.info("Chemin ABSOLU du stockage: {}", storageDirectory.toAbsolutePath());
        logger.info("Le répertoire existe? {}", Files.exists(storageDirectory));
        logger.info("Est accessible en écriture? {}", Files.isWritable(storageDirectory));
        ensureDirectoryExists();

    }

    public RealFileStorageService(String customPath) {
        this.storageDirectory = Paths.get(customPath).toAbsolutePath().normalize();
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage", e);
        }
    }

    @Override
    public String storeFile(byte[] file, String subDirectory) {
        try {
            // Vérification des paramètres
            Objects.requireNonNull(file, "Le fichier ne peut pas être null");
            Objects.requireNonNull(subDirectory, "Le sous-répertoire ne peut pas être null");

            if (file.length == 0) {
                throw new IllegalArgumentException("Le fichier est vide");
            }

            // Création du nom de fichier
            String fileName = UUID.randomUUID() + ".jpg";

            // Normalisation des chemins
            Path targetDir = storageDirectory.resolve(subDirectory).normalize();
            Path fullPath = targetDir.resolve(fileName).normalize();

            // Protection contre les Directory Traversal
            if (!fullPath.startsWith(storageDirectory)) {
                throw new SecurityException("Tentative d'accès non autorisé en dehors du répertoire de stockage");
            }

            // Création des répertoires
            Files.createDirectories(targetDir);

            // Écriture du fichier
            Files.write(fullPath, file, StandardOpenOption.CREATE_NEW);

            // Construction du chemin relatif
            String relativePath = subDirectory.replace('\\', '/') + "/" + fileName;

            logger.info("Fichier stocké avec succès. Chemin relatif: {}", relativePath);
            return relativePath;

        } catch (Exception e) {
            logger.error("Échec du stockage du fichier", e);
            return null;
        }
    }

    @Override
    public InputStream loadFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            System.err.println("Chemin de fichier vide");
            return null;
        }

        try {
            Path fullPath = storageDirectory.resolve(filePath);

            if (!Files.exists(fullPath)) {
                System.err.println("Fichier non trouvé: " + fullPath);
                return null;
            }

            return new FileInputStream(fullPath.toFile());
        } catch (Exception e) {
            System.err.println("Erreur chargement fichier " + filePath + ": " + e.getMessage());
            return null;
        }
    }
    @Override
    public boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    @Override
    public String generateUniqueFilename(String originalName) {
        String ext = getFileExtension(originalName);
        String baseName = (ext.isEmpty()) ? originalName : originalName.substring(0, originalName.length() - ext.length() - 1);
        return baseName + "_" + System.currentTimeMillis() + (ext.isEmpty() ? "" : "." + ext);
    }
}