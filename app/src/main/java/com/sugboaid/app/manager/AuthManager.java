package com.sugboaid.app.manager;

import android.content.Context;
import com.sugboaid.app.data.repository.DataRepository;
import com.sugboaid.app.data.model.User;
import com.sugboaid.app.util.Constants;

public class AuthManager {
    private DataRepository repository;
    private static AuthManager instance;

    public enum LoginResult {
        SUCCESS,
        INVALID_ROLE,
        ACCOUNT_INACTIVE,
        INVALID_CREDENTIALS
    }

    private AuthManager(Context context) {
        repository = DataRepository.getInstance(context);
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

    public LoginResult login(String email, String password, String role) {
        User user = repository.getUserByEmail(email);

        if (user == null) {
            // Create new user for demo purposes
            user = new User();
            user.setEmail(email);
            user.setName(extractNameFromEmail(email));
            user.setRole(role);
            user.setActive(true);
            repository.saveUser(user);
        }

        if (!user.isActive()) {
            return LoginResult.ACCOUNT_INACTIVE;
        }

        if (!user.getRole().equals(role)) {
            return LoginResult.INVALID_ROLE;
        }

        repository.setCurrentUser(user);
        return LoginResult.SUCCESS;
    }

    public void logout() {
        repository.clearCurrentUser();
    }

    public User getCurrentUser() {
        return repository.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public boolean hasRole(String role) {
        User user = getCurrentUser();
        return user != null && user.getRole().equals(role);
    }

    public boolean canAccessFeature(String feature) {
        User user = getCurrentUser();
        if (user == null)
            return false;

        String role = user.getRole();

        switch (feature) {
            case Constants.FEATURE_DONATION_POS:
                return role.equals(Constants.ROLE_ORGANIZATION) ||
                        role.equals(Constants.ROLE_VOLUNTEER);

            case Constants.FEATURE_INVENTORY_MANAGEMENT:
                return role.equals(Constants.ROLE_ORGANIZATION) ||
                        role.equals(Constants.ROLE_VOLUNTEER);

            case Constants.FEATURE_ANALYTICS_DASHBOARD:
                return role.equals(Constants.ROLE_ORGANIZATION);

            case Constants.FEATURE_TRANSPARENCY_VIEW:
                return true; // All roles can view transparency data

            case Constants.FEATURE_DONATION_HISTORY:
                return role.equals(Constants.ROLE_DONOR) ||
                        role.equals(Constants.ROLE_ORGANIZATION);

            default:
                return false;
        }
    }

    public User registerUser(String name, String email, String role) {
        User existingUser = repository.getUserByEmail(email);
        if (existingUser != null) {
            return null; // User already exists
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);

        repository.saveUser(user);
        return user;
    }

    private String extractNameFromEmail(String email) {
        if (email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return email;
    }

    public void updateUserProfile(User user) {
        repository.saveUser(user);
        if (getCurrentUser() != null && getCurrentUser().getId().equals(user.getId())) {
            repository.setCurrentUser(user);
        }
    }
}