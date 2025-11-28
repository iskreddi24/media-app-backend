package com.sribalajiads.media_app.service;

import com.sribalajiads.media_app.exception.ResourceNotFoundException;
import com.sribalajiads.media_app.model.Company;
import com.sribalajiads.media_app.model.Media;
import com.sribalajiads.media_app.repository.MediaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;
    private final PptGenerationService pptGenerationService;

    @Autowired
    public MediaService(MediaRepository mediaRepository, FileStorageService fileStorageService, PptGenerationService pptGenerationService) {
        this.mediaRepository = mediaRepository;
        this.fileStorageService = fileStorageService;
        this.pptGenerationService = pptGenerationService;
    }

    /**
     * Retrieves a paginated list of media using a dynamic JPA Specification.
     * This robust approach replaces the brittle if/else logic and ensures
     * that filtering criteria are applied consistently across all pages,
     * fixing bugs related to premature end-of-list results.
     */
    public Page<Media> getMedia(String company, String mediaType, String query, Pageable pageable) {
        // A Specification is a function that programmatically builds a query(s) WHERE clause.
        Specification<Media> spec = (root, criteriaQuery, criteriaBuilder) -> {
            // This list will hold all our filtering conditions (predicates).
            List<Predicate> predicates = new ArrayList<>();

            // 1. Add company filter if a valid company string is provided.
            if (company != null && !company.isBlank()) {
                try {
                    Company companyEnum = Company.valueOf(company.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("belongsTo"), companyEnum));
                } catch (IllegalArgumentException e) {
                    // If an invalid company string is passed, we can ensure no results are returned.
                    return criteriaBuilder.disjunction(); // An always-false predicate
                }
            }

            // 2. Add mediaType filter if it's provided and is not the "All" placeholder.
            if (mediaType != null && !mediaType.isBlank() && !mediaType.equalsIgnoreCase("All")) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("mediaType")),
                        mediaType.toLowerCase()
                ));
            }

            // 3. Add mediaCode search filter if a query string is provided.
            if (query != null && !query.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("mediaCode")),
                        "%" + query.toLowerCase() + "%"
                ));
            }

            // Combine all the created predicates with an "AND" condition.
            // If the predicates list is empty, this returns a clause that is always true.
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Execute the single, dynamically-built query using the findAll method from JpaSpecificationExecutor.
        return mediaRepository.findAll(spec, pageable);
    }

    // --- All other methods below remain unchanged as their logic is correct. ---

    @Transactional
    public Media createMedia(String belongsTo, String mediaCode, String location, String city, String specifications, String illumination, String mediaType, MultipartFile imageFile, String trafficView, String locationUrl) {
        String imageFilename = fileStorageService.store(imageFile, mediaCode);
        Media newMedia = new Media();
        newMedia.setBelongsTo(Company.valueOf(belongsTo.toUpperCase()));
        newMedia.setMediaCode(mediaCode);
        newMedia.setLocation(location);
        newMedia.setTrafficView(trafficView);
        newMedia.setCity(city);
        newMedia.setSpecifications(specifications);
        newMedia.setIllumination(illumination);
        newMedia.setMediaType(mediaType);
        newMedia.setImagePath(imageFilename);
        newMedia.setLocationUrl(locationUrl);
        return mediaRepository.save(newMedia);
    }

    public ByteArrayInputStream generatePresentationForMedia(List<Long> mediaIds) throws IOException {
        List<Media> selectedMedia = mediaRepository.findAllById(mediaIds);
        return pptGenerationService.generatePpt(selectedMedia);
    }

    /**
     * Generates a PowerPoint presentation for a given list of media codes.
     *
     * @param mediaCodes A list of media codes to include in the presentation.
     * @return A ByteArrayInputStream representing the generated PPTX file.
     * @throws IOException if there is an error during file generation.
     */
    public ByteArrayInputStream generatePresentationForMediaCodes(List<String> mediaCodes) throws IOException {
        // 1. Sanitize the input: remove empty strings and duplicates
        List<String> distinctCodes = mediaCodes.stream()
                .filter(code -> code != null && !code.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (distinctCodes.isEmpty()) {
            // Or throw a custom exception like new IllegalArgumentException("No valid media codes provided.")
            return new ByteArrayInputStream(new byte[0]);
        }

        // 2. Use the new repository method to fetch all matching media in one query
        List<Media> selectedMedia = mediaRepository.findAllByMediaCodeIn(distinctCodes);

        // Optional but recommended: You can check if some codes were not found and log them
        if (selectedMedia.size() != distinctCodes.size()) {
            List<String> foundCodes = selectedMedia.stream().map(Media::getMediaCode).collect(Collectors.toList());
            List<String> notFoundCodes = distinctCodes.stream().filter(code -> !foundCodes.contains(code)).collect(Collectors.toList());
            System.out.println("Warning: The following media codes were not found: " + notFoundCodes);
        }

        // 3. Reuse the existing PPT generation service. No need to write new code!
        return pptGenerationService.generatePpt(selectedMedia);
    }


    public Media getMediaById(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
    }

    @Transactional
    public Media updateMedia(
            Long id, String belongsTo, String mediaCode, String location, String city,
            String specifications, String illumination, String mediaType, String trafficView,
            MultipartFile imageFile, String locationUrl
    ) {
        Media mediaToUpdate = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));

        String oldMediaCode = mediaToUpdate.getMediaCode();
        String oldImagePath = mediaToUpdate.getImagePath();
        String newImagePath = oldImagePath;
        boolean mediaCodeChanged = !mediaCode.equals(oldMediaCode);

        if (imageFile != null && !imageFile.isEmpty()) {
            newImagePath = fileStorageService.store(imageFile, mediaCode);
            if (mediaCodeChanged && oldImagePath != null && !oldImagePath.isBlank()) {
                fileStorageService.delete(oldImagePath);
            }
        } else if (mediaCodeChanged) {
            if (oldImagePath != null && !oldImagePath.isBlank()) {
                newImagePath = fileStorageService.rename(oldImagePath, mediaCode);
            }
        }

        mediaToUpdate.setBelongsTo(Company.valueOf(belongsTo.toUpperCase()));
        mediaToUpdate.setMediaCode(mediaCode);
        mediaToUpdate.setLocation(location);
        mediaToUpdate.setCity(city);
        mediaToUpdate.setSpecifications(specifications);
        mediaToUpdate.setIllumination(illumination);
        mediaToUpdate.setMediaType(mediaType);
        mediaToUpdate.setTrafficView(trafficView);
        mediaToUpdate.setImagePath(newImagePath);
        mediaToUpdate.setLocationUrl(locationUrl);
        return mediaRepository.save(mediaToUpdate);
    }
}