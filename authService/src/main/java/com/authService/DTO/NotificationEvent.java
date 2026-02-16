package com.authService.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID eventId;
    private String eventType; // e.g., USER_REGISTERED, OTP_GENERATED
    private LocalDateTime timestamp;

    private Long actorId;        // Who triggered it (optional)
    private Long targetUserId;   // Who receives it (MANDATORY)
    private String targetEmail;  // Email of receiver

    private String priority;     // HIGH, MEDIUM, LOW

    // Which channels to use? (Can be set by Router or Publisher)
    private Set<String> channels; // EMAIL, IN_APP, PUSH

    // Dynamic payload for templates (e.g., otpCode, mongoDocId, approvalLink)
    private Map<String, Object> payload;
}