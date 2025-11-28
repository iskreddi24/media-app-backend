package com.sribalajiads.media_app.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Tells Spring to load the entire application context for this test.
@SpringBootTest
class FileStorageServiceTest {

    // Injects the actual FileStorageService bean from the application context.
    @Autowired
    private FileStorageService fileStorageService;

    // Injects the upload directory path from application.properties for verification.
    @Value("${file.upload-dir}")
    private String uploadDir;

    // This will hold the name of the file created during the test for cleanup.
    private String savedFilename;

    @Test
    void store_shouldSaveFile_whenGivenValidFile() throws IOException {
        // ARRANGE: Create a fake file in memory to simulate a real upload.
        MockMultipartFile mockFile = new MockMultipartFile(
                "test-file",          // form field name
                "test.txt",           // original filename
                "text/plain",         // content type
                "Hello, World!".getBytes() // file content
        );

        // ACT: Call the method we are testing.
        savedFilename = fileStorageService.store(mockFile);
        System.out.println("File saved with unique name: " + savedFilename);

        // ASSERT: Verify the outcome.
        Path expectedFilePath = Paths.get(uploadDir).resolve(savedFilename);

        // 1. Check that a non-empty filename was returned.
        assertThat(savedFilename).isNotNull().isNotEmpty();
        // 2. Check that the file extension is correct.
        assertThat(savedFilename).endsWith(".txt");
        // 3. Check that the file physically exists on the hard drive.
        assertThat(Files.exists(expectedFilePath)).isTrue();
    }

    // This cleanup method runs automatically after the test is finished.
    @AfterEach
    void cleanup() throws IOException {
        // If a file was saved, delete it to keep our test environment clean.
        if (savedFilename != null) {
            Path fileToDelete = Paths.get(uploadDir).resolve(savedFilename);
            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
                System.out.println("Cleaned up test file: " + savedFilename);
            }
        }
    }
}