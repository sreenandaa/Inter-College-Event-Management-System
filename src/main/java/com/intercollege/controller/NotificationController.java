package com.intercollege.controller;

import com.intercollege.dto.ApiResponse;
import com.intercollege.model.Notification;
import com.intercollege.model.User;
import com.intercollege.service.NotificationService;
import com.intercollege.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for retrieving and managing in-app notifications.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpSession session) {
        String userId = (String) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) return null;
        return userService.findById(userId).orElse(null);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(HttpSession session) {
        User user = getAuthenticatedUser(session);
        List<Notification> notifications = notificationService.getNotificationsForUser(user);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(HttpSession session) {
        User user = getAuthenticatedUser(session);
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String id) {
        boolean marked = notificationService.markAsRead(id);
        if (marked) {
            return ResponseEntity.ok(ApiResponse.success("Marked as read.", null));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error("Notification not found."));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpSession session) {
        User user = getAuthenticatedUser(session);
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read.", null));
    }
}
