package com.sugboaid.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.sugboaid.donation.R;

/**
 * Comprehensive feedback manager for authentication operations
 * Provides consistent user feedback patterns including animations, notifications, and visual cues
 */
public class AuthFeedbackManager {

    private final Context context;

    public AuthFeedbackManager(@NonNull Context context) {
        this.context = context;
    }

    /**
     * Feedback types for different authentication operations
     */
    public enum FeedbackType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO,
        VALIDATION_ERROR
    }

    /**
     * Show success feedback with animation
     * @param view View to show feedback on
     * @param message Success message
     */
    public void showSuccess(@Nullable View view, @NonNull String message) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            styleSnackbar(snackbar, FeedbackType.SUCCESS);
            snackbar.show();
            
            // Add success animation
            animateSuccess(view);
        } else {
            showToast(message, Toast.LENGTH_SHORT);
        }
    }

    /**
     * Show error feedback with retry option
     * @param view View to show feedback on
     * @param message Error message
     * @param retryAction Optional retry action
     * @param retryLabel Label for retry action
     */
    public void showError(@Nullable View view, @NonNull String message, 
                         @Nullable Runnable retryAction, @Nullable String retryLabel) {
        if (view != null) {
            int duration = retryAction != null ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG;
            Snackbar snackbar = Snackbar.make(view, message, duration);
            styleSnackbar(snackbar, FeedbackType.ERROR);
            
            if (retryAction != null) {
                String actionLabel = retryLabel != null ? retryLabel : "Retry";
                snackbar.setAction(actionLabel, v -> retryAction.run());
                snackbar.setActionTextColor(context.getResources().getColor(R.color.white, null));
            }
            
            snackbar.show();
            
            // Add error animation
            animateError(view);
        } else {
            showToast(message, Toast.LENGTH_LONG);
        }
    }

    /**
     * Show warning feedback
     * @param view View to show feedback on
     * @param message Warning message
     */
    public void showWarning(@Nullable View view, @NonNull String message) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
            styleSnackbar(snackbar, FeedbackType.WARNING);
            snackbar.show();
        } else {
            showToast(message, Toast.LENGTH_LONG);
        }
    }

    /**
     * Show info feedback
     * @param view View to show feedback on
     * @param message Info message
     */
    public void showInfo(@Nullable View view, @NonNull String message) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            styleSnackbar(snackbar, FeedbackType.INFO);
            snackbar.show();
        } else {
            showToast(message, Toast.LENGTH_SHORT);
        }
    }

    /**
     * Show validation error on specific field
     * @param field TextInputLayout field with error
     * @param message Error message
     */
    public void showValidationError(@NonNull TextInputLayout field, @NonNull String message) {
        field.setError(message);
        field.setHelperText(null);
        
        // Animate field to draw attention
        animateFieldError(field);
        
        // Update accessibility
        AccessibilityUtils.updateTextInputLayoutAccessibility(
            field, 
            field.getHint() != null ? field.getHint().toString() : "Field", 
            ValidationUtils.ValidationResult.error(message)
        );
    }

    /**
     * Clear validation error from field
     * @param field TextInputLayout field to clear
     * @param helperText Optional helper text to show
     */
    public void clearValidationError(@NonNull TextInputLayout field, @Nullable String helperText) {
        field.setError(null);
        field.setHelperText(helperText);
        
        // Update accessibility
        AccessibilityUtils.updateTextInputLayoutAccessibility(
            field, 
            field.getHint() != null ? field.getHint().toString() : "Field", 
            ValidationUtils.ValidationResult.success()
        );
    }

    /**
     * Show comprehensive form validation feedback
     * @param view Root view for Snackbar
     * @param formResult Form validation result
     */
    public void showFormValidationFeedback(@Nullable View view, @NonNull ValidationUtils.FormValidationResult formResult) {
        if (!formResult.isValid()) {
            // Count validation errors
            int errorCount = 0;
            if (formResult.getNameResult() != null && !formResult.getNameResult().isValid()) errorCount++;
            if (formResult.getEmailResult() != null && !formResult.getEmailResult().isValid()) errorCount++;
            if (formResult.getPasswordResult() != null && !formResult.getPasswordResult().isValid()) errorCount++;
            if (formResult.getConfirmPasswordResult() != null && !formResult.getConfirmPasswordResult().isValid()) errorCount++;
            
            String message = errorCount == 1 ? 
                "Please fix the validation error" : 
                String.format("Please fix %d validation errors", errorCount);
            
            if (view != null) {
                Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
                styleSnackbar(snackbar, FeedbackType.VALIDATION_ERROR);
                snackbar.show();
            }
            
            // Announce for accessibility
            if (view != null) {
                String announcement = AccessibilityUtils.createFormValidationAnnouncement(formResult);
                AccessibilityUtils.announceForAccessibility(view, announcement);
            }
        }
    }

    /**
     * Show authentication progress feedback
     * @param view View to show feedback on
     * @param operation Operation being performed (e.g., "Logging in", "Creating account")
     */
    public void showProgressFeedback(@Nullable View view, @NonNull String operation) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, operation + "...", Snackbar.LENGTH_INDEFINITE);
            styleSnackbar(snackbar, FeedbackType.INFO);
            snackbar.show();
        }
    }

    /**
     * Show session feedback (login/logout)
     * @param view View to show feedback on
     * @param isLogin true for login, false for logout
     * @param userName Optional user name for personalized message
     */
    public void showSessionFeedback(@Nullable View view, boolean isLogin, @Nullable String userName) {
        String message;
        if (isLogin) {
            message = userName != null ? 
                String.format("Welcome back, %s!", userName) : 
                "Login successful!";
        } else {
            message = userName != null ? 
                String.format("Goodbye, %s!", userName) : 
                "Logged out successfully";
        }
        
        showSuccess(view, message);
    }

    /**
     * Show network status feedback
     * @param view View to show feedback on
     * @param isOnline true if online, false if offline
     */
    public void showNetworkFeedback(@Nullable View view, boolean isOnline) {
        String message = isOnline ? 
            "Connection restored" : 
            "You are offline. Some features may be limited.";
        
        FeedbackType type = isOnline ? FeedbackType.SUCCESS : FeedbackType.WARNING;
        
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            styleSnackbar(snackbar, type);
            snackbar.show();
        } else {
            showToast(message, Toast.LENGTH_SHORT);
        }
    }

    /**
     * Show password strength feedback
     * @param field Password field
     * @param strength Password strength result
     */
    public void showPasswordStrengthFeedback(@NonNull TextInputLayout field, @NonNull ValidationUtils.PasswordStrength strength) {
        String message = strength.getMessage();
        String feedback = strength.getFeedback();
        
        if (strength.getLevel() == ValidationUtils.PasswordStrength.Level.WEAK && feedback != null) {
            field.setHelperText(feedback.replace("\n", " "));
        } else {
            field.setHelperText(message);
        }
        
        // Update accessibility for password strength
        AccessibilityUtils.updatePasswordStrengthAccessibility(field, strength);
    }

    /**
     * Animate success state
     */
    private void animateSuccess(@NonNull View view) {
        // Scale animation for success
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.05f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.05f, 1.0f);
        
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        scaleX.start();
        scaleY.start();
    }

    /**
     * Animate error state
     */
    private void animateError(@NonNull View view) {
        // Shake animation for error
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
    }

    /**
     * Animate field error
     */
    private void animateFieldError(@NonNull View field) {
        // Subtle shake for field error
        ObjectAnimator shake = ObjectAnimator.ofFloat(field, "translationX", 0, 10, -10, 10, -10, 5, -5, 0);
        shake.setDuration(400);
        shake.start();
    }

    /**
     * Style Snackbar based on feedback type
     */
    private void styleSnackbar(@NonNull Snackbar snackbar, @NonNull FeedbackType type) {
        int backgroundColor;
        int textColor = context.getResources().getColor(R.color.white, null);
        
        switch (type) {
            case SUCCESS:
                backgroundColor = context.getResources().getColor(R.color.success_green, null);
                break;
            case ERROR:
            case VALIDATION_ERROR:
                backgroundColor = context.getResources().getColor(R.color.error_red, null);
                break;
            case WARNING:
                backgroundColor = context.getResources().getColor(R.color.warning_orange, null);
                break;
            case INFO:
            default:
                backgroundColor = context.getResources().getColor(R.color.primary_blue, null);
                break;
        }
        
        snackbar.setBackgroundTint(backgroundColor);
        snackbar.setTextColor(textColor);
    }

    /**
     * Show Toast message
     */
    private void showToast(@NonNull String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    /**
     * Create pulse animation for view
     * @param view View to animate
     * @return Animator for the pulse effect
     */
    public Animator createPulseAnimation(@NonNull View view) {
        ObjectAnimator pulse = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.5f, 1.0f);
        pulse.setDuration(1000);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        return pulse;
    }

    /**
     * Create bounce animation for view
     * @param view View to animate
     * @return Animator for the bounce effect
     */
    public Animator createBounceAnimation(@NonNull View view) {
        ObjectAnimator bounce = ObjectAnimator.ofFloat(view, "translationY", 0, -20, 0);
        bounce.setDuration(600);
        bounce.setInterpolator(new AccelerateDecelerateInterpolator());
        return bounce;
    }

    /**
     * Animate view entrance
     * @param view View to animate
     * @param delay Delay before animation starts
     */
    public void animateViewEntrance(@NonNull View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Animate view exit
     * @param view View to animate
     * @param onComplete Callback when animation completes
     */
    public void animateViewExit(@NonNull View view, @Nullable Runnable onComplete) {
        view.animate()
                .alpha(0f)
                .translationY(-50f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                })
                .start();
    }

    /**
     * Show contextual help for authentication fields
     * @param view View to show help on
     * @param helpMessage Help message
     */
    public void showContextualHelp(@Nullable View view, @NonNull String helpMessage) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, helpMessage, Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(context.getResources().getColor(R.color.primary_blue_20, null));
            snackbar.setTextColor(context.getResources().getColor(R.color.text_primary, null));
            snackbar.setAction("Got it", v -> snackbar.dismiss());
            snackbar.setActionTextColor(context.getResources().getColor(R.color.primary_blue, null));
            snackbar.show();
        }
    }
}