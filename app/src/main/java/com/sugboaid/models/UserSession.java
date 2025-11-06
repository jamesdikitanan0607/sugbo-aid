package com.sugboaid.models;

import com.google.gson.Gson;

/**
 * UserSession data model for managing user authentication sessions
 * Handles session persistence and expiration logic
 */
public class UserSession {
    private String userId;
    private String email;
    private String name;
    private long loginTimestamp;
    private boolean isActive;

    // Session duration: 24 hours in milliseconds
    private static final long SESSION_DURATION = 24 * 60 * 60 * 1000L;

    // Default constructor for Gson
    public UserSession() {
        this.isActive = true;
    }

    // Constructor for creating new session
    public UserSession(String userId, String email, String name) {
        this();
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.loginTimestamp = System.currentTimeMillis();
    }

    // Constructor with all fields
    public UserSession(String userId, String email, String name, long loginTimestamp, boolean isActive) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.loginTimestamp = loginTimestamp;
        this.isActive = isActive;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public long getLoginTimestamp() {
        return loginTimestamp;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLoginTimestamp(long loginTimestamp) {
        this.loginTimestamp = loginTimestamp;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Session validation methods
    public boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - loginTimestamp) > SESSION_DURATION;
    }

    public boolean isValid() {
        return isActive && !isExpired() && 
               userId != null && !userId.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               name != null && !name.trim().isEmpty() &&
               loginTimestamp > 0;
    }

    // Get remaining session time in milliseconds
    public long getRemainingTime() {
        if (isExpired()) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        return SESSION_DURATION - (currentTime - loginTimestamp);
    }

    // Get remaining session time in hours
    public long getRemainingHours() {
        return getRemainingTime() / (60 * 60 * 1000L);
    }

    // Refresh session timestamp
    public void refreshSession() {
        this.loginTimestamp = System.currentTimeMillis();
        this.isActive = true;
    }

    // Invalidate session
    public void invalidate() {
        this.isActive = false;
    }

    // JSON serialization methods
    public String toJson() {
        return new Gson().toJson(this);
    }

    public static UserSession fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return new Gson().fromJson(json, UserSession.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Create session from User object
    public static UserSession fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserSession(user.getId(), user.getEmail(), user.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserSession session = (UserSession) obj;
        return userId != null ? userId.equals(session.userId) : session.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", loginTimestamp=" + loginTimestamp +
                ", isActive=" + isActive +
                ", isExpired=" + isExpired() +
                '}';
    }
}