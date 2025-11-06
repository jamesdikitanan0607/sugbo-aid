package com.sugboaid.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for User model
 * Tests user data validation, JSON serialization, and utility methods
 */
public class UserTest {

    @Test
    public void testUserCreation_ValidData_CreatesUser() {
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        String passwordHash = "hashedPassword123";

        // Act
        User user = new User(name, email, passwordHash);

        // Assert
        assertNotNull(user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertTrue(user.getCreatedAt() > 0);
        assertNull(user.getLastLogin());
    }

    @Test
    public void testUserCreation_DefaultConstructor_GeneratesId() {
        // Act
        User user = new User();

        // Assert
        assertNotNull(user.getId());
        assertTrue(user.getCreatedAt() > 0);
    }

    @Test
    public void testUserCreation_AllFields_SetsAllFields() {
        // Arrange
        String id = "test-id";
        String name = "John Doe";
        String email = "john@example.com";
        String passwordHash = "hashedPassword123";
        long createdAt = System.currentTimeMillis();
        Long lastLogin = System.currentTimeMillis();

        // Act
        User user = new User(id, name, email, passwordHash, createdAt, lastLogin);

        // Assert
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(lastLogin, user.getLastLogin());
    }

    @Test
    public void testUpdateLastLogin_UpdatesTimestamp() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        assertNull(user.getLastLogin());

        // Act
        user.updateLastLogin();

        // Assert
        assertNotNull(user.getLastLogin());
        assertTrue(user.getLastLogin() > 0);
    }

    @Test
    public void testIsValid_ValidUser_ReturnsTrue() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");

