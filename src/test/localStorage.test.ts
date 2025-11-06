import { describe, it, expect, vi, beforeEach } from 'vitest';

// Test localStorage integration and session persistence
describe('LocalStorage Integration', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('User Storage', () => {
    it('should store and retrieve users correctly', () => {
      const users = [
        {
          id: 'user1',
          name: 'John Doe',
          email: 'john@example.com',
          password: 'hashedpassword',
          createdAt: '2024-01-01T00:00:00.000Z',
        },
        {
          id: 'user2',
          name: 'Jane Smith',
          email: 'jane@example.com',
          password: 'hashedpassword2',
          createdAt: '2024-01-02T00:00:00.000Z',
        },
      ];

      localStorage.setItem('sugboaid_users', JSON.stringify(users));
      const retrievedUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');

      expect(retrievedUsers).toEqual(users);
      expect(retrievedUsers).toHaveLength(2);
    });

    it('should handle empty users array', () => {
      const users = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      expect(users).toEqual([]);
    });

    it('should handle malformed user data', () => {
      localStorage.setItem('sugboaid_users', 'invalid-json');
      
      let users;
      try {
        users = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      } catch (error) {
        users = [];
      }

      expect(users).toEqual([]);
    });
  });

  describe('Session Storage', () => {
    it('should store and retrieve session correctly', () => {
      const session = {
        user: {
          id: 'user1',
          name: 'John Doe',
          email: 'john@example.com',
          createdAt: '2024-01-01T00:00:00.000Z',
        },
        token: 'session-token-123',
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      };

      localStorage.setItem('sugboaid_session', JSON.stringify(session));
      const retrievedSession = JSON.parse(localStorage.getItem('sugboaid_session') || '{}');

      expect(retrievedSession).toEqual(session);
      expect(retrievedSession.user.name).toBe('John Doe');
      expect(retrievedSession.token).toBe('session-token-123');
    });

    it('should handle missing session', () => {
      const session = JSON.parse(localStorage.getItem('sugboaid_session') || 'null');
      expect(session).toBeNull();
    });

    it('should handle malformed session data', () => {
      localStorage.setItem('sugboaid_session', 'invalid-json');
      
      let session;
      try {
        session = JSON.parse(localStorage.getItem('sugboaid_session') || 'null');
      } catch (error) {
        session = null;
      }

      expect(session).toBeNull();
    });

    it('should clear session correctly', () => {
      const session = {
        user: { id: 'user1', name: 'John', email: 'john@example.com', createdAt: '2024-01-01' },
        token: 'token',
        expiresAt: new Date().toISOString(),
      };

      localStorage.setItem('sugboaid_session', JSON.stringify(session));
      expect(localStorage.getItem('sugboaid_session')).toBeTruthy();

      localStorage.removeItem('sugboaid_session');
      expect(localStorage.getItem('sugboaid_session')).toBeNull();
    });
  });

  describe('Session Validation', () => {
    it('should validate non-expired session', () => {
      const futureDate = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString();
      const session = {
        user: { id: 'user1', name: 'John', email: 'john@example.com', createdAt: '2024-01-01' },
        token: 'token',
        expiresAt: futureDate,
      };

      const isValid = new Date(session.expiresAt) > new Date();
      expect(isValid).toBe(true);
    });

    it('should invalidate expired session', () => {
      const pastDate = new Date(Date.now() - 1000).toISOString();
      const session = {
        user: { id: 'user1', name: 'John', email: 'john@example.com', createdAt: '2024-01-01' },
        token: 'token',
        expiresAt: pastDate,
      };

      const isValid = new Date(session.expiresAt) > new Date();
      expect(isValid).toBe(false);
    });

    it('should handle invalid date formats', () => {
      const session = {
        user: { id: 'user1', name: 'John', email: 'john@example.com', createdAt: '2024-01-01' },
        token: 'token',
        expiresAt: 'invalid-date',
      };

      const expirationDate = new Date(session.expiresAt);
      const isValid = !isNaN(expirationDate.getTime()) && expirationDate > new Date();
      expect(isValid).toBe(false);
    });
  });

  describe('Data Persistence Across Sessions', () => {
    it('should maintain user data across multiple operations', () => {
      // Initial user
      const user1 = {
        id: 'user1',
        name: 'John Doe',
        email: 'john@example.com',
        password: 'hash1',
        createdAt: '2024-01-01T00:00:00.000Z',
      };

      localStorage.setItem('sugboaid_users', JSON.stringify([user1]));

      // Add second user
      const existingUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      const user2 = {
        id: 'user2',
        name: 'Jane Smith',
        email: 'jane@example.com',
        password: 'hash2',
        createdAt: '2024-01-02T00:00:00.000Z',
      };

      const updatedUsers = [...existingUsers, user2];
      localStorage.setItem('sugboaid_users', JSON.stringify(updatedUsers));

      // Verify both users are stored
      const finalUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      expect(finalUsers).toHaveLength(2);
      expect(finalUsers[0].email).toBe('john@example.com');
      expect(finalUsers[1].email).toBe('jane@example.com');
    });

    it('should update user data correctly', () => {
      const user = {
        id: 'user1',
        name: 'John Doe',
        email: 'john@example.com',
        password: 'hash1',
        createdAt: '2024-01-01T00:00:00.000Z',
      };

      localStorage.setItem('sugboaid_users', JSON.stringify([user]));

      // Update user with lastLogin
      const users = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      const updatedUser = { ...users[0], lastLogin: '2024-01-03T00:00:00.000Z' };
      const updatedUsers = users.map((u: any) => u.id === updatedUser.id ? updatedUser : u);
      
      localStorage.setItem('sugboaid_users', JSON.stringify(updatedUsers));

      // Verify update
      const finalUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      expect(finalUsers[0].lastLogin).toBe('2024-01-03T00:00:00.000Z');
    });
  });

  describe('Storage Limits and Error Handling', () => {
    it('should handle storage quota exceeded gracefully', () => {
      // Mock localStorage to simulate quota exceeded
      const originalSetItem = localStorage.setItem;
      localStorage.setItem = vi.fn().mockImplementation(() => {
        throw new DOMException('QuotaExceededError');
      });

      let errorOccurred = false;
      try {
        localStorage.setItem('test', 'data');
      } catch (error) {
        errorOccurred = true;
        expect(error).toBeInstanceOf(DOMException);
      }

      expect(errorOccurred).toBe(true);

      // Restore original method
      localStorage.setItem = originalSetItem;
    });

    it('should handle concurrent access gracefully', () => {
      // Simulate concurrent user additions
      const user1 = { id: 'user1', name: 'User 1', email: 'user1@example.com', password: 'hash1', createdAt: '2024-01-01' };
      const user2 = { id: 'user2', name: 'User 2', email: 'user2@example.com', password: 'hash2', createdAt: '2024-01-02' };

      // First operation
      localStorage.setItem('sugboaid_users', JSON.stringify([user1]));
      
      // Second operation (simulating concurrent access)
      const existingUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      const updatedUsers = [...existingUsers, user2];
      localStorage.setItem('sugboaid_users', JSON.stringify(updatedUsers));

      const finalUsers = JSON.parse(localStorage.getItem('sugboaid_users') || '[]');
      expect(finalUsers).toHaveLength(2);
    });
  });
});