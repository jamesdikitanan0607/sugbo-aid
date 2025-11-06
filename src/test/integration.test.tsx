import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { LoginPage } from '../components/LoginPage';
import { SignupPage } from '../components/SignupPage';
import { useAuth } from '../hooks/useAuth';

// Mock motion components
vi.mock('motion/react', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...props}>{children}</div>,
    p: ({ children, ...props }: any) => <p {...props}>{children}</p>,
  },
}));

// Integration test component that simulates the authentication flow
function AuthenticationFlow() {
  const [currentScreen, setCurrentScreen] = React.useState<'login' | 'signup' | 'dashboard'>('login');
  const { isAuthenticated } = useAuth();

  React.useEffect(() => {
    if (isAuthenticated) {
      setCurrentScreen('dashboard');
    }
  }, [isAuthenticated]);

  const handleLoginSuccess = () => {
    setCurrentScreen('dashboard');
  };

  const handleSignupSuccess = () => {
    setCurrentScreen('dashboard');
  };

  const handleNavigateToSignup = () => {
    setCurrentScreen('signup');
  };

  const handleNavigateToLogin = () => {
    setCurrentScreen('login');
  };

  if (currentScreen === 'dashboard') {
    return <div>Dashboard - Welcome!</div>;
  }

  if (currentScreen === 'signup') {
    return (
      <SignupPage
        onNavigateToLogin={handleNavigateToLogin}
        onSignupSuccess={handleSignupSuccess}
      />
    );
  }

  return (
    <LoginPage
      onNavigateToSignup={handleNavigateToSignup}
      onLoginSuccess={handleLoginSuccess}
    />
  );
}

