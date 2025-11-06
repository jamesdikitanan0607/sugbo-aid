package com.sugboaid.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import com.sugboaid.models.User;
import com.sugboaid.models.UserSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserRepository
 * Tests user data operations, SharedPreferences integration, and session management
 */
@RunWith(RobolectricTestRunner.class)
public class UserRepositoryTest {

    private UserRepository userRepository;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        userRepository = UserRepository.getInstance(context);
        
        // Clear all data before each test
        userRepository.clearAllData();
    }

    // User Management Tests

    @Test
    public void testSaveUser_ValidUser_ReturnsTrue() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");

        // Act
        boolean result = userRepository.saveUser(user);

        // Assert
        assertTrue(result);
        assertEquals(1, userRepository.getUserCount());
    }

    @Test
    public void testSaveUser_NullUser_ReturnsFalse() {
        // Act
        boolean result = userRepository.saveUser(null);

        // Assert
        assertFalse(result);
        assertEquals(0, userRepository.getUserCount());
    }

    @Test
    public void testSaveUser_InvalidUser_ReturnsFalse() {
        // Arrange - User with empty name
        User invalidUser = new User("", "john@example.com", "hashedPassword");

        // Act
        boolean result = userRepository.saveUser(invalidUser);

        // Assert
        assertFalse(result);
        assertEquals(0, userRepository.getUserCount());
    }

    @Test
    public void testSaveUser_DuplicateEmail_ReturnsFalse() {
        // Arrange
        User user1 = new User("John Doe", "john@example.com", "hashedPassword1");
        User user2 = new User("Jane Doe", "john@example.com", "hashedPassword2");

        // Act
        boolean result1 = userRepository.saveUser(user1);
        boolean result2 = userRepository.saveUser(user2);

        // Assert
        assertTrue(result1);
        assertFalse(result2);
        assertEquals(1, userRepository.getUserCount());
    }

    @Test
    public void testGetUserByEmail_ExistingUser_ReturnsUser() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        userRepository.saveUser(user);

        // Act
        User retrievedUser = userRepository.getUserByEmail("john@example.com");

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(user.getEmail(), retrievedUser.getEmail());
        assertEquals(user.getName(), retrievedUser.getName());
    }

    @Test
    public void testGetUserByEmail_NonExistingUser_ReturnsNull() {
        // Act
        User retrievedUser = userRepository.getUserByEmail("nonexistent@example.com");

        // Assert
        assertNull(retrievedUser);
    }

    @Test
    public void testGetUserByEmail_EmptyEmail_ReturnsNull() {
        // Act
        User retrievedUser = userRepository.getUserByEmail("");

        // Assert
        assertNull(retrievedUser);
    }

    @Test
    public void testGetUserByEmail_NullEmail_ReturnsNull() {
        // Act
        User retrievedUser = userRepository.getUserByEmail(null);

        // Assert
        assertNull(retrievedUser);
    }

    @Test
    public void testGetUserByEmail_CaseInsensitive_ReturnsUser() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        userRepository.saveUser(user);

        // Act
        User retrievedUser = userRepository.getUserByEmail("JOHN@EXAMPLE.COM");

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(user.getEmail(), retrievedUser.getEmail());
    }

    @Test
    public void testGetUserById_ExistingUser_ReturnsUser() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        userRepository.saveUser(user);

        // Act
        User retrievedUser = userRepository.getUserById(user.getId());

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(user.getId(), retrievedUser.getId());
        assertEquals(user.getName(), retrievedUser.getName());
    }

    @Test
    public void testGetUserById_NonExistingUser_ReturnsNull() {
        // Act
        User retrievedUser = userRepository.getUserById("nonexistent-id");

        // Assert
        assertNull(retrievedUser);
    }

    @Test
    public void testGetAllUsers_EmptyRepository_ReturnsEmptyList() {
        // Act
        List<User> users = userRepository.getAllUsers();

        // Assert
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetAllUsers_WithUsers_ReturnsAllUsers() {
        // Arrange
        User user1 = new User("John Doe", "john@example.com", "hashedPassword1");
        User user2 = new User("Jane Doe", "jane@example.com", "hashedPassword2");
        userRepository.saveUser(user1);
        userRepository.saveUser(user2);

        // Act
        List<User> users = userRepository.getAllUsers();

        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    public void testIsEmailExists_ExistingEmail_ReturnsTrue() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        userRepository.saveUser(user);

        // Act
        boolean exists = userRepository.isEmailExists("john@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    public void testIsEmailExists_NonExistingEmail_ReturnsFalse() {
        // Act
        boolean exists = userRepository.isEmailExists("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    public void testUpdateUser_ExistingUser_ReturnsTrue() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");
        userRepository.saveUser(user);
        
        user.setName("John Updated");
        user.updateLastLogin();

        // Act
        boolean result = userRepository.updateUser(user);

        // Assert
        assertTrue(result);
        User updatedUser = userRepository.getUserById(user.getId());
        assertEquals("John Updated", updatedUser.getName());
        assertNotNull(updatedUser.getLastLogin());
    }

    @Test
    public void testUpdateUser_NonExistingUser_ReturnsFalse() {
        // Arrange
        User user = new User("John Doe", "john@example.com", "hashedPassword");

        // Act
        boolean result = userRepository.updateUser(user);

        // Assert
        assertFalse(result);
    }

    // Authentication Tests

    @Test
    public void testRegisterUser_ValidData_ReturnsUser() {
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        String password = "password123";

        // Act
        User user = userRepository.registerUser(name, email, password);

        // Assert
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(email.toLowerCase(), user.getEmail());
        assertNotNull(user.getPasswordHash());
        assertTrue(userRepository.isEmailExists(email));
    }

    @Test
    public void testRegisterUser_EmptyName_ReturnsNull() {
        // Act
        User user = userRepository.registerUser("", "john@example.com", "password123");

        // Assert
        assertNull(user);
    }

    @Test
    public void testRegisterUser_ShortName_ReturnsNull() {
        // Act
        User user = userRepository.registerUser("J", "john@example.com", "password123");

        // Assert
        assertNull(user);
    }

    @Test
    public void testRegisterUser_InvalidEmail_ReturnsNull() {
        // Act
        User user = userRepository.registerUser("John Doe", "invalid-email", "password123");

        // Assert
        assertNull(user);
    }

    @Test
    public void testRegisterUser_WeakPassword_ReturnsNull() {
        // Act
        User user = userRepository.registerUser("John Doe", "john@example.com", "123");

        // Assert
        assertNull(user);
    }

    @Test
    public void testRegisterUser_ExistingEmail_ReturnsNull() {
        // Arrange
        userRepository.registerUser("John Doe", "john@example.com", "password123");

        // Act
        User user = userRepository.registerUser("Jane Doe", "john@example.com", "password456");

        // Assert
        assertNull(user);
    }

    @Test
    public void testValidateCredentials_ValidCredentials_ReturnsUser() {
        // Arrange
        String email = "john@example.com";
        String password = "password123";
        User registeredUser = userRepository.registerUser("John Doe", email, password);

        // Act
        User validatedUser = userRepository.validateCredentials(email, password);

        // Assert
        assertNotNull(validatedUser);
        assertEquals(registeredUser.getId(), validatedUser.getId());
        assertEquals(registeredUser.getEmail(), validatedUser.getEmail());
        assertNotNull(validatedUser.getLastLogin());
    }

    @Test
    public void testValidateCredentials_InvalidPassword_ReturnsNull() {
        // Arrange
        String email = "john@example.com";
        userRepository.registerUser("John Doe", email, "password123");

        // Act
        User validatedUser = userRepository.validateCredentials(email, "wrongpassword");

        // Assert
        assertNull(validatedUser);
    }

    @Test
    public void testValidateCredentials_NonExistingUser_ReturnsNull() {
        // Act
        User validatedUser = userRepository.validateCredentials("nonexistent@example.com", "password123");

        // Assert
        assertNull(validatedUser);
    }

    @Test
    public void testValidateCredentials_EmptyCredentials_ReturnsNull() {
        // Act
        User validatedUser1 = userRepository.validateCredentials("", "password123");
        User validatedUser2 = userRepository.validateCredentials("john@example.com", "");
        User validatedUser3 = userRepository.validateCredentials(null, "password123");
        User validatedUser4 = userRepository.validateCredentials("john@example.com", null);

        // Assert
        assertNull(validatedUser1);
        assertNull(validatedUser2);
        assertNull(validatedUser3);
        assertNull(validatedUser4);
    }

    // Session Management Tests

    @Test
    public void testSaveSession_ValidUser_ReturnsTrue() {
        // Arrange
        User user = userRepository.registerUser("John Doe", "john@example.com", "password123");

        // Act
        boolean result = userRepository.saveSession(user);

        // Assert
        assertTrue(result);
        assertTrue(userRepository.isLoggedIn());
    }

    @Test
    public void testSaveSession_NullUser_ReturnsFalse() {
        // Act
        boolean result = userRepository.saveSession(null);

        // Assert
        assertFalse(result);
        assertFalse(userRepository.isLoggedIn());
    }

    @Test
    public void testGetStoredSession_ValidSession_ReturnsSession() {
        // Arrange
        User user = userRepository.registerUser("John Doe", "john@example.com", "password123");
        userRepository.saveSession(user);

        // Act
        UserSession session = userRepository.getStoredSession();

        // Assert
        assertNotNull(session);
        assertEquals(user.getId(), session.getUserId());
        assertEquals(user.getEmail(), session.getEmail());
        assertEquals(user.getName(), session.getName());
        assertTrue(session.isValid());
    }

    @Test
    public void testGetStoredSession_NoSession_ReturnsNull() {
        // Act
        UserSession session = userRepository.getStoredSession();

        // Assert
        assertNull(session);
    }

    @Test
    public void testIsLoggedIn_WithValidSession_ReturnsTrue() {
        // Arrange
        User user = userRepository.registerUser("John Doe", "john@example.com", "password123");
        userRepository.saveSession(user);

        // Act
        boolean isLoggedIn = userRepository.isLoggedIn();

        // Assert
        assertTrue(isLoggedIn);
    }

    @Test
    public void testIsLoggedIn_WithoutSession_ReturnsFalse() {
        // Act
        boolean isLoggedIn = userRepository.isLoggedIn();

        // Assert
        assertFalse(isLoggedIn);
    }

    @Test
    public void testClearSession_WithSession_ReturnsTrue() {
        // Arrange
        User user = userRepository.registerUser("John Doe", "john@example.com", "password123");
        userRepository.saveSession(user);

        // Act
        boolean result = userRepository.clearSession();

        // Assert
        assertTrue(result);
        assertFalse(userRepository.isLoggedIn());
        assertNull(userRepository.getStoredSession());
    }

    @Test
    public void testClearSession_WithoutSession_ReturnsTrue() {
        // Act
        boolean result = userRepository.clearSession();

        // Assert
        assertTrue(result);
        assertFalse(userRepository.isLoggedIn());
    }

    @Test
    public void testRefreshSession_WithValidSession_ReturnsTrue() {
        // Arrange
        User user = userRepository.registerUser("John Doe", "john@example.com", "password123");
        userRepository.saveSession(user);
        
        UserSession originalSession = userRepository.getStoredSession();
        long originalTimestamp = originalSession.getLoginTimestamp();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean result = userRepository.refreshSession();

        // Assert
        assertTrue(result);
        UserSession refreshedSession = userRepository.getStoredSession();
        assertNotNull(refreshedSession);
        assertTrue(refreshedSession.getLoginTimestamp() >= originalTimestamp);
    }

    @Test
    public void testRefreshSession_WithoutSession_ReturnsFalse() {
        // Act
        boolean result = userRepository.refreshSession();

        // Assert
        assertFalse(result);
    }

    // Validation Tests

    @Test
    public void testIsValidEmail_ValidEmails_ReturnsTrue() {
        assertTrue(userRepository.isValidEmail("test@example.com"));
        assertTrue(userRepository.isValidEmail("user.name@domain.co.uk"));
        assertTrue(userRepository.isValidEmail("test123@test-domain.com"));
    }

    @Test
    public void testIsValidEmail_InvalidEmails_ReturnsFalse() {
        assertFalse(userRepository.isValidEmail(""));
        assertFalse(userRepository.isValidEmail("invalid-email"));
        assertFalse(userRepository.isValidEmail("@domain.com"));
        assertFalse(userRepository.isValidEmail("test@"));
        assertFalse(userRepository.isValidEmail(null));
    }

    @Test
    public void testIsValidPassword_ValidPasswords_ReturnsTrue() {
        assertTrue(userRepository.isValidPassword("password123"));
        assertTrue(userRepository.isValidPassword("MySecurePass"));
        assertTrue(userRepository.isValidPassword("123456"));
    }

    @Test
    public void testIsValidPassword_InvalidPasswords_ReturnsFalse() {
        assertFalse(userRepository.isValidPassword(""));
        assertFalse(userRepository.isValidPassword("12345")); // Too short
        assertFalse(userRepository.isValidPassword(null));
    }

    @Test
    public void testIsValidName_ValidNames_ReturnsTrue() {
        assertTrue(userRepository.isValidName("John Doe"));
        assertTrue(userRepository.isValidName("Jane"));
        assertTrue(userRepository.isValidName("Mary Jane Watson"));
    }

    @Test
    public void testIsValidName_InvalidNames_ReturnsFalse() {
        assertFalse(userRepository.isValidName(""));
        assertFalse(userRepository.isValidName("J")); // Too short
        assertFalse(userRepository.isValidName(null));
    }

    // Utility Tests

    @Test
    public void testGetUserCount_EmptyRepository_ReturnsZero() {
        // Act
        int count = userRepository.getUserCount();

        // Assert
        assertEquals(0, count);
    }

    @Test
    public void testGetUserCount_WithUsers_ReturnsCorrectCount() {
        // Arrange
        userRepository.registerUser("John Doe", "john@example.com", "password123");
        userRepository.registerUser("Jane Doe", "jane@example.com", "password456");

        // Act
        int count = userRepository.getUserCount();

        // Assert
        assertEquals(2, count);
    }

    @Test
    public void testIsFirstUser_EmptyRepository_ReturnsTrue() {
        // Act
        boolean isFirst = userRepository.isFirstUser();

        // Assert
        assertTrue(isFirst);
    }

    @Test
    public void testIsFirstUser_WithUsers_ReturnsFalse() {
        // Arrange
        userRepository.registerUser("John Doe", "john@example.com", "password123");

        // Act
        boolean isFirst = userRepository.isFirstUser();

        // Assert
        assertFalse(isFirst);
    }

    @Test
    public void testClearAllData_WithData_ClearsSuccessfully() {
        // Arrange
        User user = userRepository.registerUser("John Doe", "john@example.com", "password123");
        userRepository.saveSession(user);

        // Act
        boolean result = userRepository.clearAllData();

        // Assert
        assertTrue(result);
        assertEquals(0, userRepository.getUserCount());
        assertFalse(userRepository.isLoggedIn());
        assertNull(userRepository.getStoredSession());
    }

    // Session Expiration Tests

    @Test
    public void testSessionExpiration_ExpiredSession_ReturnsNull() {
        // Arrange
        User user = userRepository.registerUser("John Doe", "john@example.com", "password123");
        userRepository.saveSession(user);
        
        // Get the session and manually expire it
        UserSession session = userRepository.getStoredSession();
        assertNotNull(session);
        
        // Create an expired session by setting old timestamp
        UserSession expiredSession = new UserSession(
            session.getUserId(),
            session.getEmail(),
            session.getName(),
            System.currentTimeMillis() - (25 * 60 * 60 * 1000L), // 25 hours ago
            true
        );
        
        // Manually save the expired session
        SharedPreferences prefs = context.getSharedPreferences("sugboaid_auth", Context.MODE_PRIVATE);
        prefs.edit().putString("current_session", expiredSession.toJson()).apply();

        // Act
        UserSession retrievedSession = userRepository.getStoredSession();

        // Assert
        assertNull(retrievedSession); // Should return null for expired session
        assertFalse(userRepository.isLoggedIn()); // Should not be logged in
    }

    // Singleton Pattern Tests

    @Test
    public void testGetInstance_SameContext_ReturnsSameInstance() {
        // Act
        UserRepository instance1 = UserRepository.getInstance(context);
        UserRepository instance2 = UserRepository.getInstance(context);

        // Assert
        assertSame(instance1, instance2);
    }

    // Password Hashing Tests

    @Test
    public void testPasswordHashing_SamePassword_ProducesSameHash() {
        // Arrange
        String password = "password123";
        User user1 = userRepository.registerUser("John Doe", "john@example.com", password);
        User user2 = userRepository.registerUser("Jane Doe", "jane@example.com", password);

        // Assert
        assertNotNull(user1);
        assertNotNull(user2);
        assertEquals(user1.getPasswordHash(), user2.getPasswordHash());
    }

    @Test
    public void testPasswordHashing_DifferentPasswords_ProduceDifferentHashes() {
        // Arrange
        User user1 = userRepository.registerUser("John Doe", "john@example.com", "password123");
        User user2 = userRepository.registerUser("Jane Doe", "jane@example.com", "password456");

        // Assert
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotEquals(user1.getPasswordHash(), user2.getPasswordHash());
    }
}