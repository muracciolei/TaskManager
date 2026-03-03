package com.example.taskmanager.exception;

/**
 * Exception thrown when attempting to register a user with an email that already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new UserAlreadyExistsException with the specified message.
     *
     * @param message the detail message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
