package com.authService.AOP;


import com.authService.DTO.NotificationEvent;
import com.authService.DTO.SignupRequest;
import com.authService.entities.User;
import com.authService.services.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(sendNotification)", returning = "result")
    public void handleNotification(JoinPoint joinPoint, SendNotification sendNotification, Object result) {
        try {
            // 1. Initialize Envelope
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(sendNotification.eventType())
                    .timestamp(LocalDateTime.now())
                    .priority("MEDIUM")
                    .payload(new HashMap<>())
                    .build();

            // 2. Populate Data based on Logic
            if (result instanceof OtpService.OtpEvent) {
                // OTP Case
                OtpService.OtpEvent otpData = (OtpService.OtpEvent) result;
                event.setTargetEmail(otpData.getEmail());
                event.getPayload().put("otp", otpData.getOtpCode());
                event.setPriority("HIGH"); // OTP is urgent
            }
            else if (joinPoint.getArgs()[0] instanceof SignupRequest) {
                // Signup Case
                SignupRequest req = (SignupRequest) joinPoint.getArgs()[0];
                event.setTargetEmail("hod@department.com"); // Logic to find HOD email needed here
                event.getPayload().put("userEmail", req.getEmail());
                event.getPayload().put("collegeId", req.getCollegeId());
            }
            else if (result instanceof User) {
                // Status Change / Admin Create Case
                User user = (User) result;
                event.setTargetUserId(user.getId());
                event.setTargetEmail(user.getEmail());

                if ("ADMIN_USER_CREATED".equals(event.getEventType())) {
                    // Password is in args for this specific method
                    // Assuming logic to extract temp password
                    event.getPayload().put("message", "Admin account created.");
                }
            }

            // 3. Send to Kafka
            String jsonMessage = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(sendNotification.topic(), jsonMessage);

            System.out.println("✅ AOP Published: " + event.getEventType());

        } catch (Exception e) {
            System.err.println("❌ AOP Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}