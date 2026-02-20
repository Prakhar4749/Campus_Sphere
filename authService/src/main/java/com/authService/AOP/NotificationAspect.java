package com.authService.AOP;

import com.authService.DTO.NotificationEvent;
import com.authService.DTO.SignupRequest;
import com.authService.entities.User;
import com.authService.services.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(sendNotification)", returning = "result")
    public void handleNotification(JoinPoint joinPoint, SendNotification sendNotification, Object result) {
        try {
            // 1. Initialize Event Envelope
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(sendNotification.eventType())
                    .timestamp(LocalDateTime.now())
                    .priority("MEDIUM")
                    .payload(new HashMap<>())
                    .build();

            // 2. Populate Event Data safely
            populateEventData(joinPoint, sendNotification.eventType(), result, event);

            // 3. Serialize
            String jsonMessage = objectMapper.writeValueAsString(event);

            // 4. Send & Handle Async Callback (Crucial for Production)
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(sendNotification.topic(), jsonMessage);

            future.whenComplete((sendResult, exception) -> {
                if (exception == null) {
                    log.info("✅ Kafka Published: [{}] on topic [{}] for [{}]",
                            event.getEventType(), sendNotification.topic(), event.getTargetEmail());
                } else {
                    log.error("❌ Kafka Delivery Failed: [{}] for [{}]. Reason: {}",
                            event.getEventType(), event.getTargetEmail(), exception.getMessage());
                }
            });

        } catch (Exception e) {
            // Log properly, do NOT use e.printStackTrace()
            log.error("❌ AOP Notification Prep Failed for event [{}]: {}", sendNotification.eventType(), e.getMessage(), e);
        }
    }

    // Extracted logic to keep the main advice clean
    private void populateEventData(JoinPoint joinPoint, String eventType, Object result, NotificationEvent event) {

        if (result instanceof OtpService.OtpEvent otpData) {
            event.setTargetEmail(otpData.getEmail());
            event.getPayload().put("otp", otpData.getOtpCode());
            event.setPriority("HIGH");

        } else if ("ADMIN_USER_CREATED".equals(eventType) && result instanceof User user) {
            event.setTargetUserId(user.getId());
            event.setTargetEmail(user.getEmail());
            if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof SignupRequest req) {
                event.getPayload().put("tempPassword", req.getPassword());
                event.getPayload().put("role", req.getRole().toString());
            }

        } else if ("USER_REGISTERED".equals(eventType)) {
            if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof SignupRequest req) {
                event.getPayload().put("userEmail", req.getEmail());
                event.getPayload().put("departmentId", req.getDepartmentId());
                event.getPayload().put("enrollmentNo", req.getEnrollmentNo());
            }

        } else if (result instanceof User user) {
            event.setTargetUserId(user.getId());
            event.setTargetEmail(user.getEmail());
            event.getPayload().put("status", user.getStatus().toString());
            if ("ACCOUNT_APPROVED".equals(eventType)) {
                event.setPriority("HIGH");
            }

        } else if ("PASSWORD_CHANGED".equals(eventType)) {
            if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof String email) {
                event.setTargetEmail(email);
                event.setPriority("HIGH");
            }
        }
    }
}