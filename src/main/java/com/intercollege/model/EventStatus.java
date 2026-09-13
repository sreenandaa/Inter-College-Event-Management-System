package com.intercollege.model;

/**
 * Dynamic status of an event calculated based on current system date/time.
 * Statuses:
 * - UPCOMING (🟢): Happening more than 3 days ahead.
 * - HAPPENING_SOON (🟡): Happening within 1 to 3 days.
 * - TODAY (🔴): Happening today before start or scheduled today.
 * - ONGOING (🔵): Currently underway (between start time and end time).
 * - COMPLETED (⚪): Event date/time has already passed.
 */
public enum EventStatus {
    UPCOMING("Upcoming", "🟢", "#408A71"),
    HAPPENING_SOON("Happening Soon", "🟡", "#E5B842"),
    TODAY("Happening Today", "🔴", "#E55353"),
    ONGOING("Ongoing", "🔵", "#4A90E2"),
    COMPLETED("Completed", "⚪", "#8E9E99");

    private final String displayName;
    private final String badgeIcon;
    private final String colorHex;

    EventStatus(String displayName, String badgeIcon, String colorHex) {
        this.displayName = displayName;
        this.badgeIcon = badgeIcon;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeIcon() {
        return badgeIcon;
    }

    public String getColorHex() {
        return colorHex;
    }
}
