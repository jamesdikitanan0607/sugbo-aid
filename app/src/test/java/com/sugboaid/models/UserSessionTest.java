package com.sugboaid.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for UserSession model
 * Tests session management, expiration logic, and JSON serialization
 */
public class UserSessionTest {

    @Test
    public void testUserSessionCreation_ValidData_CreatesSession() {
        // Arrange
        String userId = "user-123";
        String email = "john@example.com";
        String name = "John Doe";

        // Act
        UserSession session = new UserSession(userId, email, name);

        // Assert
        assertEquals(userId, session.getUserId());
        assertEquals(email, session.getEmail());
        assertEquals(name, session.getName());
        assertTrue(session.isActive());
        assertTrue(session.getLoginTimestamp() > 0);
    }

    @Test
    public void testUserSessionCreation_DefaultConstructor_SetsDefaults() {
        // Act
        UserSession session = new UserSession();

        // Assert
        assertTrue(session.isActive());
        assertEquals(0, session.getLoginTimestamp());
    }

    @Test
    public void testUserSessionCreation_AllFields_SetsAllFields() {
        // Arrange
        String userId = "user-123";
        String email = "john@example.com";
        String name = "John Doe";
        long loginTimestamp = System.currentTimeMillis();
        boolean isActive = true;

        // Act
        UserSession session = new UserSession(userId, email, name, loginTimestamp, isActive);

        // Assert
        assertEquals(userId, session.getUserId());
        assertEquals(email, session.getEmail());
        assertEquals(name, session.getName());
        assertEquals(loginTimestamp, session.getLoginTimestamp());
        assertEquals(isActive, session.isActive());
    }

    @Test
    public void testFromUser_ValidUser_CreatesSession() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");

        // Act
        UserSession session = UserSession.fromUser(user);

