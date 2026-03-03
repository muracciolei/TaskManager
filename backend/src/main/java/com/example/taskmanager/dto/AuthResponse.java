package com.example.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication operations (register/login).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT token for authenticated user.
     */
    private String token;

    /**
     * Message describing the result of the operation.
     */
    private String message;
}
