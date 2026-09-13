package com.intercollege.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intercollege.model.Notification;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * File-backed repository for notifications stored in data/notifications.json.
 */
@Repository
public class NotificationRepository {

    private static final String FILE_NAME = "notifications.json";
    private final JsonFileManager jsonFileManager;
    private final List<Notification> notificationCache = new CopyOnWriteArrayList<>();

    public NotificationRepository(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    @PostConstruct
    public synchronized void loadFromDisk() {
        List<Notification> loaded = jsonFileManager.readList(FILE_NAME, new TypeReference<List<Notification>>() {});
        notificationCache.clear();
        if (loaded != null) {
            notificationCache.addAll(loaded);
        }
    }

    private synchronized void persistToDisk() {
        jsonFileManager.writeList(FILE_NAME, new ArrayList<>(notificationCache));
    }

    public List<Notification> findAll() {
        return new ArrayList<>(notificationCache);
    }

    public List<Notification> findByUserId(String userId) {
        return notificationCache.stream()
                .filter(n -> "ALL".equalsIgnoreCase(n.getUserId()) ||
                             (userId != null && userId.equalsIgnoreCase(n.getUserId())))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public synchronized Notification save(Notification notification) {
        if (notification.getId() == null || notification.getId().trim().isEmpty()) {
            notification.setId(UUID.randomUUID().toString());
            notificationCache.add(0, notification);
        } else {
            int existingIndex = -1;
            for (int i = 0; i < notificationCache.size(); i++) {
                if (notificationCache.get(i).getId().equals(notification.getId())) {
                    existingIndex = i;
                    break;
                }
            }
            if (existingIndex >= 0) {
                notificationCache.set(existingIndex, notification);
            } else {
                notificationCache.add(0, notification);
            }
        }
        persistToDisk();
        return notification;
    }

    public synchronized boolean markAsRead(String id) {
        for (Notification n : notificationCache) {
            if (n.getId().equals(id)) {
                n.setRead(true);
                persistToDisk();
                return true;
            }
        }
        return false;
    }

    public synchronized void markAllAsRead(String userId) {
        boolean modified = false;
        for (Notification n : notificationCache) {
            if ("ALL".equalsIgnoreCase(n.getUserId()) || (userId != null && userId.equalsIgnoreCase(n.getUserId()))) {
                if (!n.isRead()) {
                    n.setRead(true);
                    modified = true;
                }
            }
        }
        if (modified) {
            persistToDisk();
        }
    }

    public long countUnread(String userId) {
        return notificationCache.stream()
                .filter(n -> !n.isRead() && ("ALL".equalsIgnoreCase(n.getUserId()) ||
                             (userId != null && userId.equalsIgnoreCase(n.getUserId()))))
                .count();
    }

    public synchronized void saveAll(List<Notification> notifications) {
        notificationCache.clear();
        notificationCache.addAll(notifications);
        persistToDisk();
    }
}
