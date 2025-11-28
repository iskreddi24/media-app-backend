package com.sribalajiads.media_app.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users") // Good practice to name the table "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // We can add roles later if needed (e.g., "ROLE_ADMIN", "ROLE_USER")
    // For now, we'll keep it simple.
}