package com.example.taskmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for JWT token operations.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * Secret key for JWT token generation and validation.
     */
    @Value("${app.jwt.secret}")
    private String secret;

    /**
     * Token expiration configuration from environment.
     */
    @Value("${app.jwt.expiration}")
    private String expiration;

    private long expirationMillis;

    @PostConstruct
    public void validateConfiguration() {
        logger.info("JWT Configuration: Initializing JWT utility...");
        logger.debug("JWT Secret length from config: {}", secret != null ? secret.length() : 0);
        validateSecret();
        expirationMillis = parseExpirationToMillis(expiration);
        logger.info("JWT Configuration: Validation successful. Secret length: {}, Expiration: {}", 
                    secret != null ? secret.length() : 0, expiration);
    }

    private void validateSecret() {
        if (secret == null || secret.trim().isEmpty()) {
            logger.error("JWT_SECRET is not set or is empty. Please set the JWT_SECRET environment variable.");
            throw new IllegalStateException("JWT secret is required. Set the JWT_SECRET environment variable.");
        }

        String trimmedSecret = secret.trim();
        logger.debug("JWT Secret (first 4 chars): {}", trimmedSecret.substring(0, Math.min(4, trimmedSecret.length())) + "...");
        
        if (trimmedSecret.length() < 48) {
            logger.error("JWT_SECRET is too short. Current length: {}, Required minimum: 48", trimmedSecret.length());
            throw new IllegalStateException("JWT secret must be at least 48 characters long.");
        }
        
        String lowerSecret = trimmedSecret.toLowerCase(Locale.ROOT);
        if (lowerSecret.contains("secret") || lowerSecret.contains("changeme") || lowerSecret.contains("password")) {
            logger.error("JWT_SECRET contains weak keywords. This is not secure.");
            throw new IllegalStateException("JWT secret appears weak. Use a long random value.");
        }
        if (trimmedSecret.chars().distinct().count() < 12) {
            logger.error("JWT_SECRET has too few unique characters. Current unique chars: {}", trimmedSecret.chars().distinct().count());
            throw new IllegalStateException("JWT secret appears weak. Use a long random value.");
        }
        logger.debug("JWT_SECRET validation passed. Length: {}, Unique chars: {}", trimmedSecret.length(), trimmedSecret.chars().distinct().count());
    }

    private long parseExpirationToMillis(String rawExpiration) {
        if (rawExpiration == null || rawExpiration.trim().isEmpty()) {
            throw new IllegalStateException("JWT expiration is required. Set the JWT_EXPIRATION environment variable.");
        }

        String value = rawExpiration.trim().toLowerCase(Locale.ROOT);
        try {
            if (value.matches("\\d+")) {
                long seconds = Long.parseLong(value);
                if (seconds <= 0) {
                    throw new IllegalStateException("JWT_EXPIRATION must be greater than zero.");
                }
                return Math.multiplyExact(seconds, 1000L);
            }

            if (!value.matches("\\d+[smhd]")) {
                throw new IllegalStateException("Invalid JWT_EXPIRATION format. Use seconds (e.g. 3600) or units s/m/h/d (e.g. 1h, 7d).");
            }

            long amount = Long.parseLong(value.substring(0, value.length() - 1));
            char unit = value.charAt(value.length() - 1);
            if (amount <= 0) {
                throw new IllegalStateException("JWT_EXPIRATION must be greater than zero.");
            }

            long multiplier = switch (unit) {
                case 's' -> 1000L;
                case 'm' -> 60_000L;
                case 'h' -> 3_600_000L;
                case 'd' -> 86_400_000L;
                default -> throw new IllegalStateException("Invalid JWT_EXPIRATION format.");
            };

            return Math.multiplyExact(amount, multiplier);
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new IllegalStateException("Invalid JWT_EXPIRATION value: " + rawExpiration, ex);
        }
    }

    public String generateToken(String email) {
        logger.debug("Generating JWT token for email: {}", email);
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(getSigningKey())
                .compact();
        logger.debug("JWT token generated successfully for: {}", email);
        return token;
    }

    public String extractEmail(String token) {
        logger.debug("Extracting email from JWT token");
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        logger.debug("Validating JWT token");
        try {
            Claims claims = extractAllClaims(token);
            boolean isValid = !claims.getExpiration().before(new Date());
            logger.debug("JWT token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return the claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Gets the signing key for JWT token generation.
     *
     * @return the secret key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
