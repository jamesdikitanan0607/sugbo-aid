import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useAuth, User, UserSession } from './useAuth';
import { toast } from 'sonner';

// Mock sonner toast
vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Mock data
const mockUser: User = {
  id: 'test-id',
  name: 'Test User',
  email: 'test@example.com',
  password: 'hashedpassword',
  createdAt: '2024-01-01T00:00:00.000Z',
};

const mockSession: UserSession = {
  user: {
    id: mockUser.id,
    name: mockUser.name,
    email: mockUser.email,
    createdAt: mockUser.createdAt,
  },
  token: 'test-token',
  expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
};

describe('useAuth Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('Initial State', () => {
    it('should initialize with unauthenticated state when no session exists', async () => {
      const { result } = renderHook(() => useAuth());
      
      // Wait for the useEffect to complete
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });
      
      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isLoading).toBe(false);
    });

    it('should initialize with authenticated state when valid session exists', async () => {
      localStorage.setItem('sugboaid_session', JSON.stringify(mockSession));
      
      const { result } = renderHook(() => useAuth());
      
      // Wait for the useEffect to complete
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });
      
      expect(result.current.user).toEqual(mockSession.user);
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.isLoading).toBe(false);
    });

    it('should clear invalid session on initialization', async () => {
      const expiredSession = {
        ...mockSession,
        expiresAt: new Date(Date.now() - 1000).toISOString(), // Expired
      };
      localStorage.setItem('sugboaid_session', JSON.stringify(expiredSession));
      
      const { result } = renderHook(() => useAuth());
      
      // Wait for the useEffect to complete
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });
      
      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(localStorage.getItem('sugboaid_session')).toBeNull();
    });
  });

  describe('Signup Function', () => {
    it('should successfully create a new user account', async () => {
      const { result } = renderHook(() => useAuth());
      
      let signupResult: boolean;
      await act(async () => {
        signupResult = await result.current.signup('Test User', 'test@example.com', 'password123');
      });
      
      expect(signupResult!).toBe(true);
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user?.name).toBe('Test User');
      expect(result.current.user?.email).toBe('test@example.com');
      expect(toast.success).toHaveBeenCalledWith('Account created successfully!');
    });

    it('should prevent duplicate email registration', async () => {
      // Setup existing user
      localStorage.setItem('sugboaid_users', JSON.stringify([mockUser]));
      
      const { result } = renderHook(() => useAuth());
      
      let signupResult: boolean;
      await act(async () => {
        signupResult = await result.current.signup('Another User', 'test@example.com', 'password123');
      });
      
      expect(signupResult!).toBe(false);
      expect(result.current.isAuthenticated).toBe(false);
      expect(toast.error).toHaveBeenCalledWith('An account with this email already exists');
    });

    it('should store user data in localStorage', async () => {
      const { result } = renderHook(() => useAuth());
      
      await act(async () => {
        await result.current.signup('Test User', 'test@example.com', 'password123');
      });
      
      const storedUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      expect(storedUsers).toHaveLength(1);
      expect(storedUsers[0].name).toBe('Test User');
      expect(storedUsers[0].email).toBe('test@example.com');
      
      const storedSession = JSON.parse(localStorage.getItem('sugboaid_session') || '{}');
      expect(storedSession.user.name).toBe('Test User');
      expect(storedSession.token).toBeDefined();
    });
  });

  describe('Login Function', () => {
    beforeEach(() => {
      // Use the correct password hash for 'password'
      const userWithCorrectHash = {
        ...mockUser,
        password: '1450575459', // This is the hash of 'password' from the useAuth implementation
      };
      localStorage.setItem('sugboaid_users', JSON.stringify([userWithCorrectHash]));
    });

    it('should successfully login with valid credentials', async () => {
      const { result } = renderHook(() => useAuth());
      
      let loginResult: boolean;
      await act(async () => {
        loginResult = await result.current.login('test@example.com', 'password');
      });
      
      expect(loginResult!).toBe(true);
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user?.email).toBe('test@example.com');
      expect(toast.success).toHaveBeenCalledWith('Login successful!');
    });

    it('should fail login with invalid email', async () => {
      const { result } = renderHook(() => useAuth());
      
      let loginResult: boolean;
      await act(async () => {
        loginResult = await result.current.login('nonexistent@example.com', 'password');
      });
      
      expect(loginResult!).toBe(false);
      expect(result.current.isAuthenticated).toBe(false);
      expect(toast.error).toHaveBeenCalledWith('No account found with this email');
    });

    it('should fail login with invalid password', async () => {
      const { result } = renderHook(() => useAuth());
      
      let loginResult: boolean;
      await act(async () => {
        loginResult = await result.current.login('test@example.com', 'wrongpassword');
      });
      
      expect(loginResult!).toBe(false);
      expect(result.current.isAuthenticated).toBe(false);
      expect(toast.error).toHaveBeenCalledWith('Invalid email or password');
    });

    it('should update lastLogin timestamp on successful login', async () => {
      const { result } = renderHook(() => useAuth());
      
      await act(async () => {
        await result.current.login('test@example.com', 'password');
      });
      
      const storedUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      expect(storedUsers[0].lastLogin).toBeDefined();
      expect(new Date(storedUsers[0].lastLogin).getTime()).toBeGreaterThan(Date.now() - 1000);
    });
  });

  describe('Logout Function', () => {
    it('should clear session and update state on logout', () => {
      localStorage.setItem('sugboaid_session', JSON.stringify(mockSession));
      
      const { result } = renderHook(() => useAuth());
      
      act(() => {
        result.current.logout();
      });
      
      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(localStorage.getItem('sugboaid_session')).toBeNull();
      expect(toast.success).toHaveBeenCalledWith('Logged out successfully!');
    });
  });

  describe('Session Management', () => {
    it('should handle localStorage errors gracefully', () => {
      // Mock localStorage to throw an error
      const originalGetItem = localStorage.getItem;
      localStorage.getItem = vi.fn().mockImplementation(() => {
        throw new Error('Storage error');
      });
      
      const { result } = renderHook(() => useAuth());
      
      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      
      // Restore original method
      localStorage.getItem = originalGetItem;
    });

    it('should validate session expiration', () => {
      const expiredSession = {
        ...mockSession,
        expiresAt: new Date(Date.now() - 1000).toISOString(),
      };
      localStorage.setItem('sugboaid_session', JSON.stringify(expiredSession));
      
      const { result } = renderHook(() => useAuth());
      
      expect(result.current.isAuthenticated).toBe(false);
      expect(localStorage.getItem('sugboaid_session')).toBeNull();
    });
  });

  describe('Loading States', () => {
    it('should set loading state during login', async () => {
      localStorage.setItem('sugboaid_users', JSON.stringify([mockUser]));
      const { result } = renderHook(() => useAuth());
      
      let loginPromise: Promise<boolean>;
      act(() => {
        loginPromise = result.current.login('test@example.com', 'password');
        expect(result.current.isLoading).toBe(true);
      });
      
      await act(async () => {
        await loginPromise!;
      });
      
      expect(result.current.isLoading).toBe(false);
    });

    it('should set loading state during signup', async () => {
      const { result } = renderHook(() => useAuth());
      
      let signupPromise: Promise<boolean>;
      act(() => {
        signupPromise = result.current.signup('Test User', 'test@example.com', 'password123');
        expect(result.current.isLoading).toBe(true);
      });
      
      await act(async () => {
        await signupPromise!;
      });
      
      expect(result.current.isLoading).toBe(false);
    });
  });
});