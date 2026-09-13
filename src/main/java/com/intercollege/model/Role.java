package com.intercollege.model;

/**
 * Represents user roles within the InterCollege application.
 * - STUDENT: Can discover events, filter by location/category, get recommendations, view event details.
 * - COORDINATOR: Can create, update, delete, and manage events belonging to their registered college.
 */
public enum Role {
    STUDENT,
    COORDINATOR
}
