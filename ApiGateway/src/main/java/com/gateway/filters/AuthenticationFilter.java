package com.gateway.filters;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${gateway.secret}")
    private String gatewaySecret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpRequest.Builder requestBuilder = request.mutate();

            // 1. Validation Logic
            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Authorization Header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // A. Validate Token
                    jwtUtil.validateToken(authHeader);

                    // B. Extract Claims (Crucial for RBAC)
                    String role = jwtUtil.extractClaim(authHeader, claims -> claims.get("role", String.class));
                    String userId = jwtUtil.extractClaim(authHeader, claims -> claims.get("userId", String.class)); // Assuming you put userId in token, else use email

                    // C. Inject User Details into Headers for Downstream Services
                    requestBuilder.header("loggedInUserRole", role);
                    requestBuilder.header("loggedInUserEmail", jwtUtil.extractUsername(authHeader));
                    requestBuilder.header("loggedInUserId", userId);

                } catch (Exception e) {
                    System.out.println("Invalid Access: " + e.getMessage());
                    throw new RuntimeException("Unauthorized access to application");
                }
            }

            // 2. Add Gateway Secret Header (For ALL requests)
            requestBuilder.header("x-gateway-secret", gatewaySecret);

            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        });
    }

    public static class Config {
    }
}