package com.example.taskmanager.service;

import com.example.taskmanager.dto.AuthResponse;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.exception.UserAlreadyExistsException;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for authentication operations.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * Logger for AuthService.
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * User repository for user operations.
     */
    private final UserRepository userRepository;

    /**
     * Password encoder for password hashing.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT utility for token operations.
     */
    private final JwtUtil jwtUtil;

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return authentication response with JWT token
     * @throws UserAlreadyExistsException if email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        logger.info("Attempting to register user with email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getEmail());

        // Generate token
        String token = jwtUtil.generateToken(user.getEmail());
        
        return new AuthResponse(token, "User registered successfully");
    }

    /**
     * Authenticates a user.
     *
     * @param request the login request
     * @return authentication response with JWT token
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Attempting to login user with email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", request.getEmail());
                    return new IllegalArgumentException("Invalid email or password");
                });

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed - invalid password for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Generate token
        String token = jwtUtil.generateToken(user.getEmail());
        logger.info("User logged in successfully: {}", user.getEmail());

        return new AuthResponse(token, "Login successful");
    }
}
