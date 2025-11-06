package com.sugboaid.utils;

/**
 * Utility class containing validation error messages for authentication
 * Provides consistent error messages throughout the authentication system
 */
public class ValidationMessages {
    
    // Name validation messages
    public static final String EMPTY_NAME = "Name is required";
    public static final String INVALID_NAME = "Name must be at least 2 characters";
    
    // Email validation messages
    public static final String EMPTY_EMAIL = "Email is required";
    public static final String INVALID_EMAIL = "Please enter a valid email address";
    public static final String EMAIL_EXISTS = "An account with this email already exists";
    
    // Password validation messages
    public static final String EMPTY_PASSWORD = "Password is required";
    public static final String WEAK_PASSWORD = "Password must be at least 6 characters";
    public static final String PASSWORD_MISMATCH = "Passwords do not match";
    
    // Authentication messages
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String USER_NOT_FOUND = "No account found with this email";
    public static final String REGISTRATION_FAILED = "Registration failed. Please try again";
    public static final String LOGIN_FAILED = "Login failed. Please try again";
    
    // Success messages
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String REGISTRATION_SUCCESS = "Account created successfully";
    public static final String LOGOUT_SUCCESS = "Logged out successfully";
    
    // Session messages
    public static final String SESSION_EXPIRED = "Your session has expired. Please log in again";
    public static final String SESSION_INVALID = "Invalid session. Please log in again";
    public static final String SESSION_SAVE_FAILED = "Failed to save session. Please try again";
    
    // General error messages
    public static final String NETWORK_ERROR = "Network error. Please check your connection and try again";
    public static final String UNKNOWN_ERROR = "An unexpected error occurred. Please try again";
    public static final String VALIDATION_ERROR = "Please check your information and try again";
    
    // Private constructor to prevent instantiation
    private ValidationMessages() {
        throw new UnsupportedOperationException("ValidationMessages is a utility class and cannot be instantiated");
    }
}