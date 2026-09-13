package com.intercollege.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of event categories supported by InterCollege.
 * Includes display names, icons, and helper methods for conversion.
 */
public enum EventCategory {
    TECHNICAL("Technical"),
    CULTURAL("Cultural"),
    TALK_SESSION("Talk Session"),
    TREASURE_HUNT("Treasure Hunt"),
    PUBLIC_SPEAKING("Public Speaking"),
    QUIZ("Quiz"),
    MAKEATHON("Makeathon"),
    WORKSHOP("Workshop"),
    SPORTS("Sports"),
    OTHER("Other");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static EventCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        for (EventCategory c : EventCategory.values()) {
            if (c.displayName.equalsIgnoreCase(text.trim()) || c.name().equalsIgnoreCase(text.trim().replace(" ", "_"))) {
                return c;
            }
        }
        return OTHER;
    }
}
