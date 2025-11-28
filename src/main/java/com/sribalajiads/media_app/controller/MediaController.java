package com.sribalajiads.media_app.controller;

import com.sribalajiads.media_app.model.Media;
import com.sribalajiads.media_app.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/media") // Base URL for all endpoints in this controller
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /*
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Media> addMedia(
            @RequestParam("belongsTo") String belongsTo,
            @RequestParam("mediaCode") String mediaCode,
            @RequestParam("location") String location,
            @RequestParam("city") String city,
            @RequestParam("specifications") String specifications,
            @RequestParam("illumination") String illumination,
            @RequestParam("mediaType") String mediaType,
            @RequestParam("trafficView") String trafficView,
            @RequestPart("image") MultipartFile imageFile,
            @RequestParam(name = "locationUrl", required = false) String locationUrl) {


        Media createdMedia = mediaService.createMedia(belongsTo, mediaCode, location, city, specifications, illumination, mediaType, imageFile, trafficView, locationUrl);


        return new ResponseEntity<>(createdMedia, HttpStatus.CREATED);
    }
    */


    /**
     * Retrieves a paginated list of media based on filter criteria.
     * Spring Boot automatically maps request parameters like ?page=0&size=20 to the Pageable object.
     * @param company   The company to filter by.
     * @param mediaType The media type to filter by.
     * @param query     A search term for the media code.
     * @param pageable  An object containing pagination information.
     * @return A ResponseEntity containing a Page of Media entities.
     */
    @GetMapping
    public ResponseEntity<Page<Media>> getMedia(
            @RequestParam(name = "company", required = false) String company,
            @RequestParam(name = "mediaType", required = false) String mediaType,
            @RequestParam(name = "query", required = false) String query,
            Pageable pageable // Spring automatically creates this from request params
    ) {
        Page<Media> mediaPage = mediaService.getMedia(company, mediaType, query, pageable);
        return ResponseEntity.ok(mediaPage);
    }


    @PostMapping("/generate-ppt")
    public ResponseEntity<InputStreamResource> generatePpt(@RequestBody List<Long> mediaIds) throws IOException {
        // Call the service to get the in-memory PPT file as a stream
        ByteArrayInputStream pptInputStream = mediaService.generatePresentationForMedia(mediaIds);

        // Set up the HTTP headers for the file download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=presentation.pptx");

        // Create the response entity
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(pptInputStream));
    }

    /**
     * Generates a PowerPoint presentation from a list of media codes.
     * The frontend will send a POST request with a JSON array of strings in the body.
     * Example: ["SBA001", "SBA002", "YUVA005"]
     *
     * @param mediaCodes A list of media codes.
     * @return A ResponseEntity containing the generated PPTX file for download.
     * @throws IOException if there is an error during file generation.
     */
    @PostMapping("/generate-ppt-by-codes")
    public ResponseEntity<InputStreamResource> generatePptByCodes(@RequestBody List<String> mediaCodes) throws IOException {
        // 1. Call the new service method
        ByteArrayInputStream pptInputStream = mediaService.generatePresentationForMediaCodes(mediaCodes);

        // 2. Set up the HTTP headers for the file download (same as the other endpoint)
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=presentation_by_codes.pptx");

        // 3. Create and return the response entity
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(pptInputStream));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Media> getMediaById(@PathVariable Long id) {
        Media media = mediaService.getMediaById(id);
        return ResponseEntity.ok(media);
    }

    /*
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Media> updateMedia(
            @PathVariable Long id,
            @RequestParam("belongsTo") String belongsTo,
            @RequestParam("mediaCode") String mediaCode,
            @RequestParam("location") String location,
            @RequestParam("city") String city,
            @RequestParam("specifications") String specifications,
            @RequestParam("illumination") String illumination,
            @RequestParam("mediaType") String mediaType,
            @RequestParam("trafficView") String trafficView,
            @RequestPart(name = "image", required = false) MultipartFile imageFile,
            @RequestParam(name = "locationUrl", required = false) String locationUrl) { // `required = false` is key!

        Media updatedMedia = mediaService.updateMedia(id, belongsTo, mediaCode, location, city, specifications, illumination, mediaType, trafficView, imageFile, locationUrl);

        return ResponseEntity.ok(updatedMedia); // Return 200 OK with the updated object
    }
    */

}