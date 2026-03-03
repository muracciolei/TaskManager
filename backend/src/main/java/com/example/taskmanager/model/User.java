package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity representing a user in the task manager system.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the user.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Email address of the user (must be unique).
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Password of the user (stored as BCrypt hash).
     */
    @Column(nullable = false)
    private String password;

    /**
     * Timestamp when the user was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Initializes the createdAt timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
