package com.sugboaid.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ValidationUtils
 * Tests comprehensive form validation functionality
 */
public class ValidationUtilsTest {

    @Test
    public void testEmailValidation() {
        // Valid emails
        assertTrue(ValidationUtils.validateEmail("test@example.com").isValid());
        assertTrue(ValidationUtils.validateEmail("user.name@domain.co.uk").isValid());
        assertTrue(ValidationUtils.validateEmail("test123@test-domain.com").isValid());
        
        // Invalid emails
        assertFalse(ValidationUtils.validateEmail("").isValid());
        assertFalse(ValidationUtils.validateEmail("invalid-email").isValid());
        assertFalse(ValidationUtils.validateEmail("@domain.com").isValid());
        assertFalse(ValidationUtils.validateEmail("test@").isValid());
        assertFalse(ValidationUtils.validateEmail(".test@domain.com").isValid());
        assertFalse(ValidationUtils.validateEmail("test@domain.com.").isValid());
        assertFalse(ValidationUtils.validateEmail("test..test@domain.com").isValid());
    }

    @Test
    public void testPasswordValidation() {
        // Valid passwords
        assertTrue(ValidationUtils.validatePassword("password123").isValid());
        assertTrue(ValidationUtils.validatePassword("MySecurePass").isValid());
        assertTrue(ValidationUtils.validatePassword("123456").isValid());
        
        // Invalid passwords
        assertFalse(ValidationUtils.validatePassword("").isValid());
        assertFalse(ValidationUtils.validatePassword("12345").isValid()); // Too short
        assertFalse(ValidationUtils.validatePassword("pass word").isValid()); // Contains space
        assertFalse(ValidationUtils.validatePassword(null).isValid());
    }

    @Test
    public void testPasswordStrength() {
        // Test different strength levels
        ValidationUtils.PasswordStrength weak = ValidationUtils.getPasswordStrength("123456");
        assertEquals(ValidationUtils.PasswordStrength.Level.WEAK, weak.getLevel());
        
        ValidationUtils.PasswordStrength medium = ValidationUtils.getPasswordStrength("Password1");
        assertEquals(ValidationUtils.PasswordStrength.Level.MEDIUM, medium.getLevel());
        
        ValidationUtils.PasswordStrength strong = ValidationUtils.getPasswordStrength("Password1!");
        assertTrue(strong.getLevel().getValue() >= ValidationUtils.PasswordStrength.Level.STRONG.getValue());
        
        ValidationUtils.PasswordStrength none = ValidationUtils.getPasswordStrength("");
        assertEquals(ValidationUtils.PasswordStrength.Level.NONE, none.getLevel());
    }

    @Test
    public void testPasswordMatch() {
        // Matching passwords
        assertTrue(ValidationUtils.validatePasswordMatch("password", "password").isValid());
        
        // Non-matching passwords
        assertFalse(ValidationUtils.validatePasswordMatch("password1", "password2").isValid());
        assertFalse(ValidationUtils.validatePasswordMatch("password", "").isValid());
        assertFalse(ValidationUtils.validatePasswordMatch("password", null).isValid());
    }

    @Test
    public void testNameValidation() {
        // Valid names
        assertTrue(ValidationUtils.validateName("John Doe").isValid());
        assertTrue(ValidationUtils.validateName("Jane").isValid());
        assertTrue(ValidationUtils.validateName("Mary Jane Watson").isValid());
        
        // Invalid names
        assertFalse(ValidationUtils.validateName("").isValid());
        assertFalse(ValidationUtils.validateName("J").isValid()); // Too short
        assertFalse(ValidationUtils.validateName("John123").isValid()); // Contains numbers
        assertFalse(ValidationUtils.validateName("John  Doe").isValid()); // Multiple spaces
        assertFalse(ValidationUtils.validateName(null).isValid());
    }

    @Test
    public void testLoginFormValidation() {
        // Valid form
        ValidationUtils.FormValidationResult validForm = 
            ValidationUtils.validateLoginForm("test@example.com", "password123");
        assertTrue(validForm.isValid());
        assertTrue(validForm.getEmailResult().isValid());
        assertTrue(validForm.getPasswordResult().isValid());
        
        // Invalid form
        ValidationUtils.FormValidationResult invalidForm = 
            ValidationUtils.validateLoginForm("invalid-email", "123");
        assertFalse(invalidForm.isValid());
        assertFalse(invalidForm.getEmailResult().isValid());
        assertFalse(invalidForm.getPasswordResult().isValid());
    }

    @Test
    public void testSignupFormValidation() {
        // Valid form
        ValidationUtils.FormValidationResult validForm = 
            ValidationUtils.validateSignupForm("John Doe", "test@example.com", "password123", "password123");
        assertTrue(validForm.isValid());
        assertTrue(validForm.getNameResult().isValid());
        assertTrue(validForm.getEmailResult().isValid());
        assertTrue(validForm.getPasswordResult().isValid());
        assertTrue(validForm.getConfirmPasswordResult().isValid());
        
        // Invalid form
        ValidationUtils.FormValidationResult invalidForm = 
            ValidationUtils.validateSignupForm("J", "invalid-email", "123", "456");
        assertFalse(invalidForm.isValid());
        assertFalse(invalidForm.getNameResult().isValid());
        assertFalse(invalidForm.getEmailResult().isValid());
        assertFalse(invalidForm.getPasswordResult().isValid());
        assertFalse(invalidForm.getConfirmPasswordResult().isValid());
    }
}