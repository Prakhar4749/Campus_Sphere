package com.notificationService.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationService.DTO.HodDetailsResponse;
import com.notificationService.DTO.NotificationEvent;
import com.notificationService.clients.AdminClient;
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
    private final AdminClient adminClient;



    private final NotificationRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"notification.otp","notification.user", "notification.system"}, groupId = "notification-group")
    public void consume(String message) {
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            log.info("Consumed Event: {} | Type: {}", event.getEventId(), event.getEventType());

            // --- ROUTING LOGIC ---
            switch (event.getEventType()) {
                // --- DISTINCT OTP HANDLING ---
                case "OTP_EMAIL_VERIFICATION":
                    sendOtpEmail(event, "Verify Your Email", "Welcome! Your signup verification code is:");
                    break;

                case "OTP_PASSWORD_RESET":
                    sendOtpEmail(event, "Reset Password Request", "Security Alert: Your password reset code is:");
                    break;

                // --- DUAL NOTIFICATION HANDLING ---
                case "USER_REGISTERED":
                    handleDualRegistrationNotification(event);
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

    // Helper for OTPs
    private void sendOtpEmail(NotificationEvent event, String subject, String prefix) {
        String otp = (String) event.getPayload().get("otp");
        String html = String.format("<h3>%s</h3><h1>%s</h1>", prefix, otp);

        // We temporarily set targetEmail on event if it's missing (though Producer sets it for OTPs)
        emailService.sendEmail(event.getTargetEmail(), subject, html);
    }

    // Helper for Dual Notification
    private void handleDualRegistrationNotification(NotificationEvent event) {
        Map<String, Object> payload = event.getPayload();
        String userEmail = (String) payload.get("userEmail");
        String userName = (String) payload.get("enrollmentNo");

        // 1. Send Email to the USER (Student)
        String userHtml = "<h1>Registration Successful</h1><p>Hi " + userName + ", your account is pending approval.</p>";
        emailService.sendEmail(userEmail, "Welcome to the Platform", userHtml);
        log.info("✅ Sent Welcome Email to User: {}", userEmail);

        // 2. Notify HOD
        try {
            Long deptId = ((Number) payload.get("departmentId")).longValue();

            // CALL ADMIN SERVICE VIA FEIGN (Easy & Reliable)
            HodDetailsResponse hod = adminClient.getHodDetails(deptId);

            if (hod != null && hod.getEmail() != null) {
                // A. Send Email
                String msg = "New student waiting for approval.";
                emailService.sendEmail(hod.getEmail(), "Approval Request", msg);

                // B. Send Real-time Notification
                // Create a temporary event targeting the HOD's ID
                NotificationEvent hodEvent = NotificationEvent.builder()
                        .targetUserId(hod.getUserId()) // <--- Now we have the ID!
                        .payload(payload)
                        .build();

                saveAndSendInApp(hodEvent, "New Student", msg, "INFO");
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch HOD details or send notification: {}", e.getMessage());
        }
    }


    private void handleApprovalRequest(NotificationEvent event) {
        // Rule: EMAIL + IN_APP (For Admin/HOD)
        String userEmail = (String) event.getPayload().get("userEmail");
        String message = "New user registration: " + userEmail + ". Needs approval.";

        // 1. Send Email
        emailService.sendEmail(event.getTargetEmail(), "Action Required: New Registration", "<p>" + message + "</p>");

        // 2. Save In-App Notification
        saveAndSendInApp(event, "New Registration", message, "INFO");
    }

    private void handleAccountApproved(NotificationEvent event) {
        // Rule: EMAIL + IN_APP (For Student)
        String msg = "Congratulations! Your account has been approved.";
        emailService.sendEmail(event.getTargetEmail(), "Welcome to University Platform", "<p>" + msg + "</p>");
        saveAndSendInApp(event, "Account Approved", msg, "SUCCESS");
    }

    private void handlePasswordReset(NotificationEvent event) {
        // Rule: EMAIL ONLY (Security)
        emailService.sendEmail(event.getTargetEmail(), "Security Alert", "<p>Your password was just changed.</p>");
    }


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