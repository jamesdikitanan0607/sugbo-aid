import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { SignupPage } from './SignupPage';
import { useAuth } from '../hooks/useAuth';

// Mock the useAuth hook
vi.mock('../hooks/useAuth');
const mockUseAuth = vi.mocked(useAuth);

// Mock motion components to avoid animation issues in tests
vi.mock('motion/react', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...props}>{children}</div>,
    p: ({ children, ...props }: any) => <p {...props}>{children}</p>,
  },
}));

describe('SignupPage Component', () => {
  const mockOnNavigateToLogin = vi.fn();
  const mockOnSignupSuccess = vi.fn();
  const mockSignup = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      login: vi.fn(),
      signup: mockSignup,
      logout: vi.fn(),
      checkAuth: vi.fn(),
    });
  });

  const renderSignupPage = () => {
    return render(
      <SignupPage
        onNavigateToLogin={mockOnNavigateToLogin}
        onSignupSuccess={mockOnSignupSuccess}
      />
    );
  };

  describe('Rendering', () => {
    it('should render signup form with all required fields', () => {
      renderSignupPage();

      expect(screen.getByText('Join SugboAid')).toBeInTheDocument();
      expect(screen.getByText('Create your account to start helping Cebu')).toBeInTheDocument();
      expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
      expect(screen.getByText(/sign in/i)).toBeInTheDocument();
    });

    it('should render password visibility toggles for both password fields', () => {
      renderSignupPage();

      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);
      
      expect(passwordInput).toHaveAttribute('type', 'password');
      expect(confirmPasswordInput).toHaveAttribute('type', 'password');

      // Should have two eye icon buttons (one for each password field)
      const toggleButtons = screen.getAllByRole('button', { name: '' });
      expect(toggleButtons).toHaveLength(2);
    });

    it('should show loading state when isLoading is true', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        isAuthenticated: false,
        isLoading: true,
        login: vi.fn(),
        signup: mockSignup,
        logout: vi.fn(),
        checkAuth: vi.fn(),
      });

      renderSignupPage();

      const submitButton = screen.getByRole('button', { name: /create account/i });
      expect(submitButton).toBeDisabled();
    });
  });

  describe('Form Validation', () => {
    it('should show name validation error for short name', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      await user.type(nameInput, 'A');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText('Name must be at least 2 characters')).toBeInTheDocument();
      });
    });

    it('should show email validation error for invalid email', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const emailInput = screen.getByLabelText(/email address/i);
      await user.type(emailInput, 'invalid-email');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText('Please enter a valid email address')).toBeInTheDocument();
      });
    });

    it('should show password validation error for short password', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const passwordInput = screen.getByLabelText(/^password$/i);
      await user.type(passwordInput, '123');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText('Password must be at least 6 characters')).toBeInTheDocument();
      });
    });

    it('should show confirm password validation error for non-matching passwords', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);

      await user.type(passwordInput, 'password123');
      await user.type(confirmPasswordInput, 'different123');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText('Passwords do not match')).toBeInTheDocument();
      });
    });

    it('should show all required field errors when form is submitted empty', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const submitButton = screen.getByRole('button', { name: /create account/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Name is required')).toBeInTheDocument();
        expect(screen.getByText('Email is required')).toBeInTheDocument();
        expect(screen.getByText('Password is required')).toBeInTheDocument();
        expect(screen.getByText('Please confirm your password')).toBeInTheDocument();
      });
    });

    it('should update confirm password validation when password changes', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);

      // Set initial passwords that don't match
      await user.type(passwordInput, 'password123');
      await user.type(confirmPasswordInput, 'different123');
      await user.tab();

      await waitFor(() => {
        expect(screen.getByText('Passwords do not match')).toBeInTheDocument();
      });

      // Change password to match confirm password
      await user.clear(passwordInput);
      await user.type(passwordInput, 'different123');

      await waitFor(() => {
        expect(screen.queryByText('Passwords do not match')).not.toBeInTheDocument();
      });
    });
  });

  describe('Form Submission', () => {
    it('should call signup function with correct data', async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue(true);
      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      const emailInput = screen.getByLabelText(/email address/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);
      const submitButton = screen.getByRole('button', { name: /create account/i });

      await user.type(nameInput, 'John Doe');
      await user.type(emailInput, 'john@example.com');
      await user.type(passwordInput, 'password123');
      await user.type(confirmPasswordInput, 'password123');
      await user.click(submitButton);

      expect(mockSignup).toHaveBeenCalledWith('John Doe', 'john@example.com', 'password123');
    });

    it('should trim whitespace from name', async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue(true);
      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      const emailInput = screen.getByLabelText(/email address/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);
      const submitButton = screen.getByRole('button', { name: /create account/i });

      await user.type(nameInput, '  John Doe  ');
      await user.type(emailInput, 'john@example.com');
      await user.type(passwordInput, 'password123');
      await user.type(confirmPasswordInput, 'password123');
      await user.click(submitButton);

      expect(mockSignup).toHaveBeenCalledWith('John Doe', 'john@example.com', 'password123');
    });

    it('should call onSignupSuccess when signup is successful', async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue(true);
      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      const emailInput = screen.getByLabelText(/email address/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);
      const submitButton = screen.getByRole('button', { name: /create account/i });

      await user.type(nameInput, 'John Doe');
      await user.type(emailInput, 'john@example.com');
      await user.type(passwordInput, 'password123');
      await user.type(confirmPasswordInput, 'password123');
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnSignupSuccess).toHaveBeenCalled();
      });
    });

    it('should not call onSignupSuccess when signup fails', async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue(false);
      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      const emailInput = screen.getByLabelText(/email address/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);
      const submitButton = screen.getByRole('button', { name: /create account/i });

      await user.type(nameInput, 'John Doe');
      await user.type(emailInput, 'existing@example.com');
      await user.type(passwordInput, 'password123');
      await user.type(confirmPasswordInput, 'password123');
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockSignup).toHaveBeenCalled();
      });

      expect(mockOnSignupSuccess).not.toHaveBeenCalled();
    });

    it('should not submit form with validation errors', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      const submitButton = screen.getByRole('button', { name: /create account/i });

      await user.type(nameInput, 'A'); // Too short
      await user.click(submitButton);

      expect(mockSignup).not.toHaveBeenCalled();
    });
  });

  describe('User Interactions', () => {
    it('should toggle password visibility for password field', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const passwordInput = screen.getByLabelText(/^password$/i);
      const toggleButtons = screen.getAllByRole('button', { name: '' });
      const passwordToggle = toggleButtons[0]; // First toggle is for password

      expect(passwordInput).toHaveAttribute('type', 'password');

      await user.click(passwordToggle);
      expect(passwordInput).toHaveAttribute('type', 'text');

      await user.click(passwordToggle);
      expect(passwordInput).toHaveAttribute('type', 'password');
    });

    it('should toggle password visibility for confirm password field', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);
      const toggleButtons = screen.getAllByRole('button', { name: '' });
      const confirmPasswordToggle = toggleButtons[1]; // Second toggle is for confirm password

      expect(confirmPasswordInput).toHaveAttribute('type', 'password');

      await user.click(confirmPasswordToggle);
      expect(confirmPasswordInput).toHaveAttribute('type', 'text');

      await user.click(confirmPasswordToggle);
      expect(confirmPasswordInput).toHaveAttribute('type', 'password');
    });

    it('should navigate to login page when sign in link is clicked', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const signInLink = screen.getByText(/sign in/i);
      await user.click(signInLink);

      expect(mockOnNavigateToLogin).toHaveBeenCalled();
    });

    it('should disable form elements when loading', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        isAuthenticated: false,
        isLoading: true,
        login: vi.fn(),
        signup: mockSignup,
        logout: vi.fn(),
        checkAuth: vi.fn(),
      });

      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      const emailInput = screen.getByLabelText(/email address/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);
      const submitButton = screen.getByRole('button', { name: /create account/i });
      const signInLink = screen.getByText(/sign in/i);

      expect(nameInput).toBeDisabled();
      expect(emailInput).toBeDisabled();
      expect(passwordInput).toBeDisabled();
      expect(confirmPasswordInput).toBeDisabled();
      expect(submitButton).toBeDisabled();
      expect(signInLink).toBeDisabled();
    });
  });

  describe('Accessibility', () => {
    it('should have proper form labels and ARIA attributes', () => {
      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      const emailInput = screen.getByLabelText(/email address/i);
      const passwordInput = screen.getByLabelText(/^password$/i);
      const confirmPasswordInput = screen.getByLabelText(/confirm password/i);

      expect(nameInput).toHaveAttribute('id', 'name');
      expect(emailInput).toHaveAttribute('id', 'email');
      expect(passwordInput).toHaveAttribute('id', 'password');
      expect(confirmPasswordInput).toHaveAttribute('id', 'confirmPassword');
    });

    it('should set aria-invalid when validation errors occur', async () => {
      const user = userEvent.setup();
      renderSignupPage();

      const nameInput = screen.getByLabelText(/full name/i);
      await user.type(nameInput, 'A');
      await user.tab();

      await waitFor(() => {
        expect(nameInput).toHaveAttribute('aria-invalid', 'true');
      });
    });
  });
});