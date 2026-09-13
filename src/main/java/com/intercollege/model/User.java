package com.intercollege.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered user in the InterCollege application.
 * Users can either be a STUDENT or a COORDINATOR.
 *
 * NOTE FOR STUDENTS:
 * Passwords are never stored in plain text. We store a cryptographic hash
 * of the password combined with a unique salt to protect user credentials.
 */
public class User {
    private String id;
    private String fullName;
    private String username;

    // Hashed credentials - protected from being sent back in REST responses
    private String passwordHash;
    private String salt;

    private String college;
    private Role role;

    // Student specific fields
    private String location;
    private List<String> interests = new ArrayList<>();

    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String id, String fullName, String username, String passwordHash, String salt,
                String college, Role role, String location, List<String> interests) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.college = college;
        this.role = role;
        this.location = location;
        if (interests != null) {
            this.interests = new ArrayList<>(interests);
        }
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // For file serialization, we read/write passwordHash and salt,
    // but in REST responses we can strip them or use a DTO.
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Creates a safe copy of the user with password details removed.
     * This is sent to the frontend so passwords are never exposed.
     */
    public User toSafeUser() {
        User safe = new User();
        safe.setId(this.id);
        safe.setFullName(this.fullName);
        safe.setUsername(this.username);
        safe.setCollege(this.college);
        safe.setRole(this.role);
        safe.setLocation(this.location);
        safe.setInterests(this.interests);
        safe.setCreatedAt(this.createdAt);
        // passwordHash and salt remain null
        return safe;
    }
}
