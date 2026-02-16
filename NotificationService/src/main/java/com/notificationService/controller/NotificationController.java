package com.notificationService.controller;

import com.notificationService.entities.Notification;
import com.notificationService.services.NotificationService; // We will create this below
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 1. Fetch unread or all notifications for the logged-in user
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            @RequestHeader("loggedInUserId") Long userId, // From Gateway
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, unreadOnly));
    }

    // 2. Mark a specific notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 3. Mark ALL as read
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("loggedInUserId") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}