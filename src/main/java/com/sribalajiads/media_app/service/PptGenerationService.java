package com.sribalajiads.media_app.service;

import com.sribalajiads.media_app.model.Media;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PptGenerationService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Define modern, corporate-friendly colors and fonts for a professional look
    private static final Color CORPORATE_BLUE = new Color(0, 120, 212);
    private static final Color DARK_GRAY_TEXT = new Color(64, 64, 64);
    private static final String MODERN_FONT = "Segoe UI";

    // Supported extensions to check if the exact filename isn't found
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp");

    public ByteArrayInputStream generatePpt(List<Media> mediaList) throws IOException {

        try (XMLSlideShow ppt = new XMLSlideShow()) {

            double slideWidth = ppt.getPageSize().getWidth();
            double slideHeight = ppt.getPageSize().getHeight();

            for (Media media : mediaList) {
                XSLFSlide slide = ppt.createSlide();

                // Set a clean, white background for a professional feel
                slide.getBackground().setFillColor(Color.WHITE);

                // --- Resilient Image Handling Section ---
                // 1. Resolve the file path (Handles extension mismatches)
                Path resolvedPath = findImageFile(media.getImagePath());

                if (resolvedPath != null) {
                    try {
                        // 2. Read Dimensions to calculate scaling
                        // Note: We use the resolvedPath here, not the original string from Media object
                        try (InputStream imageStream = new FileInputStream(resolvedPath.toFile())) {
                            BufferedImage bufferedImage = ImageIO.read(imageStream);

                            if (bufferedImage == null) {
                                System.err.println("Warning: Skipping corrupt or invalid image file: " + resolvedPath);
                                addErrorText(slide, media.getMediaCode(), "Image file is corrupt.");
                                // We continue to text generation even if image is corrupt
                            } else {
                                double imgWidth = bufferedImage.getWidth();
                                double imgHeight = bufferedImage.getHeight();

                                // 3. Embed the image into the PPT
                                try (InputStream picStream = new FileInputStream(resolvedPath.toFile())) {
                                    byte[] pictureBytes = IOUtils.toByteArray(picStream);

                                    // Pass the ACTUAL filename found on disk to determine type
                                    PictureData.PictureType picType = getPictureType(resolvedPath.getFileName().toString());

                                    PictureData pd = ppt.addPicture(pictureBytes, picType);
                                    XSLFPictureShape pic = slide.createPicture(pd);

                                    double margin = 40;
                                    double textHeight = 80;
                                    double availableWidth = slideWidth - 2 * margin;
                                    double availableHeight = slideHeight - textHeight - 2 * margin;

                                    double scale = Math.min(availableWidth / imgWidth, availableHeight / imgHeight);
                                    double scaledWidth = imgWidth * scale;
                                    double scaledHeight = imgHeight * scale;
                                    double x = (slideWidth - scaledWidth) / 2;
                                    double y = margin;

                                    pic.setAnchor(new Rectangle2D.Double(x, y, scaledWidth, scaledHeight));
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Warning: Error reading image file: " + resolvedPath + ". " + e.getMessage());
                        addErrorText(slide, media.getMediaCode(), "Error reading image.");
                    }
                } else {
                    // File was not found even after checking variations
                    System.err.println("Warning: Could not find image file (checked extensions): " + media.getImagePath());
                    addErrorText(slide, media.getMediaCode(), "Image not found.");
                }

                // --- Modernized Text Box Section (media_code removed) ---
                XSLFTextBox textBox = slide.createTextBox();
                double textBoxHeight = 60; // Reduced height as there's less text
                double textBoxY = slideHeight - textBoxHeight - 20; // Position at the bottom
                textBox.setAnchor(new Rectangle2D.Double(40, textBoxY, slideWidth - 80, textBoxHeight));

                // Paragraph 1: All descriptive details
                XSLFTextParagraph p1 = textBox.addNewTextParagraph();
                p1.setTextAlign(XSLFTextParagraph.TextAlign.CENTER);
                XSLFTextRun r1 = p1.addNewTextRun();

                // Build the details string cleanly, filtering out null or empty parts
                List<String> details = new ArrayList<>();
                details.add(media.getLocation());
                details.add(media.getTrafficView());
                details.add(media.getSpecifications());
                if (media.getIllumination() != null && !media.getIllumination().isBlank()) {
                    details.add(media.getIllumination());
                }
                r1.setText(String.join("  |  ", details));
                r1.setFontFamily(MODERN_FONT);
                r1.setFontColor(DARK_GRAY_TEXT);
                r1.setFontSize(18.0);

                // Paragraph 2: The Map Link (only if a valid URL exists)
                String locationUrl = media.getLocationUrl();
                boolean hasUrl = locationUrl != null && !locationUrl.isBlank();

                if (hasUrl) {
                    XSLFTextParagraph p2 = textBox.addNewTextParagraph();
                    p2.setTextAlign(XSLFTextParagraph.TextAlign.CENTER);

                    XSLFTextRun iconRun = p2.addNewTextRun();
                    iconRun.setText("📍 "); // U+1F4CD Round Pushpin emoji
                    iconRun.setFontFamily(MODERN_FONT);
                    iconRun.setFontColor(CORPORATE_BLUE);
                    iconRun.setFontSize(16.0);

                    XSLFTextRun linkRun = p2.addNewTextRun();
                    linkRun.setText("View on Map");
                    linkRun.setFontFamily(MODERN_FONT);
                    linkRun.setFontColor(CORPORATE_BLUE);
                    linkRun.setFontSize(16.0);
                    linkRun.setUnderlined(true);

                    XSLFHyperlink link = linkRun.createHyperlink();
                    link.setAddress(locationUrl);
                }
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ppt.write(out);
                return new ByteArrayInputStream(out.toByteArray());
            }
        }
    }

    /**
     * Attempts to find the image file.
     * 1. Checks the exact path provided.
     * 2. If not found, strips extension and tries other supported extensions (png, jpg, etc.).
     */
    private Path findImageFile(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }

        // 1. Try exact match
        Path exactPath = Paths.get(uploadDir, originalFilename);
        if (Files.exists(exactPath)) {
            return exactPath;
        }

        // 2. Identify base name (SBA001.jpg -> SBA001)
        String baseName = originalFilename;
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            baseName = originalFilename.substring(0, dotIndex);
        }

        // 3. Try other extensions
        for (String ext : SUPPORTED_EXTENSIONS) {
            Path alternativePath = Paths.get(uploadDir, baseName + ext);
            if (Files.exists(alternativePath)) {
                return alternativePath;
            }
            // Also try uppercase extension just in case (Linux is case sensitive)
            Path upperPath = Paths.get(uploadDir, baseName + ext.toUpperCase());
            if (Files.exists(upperPath)) {
                return upperPath;
            }
        }

        return null;
    }

    private PictureData.PictureType getPictureType(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1) {
            return PictureData.PictureType.JPEG;
        }
        String ext = filename.substring(dotIndex + 1).toLowerCase();
        switch (ext) {
            case "png": return PictureData.PictureType.PNG;
            case "gif": return PictureData.PictureType.GIF;
            case "wmf": return PictureData.PictureType.WMF;
            case "emf": return PictureData.PictureType.EMF;
            case "bmp": return PictureData.PictureType.BMP;
            case "jpg":
            case "jpeg":
            default: return PictureData.PictureType.JPEG;
        }
    }

    private void addErrorText(XSLFSlide slide, String mediaCode, String errorMessage) {
        XSLFTextBox errorBox = slide.createTextBox();
        errorBox.setAnchor(new Rectangle2D.Double(50, 200, 620, 100));
        XSLFTextParagraph p = errorBox.addNewTextParagraph();
        p.setTextAlign(XSLFTextParagraph.TextAlign.CENTER);

        XSLFTextRun r1 = p.addNewTextRun();
        r1.setText("Error loading image for Media Code: " + mediaCode);
        r1.setFontColor(Color.RED);
        r1.setFontSize(18.0);
        r1.setBold(true);

        p.addLineBreak();

        XSLFTextRun r2 = p.addNewTextRun();
        r2.setText("(" + errorMessage + ")");
        r2.setFontColor(Color.GRAY);
        r2.setFontSize(14.0);
    }
}