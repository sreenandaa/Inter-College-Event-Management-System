package com.intercollege.service;

import com.intercollege.model.Event;
import com.intercollege.model.Notification;
import com.intercollege.model.User;
import com.intercollege.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service managing in-app notifications and smart event reminders.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getNotificationsForUser(User user) {
        String userId = (user != null) ? user.getId() : "ALL";
        return notificationRepository.findByUserId(userId);
    }

    public long getUnreadCount(User user) {
        String userId = (user != null) ? user.getId() : "ALL";
        return notificationRepository.countUnread(userId);
    }

    public boolean markAsRead(String notificationId) {
        return notificationRepository.markAsRead(notificationId);
    }

    public void markAllAsRead(User user) {
        String userId = (user != null) ? user.getId() : "ALL";
        notificationRepository.markAllAsRead(userId);
    }

    public Notification sendNotification(String userId, String title, String message, String type, String relatedEventId) {
        Notification notification = new Notification();
        notification.setUserId(userId != null ? userId : "ALL");
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEventId(relatedEventId);
        return notificationRepository.save(notification);
    }

    public void notifyNewEvent(Event event) {
        String title = "New Event: " + event.getName();
        String message = event.getCollege() + " just announced '" + event.getName() + "' in " + event.getCity() + "!";
        sendNotification("ALL", title, message, "NEW_EVENT", event.getId());
    }
}
