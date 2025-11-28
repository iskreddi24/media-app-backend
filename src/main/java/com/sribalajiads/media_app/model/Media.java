package com.sribalajiads.media_app.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data // Lombok annotation: generates getters, setters, equals, hashCode, and toString methods.
@Entity // Marks this class as a JPA entity.
@Table(name = "media") // Maps this entity to the 'media' table in the database.
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // Stores the enum name ("SBA", "YUVA") in the DB
    @Column(name = "belongs_to", nullable = false)
    private Company belongsTo;

    @Column(name = "media_code", nullable = false, unique = true)
    private String mediaCode;

    @Column(nullable = false)
    private String location;

    @Column(name = "traffic_view", nullable = false)
    private String trafficView;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String specifications;

    @Column
    private String illumination;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    // This field is optional, so nullable = true is the default and acceptable.
    @Column(name = "location_url")
    private String locationUrl;

    @CreationTimestamp // Automatically sets this field to the current timestamp when a new entity is created.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}