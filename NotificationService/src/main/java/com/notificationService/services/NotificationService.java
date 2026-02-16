package com.notificationService.services;

import com.notificationService.entities.Notification;

import com.notificationService.repositopries.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public List<Notification> getUserNotifications(Long userId, boolean unreadOnly) {
        if (unreadOnly) {
            return repository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        }
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        repository.save(n);
    }

    public void markAllAsRead(Long userId) {
        repository.markAllAsRead(userId); // Custom Query in Repo
    }
}