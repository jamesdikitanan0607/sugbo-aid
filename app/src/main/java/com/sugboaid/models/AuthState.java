package com.sugboaid.models;

/**
 * AuthState data model for managing authentication state
 * Represents the current authentication status and user information
 */
public class AuthState {
    private boolean isAuthenticated;
    private User user;
    private UserSession session;

    // Default constructor
    public AuthState() {
        this.isAuthenticated = false;
        this.user = null;
        this.session = null;
    }

    // Constructor for authenticated state
    public AuthState(boolean isAuthenticated, User user, UserSession session) {
        this.isAuthenticated = isAuthenticated;
        this.user = user;
        this.session = session;
    }

    // Static factory methods
    public static AuthState authenticated(User user, UserSession session) {
        return new AuthState(true, user, session);
    }

    public static AuthState unauthenticated() {
        return new AuthState(false, null, null);
    }

    // Getters
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public User getUser() {
        return user;
    }

    public UserSession getSession() {
        return session;
    }

    // Setters
    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }

    // Utility methods
    public boolean hasValidSession() {
        return isAuthenticated && session != null && session.isValid();
    }

    public String getUserName() {
        return user != null ? user.getName() : null;
    }

    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }

    public String getUserId() {
        return user != null ? user.getId() : null;
    }

    // Clear authentication state
    public void clear() {
        this.isAuthenticated = false;
        this.user = null;
        this.session = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AuthState authState = (AuthState) obj;
        return isAuthenticated == authState.isAuthenticated &&
               (user != null ? user.equals(authState.user) : authState.user == null) &&
               (session != null ? session.equals(authState.session) : authState.session == null);
    }

    @Override
    public int hashCode() {
        int result = (isAuthenticated ? 1 : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuthState{" +
                "isAuthenticated=" + isAuthenticated +
                ", user=" + (user != null ? user.toString() : "null") +
                ", session=" + (session != null ? session.toString() : "null") +
                '}';
    }
}