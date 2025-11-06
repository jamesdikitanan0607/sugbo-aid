package com.sugboaid.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.sugboaid.models.AuthState;
import com.sugboaid.models.User;
import com.sugboaid.models.UserSession;
import com.sugboaid.repositories.UserRepository;
import com.sugboaid.utils.AuthErrorHandler;
import com.sugboaid.utils.ValidationUtils;

/**
 * AuthViewModel for managing authentication state and business logic
 * Handles login, signup, logout, and session management operations
 */
public class AuthViewModel extends AndroidViewModel {
    
    private final UserRepository userRepository;
    
    // LiveData for authentication state
    private final MutableLiveData<AuthState> _authState = new MutableLiveData<>();
    public final LiveData<AuthState> authState = _authState;
    
    // LiveData for loading state
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;
    
    // LiveData for error messages
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    
    // LiveData for success messages
    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public final LiveData<String> successMessage = _successMessage;
    
    // LiveData for comprehensive error handling
    private final MutableLiveData<AuthErrorHandler.ErrorResult> _errorResult = new MutableLiveData<>();
    public final LiveData<AuthErrorHandler.ErrorResult> errorResult = _errorResult;
    
    // LiveData for retry actions
    private final MutableLiveData<Runnable> _retryAction = new MutableLiveData<>();
    public final LiveData<Runnable> retryAction = _retryAction;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance(application);
        
