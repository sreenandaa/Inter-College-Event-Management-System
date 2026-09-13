package com.intercollege.model;

import java.time.LocalDateTime;

/**
 * Model representing in-app notifications for students.
 * Stored in data/notifications.json.
 */
public class Notification {
    private String id;
    private String userId; // Specific student id or "ALL"
    private String title;
    private String message;
    private String type; // URGENCY, INTEREST_MATCH, LOCATION_MATCH, NEW_EVENT
    private String relatedEventId;
    private boolean read;
    private LocalDateTime createdAt;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public Notification(String id, String userId, String title, String message, String type, String relatedEventId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedEventId = relatedEventId;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRelatedEventId() {
        return relatedEventId;
    }

    public void setRelatedEventId(String relatedEventId) {
        this.relatedEventId = relatedEventId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
