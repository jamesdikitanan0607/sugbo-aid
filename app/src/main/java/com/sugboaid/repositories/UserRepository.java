package com.sugboaid.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sugboaid.models.User;
import com.sugboaid.models.UserSession;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing user data operations and authentication
 * Handles user storage, session management, and authentication logic
 */
public class UserRepository {
    private static final String PREF_NAME = "sugboaid_auth";
    private static final String KEY_USERS = "users";
    private static final String KEY_CURRENT_SESSION = "current_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_TIMESTAMP = "login_timestamp";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private static UserRepository instance;

    // Private constructor for singleton pattern
    private UserRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Singleton instance getter
    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context.getApplicationContext());
        }
        return instance;
    }

    // User Management Methods

    /**
     * Save a new user to local storage
     * @param user User object to save
     * @return true if successful, false otherwise
     */
    public boolean saveUser(User user) {
        if (user == null || !user.isValid()) {
            return false;
        }

        // Check if email already exists
        if (isEmailExists(user.getEmail())) {
            return false;
        }

        List<User> users = getAllUsers();
        users.add(user);
        return saveAllUsers(users);
    }

    /**
     * Get user by email address
     * @param email Email to search for
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email.trim())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Get user by ID
     * @param userId User ID to search for
     * @return User object if found, null otherwise
     */
    public User getUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Get all users from storage
     * @return List of all users
     */
    public List<User> getAllUsers() {
        String usersJson = sharedPreferences.getString(KEY_USERS, "[]");
        Type listType = new TypeToken<List<User>>(){}.getType();
        List<User> users = gson.fromJson(usersJson, listType);
        return users != null ? users : new ArrayList<>();
    }

    /**
     * Save all users to storage
     * @param users List of users to save
     * @return true if successful, false otherwise
     */
    private boolean saveAllUsers(List<User> users) {
        try {
            String usersJson = gson.toJson(users);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERS, usersJson);
            return editor.commit();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if email already exists in storage
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    public boolean isEmailExists(String email) {
        return getUserByEmail(email) != null;
    }

    // Authentication Methods

    /**
     * Validate user credentials for login
     * @param email User email
     * @param password Plain text password
     * @return User object if credentials are valid, null otherwise
     */
    public User validateCredentials(String email, String password) {
        if (email == null || password == null || 
            email.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }

        User user = getUserByEmail(email);
        if (user == null) {
            return null;
        }

        String hashedPassword = hashPassword(password);
        if (user.getPasswordHash().equals(hashedPassword)) {
            // Update last login timestamp
            user.updateLastLogin();
            updateUser(user);
            return user;
        }

        return null;
    }

    /**
     * Register a new user with role
     * @param name User's full name
     * @param email User's email
     * @param password Plain text password
     * @param role User's selected role
     * @return User object if registration successful, null otherwise
     */
    public User registerUser(String name, String email, String password, String role) {
        if (name == null || email == null || password == null ||
            name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }

        // Validate input
        if (name.trim().length() < 2) {
            return null;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return null;
        }

        if (password.length() < 6) {
            return null;
        }

        // Check if email already exists
        if (isEmailExists(email)) {
            return null;
        }

        // Create new user
        String hashedPassword = hashPassword(password);
        User newUser = new User(name.trim(), email.trim().toLowerCase(), hashedPassword, role != null ? role : "Guest");

        if (saveUser(newUser)) {
            return newUser;
        }

        return null;
    }

    /**
     * Register a new user (backward compatibility)
     * @param name User's full name
     * @param email User's email
     * @param password Plain text password
     * @return User object if registration successful, null otherwise
     */
    public User registerUser(String name, String email, String password) {
        return registerUser(name, email, password, "Guest");
    }

    /**
     * Update existing user
     * @param user User object to update
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        if (user == null || !user.isValid()) {
            return false;
        }

        List<User> users = getAllUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                return saveAllUsers(users);
            }
        }
        return false;
    }

    // Session Management Methods

    /**
     * Save user session after successful login
     * @param user User object to create session for
     * @return true if successful, false otherwise
     */
    public boolean saveSession(User user) {
        if (user == null) {
            return false;
        }

        UserSession session = UserSession.fromUser(user);
        if (session == null) {
            return false;
        }

        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_SESSION, session.toJson());
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putLong(KEY_LOGIN_TIMESTAMP, session.getLoginTimestamp());
            return editor.commit();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get stored user session
     * @return UserSession object if valid session exists, null otherwise
     */
    public UserSession getStoredSession() {
        String sessionJson = sharedPreferences.getString(KEY_CURRENT_SESSION, null);
        if (sessionJson == null) {
            return null;
        }

        UserSession session = UserSession.fromJson(sessionJson);
        if (session != null && session.isValid()) {
            return session;
        }

        // Clear invalid session
        clearSession();
        return null;
    }

    /**
     * Check if user is currently logged in with valid session
     * @return true if logged in with valid session, false otherwise
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!isLoggedIn) {
            return false;
        }

        UserSession session = getStoredSession();
        return session != null && session.isValid();
    }

    /**
     * Clear user session (logout)
     * @return true if successful, false otherwise
     */
    public boolean clearSession() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(KEY_CURRENT_SESSION);
            editor.putBoolean(KEY_IS_LOGGED_IN, false);
            editor.remove(KEY_LOGIN_TIMESTAMP);
            return editor.commit();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh current session timestamp
     * @return true if successful, false otherwise
     */
    public boolean refreshSession() {
        UserSession session = getStoredSession();
        if (session == null) {
            return false;
        }

        session.refreshSession();
        return saveSessionObject(session);
    }

    /**
     * Save session object to storage
     * @param session UserSession object to save
     * @return true if successful, false otherwise
     */
    private boolean saveSessionObject(UserSession session) {
        if (session == null) {
            return false;
        }

        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_SESSION, session.toJson());
            editor.putBoolean(KEY_IS_LOGGED_IN, session.isActive());
            editor.putLong(KEY_LOGIN_TIMESTAMP, session.getLoginTimestamp());
            return editor.commit();
        } catch (Exception e) {
            return false;
        }
    }

    // Utility Methods

    /**
     * Hash password using SHA-256
     * @param password Plain text password
     * @return Hashed password string
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash if SHA-256 is not available
            return String.valueOf(password.hashCode());
        }
    }

    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid format, false otherwise
     */
    public boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if meets requirements, false otherwise
     */
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validate name format
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    /**
     * Clear all user data (for testing or reset purposes)
     * @return true if successful, false otherwise
     */
    public boolean clearAllData() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            return editor.commit();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get total number of registered users
     * @return Number of users
     */
    public int getUserCount() {
        return getAllUsers().size();
    }

    /**
     * Check if this is the first user registration
     * @return true if no users exist, false otherwise
     */
    public boolean isFirstUser() {
        return getUserCount() == 0;
    }
}