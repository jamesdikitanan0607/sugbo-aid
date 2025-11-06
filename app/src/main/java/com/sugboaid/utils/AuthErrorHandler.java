package com.sugboaid.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.sugboaid.donation.R;

/**
 * Comprehensive error handler for authentication operations
 * Provides consistent error messaging and user feedback throughout the authentication system
 */
public class AuthErrorHandler {

    /**
     * Error types for authentication operations
     */
    public enum ErrorType {
        NETWORK_ERROR,
        VALIDATION_ERROR,
        AUTHENTICATION_ERROR,
        SESSION_ERROR,
        UNKNOWN_ERROR,
        RETRY_NEEDED
    }

    /**
     * Error severity levels
     */
    public enum ErrorSeverity {
        LOW,      // Minor validation errors, user can continue
        MEDIUM,   // Authentication failures, user needs to retry
        HIGH,     // Network errors, session issues
        CRITICAL  // System errors, app functionality affected
    }

    /**
     * Error result class containing error information and recovery options
     */
    public static class ErrorResult {
        private final ErrorType type;
        private final ErrorSeverity severity;
        private final String message;
        private final String userMessage;
        private final boolean canRetry;
        private final Runnable retryAction;
        private final String actionLabel;

        private ErrorResult(Builder builder) {
            this.type = builder.type;
            this.severity = builder.severity;
            this.message = builder.message;
            this.userMessage = builder.userMessage;
            this.canRetry = builder.canRetry;
            this.retryAction = builder.retryAction;
            this.actionLabel = builder.actionLabel;
        }

        public ErrorType getType() { return type; }
        public ErrorSeverity getSeverity() { return severity; }
        public String getMessage() { return message; }
        public String getUserMessage() { return userMessage; }
        public boolean canRetry() { return canRetry; }
        public Runnable getRetryAction() { return retryAction; }
        public String getActionLabel() { return actionLabel; }

        public static class Builder {
            private ErrorType type = ErrorType.UNKNOWN_ERROR;
            private ErrorSeverity severity = ErrorSeverity.MEDIUM;
            private String message = "";
            private String userMessage = "";
            private boolean canRetry = false;
            private Runnable retryAction = null;
            private String actionLabel = null;

            public Builder setType(ErrorType type) {
                this.type = type;
                return this;
            }

            public Builder setSeverity(ErrorSeverity severity) {
                this.severity = severity;
                return this;
            }

            public Builder setMessage(String message) {
                this.message = message;
                return this;
            }

            public Builder setUserMessage(String userMessage) {
                this.userMessage = userMessage;
                return this;
            }

            public Builder setCanRetry(boolean canRetry) {
                this.canRetry = canRetry;
                return this;
            }

            public Builder setRetryAction(Runnable retryAction) {
                this.retryAction = retryAction;
                return this;
            }

            public Builder setActionLabel(String actionLabel) {
                this.actionLabel = actionLabel;
                return this;
            }

            public ErrorResult build() {
                return new ErrorResult(this);
            }
        }
    }

    /**
     * Handle authentication errors with appropriate user feedback
     * @param context Application context
     * @param view View for showing Snackbar (optional)
     * @param error Error result containing error information
     */
    public static void handleError(@NonNull Context context, @Nullable View view, @NonNull ErrorResult error) {
        switch (error.getSeverity()) {
            case LOW:
                showToast(context, error.getUserMessage(), Toast.LENGTH_SHORT);
                break;
            case MEDIUM:
                if (view != null) {
                    showSnackbar(view, error.getUserMessage(), error, Snackbar.LENGTH_LONG);
                } else {
                    showToast(context, error.getUserMessage(), Toast.LENGTH_LONG);
                }
                break;
            case HIGH:
            case CRITICAL:
                if (view != null) {
                    showErrorSnackbar(view, error.getUserMessage(), error, Snackbar.LENGTH_INDEFINITE);
                } else {
                    showToast(context, error.getUserMessage(), Toast.LENGTH_LONG);
                }
                break;
        }

        // Log error for debugging
        logError(error);
    }

