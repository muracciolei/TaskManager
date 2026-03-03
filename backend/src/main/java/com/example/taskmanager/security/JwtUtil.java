package com.example.taskmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
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
        validateSecret();
        expirationMillis = parseExpirationToMillis(expiration);
    }

    private void validateSecret() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is required. Set the JWT_SECRET environment variable.");
        }

        String trimmedSecret = secret.trim();
        String lowerSecret = trimmedSecret.toLowerCase(Locale.ROOT);
        if (trimmedSecret.length() < 48) {
            throw new IllegalStateException("JWT secret must be at least 48 characters long.");
        }
        if (lowerSecret.contains("secret") || lowerSecret.contains("changeme") || lowerSecret.contains("password")) {
            throw new IllegalStateException("JWT secret appears weak. Use a long random value.");
        }
        if (trimmedSecret.chars().distinct().count() < 12) {
            throw new IllegalStateException("JWT secret appears weak. Use a long random value.");
        }
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
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
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
