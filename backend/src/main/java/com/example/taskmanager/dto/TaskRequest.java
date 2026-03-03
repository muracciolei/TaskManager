package com.example.taskmanager.dto;

import com.example.taskmanager.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating a task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    /**
     * Title of the task.
     */
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    /**
     * Description of the task.
     */
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    /**
     * Status of the task.
     */
    private Task.Status status;
}