    /**
     * Show success message with appropriate styling
     * @param context Application context
     * @param view View for showing Snackbar (optional)
     * @param message Success message
     */
    public static void showSuccess(@NonNull Context context, @Nullable View view, @NonNull String message) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(context.getResources().getColor(R.color.success_green, null));
            snackbar.setTextColor(context.getResources().getColor(R.color.white, null));
            snackbar.show();
        } else {
            showToast(context, message, Toast.LENGTH_SHORT);
        }
    }

    /**
     * Create error result for network errors
     * @param retryAction Action to retry the operation
     * @return ErrorResult for network error
     */
    public static ErrorResult createNetworkError(@Nullable Runnable retryAction) {
        return new ErrorResult.Builder()
                .setType(ErrorType.NETWORK_ERROR)
                .setSeverity(ErrorSeverity.HIGH)
                .setMessage("Network connection failed")
                .setUserMessage("Network error. Please check your connection and try again.")
                .setCanRetry(true)
                .setRetryAction(retryAction)
                .setActionLabel("Retry")
                .build();
    }

    /**
     * Create error result for validation errors
     * @param validationMessage Specific validation error message
     * @return ErrorResult for validation error
     */
    public static ErrorResult createValidationError(@NonNull String validationMessage) {
        return new ErrorResult.Builder()
                .setType(ErrorType.VALIDATION_ERROR)
                .setSeverity(ErrorSeverity.LOW)
                .setMessage("Validation failed: " + validationMessage)
                .setUserMessage(validationMessage)
                .setCanRetry(false)
                .build();
    }

    /**
     * Create error result for authentication failures
     * @param retryAction Action to retry authentication
     * @return ErrorResult for authentication error
     */
    public static ErrorResult createAuthenticationError(@Nullable Runnable retryAction) {
        return new ErrorResult.Builder()
                .setType(ErrorType.AUTHENTICATION_ERROR)
                .setSeverity(ErrorSeverity.MEDIUM)
                .setMessage("Authentication failed")
                .setUserMessage("Invalid email or password. Please check your credentials and try again.")
                .setCanRetry(true)
                .setRetryAction(retryAction)
                .setActionLabel("Try Again")
                .build();
    }

    /**
     * Create error result for session errors
     * @param loginAction Action to navigate to login
     * @return ErrorResult for session error
     */
    public static ErrorResult createSessionError(@Nullable Runnable loginAction) {
        return new ErrorResult.Builder()
                .setType(ErrorType.SESSION_ERROR)
                .setSeverity(ErrorSeverity.HIGH)
                .setMessage("Session expired or invalid")
                .setUserMessage("Your session has expired. Please log in again.")
                .setCanRetry(true)
                .setRetryAction(loginAction)
                .setActionLabel("Login")
                .build();
    }

    /**
     * Create error result for registration failures
     * @param message Specific error message
     * @param retryAction Action to retry registration
     * @return ErrorResult for registration error
     */
    public static ErrorResult createRegistrationError(@NonNull String message, @Nullable Runnable retryAction) {
        return new ErrorResult.Builder()
                .setType(ErrorType.AUTHENTICATION_ERROR)
                .setSeverity(ErrorSeverity.MEDIUM)
                .setMessage("Registration failed: " + message)
                .setUserMessage(message)
                .setCanRetry(true)
                .setRetryAction(retryAction)
                .setActionLabel("Try Again")
                .build();
    }

    /**
     * Create error result for unknown errors
     * @param retryAction Action to retry the operation
     * @return ErrorResult for unknown error
     */
    public static ErrorResult createUnknownError(@Nullable Runnable retryAction) {
        return new ErrorResult.Builder()
                .setType(ErrorType.UNKNOWN_ERROR)
                .setSeverity(ErrorSeverity.MEDIUM)
                .setMessage("Unknown error occurred")
                .setUserMessage("An unexpected error occurred. Please try again.")
                .setCanRetry(true)
                .setRetryAction(retryAction)
                .setActionLabel("Retry")
                .build();
    }

    /**
     * Show standard Snackbar with retry action
     */
    private static void showSnackbar(@NonNull View view, @NonNull String message, @NonNull ErrorResult error, int duration) {
        Snackbar snackbar = Snackbar.make(view, message, duration);
        
        if (error.canRetry() && error.getRetryAction() != null) {
            String actionLabel = error.getActionLabel() != null ? error.getActionLabel() : "Retry";
            snackbar.setAction(actionLabel, v -> error.getRetryAction().run());
            snackbar.setActionTextColor(view.getContext().getResources().getColor(R.color.accent_yellow, null));
        }
        
        snackbar.show();
    }

    /**
     * Show error Snackbar with error styling
     */
    private static void showErrorSnackbar(@NonNull View view, @NonNull String message, @NonNull ErrorResult error, int duration) {
        Snackbar snackbar = Snackbar.make(view, message, duration);
        snackbar.setBackgroundTint(view.getContext().getResources().getColor(R.color.error_red, null));
        snackbar.setTextColor(view.getContext().getResources().getColor(R.color.white, null));
        
        if (error.canRetry() && error.getRetryAction() != null) {
            String actionLabel = error.getActionLabel() != null ? error.getActionLabel() : "Retry";
            snackbar.setAction(actionLabel, v -> error.getRetryAction().run());
            snackbar.setActionTextColor(view.getContext().getResources().getColor(R.color.white, null));
        }
        
        snackbar.show();
    }

    /**
     * Show Toast message
     */
    private static void showToast(@NonNull Context context, @NonNull String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    /**
     * Log error for debugging purposes
     */
    private static void logError(@NonNull ErrorResult error) {
        String logMessage = String.format(
            "AuthError [%s/%s]: %s", 
            error.getType().name(), 
            error.getSeverity().name(), 
            error.getMessage()
        );
        
        switch (error.getSeverity()) {
            case LOW:
                android.util.Log.d("AuthErrorHandler", logMessage);
                break;
            case MEDIUM:
                android.util.Log.w("AuthErrorHandler", logMessage);
                break;
            case HIGH:
            case CRITICAL:
                android.util.Log.e("AuthErrorHandler", logMessage);
                break;
        }
    }

    /**
     * Check if error is recoverable
     * @param error Error result to check
     * @return true if error can be recovered from
     */
    public static boolean isRecoverable(@NonNull ErrorResult error) {
        return error.canRetry() && error.getRetryAction() != null;
    }

    /**
     * Get user-friendly error message based on exception
     * @param throwable Exception that occurred
     * @return User-friendly error message
     */
    public static String getErrorMessage(@NonNull Throwable throwable) {
        String message = throwable.getMessage();
        
        if (message == null || message.isEmpty()) {
            return "An unexpected error occurred. Please try again.";
        }
        
        // Network-related errors
        if (message.contains("network") || message.contains("connection") || 
            message.contains("timeout") || message.contains("unreachable")) {
            return "Network error. Please check your connection and try again.";
        }
        
        // Authentication-related errors
        if (message.contains("authentication") || message.contains("credentials") || 
            message.contains("unauthorized") || message.contains("invalid")) {
            return "Authentication failed. Please check your credentials and try again.";
        }
        
        // Session-related errors
        if (message.contains("session") || message.contains("expired") || 
            message.contains("token")) {
            return "Your session has expired. Please log in again.";
        }
        
        // Default error message
        return "An error occurred. Please try again.";
    }

    /**
     * Create error result from exception
     * @param throwable Exception that occurred
     * @param retryAction Action to retry the operation
     * @return ErrorResult based on exception
     */
    public static ErrorResult fromException(@NonNull Throwable throwable, @Nullable Runnable retryAction) {
        String message = throwable.getMessage();
        
        if (message != null) {
            // Network errors
            if (message.contains("network") || message.contains("connection") || 
                message.contains("timeout") || message.contains("unreachable")) {
                return createNetworkError(retryAction);
            }
            
            // Authentication errors
            if (message.contains("authentication") || message.contains("credentials") || 
                message.contains("unauthorized") || message.contains("invalid")) {
                return createAuthenticationError(retryAction);
            }
            
            // Session errors
            if (message.contains("session") || message.contains("expired") || 
                message.contains("token")) {
                return createSessionError(retryAction);
            }
        }
        
        // Default to unknown error
        return createUnknownError(retryAction);
    }
}