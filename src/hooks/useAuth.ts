import { useState, useEffect, useCallback } from "react";
import { toast } from "sonner";

// Storage keys
const STORAGE_KEYS = {
  USERS: 'sugboaid_users',
  CURRENT_SESSION: 'sugboaid_session'
} as const;

// Type definitions
export type UserRole = 'Donor' | 'Organization' | 'Volunteer' | 'Recipient' | 'Guest';

export interface User {
  id: string;
  name: string;
  email: string;
  password: string;
  role: UserRole;
  createdAt: string;
  lastLogin?: string;
}

export interface UserSession {
  user: Omit<User, 'password'>;
  token: string;
  expiresAt: string;
}

export interface AuthState {
  user: Omit<User, 'password'> | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface AuthActions {
  login: (email: string, password: string) => Promise<boolean>;
  signup: (name: string, email: string, password: string, role?: UserRole) => Promise<boolean>;
  logout: () => void;
  checkAuth: () => void;
}

export interface UseAuthReturn extends AuthState, AuthActions {}

// Utility functions
const generateId = (): string => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2);
};

const generateToken = (): string => {
  return Math.random().toString(36).substr(2) + Date.now().toString(36);
};

const hashPassword = (password: string): string => {
  // Simple hash function for demo purposes
  let hash = 0;
  for (let i = 0; i < password.length; i++) {
    const char = password.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // Convert to 32-bit integer
  }
  return hash.toString();
};

const isSessionValid = (session: UserSession): boolean => {
  return new Date(session.expiresAt) > new Date();
};

// localStorage utilities
const getStoredUsers = (): User[] => {
  try {
    const users = localStorage.getItem(STORAGE_KEYS.USERS);
    return users ? JSON.parse(users) : [];
  } catch (error) {
    console.error('Error reading users from localStorage:', error);
    return [];
  }
};

const storeUsers = (users: User[]): void => {
  try {
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(users));
  } catch (error) {
    console.error('Error storing users to localStorage:', error);
  }
};

const getStoredSession = (): UserSession | null => {
  try {
    const session = localStorage.getItem(STORAGE_KEYS.CURRENT_SESSION);
    return session ? JSON.parse(session) : null;
  } catch (error) {
    console.error('Error reading session from localStorage:', error);
    return null;
  }
};

const storeSession = (session: UserSession): void => {
  try {
    localStorage.setItem(STORAGE_KEYS.CURRENT_SESSION, JSON.stringify(session));
  } catch (error) {
    console.error('Error storing session to localStorage:', error);
  }
};

const clearSession = (): void => {
  try {
    localStorage.removeItem(STORAGE_KEYS.CURRENT_SESSION);
  } catch (error) {
    console.error('Error clearing session from localStorage:', error);
  }
};

export const useAuth = (): UseAuthReturn => {
  const [authState, setAuthState] = useState<AuthState>({
    user: null,
    isAuthenticated: false,
    isLoading: true,
  });

  // Check authentication status on mount
  const checkAuth = useCallback(() => {
    setAuthState(prev => ({ ...prev, isLoading: true }));
    
    const session = getStoredSession();
    
    if (session && isSessionValid(session)) {
      setAuthState({
        user: session.user,
        isAuthenticated: true,
        isLoading: false,
      });
    } else {
      // Clear invalid session
      if (session) {
        clearSession();
      }
      setAuthState({
        user: null,
        isAuthenticated: false,
        isLoading: false,
      });
    }
  }, []);

  // Login function
  const login = useCallback(async (email: string, password: string): Promise<boolean> => {
    setAuthState(prev => ({ ...prev, isLoading: true }));
    
    try {
      const users = getStoredUsers();
      const user = users.find(u => u.email === email);
      
      if (!user) {
        toast.error("No account found with this email");
        setAuthState(prev => ({ ...prev, isLoading: false }));
        return false;
      }
      
      const hashedPassword = hashPassword(password);
      if (user.password !== hashedPassword) {
        toast.error("Invalid email or password");
        setAuthState(prev => ({ ...prev, isLoading: false }));
        return false;
      }
      
      // Update last login
      const updatedUser = { ...user, lastLogin: new Date().toISOString() };
      const updatedUsers = users.map(u => u.id === user.id ? updatedUser : u);
      storeUsers(updatedUsers);
      
      // Create session
      const session: UserSession = {
        user: {
          id: updatedUser.id,
          name: updatedUser.name,
          email: updatedUser.email,
          role: updatedUser.role,
          createdAt: updatedUser.createdAt,
          lastLogin: updatedUser.lastLogin,
        },
        token: generateToken(),
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), // 24 hours
      };
      
      storeSession(session);
      
      setAuthState({
        user: session.user,
        isAuthenticated: true,
        isLoading: false,
      });
      
      toast.success("Login successful!");
      return true;
    } catch (error) {
      console.error('Login error:', error);
      toast.error("Something went wrong. Please try again.");
      setAuthState(prev => ({ ...prev, isLoading: false }));
      return false;
    }
  }, []);

  // Signup function
  const signup = useCallback(async (name: string, email: string, password: string, role: UserRole = 'Donor'): Promise<boolean> => {
    setAuthState(prev => ({ ...prev, isLoading: true }));
    
    try {
      const users = getStoredUsers();
      
      // Check if email already exists
      if (users.find(u => u.email === email)) {
        toast.error("An account with this email already exists");
        setAuthState(prev => ({ ...prev, isLoading: false }));
        return false;
      }
      
      // Create new user
      const newUser: User = {
        id: generateId(),
        name,
        email,
        role,
        password: hashPassword(password),
        createdAt: new Date().toISOString(),
      };
      
      // Store user
      const updatedUsers = [...users, newUser];
      storeUsers(updatedUsers);
      
      // Create session
      const session: UserSession = {
        user: {
          id: newUser.id,
          name: newUser.name,
          email: newUser.email,
          createdAt: newUser.createdAt,
        },
        token: generateToken(),
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), // 24 hours
      };
      
      storeSession(session);
      
      setAuthState({
        user: session.user,
        isAuthenticated: true,
        isLoading: false,
      });
      
      toast.success("Account created successfully!");
      return true;
    } catch (error) {
      console.error('Signup error:', error);
      toast.error("Something went wrong. Please try again.");
      setAuthState(prev => ({ ...prev, isLoading: false }));
      return false;
    }
  }, []);

  // Logout function
  const logout = useCallback(() => {
    clearSession();
    setAuthState({
      user: null,
      isAuthenticated: false,
      isLoading: false,
    });
    toast.success("Logged out successfully!");
  }, []);

  // Initialize auth check on mount
  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  return {
    ...authState,
    login,
    signup,
    logout,
    checkAuth,
  };
};
  