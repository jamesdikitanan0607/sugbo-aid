package com.sugboaid.app.data.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String role; // DONOR, ORGANIZATION, VOLUNTEER, RECIPIENT, GUEST
    private String profileImage;
    private long createdAt;
    private boolean isActive;

    public User() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    public User(String id, String name, String email, String role) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}