package com.notificationService.services;


import com.notificationService.DTO.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendEmail(NotificationEvent event, String subject, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();

            // Sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", "University Platform");
            sender.put("email", "no-reply@university.com");
            body.put("sender", sender);

            // Receiver
            Map<String, String> to = new HashMap<>();
            to.put("email", event.getTargetEmail());
            body.put("to", List.of(to));

            // Content
            body.put("subject", subject);
            body.put("htmlContent", "<html><body>" + content + "</body></html>");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // Send
            restTemplate.postForEntity(BREVO_URL, request, String.class);
            log.info("📧 Email sent to {}", event.getTargetEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send email: {}", e.getMessage());
            // In production: Push to Dead Letter Queue (DLQ)
        }
    }
}