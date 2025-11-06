package com.sugboaid.utils;

import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Utility class for accessibility support in form validation
 * Provides methods to enhance accessibility for users with disabilities
 */
public class AccessibilityUtils {

    /**
     * Check if accessibility services are enabled
     * @param context Application context
     * @return true if accessibility services are enabled
     */
    public static boolean isAccessibilityEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return am != null && am.isEnabled();
    }

    /**
     * Check if TalkBack is enabled
     * @param context Application context
     * @return true if TalkBack is enabled
     */
    public static boolean isTalkBackEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return am != null && am.isTouchExplorationEnabled();
    }

    /**
     * Announce text for screen readers
     * @param view View to announce from
     * @param text Text to announce
     */
    public static void announceForAccessibility(View view, String text) {
        if (view != null && text != null && !text.isEmpty()) {
            view.announceForAccessibility(text);
        }
    }

    /**
     * Send accessibility event
     * @param view View to send event from
     * @param eventType Type of accessibility event
     * @param text Text to include in event
     */
    public static void sendAccessibilityEvent(View view, int eventType, String text) {
        if (view != null && text != null) {
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            event.getText().add(text);
            view.sendAccessibilityEventUnchecked(event);
        }
    }

    /**
     * Update TextInputLayout accessibility properties based on validation state
     * @param layout TextInputLayout to update
     * @param fieldName Name of the field for accessibility
     * @param validationResult Validation result
     */
    public static void updateTextInputLayoutAccessibility(TextInputLayout layout, String fieldName, ValidationUtils.ValidationResult validationResult) {
        if (layout == null || fieldName == null) return;

        StringBuilder contentDescription = new StringBuilder(fieldName + " field");
        
        if (validationResult != null) {
            if (!validationResult.isValid() && validationResult.getMessage() != null) {
                contentDescription.append(", error: ").append(validationResult.getMessage());
                layout.setError(validationResult.getMessage());
                
                // Announce error for screen readers
                announceForAccessibility(layout, fieldName + " field has error: " + validationResult.getMessage());
            } else if (validationResult.isValid()) {
                contentDescription.append(", valid");
                layout.setError(null);
                
                // Set helper text for positive feedback
                if (layout.getHelperText() == null) {
                    layout.setHelperText("Valid " + fieldName.toLowerCase());
                }
            }
        }
        
        layout.setContentDescription(contentDescription.toString());
    }

    /**
     * Update TextInputLayout accessibility for password strength
     * @param layout TextInputLayout to update
     * @param passwordStrength Password strength information
     */
    public static void updatePasswordStrengthAccessibility(TextInputLayout layout, ValidationUtils.PasswordStrength passwordStrength) {
        if (layout == null || passwordStrength == null) return;

        StringBuilder contentDescription = new StringBuilder("Password field");
        
        switch (passwordStrength.getLevel()) {
            case NONE:
                contentDescription.append(", no password entered");
                break;
            case WEAK:
                contentDescription.append(", weak password");
                if (passwordStrength.getFeedback() != null) {
                    contentDescription.append(", requirements: ").append(passwordStrength.getFeedback().replace("\n", ", "));
                }
                break;
            case MEDIUM:
                contentDescription.append(", medium strength password");
                break;
            case STRONG:
                contentDescription.append(", strong password");
                break;
            case VERY_STRONG:
                contentDescription.append(", very strong password");
                break;
        }
        
        layout.setContentDescription(contentDescription.toString());
        
        // Set helper text with strength information
        if (passwordStrength.getLevel() != ValidationUtils.PasswordStrength.Level.NONE) {
            layout.setHelperText(passwordStrength.getMessage());
        }
    }

    /**
     * Create accessibility announcement for form validation results
     * @param formResult Form validation result
     * @return Accessibility announcement text
     */
    public static String createFormValidationAnnouncement(ValidationUtils.FormValidationResult formResult) {
        if (formResult == null) return "";

        if (formResult.isValid()) {
            return "Form validation successful. All fields are valid.";
        }

        StringBuilder announcement = new StringBuilder("Form validation failed. ");
        int errorCount = 0;

        if (formResult.getNameResult() != null && !formResult.getNameResult().isValid()) {
            announcement.append("Name field: ").append(formResult.getNameResult().getMessage()).append(". ");
            errorCount++;
        }

        if (formResult.getEmailResult() != null && !formResult.getEmailResult().isValid()) {
            announcement.append("Email field: ").append(formResult.getEmailResult().getMessage()).append(". ");
            errorCount++;
        }

        if (formResult.getPasswordResult() != null && !formResult.getPasswordResult().isValid()) {
            announcement.append("Password field: ").append(formResult.getPasswordResult().getMessage()).append(". ");
            errorCount++;
        }

        if (formResult.getConfirmPasswordResult() != null && !formResult.getConfirmPasswordResult().isValid()) {
            announcement.append("Confirm password field: ").append(formResult.getConfirmPasswordResult().getMessage()).append(". ");
            errorCount++;
        }

        if (errorCount > 1) {
            announcement.insert(announcement.indexOf("Form validation failed. ") + "Form validation failed. ".length(),
                    errorCount + " fields have errors. ");
        }

        return announcement.toString();
    }

    /**
     * Set up accessibility for password strength indicator
     * @param view Password strength indicator view
     * @param passwordStrength Current password strength
     */
    public static void updatePasswordStrengthIndicatorAccessibility(View view, ValidationUtils.PasswordStrength passwordStrength) {
        if (view == null) return;

        String contentDescription;
        if (passwordStrength == null || passwordStrength.getLevel() == ValidationUtils.PasswordStrength.Level.NONE) {
            contentDescription = "Password strength indicator: No password entered";
        } else {
            contentDescription = "Password strength indicator: " + passwordStrength.getMessage();
            if (passwordStrength.getFeedback() != null && !passwordStrength.getFeedback().isEmpty()) {
                contentDescription += ". Requirements: " + passwordStrength.getFeedback().replace("\n", ", ");
            }
        }

        view.setContentDescription(contentDescription);
    }

    /**
     * Create live region announcement for real-time validation
     * @param fieldName Name of the field
     * @param validationResult Validation result
     * @return Live region announcement text
     */
    public static String createLiveValidationAnnouncement(String fieldName, ValidationUtils.ValidationResult validationResult) {
        if (fieldName == null || validationResult == null) return "";

        if (validationResult.isValid()) {
            return fieldName + " field is now valid";
        } else {
            return fieldName + " field error: " + validationResult.getMessage();
        }
    }

    /**
     * Set up form field for accessibility
     * @param layout TextInputLayout to set up
     * @param fieldName Name of the field
     * @param hint Hint text for the field
     */
    public static void setupFormFieldAccessibility(TextInputLayout layout, String fieldName, String hint) {
        if (layout == null || fieldName == null) return;

        // Set content description
        layout.setContentDescription(fieldName + " field");
        
        // Set hint if provided
        if (hint != null && layout.getEditText() != null) {
            layout.getEditText().setHint(hint);
        }
        
        // Enable accessibility focus
        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
        
        // Set importance for accessibility
        layout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    /**
     * Announce validation state change for live regions
     * @param view View to announce from
     * @param fieldName Name of the field
     * @param isValid Whether the field is now valid
     * @param message Validation message
     */
    public static void announceValidationStateChange(View view, String fieldName, boolean isValid, String message) {
        if (view == null || fieldName == null) return;

        String announcement;
        if (isValid) {
            announcement = fieldName + " is now valid";
        } else {
            announcement = fieldName + " error: " + (message != null ? message : "Invalid input");
        }

        announceForAccessibility(view, announcement);
    }

    /**
     * Set up navigation accessibility for bottom navigation items
     * @param navItem Navigation item view
     * @param destination Destination name
     * @param isSelected Whether the item is currently selected
     */
    public static void setupNavigationAccessibility(View navItem, String destination, boolean isSelected) {
        if (navItem == null || destination == null) return;
        
        String contentDescription = destination + " navigation";
        if (isSelected) {
            contentDescription += ", selected";
        }
        
        navItem.setContentDescription(contentDescription);
        navItem.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    /**
     * Set up clickable accessibility for buttons and clickable views
     * @param view Clickable view
     * @param actionDescription Description of the action
     * @param hint Additional hint for the action
     */
    public static void setupClickableAccessibility(View view, String actionDescription, String hint) {
        if (view == null || actionDescription == null) return;
        
        String contentDescription = actionDescription;
        if (hint != null && !hint.isEmpty()) {
            contentDescription += ", " + hint;
        }
        
        view.setContentDescription(contentDescription);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        view.setClickable(true);
        view.setFocusable(true);
    }

    /**
     * Set up live region for dynamic content updates
     * @param view View to set as live region
     * @param liveRegionMode Live region mode (ACCESSIBILITY_LIVE_REGION_POLITE or ACCESSIBILITY_LIVE_REGION_ASSERTIVE)
     */
    public static void setupLiveRegion(View view, int liveRegionMode) {
        if (view == null) return;
        
        androidx.core.view.ViewCompat.setAccessibilityLiveRegion(view, liveRegionMode);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    /**
     * Announce navigation change for screen readers
     * @param context Context for accessibility manager
     * @param navigationView Navigation view
     * @param destination Destination name
     */
    public static void announceNavigation(Context context, View navigationView, String destination) {
        if (context == null || navigationView == null || destination == null) return;
        
        if (isAccessibilityEnabled(context)) {
            String announcement = "Navigated to " + destination;
            announceForAccessibility(navigationView, announcement);
        }
    }
}