package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task entity representing a task in the task manager system.
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    /**
     * Unique identifier for the task.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the task.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Description of the task.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Status of the task (TODO, IN_PROGRESS, DONE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.TODO;

    /**
     * Timestamp when the task was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * User who owns this task.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Initializes the createdAt timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Enum representing the possible statuses of a task.
     */
    public enum Status {
        TODO,
        IN_PROGRESS,
        DONE
    }
}
