package com.sribalajiads.media_app.service;

import com.sribalajiads.media_app.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    // This constructor injects the value from 'file.upload-dir' in your properties file.
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    /**
     * This method runs automatically after the service is created.
     * It ensures the upload directory exists, creating it if necessary.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage location", e);
        }
    }

    /**
     * Stores an uploaded file securely using a custom filename.
     * @param file The file uploaded by the user.
     * @param desiredFilename The base name for the file (without extension).
     * @return The full filename with extension.
     */
    public String store(MultipartFile file, String desiredFilename) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String finalFilename = desiredFilename + "." + fileExtension;

        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file " + originalFilename);
            }
            if (finalFilename.contains("..")) {
                // This is a security check to prevent directory traversal attacks
                throw new FileStorageException("Cannot store file with relative path outside current directory " + finalFilename);
            }

            Path destinationFile = this.rootLocation.resolve(Paths.get(finalFilename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return finalFilename;

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file " + finalFilename, e);
        }
    }

    /**
     * Renames a file in the storage directory.
     * @param oldFilename The current name of the file (e.g., "old-code.jpg").
     * @param newFilenameBase The new base name for the file, without extension (e.g., "new-code").
     * @return The full new filename with the extension (e.g., "new-code.jpg").
     */
    public String rename(String oldFilename, String newFilenameBase) {
        // Construct the full path for the old file
        Path oldFilePath = this.rootLocation.resolve(oldFilename).normalize().toAbsolutePath();

        // Ensure the source file actually exists before trying to rename it
        if (!Files.exists(oldFilePath)) {
            // This prevents errors and highlights data inconsistency if the file is missing
            throw new FileStorageException("Cannot rename file. Source file not found: " + oldFilename);
        }

        // Preserve the original file extension
        String fileExtension = StringUtils.getFilenameExtension(oldFilename);
        String finalNewFilename = newFilenameBase + "." + fileExtension;
        Path newFilePath = this.rootLocation.resolve(finalNewFilename).normalize().toAbsolutePath();

        try {
            // Files.move is the correct and most efficient way to rename a file.
            Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
            return finalNewFilename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to rename file from " + oldFilename + " to " + finalNewFilename, e);
        }
    }


    /**
     * Deletes a file from the storage directory.
     * @param filename The name of the file to delete.
     */
    public void delete(String filename) {
        if (filename == null || filename.isBlank()) return;

        try {
            Path fileToDelete = rootLocation.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file " + filename, e);
        }
    }
}