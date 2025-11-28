package com.sribalajiads.media_app.service;

import com.sribalajiads.media_app.model.Media;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PptGenerationServiceTest {

    @Test
    void generatePpt_shouldCreatePresentation_withCorrectContent() throws IOException {
        // --- ARRANGE (Set up the test) ---

        // 1. Manually create an instance of the service.
        PptGenerationService pptGenerationService = new PptGenerationService();

        // 2. We need to manually set the 'uploadDir' field for this test.
        // We'll point it to our test resources folder.
        // NOTE: This uses reflection and is a common pattern for testing non-Spring-managed objects.
        try {
            java.lang.reflect.Field field = PptGenerationService.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            field.set(pptGenerationService, "src/test/resources");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // 3. Create a mock list of Media objects.
        List<Media> mediaList = new ArrayList<>();
        Media media1 = new Media();
        media1.setImagePath("51c7c461-8501-4e1a-a708-29d5aa913a37.jpg"); // IMPORTANT: Must match the image in src/test/resources
        media1.setLocation("Test Location 1");
        media1.setSpecifications("50' x 30'");
        media1.setIllumination("Backlit");
        mediaList.add(media1);

        Media media2 = new Media();
        media2.setImagePath("645fb84b-0b20-4e58-9e4a-1c63faeab40f.jpg"); // Using the same image for simplicity
        media2.setLocation("Another Location 2");
        media2.setSpecifications("20' x 10'");
        media2.setIllumination("Frontlit");
        mediaList.add(media2);


        // --- ACT (Call the method) ---
        ByteArrayInputStream pptStream = pptGenerationService.generatePpt(mediaList);


        // --- ASSERT (Verify the results) ---

        // 1. Programmatic Check: Ensure a file was actually generated.
        assertThat(pptStream).isNotNull();
        assertThat(pptStream.available()).isGreaterThan(0);

        // 2. Visual Check: Save the generated stream to a file for manual inspection.
        Path outputPath = Paths.get("target/test-presentation.pptx");
        Files.createDirectories(outputPath.getParent()); // Ensure the 'target' directory exists
        FileCopyUtils.copy(pptStream, Files.newOutputStream(outputPath));
        System.out.println("Test presentation saved to: " + outputPath.toAbsolutePath());
    }
}