        // Act & Assert
        assertTrue(user.isValid());
    }

    @Test
    public void testIsValid_EmptyId_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.setId("");

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_NullId_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.setId(null);

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_EmptyName_ReturnsFalse() {
        // Arrange
        User user = new User("", "john@example.com", "hashedPassword123");

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_ShortName_ReturnsFalse() {
        // Arrange
        User user = new User("J", "john@example.com", "hashedPassword123");

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_NullName_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.setName(null);

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_EmptyEmail_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "", "hashedPassword123");

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_InvalidEmail_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "invalid-email", "hashedPassword123");

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_NullEmail_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.setEmail(null);

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_EmptyPasswordHash_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "");

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_NullPasswordHash_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.setPasswordHash(null);

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_ZeroCreatedAt_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.setCreatedAt(0);

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testIsValid_NegativeCreatedAt_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.setCreatedAt(-1);

        // Act & Assert
        assertFalse(user.isValid());
    }

    @Test
    public void testJsonSerialization_ValidUser_SerializesAndDeserializes() {
        // Arrange
        User originalUser = new User("John Doe", "john@example.com", "hashedPassword123");
        originalUser.updateLastLogin();

        // Act
        String json = originalUser.toJson();
        User deserializedUser = User.fromJson(json);

        // Assert
        assertNotNull(json);
        assertNotNull(deserializedUser);
        assertEquals(originalUser.getId(), deserializedUser.getId());
        assertEquals(originalUser.getName(), deserializedUser.getName());
        assertEquals(originalUser.getEmail(), deserializedUser.getEmail());
        assertEquals(originalUser.getPasswordHash(), deserializedUser.getPasswordHash());
        assertEquals(originalUser.getCreatedAt(), deserializedUser.getCreatedAt());
        assertEquals(originalUser.getLastLogin(), deserializedUser.getLastLogin());
    }

    @Test
    public void testJsonDeserialization_InvalidJson_ReturnsNull() {
        // Act
        User user = User.fromJson("invalid json");

        // Assert
        assertNull(user);
    }

    @Test
    public void testJsonDeserialization_EmptyJson_ReturnsNull() {
        // Act
        User user = User.fromJson("");

        // Assert
        assertNull(user);
    }

    @Test
    public void testJsonDeserialization_NullJson_ReturnsNull() {
        // Act
        User user = User.fromJson(null);

        // Assert
        assertNull(user);
    }

    @Test
    public void testCreateSafeUser_RemovesPasswordHash() {
        // Arrange
        User originalUser = new User("John Doe", "john@example.com", "hashedPassword123");
        originalUser.updateLastLogin();

        // Act
        User safeUser = originalUser.createSafeUser();

        // Assert
        assertNotNull(safeUser);
        assertEquals(originalUser.getId(), safeUser.getId());
        assertEquals(originalUser.getName(), safeUser.getName());
        assertEquals(originalUser.getEmail(), safeUser.getEmail());
        assertEquals(originalUser.getCreatedAt(), safeUser.getCreatedAt());
        assertEquals(originalUser.getLastLogin(), safeUser.getLastLogin());
        assertNull(safeUser.getPasswordHash()); // Password hash should be null
    }

    @Test
    public void testEquals_SameId_ReturnsTrue() {
        // Arrange
        String id = "test-id";
        User user1 = new User(id, "John Doe", "john@example.com", "hash1", System.currentTimeMillis(), null);
        User user2 = new User(id, "Jane Doe", "jane@example.com", "hash2", System.currentTimeMillis(), null);

        // Act & Assert
        assertTrue(user1.equals(user2));
    }

    @Test
    public void testEquals_DifferentId_ReturnsFalse() {
        // Arrange
        User user1 = new User("id1", "John Doe", "john@example.com", "hash1", System.currentTimeMillis(), null);
        User user2 = new User("id2", "John Doe", "john@example.com", "hash1", System.currentTimeMillis(), null);

        // Act & Assert
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_SameObject_ReturnsTrue() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");

        // Act & Assert
        assertTrue(user.equals(user));
    }

    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");

        // Act & Assert
        assertFalse(user.equals(null));
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        String notAUser = "not a user";

        // Act & Assert
        assertFalse(user.equals(notAUser));
    }

    @Test
    public void testHashCode_SameId_SameHashCode() {
        // Arrange
        String id = "test-id";
        User user1 = new User(id, "John Doe", "john@example.com", "hash1", System.currentTimeMillis(), null);
        User user2 = new User(id, "Jane Doe", "jane@example.com", "hash2", System.currentTimeMillis(), null);

        // Act & Assert
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testHashCode_DifferentId_DifferentHashCode() {
        // Arrange
        User user1 = new User("id1", "John Doe", "john@example.com", "hash1", System.currentTimeMillis(), null);
        User user2 = new User("id2", "John Doe", "john@example.com", "hash1", System.currentTimeMillis(), null);

        // Act & Assert
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testHashCode_NullId_ReturnsZero() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.setId(null);

        // Act & Assert
        assertEquals(0, user.hashCode());
    }

    @Test
    public void testToString_ContainsUserInfo() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword123");
        user.updateLastLogin();

        // Act
        String toString = user.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains(user.getId()));
        assertTrue(toString.contains(user.getName()));
        assertTrue(toString.contains(user.getEmail()));
        assertTrue(toString.contains(String.valueOf(user.getCreatedAt())));
        assertTrue(toString.contains(String.valueOf(user.getLastLogin())));
        assertFalse(toString.contains(user.getPasswordHash())); // Password hash should not be in toString
    }

    @Test
    public void testSetters_UpdateFields() {
        // Arrange
        User user = new User();

        // Act
        user.setId("test-id");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashedPassword123");
        user.setCreatedAt(12345L);
        user.setLastLogin(67890L);

        // Assert
        assertEquals("test-id", user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("hashedPassword123", user.getPasswordHash());
        assertEquals(12345L, user.getCreatedAt());
        assertEquals(Long.valueOf(67890L), user.getLastLogin());
    }

    @Test
    public void testEmailValidation_ValidEmails_PassValidation() {
        // Valid emails should create valid users
        assertTrue(new User("John Doe", "test@example.com", "hash").isValid());
        assertTrue(new User("John Doe", "user.name@domain.co.uk", "hash").isValid());
        assertTrue(new User("John Doe", "test123@test-domain.com", "hash").isValid());
    }

    @Test
    public void testEmailValidation_InvalidEmails_FailValidation() {
        // Invalid emails should create invalid users
        assertFalse(new User("John Doe", "invalid-email", "hash").isValid());
        assertFalse(new User("John Doe", "@domain.com", "hash").isValid());
        assertFalse(new User("John Doe", "test@", "hash").isValid());
        assertFalse(new User("John Doe", ".test@domain.com", "hash").isValid());
        assertFalse(new User("John Doe", "test@domain.com.", "hash").isValid());
    }

    @Test
    public void testNameValidation_ValidNames_PassValidation() {
        // Valid names should create valid users
        assertTrue(new User("John Doe", "test@example.com", "hash").isValid());
        assertTrue(new User("Jane", "test@example.com", "hash").isValid());
        assertTrue(new User("Mary Jane Watson", "test@example.com", "hash").isValid());
    }

    @Test
    public void testNameValidation_InvalidNames_FailValidation() {
        // Invalid names should create invalid users
        assertFalse(new User("J", "test@example.com", "hash").isValid()); // Too short
        assertFalse(new User("", "test@example.com", "hash").isValid()); // Empty
        assertFalse(new User("   ", "test@example.com", "hash").isValid()); // Whitespace only
    }
}