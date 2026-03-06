package com.example.taskmanager.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class to load .env file and set environment variables.
 * This allows Spring Boot to read from .env files.
 */
public class DotenvConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        Map<String, Object> properties = new HashMap<>();
        
        // Load JWT_SECRET from .env if present
        dotenv.entries().forEach(entry -> {
            properties.put(entry.getKey(), entry.getValue());
        });

        environment.getPropertySources().addFirst(new MapPropertySource("dotenv", properties));
    }
}