        // Assert
        assertNotNull(session);
        assertEquals(user.getId(), session.getUserId());
        assertEquals(user.getEmail(), session.getEmail());
        assertEquals(user.getName(), session.getName());
        assertTrue(session.isActive());
        assertTrue(session.getLoginTimestamp() > 0);
    }

    @Test
    public void testFromUser_NullUser_ReturnsNull() {
        // Act
        UserSession session = UserSession.fromUser(null);

        // Assert
        assertNull(session);
    }

    @Test
    public void testIsValid_ValidSession_ReturnsTrue() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");

        // Act & Assert
        assertTrue(session.isValid());
    }

    @Test
    public void testIsValid_InactiveSession_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        session.setActive(false);

        // Act & Assert
        assertFalse(session.isValid());
    }

    @Test
    public void testIsValid_ExpiredSession_ReturnsFalse() {
        // Arrange - Create session with old timestamp (25 hours ago)
        long oldTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L);
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe", oldTimestamp, true);

        // Act & Assert
        assertFalse(session.isValid());
        assertTrue(session.isExpired());
    }

    @Test
    public void testIsValid_EmptyUserId_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("", "john@example.com", "John Doe");

        // Act & Assert
        assertFalse(session.isValid());
    }

    @Test
    public void testIsValid_NullUserId_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        session.setUserId(null);

        // Act & Assert
        assertFalse(session.isValid());
    }

    @Test
    public void testIsValid_EmptyEmail_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "", "John Doe");

        // Act & Assert
        assertFalse(session.isValid());
    }

    @Test
    public void testIsValid_NullEmail_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        session.setEmail(null);

        // Act & Assert
        assertFalse(session.isValid());
    }

    @Test
    public void testIsValid_EmptyName_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "");

        // Act & Assert
        assertFalse(session.isValid());
    }

    @Test
    public void testIsValid_NullName_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        session.setName(null);

        // Act & Assert
        assertFalse(session.isValid());
    }

    @Test
    public void testIsValid_ZeroTimestamp_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe", 0, true);

        // Act & Assert
        assertFalse(session.isValid());
    }

    @Test
    public void testIsExpired_FreshSession_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");

        // Act & Assert
        assertFalse(session.isExpired());
    }

    @Test
    public void testIsExpired_ExpiredSession_ReturnsTrue() {
        // Arrange - Create session with old timestamp (25 hours ago)
        long oldTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L);
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe", oldTimestamp, true);

        // Act & Assert
        assertTrue(session.isExpired());
    }

    @Test
    public void testIsExpired_ExactlyExpired_ReturnsTrue() {
        // Arrange - Create session with timestamp exactly 24 hours ago
        long exactlyExpiredTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000L);
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe", exactlyExpiredTimestamp, true);

        // Act & Assert
        assertTrue(session.isExpired());
    }

    @Test
    public void testGetRemainingTime_FreshSession_ReturnsPositiveTime() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");

        // Act
        long remainingTime = session.getRemainingTime();

        // Assert
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= (24 * 60 * 60 * 1000L)); // Should be less than or equal to 24 hours
    }

    @Test
    public void testGetRemainingTime_ExpiredSession_ReturnsZero() {
        // Arrange - Create session with old timestamp (25 hours ago)
        long oldTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L);
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe", oldTimestamp, true);

        // Act
        long remainingTime = session.getRemainingTime();

        // Assert
        assertEquals(0, remainingTime);
    }

    @Test
    public void testGetRemainingHours_FreshSession_ReturnsHours() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");

        // Act
        long remainingHours = session.getRemainingHours();

        // Assert
        assertTrue(remainingHours >= 0);
        assertTrue(remainingHours <= 24);
    }

    @Test
    public void testGetRemainingHours_ExpiredSession_ReturnsZero() {
        // Arrange - Create session with old timestamp (25 hours ago)
        long oldTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L);
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe", oldTimestamp, true);

        // Act
        long remainingHours = session.getRemainingHours();

        // Assert
        assertEquals(0, remainingHours);
    }

    @Test
    public void testRefreshSession_UpdatesTimestamp() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        long originalTimestamp = session.getLoginTimestamp();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        session.refreshSession();

        // Assert
        assertTrue(session.getLoginTimestamp() > originalTimestamp);
        assertTrue(session.isActive());
    }

    @Test
    public void testRefreshSession_InactiveSession_ActivatesSession() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        session.setActive(false);

        // Act
        session.refreshSession();

        // Assert
        assertTrue(session.isActive());
    }

    @Test
    public void testInvalidate_DeactivatesSession() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        assertTrue(session.isActive());

        // Act
        session.invalidate();

        // Assert
        assertFalse(session.isActive());
        assertFalse(session.isValid());
    }

    @Test
    public void testJsonSerialization_ValidSession_SerializesAndDeserializes() {
        // Arrange
        UserSession originalSession = new UserSession("user-123", "john@example.com", "John Doe");

        // Act
        String json = originalSession.toJson();
        UserSession deserializedSession = UserSession.fromJson(json);

        // Assert
        assertNotNull(json);
        assertNotNull(deserializedSession);
        assertEquals(originalSession.getUserId(), deserializedSession.getUserId());
        assertEquals(originalSession.getEmail(), deserializedSession.getEmail());
        assertEquals(originalSession.getName(), deserializedSession.getName());
        assertEquals(originalSession.getLoginTimestamp(), deserializedSession.getLoginTimestamp());
        assertEquals(originalSession.isActive(), deserializedSession.isActive());
    }

    @Test
    public void testJsonDeserialization_InvalidJson_ReturnsNull() {
        // Act
        UserSession session = UserSession.fromJson("invalid json");

        // Assert
        assertNull(session);
    }

    @Test
    public void testJsonDeserialization_EmptyJson_ReturnsNull() {
        // Act
        UserSession session = UserSession.fromJson("");

        // Assert
        assertNull(session);
    }

    @Test
    public void testJsonDeserialization_NullJson_ReturnsNull() {
        // Act
        UserSession session = UserSession.fromJson(null);

        // Assert
        assertNull(session);
    }

    @Test
    public void testEquals_SameUserId_ReturnsTrue() {
        // Arrange
        String userId = "user-123";
        UserSession session1 = new UserSession(userId, "john@example.com", "John Doe");
        UserSession session2 = new UserSession(userId, "jane@example.com", "Jane Doe");

        // Act & Assert
        assertTrue(session1.equals(session2));
    }

    @Test
    public void testEquals_DifferentUserId_ReturnsFalse() {
        // Arrange
        UserSession session1 = new UserSession("user-123", "john@example.com", "John Doe");
        UserSession session2 = new UserSession("user-456", "john@example.com", "John Doe");

        // Act & Assert
        assertFalse(session1.equals(session2));
    }

    @Test
    public void testEquals_SameObject_ReturnsTrue() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");

        // Act & Assert
        assertTrue(session.equals(session));
    }

    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");

        // Act & Assert
        assertFalse(session.equals(null));
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        String notASession = "not a session";

        // Act & Assert
        assertFalse(session.equals(notASession));
    }

    @Test
    public void testHashCode_SameUserId_SameHashCode() {
        // Arrange
        String userId = "user-123";
        UserSession session1 = new UserSession(userId, "john@example.com", "John Doe");
        UserSession session2 = new UserSession(userId, "jane@example.com", "Jane Doe");

        // Act & Assert
        assertEquals(session1.hashCode(), session2.hashCode());
    }

    @Test
    public void testHashCode_DifferentUserId_DifferentHashCode() {
        // Arrange
        UserSession session1 = new UserSession("user-123", "john@example.com", "John Doe");
        UserSession session2 = new UserSession("user-456", "john@example.com", "John Doe");

        // Act & Assert
        assertNotEquals(session1.hashCode(), session2.hashCode());
    }

    @Test
    public void testHashCode_NullUserId_ReturnsZero() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        session.setUserId(null);

        // Act & Assert
        assertEquals(0, session.hashCode());
    }

    @Test
    public void testToString_ContainsSessionInfo() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");

        // Act
        String toString = session.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains(session.getUserId()));
        assertTrue(toString.contains(session.getEmail()));
        assertTrue(toString.contains(session.getName()));
        assertTrue(toString.contains(String.valueOf(session.getLoginTimestamp())));
        assertTrue(toString.contains(String.valueOf(session.isActive())));
        assertTrue(toString.contains(String.valueOf(session.isExpired())));
    }

    @Test
    public void testSetters_UpdateFields() {
        // Arrange
        UserSession session = new UserSession();

        // Act
        session.setUserId("user-123");
        session.setEmail("john@example.com");
        session.setName("John Doe");
        session.setLoginTimestamp(12345L);
        session.setActive(false);

        // Assert
        assertEquals("user-123", session.getUserId());
        assertEquals("john@example.com", session.getEmail());
        assertEquals("John Doe", session.getName());
        assertEquals(12345L, session.getLoginTimestamp());
        assertFalse(session.isActive());
    }

    @Test
    public void testSessionDuration_24Hours_IsCorrect() {
        // Arrange
        UserSession session = new UserSession("user-123", "john@example.com", "John Doe");
        
        // Create a session that's almost expired (23 hours 59 minutes ago)
        long almostExpiredTimestamp = System.currentTimeMillis() - (23 * 60 * 60 * 1000L + 59 * 60 * 1000L);
        session.setLoginTimestamp(almostExpiredTimestamp);

        // Act & Assert
        assertFalse(session.isExpired()); // Should not be expired yet
        assertTrue(session.getRemainingTime() > 0); // Should have some time left
        assertTrue(session.getRemainingTime() < (60 * 1000L)); // Should be less than 1 minute
    }

    @Test
    public void testSessionValidation_AllFieldsRequired() {
        // Test that all required fields must be present for a valid session
        UserSession session = new UserSession();
        
        // Empty session should be invalid
        assertFalse(session.isValid());
        
        // Add fields one by one
        session.setUserId("user-123");
        assertFalse(session.isValid()); // Still missing other fields
        
        session.setEmail("john@example.com");
        assertFalse(session.isValid()); // Still missing name and timestamp
        
        session.setName("John Doe");
        assertFalse(session.isValid()); // Still missing valid timestamp
        
        session.setLoginTimestamp(System.currentTimeMillis());
        assertTrue(session.isValid()); // Now should be valid
    }
}