        // Initialize with current authentication state
        checkAuthenticationStatus();
    }

    // Authentication Methods

    /**
     * Attempt to log in user with email and password
     * @param email User email
     * @param password User password
     */
    public void login(String email, String password) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _errorResult.setValue(null);
        
        // Create retry action for this login attempt
        Runnable retryAction = () -> login(email, password);
        
        // Validate input
        if (!validateLoginInput(email, password)) {
            _isLoading.setValue(false);
            return;
        }
        
        // Simulate async operation (in real app, this might be a network call)
        new Thread(() -> {
            try {
                User user = userRepository.validateCredentials(email, password);
                
                if (user != null) {
                    // Login successful
                    boolean sessionSaved = userRepository.saveSession(user);
                    
                    if (sessionSaved) {
                        UserSession session = userRepository.getStoredSession();
                        AuthState authState = AuthState.authenticated(user, session);
                        
                        _authState.postValue(authState);
                        _successMessage.postValue("Login successful");
                    } else {
                        AuthErrorHandler.ErrorResult error = new AuthErrorHandler.ErrorResult.Builder()
                                .setType(AuthErrorHandler.ErrorType.SESSION_ERROR)
                                .setSeverity(AuthErrorHandler.ErrorSeverity.HIGH)
                                .setMessage("Failed to save session")
                                .setUserMessage("Failed to save session. Please try again.")
                                .setCanRetry(true)
                                .setRetryAction(retryAction)
                                .setActionLabel("Try Again")
                                .build();
                        _errorResult.postValue(error);
                        _errorMessage.postValue(error.getUserMessage());
                    }
                } else {
                    AuthErrorHandler.ErrorResult error = AuthErrorHandler.createAuthenticationError(retryAction);
                    _errorResult.postValue(error);
                    _errorMessage.postValue(error.getUserMessage());
                }
            } catch (Exception e) {
                AuthErrorHandler.ErrorResult error = AuthErrorHandler.fromException(e, retryAction);
                _errorResult.postValue(error);
                _errorMessage.postValue(error.getUserMessage());
            } finally {
                _isLoading.postValue(false);
            }
        }).start();
    }

    /**
     * Attempt to register new user
     * @param name User's full name
     * @param email User's email
     * @param password User's password
     * @param confirmPassword Password confirmation
     * @param role User's selected role
     */
    public void signup(String name, String email, String password, String confirmPassword, String role) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _errorResult.setValue(null);
        
        // Create retry action for this signup attempt
        Runnable retryAction = () -> signup(name, email, password, confirmPassword, role);
        
        // Validate input
        if (!validateSignupInput(name, email, password, confirmPassword)) {
            _isLoading.setValue(false);
            return;
        }
        
        // Simulate async operation
        new Thread(() -> {
            try {
                User newUser = userRepository.registerUser(name, email, password, role);
                
                if (newUser != null) {
                    // Registration successful, automatically log in
                    boolean sessionSaved = userRepository.saveSession(newUser);
                    
                    if (sessionSaved) {
                        UserSession session = userRepository.getStoredSession();
                        AuthState authState = AuthState.authenticated(newUser, session);
                        
                        _authState.postValue(authState);
                        _successMessage.postValue("Account created successfully");
                    } else {
                        AuthErrorHandler.ErrorResult error = new AuthErrorHandler.ErrorResult.Builder()
                                .setType(AuthErrorHandler.ErrorType.SESSION_ERROR)
                                .setSeverity(AuthErrorHandler.ErrorSeverity.MEDIUM)
                                .setMessage("Account created but failed to save session")
                                .setUserMessage("Account created but failed to log in. Please try logging in manually.")
                                .setCanRetry(false)
                                .build();
                        _errorResult.postValue(error);
                        _errorMessage.postValue(error.getUserMessage());
                    }
                } else {
                    // Check specific failure reason
                    String errorMessage;
                    if (userRepository.isEmailExists(email)) {
                        errorMessage = "An account with this email already exists";
                    } else {
                        errorMessage = "Registration failed. Please check your information and try again.";
                    }
                    
                    AuthErrorHandler.ErrorResult error = AuthErrorHandler.createRegistrationError(errorMessage, retryAction);
                    _errorResult.postValue(error);
                    _errorMessage.postValue(error.getUserMessage());
                }
            } catch (Exception e) {
                AuthErrorHandler.ErrorResult error = AuthErrorHandler.fromException(e, retryAction);
                _errorResult.postValue(error);
                _errorMessage.postValue(error.getUserMessage());
            } finally {
                _isLoading.postValue(false);
            }
        }).start();
    }

    /**
     * Backward compatibility method for signup without role
     * @param name User's full name
     * @param email User's email
     * @param password User's password
     * @param confirmPassword Password confirmation
     */
    public void signup(String name, String email, String password, String confirmPassword) {
        signup(name, email, password, confirmPassword, "Guest");
    }

    /**
     * Log out current user
     */
    public void logout() {
        _isLoading.setValue(true);
        _errorResult.setValue(null);
        
        // Create retry action for logout
        Runnable retryAction = this::logout;
        
        new Thread(() -> {
            try {
                boolean cleared = userRepository.clearSession();
                
                if (cleared) {
                    _authState.postValue(AuthState.unauthenticated());
                    _successMessage.postValue("Logged out successfully");
                } else {
                    AuthErrorHandler.ErrorResult error = new AuthErrorHandler.ErrorResult.Builder()
                            .setType(AuthErrorHandler.ErrorType.SESSION_ERROR)
                            .setSeverity(AuthErrorHandler.ErrorSeverity.MEDIUM)
                            .setMessage("Failed to clear session")
                            .setUserMessage("Logout failed. Please try again.")
                            .setCanRetry(true)
                            .setRetryAction(retryAction)
                            .setActionLabel("Try Again")
                            .build();
                    _errorResult.postValue(error);
                    _errorMessage.postValue(error.getUserMessage());
                }
            } catch (Exception e) {
                AuthErrorHandler.ErrorResult error = AuthErrorHandler.fromException(e, retryAction);
                _errorResult.postValue(error);
                _errorMessage.postValue(error.getUserMessage());
            } finally {
                _isLoading.postValue(false);
            }
        }).start();
    }

    /**
     * Check current authentication status
     * @return true if user is authenticated, false otherwise
     */
    public boolean checkAuthenticationStatus() {
        try {
            if (userRepository.isLoggedIn()) {
                UserSession session = userRepository.getStoredSession();
                if (session != null && session.isValid()) {
                    User user = userRepository.getUserById(session.getUserId());
                    if (user != null) {
                        AuthState authState = AuthState.authenticated(user, session);
                        _authState.setValue(authState);
                        return true;
                    }
                }
            }
            
            // No valid authentication found
            _authState.setValue(AuthState.unauthenticated());
            return false;
        } catch (Exception e) {
            _authState.setValue(AuthState.unauthenticated());
            return false;
        }
    }

    /**
     * Refresh current session
     */
    public void refreshSession() {
        if (userRepository.isLoggedIn()) {
            userRepository.refreshSession();
            checkAuthenticationStatus();
        }
    }

    // Validation Methods

    /**
     * Validate login input
     * @param email User email
     * @param password User password
     * @return true if valid, false otherwise
     */
    private boolean validateLoginInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Email is required");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Password is required");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        if (!validateEmail(email)) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Please enter a valid email address");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        return true;
    }

    /**
     * Validate signup input
     * @param name User's name
     * @param email User's email
     * @param password User's password
     * @param confirmPassword Password confirmation
     * @return true if valid, false otherwise
     */
    private boolean validateSignupInput(String name, String email, String password, String confirmPassword) {
        if (name == null || name.trim().isEmpty()) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Name is required");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        if (name.trim().length() < 2) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Name must be at least 2 characters");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        if (email == null || email.trim().isEmpty()) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Email is required");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        if (!validateEmail(email)) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Please enter a valid email address");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Password is required");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        if (!validatePassword(password)) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Password must be at least 6 characters");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        if (!validatePasswordMatch(password, confirmPassword)) {
            AuthErrorHandler.ErrorResult error = AuthErrorHandler.createValidationError("Passwords do not match");
            _errorResult.setValue(error);
            _errorMessage.setValue(error.getUserMessage());
            return false;
        }
        
        return true;
    }

    /**
     * Validate email format using comprehensive validation
     * @param email Email to validate
     * @return ValidationResult with detailed feedback
     */
    public ValidationUtils.ValidationResult validateEmailDetailed(String email) {
        return ValidationUtils.validateEmail(email);
    }

    /**
     * Validate password strength using comprehensive validation
     * @param password Password to validate
     * @return ValidationResult with detailed feedback
     */
    public ValidationUtils.ValidationResult validatePasswordDetailed(String password) {
        return ValidationUtils.validatePassword(password);
    }

    /**
     * Get password strength analysis
     * @param password Password to analyze
     * @return PasswordStrength with level and feedback
     */
    public ValidationUtils.PasswordStrength getPasswordStrength(String password) {
        return ValidationUtils.getPasswordStrength(password);
    }

    /**
     * Validate password confirmation using comprehensive validation
     * @param password Original password
     * @param confirmPassword Confirmation password
     * @return ValidationResult with detailed feedback
     */
    public ValidationUtils.ValidationResult validatePasswordMatchDetailed(String password, String confirmPassword) {
        return ValidationUtils.validatePasswordMatch(password, confirmPassword);
    }

    /**
     * Validate name format using comprehensive validation
     * @param name Name to validate
     * @return ValidationResult with detailed feedback
     */
    public ValidationUtils.ValidationResult validateNameDetailed(String name) {
        return ValidationUtils.validateName(name);
    }

    /**
     * Validate complete login form
     * @param email Email address
     * @param password Password
     * @return FormValidationResult with all field validations
     */
    public ValidationUtils.FormValidationResult validateLoginForm(String email, String password) {
        return ValidationUtils.validateLoginForm(email, password);
    }

    /**
     * Validate complete signup form
     * @param name Full name
     * @param email Email address
     * @param password Password
     * @param confirmPassword Password confirmation
     * @return FormValidationResult with all field validations
     */
    public ValidationUtils.FormValidationResult validateSignupForm(String name, String email, String password, String confirmPassword) {
        return ValidationUtils.validateSignupForm(name, email, password, confirmPassword);
    }

    // Legacy validation methods for backward compatibility
    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public boolean validateEmail(String email) {
        return ValidationUtils.validateEmail(email).isValid();
    }

    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePassword(String password) {
        return ValidationUtils.validatePassword(password).isValid();
    }

    /**
     * Validate password confirmation
     * @param password Original password
     * @param confirmPassword Confirmation password
     * @return true if passwords match, false otherwise
     */
    public boolean validatePasswordMatch(String password, String confirmPassword) {
        return ValidationUtils.validatePasswordMatch(password, confirmPassword).isValid();
    }

    /**
     * Validate name format
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public boolean validateName(String name) {
        return ValidationUtils.validateName(name).isValid();
    }

    // Utility Methods

    /**
     * Clear error message
     */
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    /**
     * Clear success message
     */
    public void clearSuccessMessage() {
        _successMessage.setValue(null);
    }

    /**
     * Clear error result
     */
    public void clearErrorResult() {
        _errorResult.setValue(null);
    }

    /**
     * Clear retry action
     */
    public void clearRetryAction() {
        _retryAction.setValue(null);
    }

    /**
     * Clear all messages
     */
    public void clearMessages() {
        clearErrorMessage();
        clearSuccessMessage();
        clearErrorResult();
        clearRetryAction();
    }

    /**
     * Get current error result
     * @return Current error result or null
     */
    public AuthErrorHandler.ErrorResult getCurrentErrorResult() {
        return _errorResult.getValue();
    }

    /**
     * Trigger retry action if available
     */
    public void retryLastAction() {
        AuthErrorHandler.ErrorResult currentError = _errorResult.getValue();
        if (currentError != null && currentError.canRetry() && currentError.getRetryAction() != null) {
            currentError.getRetryAction().run();
        }
    }

    /**
     * Get current user if authenticated
     * @return User object or null
     */
    public User getCurrentUser() {
        AuthState currentState = _authState.getValue();
        return currentState != null ? currentState.getUser() : null;
    }

    /**
     * Get current session if authenticated
     * @return UserSession object or null
     */
    public UserSession getCurrentSession() {
        AuthState currentState = _authState.getValue();
        return currentState != null ? currentState.getSession() : null;
    }

    /**
     * Check if user is currently authenticated
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        AuthState currentState = _authState.getValue();
        return currentState != null && currentState.isAuthenticated();
    }

    /**
     * Get user repository instance (for testing purposes)
     * @return UserRepository instance
     */
    public UserRepository getUserRepository() {
        return userRepository;
    }
}