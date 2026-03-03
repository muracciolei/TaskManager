package com.example.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * Unique identifier for the user.
     */
    private Long id;

    /**
     * Name of the user.
     */
    private String name;

    /**
     * Email address of the user.
     */
    private String email;
}
