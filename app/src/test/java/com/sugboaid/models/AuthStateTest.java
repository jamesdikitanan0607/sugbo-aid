package com.sugboaid.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for AuthState model
 * Tests authentication state management and utility methods
 */
public class AuthStateTest {

    @Test
    public void testAuthStateCreation_DefaultConstructor_CreatesUnauthenticatedState() {
        // Act
        AuthState authState = new AuthState();

        // Assert
        assertFalse(authState.isAuthenticated());
        assertNull(authState.getUser());
        assertNull(authState.getSession());
    }

    @Test
    public void testAuthStateCreation_WithParameters_SetsFields() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);

        // Act
        AuthState authState = new AuthState(true, user, session);

        // Assert
        assertTrue(authState.isAuthenticated());
        assertEquals(user, authState.getUser());
        assertEquals(session, authState.getSession());
    }

    @Test
    public void testAuthenticated_StaticFactory_CreatesAuthenticatedState() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);

        // Act
        AuthState authState = AuthState.authenticated(user, session);

        // Assert
        assertTrue(authState.isAuthenticated());
        assertEquals(user, authState.getUser());
        assertEquals(session, authState.getSession());
    }

    @Test
    public void testUnauthenticated_StaticFactory_CreatesUnauthenticatedState() {
        // Act
        AuthState authState = AuthState.unauthenticated();

        // Assert
        assertFalse(authState.isAuthenticated());
        assertNull(authState.getUser());
        assertNull(authState.getSession());
    }

    @Test
    public void testHasValidSession_AuthenticatedWithValidSession_ReturnsTrue() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Act & Assert
        assertTrue(authState.hasValidSession());
    }

    @Test
    public void testHasValidSession_AuthenticatedWithNullSession_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        AuthState authState = new AuthState(true, user, null);

        // Act & Assert
        assertFalse(authState.hasValidSession());
    }

    @Test
    public void testHasValidSession_AuthenticatedWithInvalidSession_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        session.setActive(false); // Make session invalid
        AuthState authState = AuthState.authenticated(user, session);

        // Act & Assert
        assertFalse(authState.hasValidSession());
    }

    @Test
    public void testHasValidSession_Unauthenticated_ReturnsFalse() {
        // Arrange
        AuthState authState = AuthState.unauthenticated();

        // Act & Assert
        assertFalse(authState.hasValidSession());
    }

    @Test
    public void testGetUserName_WithUser_ReturnsUserName() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Act
        String userName = authState.getUserName();

        // Assert
        assertEquals("John Doe", userName);
    }

    @Test
    public void testGetUserName_WithoutUser_ReturnsNull() {
        // Arrange
        AuthState authState = AuthState.unauthenticated();

        // Act
        String userName = authState.getUserName();

        // Assert
        assertNull(userName);
    }

    @Test
    public void testGetUserEmail_WithUser_ReturnsUserEmail() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Act
        String userEmail = authState.getUserEmail();

        // Assert
        assertEquals("john@example.com", userEmail);
    }

    @Test
    public void testGetUserEmail_WithoutUser_ReturnsNull() {
        // Arrange
        AuthState authState = AuthState.unauthenticated();

        // Act
        String userEmail = authState.getUserEmail();

        // Assert
        assertNull(userEmail);
    }

    @Test
    public void testGetUserId_WithUser_ReturnsUserId() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Act
        String userId = authState.getUserId();

        // Assert
        assertEquals(user.getId(), userId);
    }

    @Test
    public void testGetUserId_WithoutUser_ReturnsNull() {
        // Arrange
        AuthState authState = AuthState.unauthenticated();

        // Act
        String userId = authState.getUserId();

        // Assert
        assertNull(userId);
    }

    @Test
    public void testClear_AuthenticatedState_ClearsState() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Act
        authState.clear();

        // Assert
        assertFalse(authState.isAuthenticated());
        assertNull(authState.getUser());
        assertNull(authState.getSession());
    }

    @Test
    public void testClear_UnauthenticatedState_RemainsCleared() {
        // Arrange
        AuthState authState = AuthState.unauthenticated();

        // Act
        authState.clear();

        // Assert
        assertFalse(authState.isAuthenticated());
        assertNull(authState.getUser());
        assertNull(authState.getSession());
    }

    @Test
    public void testSetters_UpdateFields() {
        // Arrange
        AuthState authState = new AuthState();
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);

        // Act
        authState.setAuthenticated(true);
        authState.setUser(user);
        authState.setSession(session);

        // Assert
        assertTrue(authState.isAuthenticated());
        assertEquals(user, authState.getUser());
        assertEquals(session, authState.getSession());
    }

    @Test
    public void testEquals_SameState_ReturnsTrue() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState1 = AuthState.authenticated(user, session);
        AuthState authState2 = AuthState.authenticated(user, session);

        // Act & Assert
        assertTrue(authState1.equals(authState2));
    }

    @Test
    public void testEquals_DifferentAuthentication_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState1 = AuthState.authenticated(user, session);
        AuthState authState2 = AuthState.unauthenticated();

        // Act & Assert
        assertFalse(authState1.equals(authState2));
    }

    @Test
    public void testEquals_DifferentUser_ReturnsFalse() {
        // Arrange
        User user1 = new User("John Doe", "john@example.com", "hashedPassword1");
        User user2 = new User("Jane Doe", "jane@example.com", "hashedPassword2");
        UserSession session1 = UserSession.fromUser(user1);
        UserSession session2 = UserSession.fromUser(user2);
        AuthState authState1 = AuthState.authenticated(user1, session1);
        AuthState authState2 = AuthState.authenticated(user2, session2);

        // Act & Assert
        assertFalse(authState1.equals(authState2));
    }

    @Test
    public void testEquals_SameObject_ReturnsTrue() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Act & Assert
        assertTrue(authState.equals(authState));
    }

    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Act & Assert
        assertFalse(authState.equals(null));
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);
        String notAnAuthState = "not an auth state";

        // Act & Assert
        assertFalse(authState.equals(notAnAuthState));
    }

    @Test
    public void testEquals_NullUser_HandledCorrectly() {
        // Arrange
        AuthState authState1 = new AuthState(true, null, null);
        AuthState authState2 = new AuthState(true, null, null);
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        AuthState authState3 = new AuthState(true, user, null);

        // Act & Assert
        assertTrue(authState1.equals(authState2)); // Both have null user
        assertFalse(authState1.equals(authState3)); // One has null user, one doesn't
        assertFalse(authState3.equals(authState1)); // Reverse check
    }

    @Test
    public void testEquals_NullSession_HandledCorrectly() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        AuthState authState1 = new AuthState(true, user, null);
        AuthState authState2 = new AuthState(true, user, null);
        UserSession session = UserSession.fromUser(user);
        AuthState authState3 = new AuthState(true, user, session);

        // Act & Assert
        assertTrue(authState1.equals(authState2)); // Both have null session
        assertFalse(authState1.equals(authState3)); // One has null session, one doesn't
        assertFalse(authState3.equals(authState1)); // Reverse check
    }

    @Test
    public void testHashCode_SameState_SameHashCode() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState1 = AuthState.authenticated(user, session);
        AuthState authState2 = AuthState.authenticated(user, session);

        // Act & Assert
        assertEquals(authState1.hashCode(), authState2.hashCode());
    }

    @Test
    public void testHashCode_DifferentState_DifferentHashCode() {
        // Arrange
        User user1 = new User("John Doe", "john@example.com", "hashedPassword1");
        User user2 = new User("Jane Doe", "jane@example.com", "hashedPassword2");
        UserSession session1 = UserSession.fromUser(user1);
        UserSession session2 = UserSession.fromUser(user2);
        AuthState authState1 = AuthState.authenticated(user1, session1);
        AuthState authState2 = AuthState.authenticated(user2, session2);

        // Act & Assert
        assertNotEquals(authState1.hashCode(), authState2.hashCode());
    }

    @Test
    public void testHashCode_NullFields_HandledCorrectly() {
        // Arrange
        AuthState authState1 = new AuthState(false, null, null);
        AuthState authState2 = new AuthState(true, null, null);

        // Act & Assert
        assertNotEquals(authState1.hashCode(), authState2.hashCode()); // Different authentication status
    }

    @Test
    public void testToString_ContainsStateInfo() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Act
        String toString = authState.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("true")); // isAuthenticated
        assertTrue(toString.contains("AuthState"));
    }

    @Test
    public void testToString_UnauthenticatedState_ContainsCorrectInfo() {
        // Arrange
        AuthState authState = AuthState.unauthenticated();

        // Act
        String toString = authState.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("false")); // isAuthenticated
        assertTrue(toString.contains("null")); // user and session should be null
        assertTrue(toString.contains("AuthState"));
    }

    @Test
    public void testStateTransitions_AuthenticatedToUnauthenticated() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);
        AuthState authState = AuthState.authenticated(user, session);

        // Verify initial state
        assertTrue(authState.isAuthenticated());
        assertTrue(authState.hasValidSession());

        // Act - transition to unauthenticated
        authState.setAuthenticated(false);
        authState.setUser(null);
        authState.setSession(null);

        // Assert
        assertFalse(authState.isAuthenticated());
        assertFalse(authState.hasValidSession());
        assertNull(authState.getUserName());
        assertNull(authState.getUserEmail());
        assertNull(authState.getUserId());
    }

    @Test
    public void testStateTransitions_UnauthenticatedToAuthenticated() {
        // Arrange
        AuthState authState = AuthState.unauthenticated();
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        UserSession session = UserSession.fromUser(user);

        // Verify initial state
        assertFalse(authState.isAuthenticated());
        assertFalse(authState.hasValidSession());

        // Act - transition to authenticated
        authState.setAuthenticated(true);
        authState.setUser(user);
        authState.setSession(session);

        // Assert
        assertTrue(authState.isAuthenticated());
        assertTrue(authState.hasValidSession());
        assertEquals("John Doe", authState.getUserName());
        assertEquals("john@example.com", authState.getUserEmail());
        assertEquals(user.getId(), authState.getUserId());
    }
}