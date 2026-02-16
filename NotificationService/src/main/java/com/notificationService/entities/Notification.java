package com.notificationService.entities;

import com.notificationService.ENUM.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // The receiver

    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // INFO, WARNING, SUCCESS, ERROR

    private boolean isRead;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Stores extra data (like MongoDB ID for approvals) as JSON string
    @Column(columnDefinition = "TEXT")
    private String metadata;
}

