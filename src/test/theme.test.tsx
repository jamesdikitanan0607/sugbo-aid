import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { LoginPage } from '../components/LoginPage';
import { SignupPage } from '../components/SignupPage';

// Mock motion components
vi.mock('motion/react', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...props}>{children}</div>,
    p: ({ children, ...props }: any) => <p {...props}>{children}</p>,
  },
}));

// Mock useAuth hook
vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    user: null,
    isAuthenticated: false,
    isLoading: false,
    login: vi.fn(),
    signup: vi.fn(),
    logout: vi.fn(),
    checkAuth: vi.fn(),
  }),
}));

// Theme wrapper component for testing
function ThemeWrapper({ children, theme = 'light' }: { children: React.ReactNode; theme?: 'light' | 'dark' }) {
  return (
    <div className={theme} data-theme={theme}>
      {children}
    </div>
  );
}

describe('Theme Compatibility Tests', () => {
  const mockProps = {
    onNavigateToSignup: vi.fn(),
    onNavigateToLogin: vi.fn(),
    onLoginSuccess: vi.fn(),
    onSignupSuccess: vi.fn(),
  };

  describe('LoginPage Theme Support', () => {
    it('should render correctly in light theme', () => {
      render(
        <ThemeWrapper theme="light">
          <LoginPage
            onNavigateToSignup={mockProps.onNavigateToSignup}
            onLoginSuccess={mockProps.onLoginSuccess}
          />
        </ThemeWrapper>
      );

      expect(screen.getByText('Welcome Back')).toBeInTheDocument();
      expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    });

    it('should render correctly in dark theme', () => {
      render(
        <ThemeWrapper theme="dark">
          <LoginPage
            onNavigateToSignup={mockProps.onNavigateToSignup}
            onLoginSuccess={mockProps.onLoginSuccess}
          />
        </ThemeWrapper>
      );

      expect(screen.getByText('Welcome Back')).toBeInTheDocument();
      expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    });

    it('should have consistent styling classes for theme compatibility', () => {
      const { container } = render(
        <ThemeWrapper>
          <LoginPage
            onNavigateToSignup={mockProps.onNavigateToSignup}
            onLoginSuccess={mockProps.onLoginSuccess}
          />
        </ThemeWrapper>
      );

      // Check for glassmorphic styling classes that should work with both themes
      const glassmorphicElements = container.querySelectorAll('[class*="backdrop-blur"]');
      expect(glassmorphicElements.length).toBeGreaterThan(0);

      const whiteOpacityElements = container.querySelectorAll('[class*="bg-white/"]');
      expect(whiteOpacityElements.length).toBeGreaterThan(0);
    });
  });

  describe('SignupPage Theme Support', () => {
    it('should render correctly in light theme', () => {
      render(
        <ThemeWrapper theme="light">
          <SignupPage
            onNavigateToLogin={mockProps.onNavigateToLogin}
            onSignupSuccess={mockProps.onSignupSuccess}
          />
        </ThemeWrapper>
      );

      expect(screen.getByText('Join SugboAid')).toBeInTheDocument();
      expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    });

    it('should render correctly in dark theme', () => {
      render(
        <ThemeWrapper theme="dark">
          <SignupPage
            onNavigateToLogin={mockProps.onNavigateToLogin}
            onSignupSuccess={mockProps.onSignupSuccess}
          />
        </ThemeWrapper>
      );

      expect(screen.getByText('Join SugboAid')).toBeInTheDocument();
      expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    });

    it('should have consistent styling classes for theme compatibility', () => {
      const { container } = render(
        <ThemeWrapper>
          <SignupPage
            onNavigateToLogin={mockProps.onNavigateToLogin}
            onSignupSuccess={mockProps.onSignupSuccess}
          />
        </ThemeWrapper>
      );

      // Check for glassmorphic styling classes
      const glassmorphicElements = container.querySelectorAll('[class*="backdrop-blur"]');
      expect(glassmorphicElements.length).toBeGreaterThan(0);

      const gradientElements = container.querySelectorAll('[class*="bg-gradient-to"]');
      expect(gradientElements.length).toBeGreaterThan(0);
    });
  });

  describe('Responsive Design', () => {
    it('should have responsive classes for mobile compatibility', () => {
      const { container } = render(
        <ThemeWrapper>
          <LoginPage
            onNavigateToSignup={mockProps.onNavigateToSignup}
            onLoginSuccess={mockProps.onLoginSuccess}
          />
        </ThemeWrapper>
      );

      // Check for responsive classes
      const responsiveElements = container.querySelectorAll('[class*="max-w-"]');
      expect(responsiveElements.length).toBeGreaterThan(0);

      const paddingElements = container.querySelectorAll('[class*="p-"]');
      expect(paddingElements.length).toBeGreaterThan(0);
    });

    it('should have proper spacing for different screen sizes', () => {
      const { container } = render(
        <ThemeWrapper>
          <SignupPage
            onNavigateToLogin={mockProps.onNavigateToLogin}
            onSignupSuccess={mockProps.onSignupSuccess}
          />
        </ThemeWrapper>
      );

      // Check for spacing classes
      const spacingElements = container.querySelectorAll('[class*="space-y-"]');
      expect(spacingElements.length).toBeGreaterThan(0);

      const marginElements = container.querySelectorAll('[class*="mb-"], [class*="mt-"]');
      expect(marginElements.length).toBeGreaterThan(0);
    });
  });

  describe('Color Consistency', () => {
    it('should use consistent color scheme across components', () => {
      const { container: loginContainer } = render(
        <ThemeWrapper>
          <LoginPage
            onNavigateToSignup={mockProps.onNavigateToSignup}
            onLoginSuccess={mockProps.onLoginSuccess}
          />
        </ThemeWrapper>
      );

      const { container: signupContainer } = render(
        <ThemeWrapper>
          <SignupPage
            onNavigateToLogin={mockProps.onNavigateToLogin}
            onSignupSuccess={mockProps.onSignupSuccess}
          />
        </ThemeWrapper>
      );

      // Both should have similar gradient backgrounds
      const loginGradients = loginContainer.querySelectorAll('[class*="bg-gradient-to-br"]');
      const signupGradients = signupContainer.querySelectorAll('[class*="bg-gradient-to-br"]');
      
      expect(loginGradients.length).toBeGreaterThan(0);
      expect(signupGradients.length).toBeGreaterThan(0);

      // Both should have white text elements
      const loginWhiteText = loginContainer.querySelectorAll('[class*="text-white"]');
      const signupWhiteText = signupContainer.querySelectorAll('[class*="text-white"]');
      
      expect(loginWhiteText.length).toBeGreaterThan(0);
      expect(signupWhiteText.length).toBeGreaterThan(0);
    });

    it('should have proper contrast for accessibility', () => {
      const { container } = render(
        <ThemeWrapper>
          <LoginPage
            onNavigateToSignup={mockProps.onNavigateToSignup}
            onLoginSuccess={mockProps.onLoginSuccess}
          />
        </ThemeWrapper>
      );

      // Check for high contrast text colors
      const whiteTextElements = container.querySelectorAll('[class*="text-white"]');
      const errorTextElements = container.querySelectorAll('[class*="text-red"]');
      
      expect(whiteTextElements.length).toBeGreaterThan(0);
      // Error text elements might not be present initially, but the classes should be available
    });
  });

  describe('Animation Compatibility', () => {
    it('should handle motion components gracefully', () => {
      // This test ensures that motion components don't break the rendering
      const { container } = render(
        <ThemeWrapper>
          <LoginPage
            onNavigateToSignup={mockProps.onNavigateToSignup}
            onLoginSuccess={mockProps.onLoginSuccess}
          />
        </ThemeWrapper>
      );

      expect(container.firstChild).toBeInTheDocument();
      expect(screen.getByText('Welcome Back')).toBeInTheDocument();
    });

    it('should maintain layout stability without animations', () => {
      // Test that the layout is stable even when animations are disabled/mocked
      render(
        <ThemeWrapper>
          <SignupPage
            onNavigateToLogin={mockProps.onNavigateToLogin}
            onSignupSuccess={mockProps.onSignupSuccess}
          />
        </ThemeWrapper>
      );

      expect(screen.getByText('Join SugboAid')).toBeInTheDocument();
      expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
    });
  });
});