package com.example.taskmanager.security;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT authentication filter that processes each request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT utility for token operations.
     */
    private final JwtUtil jwtUtil;

    /**
     * User repository for fetching user details.
     */
    private final UserRepository userRepository;

    /**
     * Filters each request to extract and validate JWT token.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");

        // No token provided: continue so security config can return 401 for protected endpoints
        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Authorization header");
            return;
        }

        final String jwt = authHeader.substring(7).trim();
        if (jwt.isEmpty() || !jwtUtil.validateToken(jwt)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired JWT token");
            return;
        }

        try {
            final String userEmail = jwtUtil.extractEmail(jwt);

            // If email is extracted and no authentication is set
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Find user by email
                User user = userRepository.findByEmail(userEmail).orElse(null);

                // If user exists and token is valid, set authentication
                if (user == null) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired JWT token");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.emptyList()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired JWT token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
