package com.sugboaid.models;

import com.google.gson.Gson;
import java.util.UUID;

/**
 * User data model for authentication system
 * Represents a user account with authentication and profile information
 */
public class User {
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_DONOR = "Donor";
    public static final String ROLE_RECIPIENT = "Recipient";
    public static final String ROLE_GUEST = "Guest";
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private String role;
    private long createdAt;
    private Long lastLogin;

    // Default constructor for Gson
    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor for creating new user
    public User(String name, String email, String passwordHash) {
        this();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Constructor for creating new user with role
    public User(String name, String email, String passwordHash, String role) {
        this();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Constructor with all fields
    public User(String id, String name, String email, String passwordHash, String role, long createdAt, Long lastLogin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    // Update last login timestamp
    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    // JSON serialization methods
    public String toJson() {
        return new Gson().toJson(this);
    }

    public static User fromJson(String json) {
        return new Gson().fromJson(json, User.class);
    }

    // Validation method
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               name != null && !name.trim().isEmpty() && name.trim().length() >= 2 &&
               email != null && !email.trim().isEmpty() && isValidEmail(email) &&
               passwordHash != null && !passwordHash.trim().isEmpty() &&
               createdAt > 0;
    }

    // Email validation helper
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Create a copy without sensitive data for display purposes
    public User createSafeUser() {
        User safeUser = new User();
        safeUser.id = this.id;
        safeUser.name = this.name;
        safeUser.email = this.email;
        safeUser.role = this.role;
        safeUser.createdAt = this.createdAt;
        safeUser.lastLogin = this.lastLogin;
        // passwordHash is intentionally omitted
        return safeUser;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return id != null ? id.equals(user.id) : user.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                '}';
    }
}