package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow all origins (using patterns is safer and required when credentials are true)
        corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));

        // Allow all HTTP methods (GET, POST, OPTIONS, etc.)
        corsConfig.setAllowedMethods(Arrays.asList("*"));

        // Allow all headers
        corsConfig.setAllowedHeaders(Arrays.asList("*"));

        // Required for WebSockets and passing headers/tokens
        corsConfig.setAllowCredentials(true);

        // Cache the CORS response for 1 hour to speed up requests
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS policy to ALL routes in the gateway
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}