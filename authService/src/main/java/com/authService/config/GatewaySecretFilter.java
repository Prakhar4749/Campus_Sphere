package com.authService.config; // Change package per service

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GatewaySecretFilter extends OncePerRequestFilter {

    // Read from application.properties
    @Value("${gateway.secret}")
    private String expectedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get Header
        String requestSecret = request.getHeader("x-gateway-secret");

        // 2. Validate
        if (requestSecret == null || !requestSecret.equals(expectedSecret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied: Request must come through API Gateway");
            return; // Stop execution
        }

        // 3. Continue if valid
        filterChain.doFilter(request, response);
    }
}