describe('Authentication Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('Complete Authentication Flow', () => {
    it('should complete full signup to login flow', async () => {
      const user = userEvent.setup();
      render(<AuthenticationFlow />);

      // Start on login page
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();

      // Navigate to signup
      const createAccountLink = screen.getByText(/create account/i);
      await user.click(createAccountLink);

      // Should now be on signup page
      expect(screen.getByText('Join SugboAid')).toBeInTheDocument();

      // Fill out signup form
      await user.type(screen.getByLabelText(/full name/i), 'John Doe');
      await user.type(screen.getByLabelText(/email address/i), 'john@example.com');
      await user.type(screen.getByLabelText(/^password$/i), 'password123');
      await user.type(screen.getByLabelText(/confirm password/i), 'password123');

      // Submit signup form
      const signupButton = screen.getByRole('button', { name: /create account/i });
      await user.click(signupButton);

      // Should navigate to dashboard after successful signup
      await waitFor(() => {
        expect(screen.getByText('Dashboard - Welcome!')).toBeInTheDocument();
      });

      // Verify user data is stored in localStorage
      const storedUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      expect(storedUsers).toHaveLength(1);
      expect(storedUsers[0].name).toBe('John Doe');
      expect(storedUsers[0].email).toBe('john@example.com');

      const storedSession = JSON.parse(localStorage.getItem('sugboaid_session') || '{}');
      expect(storedSession.user.name).toBe('John Doe');
      expect(storedSession.user.email).toBe('john@example.com');
    });

    it('should handle login after signup', async () => {
      const user = userEvent.setup();
      
      // Pre-populate localStorage with a user
      const existingUser = {
        id: 'test-id',
        name: 'Jane Doe',
        email: 'jane@example.com',
        password: '1450575459', // Hash of 'password123'
        createdAt: '2024-01-01T00:00:00.000Z',
      };
      localStorage.setItem('sugboaid_users', JSON.stringify([existingUser]));

      render(<AuthenticationFlow />);

      // Should start on login page
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();

      // Fill out login form
      await user.type(screen.getByLabelText(/email address/i), 'jane@example.com');
      await user.type(screen.getByLabelText(/password/i), 'password123');

      // Submit login form
      const loginButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(loginButton);

      // Should navigate to dashboard after successful login
      await waitFor(() => {
        expect(screen.getByText('Dashboard - Welcome!')).toBeInTheDocument();
      });

      // Verify session is created
      const storedSession = JSON.parse(localStorage.getItem('sugboaid_session') || '{}');
      expect(storedSession.user.name).toBe('Jane Doe');
      expect(storedSession.user.email).toBe('jane@example.com');
    });

    it('should handle navigation between login and signup pages', async () => {
      const user = userEvent.setup();
      render(<AuthenticationFlow />);

      // Start on login page
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();

      // Navigate to signup
      await user.click(screen.getByText(/create account/i));
      expect(screen.getByText('Join SugboAid')).toBeInTheDocument();

      // Navigate back to login
      await user.click(screen.getByText(/sign in/i));
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();
    });

    it('should prevent duplicate email registration', async () => {
      const user = userEvent.setup();
      
      // Pre-populate localStorage with a user
      const existingUser = {
        id: 'test-id',
        name: 'Existing User',
        email: 'existing@example.com',
        password: 'hashedpassword',
        createdAt: '2024-01-01T00:00:00.000Z',
      };
      localStorage.setItem('sugboaid_users', JSON.stringify([existingUser]));

      render(<AuthenticationFlow />);

      // Navigate to signup
      await user.click(screen.getByText(/create account/i));

      // Try to signup with existing email
      await user.type(screen.getByLabelText(/full name/i), 'New User');
      await user.type(screen.getByLabelText(/email address/i), 'existing@example.com');
      await user.type(screen.getByLabelText(/^password$/i), 'password123');
      await user.type(screen.getByLabelText(/confirm password/i), 'password123');

      const signupButton = screen.getByRole('button', { name: /create account/i });
      await user.click(signupButton);

      // Should remain on signup page (not navigate to dashboard)
      await waitFor(() => {
        expect(screen.getByText('Join SugboAid')).toBeInTheDocument();
      });

      // Should not create a session
      expect(localStorage.getItem('sugboaid_session')).toBeNull();
    });
  });

  describe('Session Persistence', () => {
    it('should restore session on page reload', () => {
      // Setup a valid session in localStorage
      const mockSession = {
        user: {
          id: 'test-id',
          name: 'Test User',
          email: 'test@example.com',
          createdAt: '2024-01-01T00:00:00.000Z',
        },
        token: 'test-token',
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      };
      localStorage.setItem('sugboaid_session', JSON.stringify(mockSession));

      render(<AuthenticationFlow />);

      // Should automatically navigate to dashboard due to existing session
      expect(screen.getByText('Dashboard - Welcome!')).toBeInTheDocument();
    });

    it('should handle expired session gracefully', () => {
      // Setup an expired session in localStorage
      const expiredSession = {
        user: {
          id: 'test-id',
          name: 'Test User',
          email: 'test@example.com',
          createdAt: '2024-01-01T00:00:00.000Z',
        },
        token: 'test-token',
        expiresAt: new Date(Date.now() - 1000).toISOString(), // Expired
      };
      localStorage.setItem('sugboaid_session', JSON.stringify(expiredSession));

      render(<AuthenticationFlow />);

      // Should show login page and clear expired session
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();
      expect(localStorage.getItem('sugboaid_session')).toBeNull();
    });
  });

  describe('Error Handling', () => {
    it('should handle localStorage errors gracefully', () => {
      // Mock localStorage to throw errors
      const originalGetItem = localStorage.getItem;
      localStorage.getItem = vi.fn().mockImplementation(() => {
        throw new Error('Storage error');
      });

      render(<AuthenticationFlow />);

      // Should still render login page despite storage error
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();

      // Restore original method
      localStorage.getItem = originalGetItem;
    });

    it('should handle malformed session data', () => {
      // Setup malformed session data
      localStorage.setItem('sugboaid_session', 'invalid-json');

      render(<AuthenticationFlow />);

      // Should show login page and handle the error gracefully
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();
    });
  });
});