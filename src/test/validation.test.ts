import { describe, it, expect } from 'vitest';

// Form validation functions extracted for testing
export const validateEmail = (email: string): { isValid: boolean; error: string } => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!email) {
    return { isValid: false, error: "Email is required" };
  }
  if (!emailRegex.test(email)) {
    return { isValid: false, error: "Please enter a valid email address" };
  }
  return { isValid: true, error: "" };
};

export const validatePassword = (password: string): { isValid: boolean; error: string } => {
  if (!password) {
    return { isValid: false, error: "Password is required" };
  }
  if (password.length < 6) {
    return { isValid: false, error: "Password must be at least 6 characters" };
  }
  return { isValid: true, error: "" };
};

export const validateName = (name: string): { isValid: boolean; error: string } => {
  if (!name.trim()) {
    return { isValid: false, error: "Name is required" };
  }
  if (name.trim().length < 2) {
    return { isValid: false, error: "Name must be at least 2 characters" };
  }
  return { isValid: true, error: "" };
};

export const validateConfirmPassword = (confirmPassword: string, password: string): { isValid: boolean; error: string } => {
  if (!confirmPassword) {
    return { isValid: false, error: "Please confirm your password" };
  }
  if (confirmPassword !== password) {
    return { isValid: false, error: "Passwords do not match" };
  }
  return { isValid: true, error: "" };
};

describe('Form Validation', () => {
  describe('Email Validation', () => {
    it('should validate correct email formats', () => {
      const validEmails = [
        'test@example.com',
        'user.name@domain.co.uk',
        'user+tag@example.org',
        'user123@test-domain.com',
      ];

      validEmails.forEach(email => {
        const result = validateEmail(email);
        expect(result.isValid).toBe(true);
        expect(result.error).toBe('');
      });
    });

    it('should reject invalid email formats', () => {
      const invalidEmails = [
        '',
        'invalid-email',
        '@example.com',
        'user@',
        'user@domain',
        'user.domain.com',
        'user @example.com',
        'user@ex ample.com',
      ];

      invalidEmails.forEach(email => {
        const result = validateEmail(email);
        expect(result.isValid).toBe(false);
        expect(result.error).toBeTruthy();
      });
    });

    it('should return appropriate error messages', () => {
      expect(validateEmail('').error).toBe('Email is required');
      expect(validateEmail('invalid').error).toBe('Please enter a valid email address');
    });
  });

  describe('Password Validation', () => {
    it('should validate passwords with minimum length', () => {
      const validPasswords = [
        'password123',
        '123456',
        'abcdef',
        'P@ssw0rd!',
      ];

      validPasswords.forEach(password => {
        const result = validatePassword(password);
        expect(result.isValid).toBe(true);
        expect(result.error).toBe('');
      });
    });

    it('should reject passwords that are too short', () => {
      const invalidPasswords = [
        '',
        '12345',
        'abc',
        'a',
      ];

      invalidPasswords.forEach(password => {
        const result = validatePassword(password);
        expect(result.isValid).toBe(false);
        expect(result.error).toBeTruthy();
      });
    });

    it('should return appropriate error messages', () => {
      expect(validatePassword('').error).toBe('Password is required');
      expect(validatePassword('12345').error).toBe('Password must be at least 6 characters');
    });
  });

  describe('Name Validation', () => {
    it('should validate names with minimum length', () => {
      const validNames = [
        'John Doe',
        'Jane',
        'Al',
        'María José',
        'Jean-Pierre',
      ];

      validNames.forEach(name => {
        const result = validateName(name);
        expect(result.isValid).toBe(true);
        expect(result.error).toBe('');
      });
    });

    it('should reject invalid names', () => {
      const invalidNames = [
        '',
        ' ',
        'A',
        '  A  ',
      ];

      invalidNames.forEach(name => {
        const result = validateName(name);
        expect(result.isValid).toBe(false);
        expect(result.error).toBeTruthy();
      });
    });

    it('should handle whitespace correctly', () => {
      expect(validateName('  John  ').isValid).toBe(true);
      expect(validateName('   ').isValid).toBe(false);
    });

    it('should return appropriate error messages', () => {
      expect(validateName('').error).toBe('Name is required');
      expect(validateName('A').error).toBe('Name must be at least 2 characters');
    });
  });

  describe('Confirm Password Validation', () => {
    it('should validate matching passwords', () => {
      const password = 'password123';
      const result = validateConfirmPassword(password, password);
      
      expect(result.isValid).toBe(true);
      expect(result.error).toBe('');
    });

    it('should reject non-matching passwords', () => {
      const result = validateConfirmPassword('password123', 'different123');
      
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Passwords do not match');
    });

    it('should reject empty confirm password', () => {
      const result = validateConfirmPassword('', 'password123');
      
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Please confirm your password');
    });

    it('should be case sensitive', () => {
      const result = validateConfirmPassword('Password123', 'password123');
      
      expect(result.isValid).toBe(false);
      expect(result.error).toBe('Passwords do not match');
    });
  });
});