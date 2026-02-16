package com.notificationService.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationService.DTO.NotificationEvent;
import com.notificationService.entities.Notification;

import com.notificationService.repositopries.NotificationRepository;
import com.notificationService.services.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"notification.user", "notification.system"}, groupId = "notification-group")
    public void consume(String message) {
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            log.info("Consumed Event: {} | Type: {}", event.getEventId(), event.getEventType());

            // --- ROUTING LOGIC ---
            switch (event.getEventType()) {
                case "OTP_GENERATED":
                    handleOtpEvent(event);
                    break;
                case "USER_REGISTERED":
                    handleApprovalRequest(event); // For HOD
                    break;
                case "ACCOUNT_APPROVED":
                    handleAccountApproved(event);
                    break;
                case "PASSWORD_RESET":
                    handlePasswordReset(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage());
        }
    }

    // --- HANDLERS ---

    private void handleOtpEvent(NotificationEvent event) {
        // Rule: EMAIL ONLY
        String otp = (String) event.getPayload().get("otp");
        String html = "<h1>Login OTP</h1><p>Your One-Time Password is: <b>" + otp + "</b></p>";
        emailService.sendEmail(event, "Your Verification Code", html);
    }

    private void handleApprovalRequest(NotificationEvent event) {
        // Rule: EMAIL + IN_APP (For Admin/HOD)
        String userEmail = (String) event.getPayload().get("userEmail");
        String message = "New user registration: " + userEmail + ". Needs approval.";

        // 1. Send Email
        emailService.sendEmail(event, "Action Required: New Registration", "<p>" + message + "</p>");

        // 2. Save In-App Notification
        saveAndSendInApp(event, "New Registration", message, "INFO");
    }

    private void handleAccountApproved(NotificationEvent event) {
        // Rule: EMAIL + IN_APP (For Student)
        String msg = "Congratulations! Your account has been approved.";
        emailService.sendEmail(event, "Welcome to University Platform", "<p>" + msg + "</p>");
        saveAndSendInApp(event, "Account Approved", msg, "SUCCESS");
    }

    private void handlePasswordReset(NotificationEvent event) {
        // Rule: EMAIL ONLY (Security)
        emailService.sendEmail(event, "Security Alert", "<p>Your password was just changed.</p>");
    }

    // --- HELPER: Save to DB & Push to WebSocket ---
    private void saveAndSendInApp(NotificationEvent event, String title, String message, String type) {
        // 1. Persist
        Notification n = Notification.builder()
                .userId(event.getTargetUserId())
                .title(title)
                .message(message)
                //.type(NotificationType.valueOf(type))
                .isRead(false)
                .metadata(event.getPayload().toString())
                .build();
        repository.save(n);

        // 2. Real-time Push (Topic: /topic/notifications/{userId})
        simpMessagingTemplate.convertAndSend("/topic/notifications/" + event.getTargetUserId(), n);
    }
}