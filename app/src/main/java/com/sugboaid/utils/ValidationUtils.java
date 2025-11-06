package com.sugboaid.utils;

import android.util.Patterns;
import java.util.regex.Pattern;

/**
 * Utility class for comprehensive form validation
 * Provides advanced validation methods for authentication forms
 */
public class ValidationUtils {

    // Email validation patterns
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;
    
    // Password strength patterns
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    // Name validation pattern
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
    
    // Validation constants
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;

    /**
     * Validate email format using Android patterns
     * @param email Email to validate
     * @return ValidationResult with validation status and message
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.error("Email is required");
        }
        
        String trimmedEmail = email.trim();
        
        // Check basic format
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return ValidationResult.error("Please enter a valid email address");
        }
        
        // Additional email validation checks
        if (trimmedEmail.length() > 254) {
            return ValidationResult.error("Email address is too long");
        }
        
        if (trimmedEmail.startsWith(".") || trimmedEmail.endsWith(".")) {
            return ValidationResult.error("Email cannot start or end with a period");
        }
        
        if (trimmedEmail.contains("..")) {
            return ValidationResult.error("Email cannot contain consecutive periods");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate password with comprehensive strength requirements
     * @param password Password to validate
     * @return ValidationResult with validation status and message
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.error("Password is required");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ValidationResult.error("Password cannot exceed " + MAX_PASSWORD_LENGTH + " characters");
        }
        
        // Check for whitespace
        if (password.contains(" ")) {
            return ValidationResult.error("Password cannot contain spaces");
        }
        
        return ValidationResult.success();
    }

    /**
     * Get password strength level and feedback
     * @param password Password to analyze
     * @return PasswordStrength object with strength level and feedback
     */
    public static PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(PasswordStrength.Level.NONE, "Enter a password");
        }
        
        int score = 0;
        StringBuilder feedback = new StringBuilder();
        
        // Length check
        if (password.length() >= MIN_PASSWORD_LENGTH) {
            score += 1;
        } else {
            feedback.append("• At least ").append(MIN_PASSWORD_LENGTH).append(" characters\n");
        }
        
        // Uppercase check
        if (PASSWORD_UPPERCASE.matcher(password).matches()) {
            score += 1;
        } else {
            feedback.append("• One uppercase letter\n");
        }
        
        // Lowercase check
        if (PASSWORD_LOWERCASE.matcher(password).matches()) {
            score += 1;
        } else {
            feedback.append("• One lowercase letter\n");
        }
        
        // Digit check
        if (PASSWORD_DIGIT.matcher(password).matches()) {
            score += 1;
        } else {
            feedback.append("• One number\n");
        }
        
        // Special character check
        if (PASSWORD_SPECIAL.matcher(password).matches()) {
            score += 1;
        } else {
            feedback.append("• One special character\n");
        }
        
        // Determine strength level
        PasswordStrength.Level level;
        String message;
        
        if (score == 0) {
            level = PasswordStrength.Level.NONE;
            message = "Enter a password";
        } else if (score <= 2) {
            level = PasswordStrength.Level.WEAK;
            message = "Weak password";
        } else if (score <= 3) {
            level = PasswordStrength.Level.MEDIUM;
            message = "Medium strength";
        } else if (score <= 4) {
            level = PasswordStrength.Level.STRONG;
            message = "Strong password";
        } else {
            level = PasswordStrength.Level.VERY_STRONG;
            message = "Very strong password";
        }
        
        return new PasswordStrength(level, message, feedback.toString().trim());
    }

    /**
     * Validate password confirmation
     * @param password Original password
     * @param confirmPassword Confirmation password
     * @return ValidationResult with validation status and message
     */
    public static ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return ValidationResult.error("Please confirm your password");
        }
        
        if (!password.equals(confirmPassword)) {
            return ValidationResult.error("Passwords do not match");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate name format and requirements
     * @param name Name to validate
     * @return ValidationResult with validation status and message
     */
    public static ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.error("Name is required");
        }
        
        String trimmedName = name.trim();
        
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            return ValidationResult.error("Name must be at least " + MIN_NAME_LENGTH + " characters");
        }
        
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            return ValidationResult.error("Name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
        
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return ValidationResult.error("Name can only contain letters and spaces");
        }
        
        // Check for multiple consecutive spaces
        if (trimmedName.contains("  ")) {
            return ValidationResult.error("Name cannot contain multiple consecutive spaces");
        }
        
        return ValidationResult.success();
    }

    /**
     * Validate all login form fields
     * @param email Email address
     * @param password Password
     * @return FormValidationResult with overall validation status
     */
    public static FormValidationResult validateLoginForm(String email, String password) {
        FormValidationResult result = new FormValidationResult();
        
        ValidationResult emailResult = validateEmail(email);
        ValidationResult passwordResult = validatePassword(password);
        
        result.setEmailResult(emailResult);
        result.setPasswordResult(passwordResult);
        result.setValid(emailResult.isValid() && passwordResult.isValid());
        
        return result;
    }

    /**
     * Validate all signup form fields
     * @param name Full name
     * @param email Email address
     * @param password Password
     * @param confirmPassword Password confirmation
     * @return FormValidationResult with overall validation status
     */
    public static FormValidationResult validateSignupForm(String name, String email, String password, String confirmPassword) {
        FormValidationResult result = new FormValidationResult();
        
        ValidationResult nameResult = validateName(name);
        ValidationResult emailResult = validateEmail(email);
        ValidationResult passwordResult = validatePassword(password);
        ValidationResult confirmPasswordResult = validatePasswordMatch(password, confirmPassword);
        
        result.setNameResult(nameResult);
        result.setEmailResult(emailResult);
        result.setPasswordResult(passwordResult);
        result.setConfirmPasswordResult(confirmPasswordResult);
        result.setValid(nameResult.isValid() && emailResult.isValid() && 
                       passwordResult.isValid() && confirmPasswordResult.isValid());
        
        return result;
    }

    /**
     * ValidationResult class to hold validation status and message
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * PasswordStrength class to hold password strength information
     */
    public static class PasswordStrength {
        public enum Level {
            NONE(0),
            WEAK(1),
            MEDIUM(2),
            STRONG(3),
            VERY_STRONG(4);

            private final int value;

            Level(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }

        private final Level level;
        private final String message;
        private final String feedback;

        public PasswordStrength(Level level, String message) {
            this(level, message, null);
        }

        public PasswordStrength(Level level, String message, String feedback) {
            this.level = level;
            this.message = message;
            this.feedback = feedback;
        }

        public Level getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public String getFeedback() {
            return feedback;
        }
    }

    /**
     * FormValidationResult class to hold complete form validation results
     */
    public static class FormValidationResult {
        private boolean valid;
        private ValidationResult nameResult;
        private ValidationResult emailResult;
        private ValidationResult passwordResult;
        private ValidationResult confirmPasswordResult;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public ValidationResult getNameResult() {
            return nameResult;
        }

        public void setNameResult(ValidationResult nameResult) {
            this.nameResult = nameResult;
        }

        public ValidationResult getEmailResult() {
            return emailResult;
        }

        public void setEmailResult(ValidationResult emailResult) {
            this.emailResult = emailResult;
        }

        public ValidationResult getPasswordResult() {
            return passwordResult;
        }

        public void setPasswordResult(ValidationResult passwordResult) {
            this.passwordResult = passwordResult;
        }

        public ValidationResult getConfirmPasswordResult() {
            return confirmPasswordResult;
        }

        public void setConfirmPasswordResult(ValidationResult confirmPasswordResult) {
            this.confirmPasswordResult = confirmPasswordResult;
        }
    }
}