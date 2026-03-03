package com.example.taskmanager.dto;

import com.example.taskmanager.model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for task operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    /**
     * Unique identifier for the task.
     */
    private Long id;

    /**
     * Title of the task.
     */
    private String title;

    /**
     * Description of the task.
     */
    private String description;

    /**
     * Status of the task.
     */
    private Task.Status status;

    /**
     * Timestamp when the task was created.
     */
    private LocalDateTime createdAt;

    /**
     * ID of the user who owns this task.
     */
    private Long userId;

    /**
     * Name of the user who owns this task.
     */
    private String userName;
}
