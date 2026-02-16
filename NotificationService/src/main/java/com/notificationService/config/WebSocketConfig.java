package com.notificationService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // --- SECURITY INTERCEPTOR ---
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // 1. Check if user is trying to CONNECT
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract User ID from STOMP Headers (Frontend must send this!)
                    String userId = accessor.getFirstNativeHeader("userId");
                    // In production: You should pass a JWT here and validate it

                    if (userId == null) {
                        throw new IllegalArgumentException("Missing User Identity");
                    }

                    // Save userId in session attributes for later use
                    accessor.getSessionAttributes().put("userId", userId);
                }

                // 2. Check if user is trying to SUBSCRIBE
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination(); // e.g., /topic/notifications/5
                    String sessionUserId = (String) accessor.getSessionAttributes().get("userId");

                    // ENFORCE RULE: User 5 can only subscribe to /topic/notifications/5
                    if (destination != null && !destination.endsWith("/" + sessionUserId)) {
                        throw new IllegalArgumentException("Unauthorized Subscription");
                    }
                }
                return message;
            }
        });
    }
}