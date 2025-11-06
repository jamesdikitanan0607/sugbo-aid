package com.sugboaid.viewmodels;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import com.sugboaid.models.AuthState;
import com.sugboaid.models.User;
import com.sugboaid.models.UserSession;
import com.sugboaid.repositories.UserRepository;
import com.sugboaid.utils.AuthErrorHandler;
import com.sugboaid.utils.ValidationUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthViewModel
 * Tests authentication logic, state management, and validation
 */
@RunWith(RobolectricTestRunner.class)
public class AuthViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private Observer<AuthState> authStateObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<String> errorMessageObserver;

    @Mock
    private Observer<String> successMessageObserver;

    @Mock
    private Observer<AuthErrorHandler.ErrorResult> errorResultObserver;

    private AuthViewModel authViewModel;
    private Application application;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        application = RuntimeEnvironment.getApplication();
        
        // Create AuthViewModel with mocked repository
        authViewModel = new AuthViewModel(application) {
            @Override
            public UserRepository getUserRepository() {
                return mockUserRepository;
            }
        };

        // Set up observers
        authViewModel.authState.observeForever(authStateObserver);
        authViewModel.isLoading.observeForever(loadingObserver);
        authViewModel.errorMessage.observeForever(errorMessageObserver);
        authViewModel.successMessage.observeForever(successMessageObserver);
        authViewModel.errorResult.observeForever(errorResultObserver);
    }

    // Authentication Status Tests

    @Test
    public void testCheckAuthenticationStatus_ValidSession_ReturnsTrue() {
        // Arrange
        User testUser = new User("Test User", "test@example.com", "hashedPassword");
        UserSession testSession = UserSession.fromUser(testUser);
        
        when(mockUserRepository.isLoggedIn()).thenReturn(true);
        when(mockUserRepository.getStoredSession()).thenReturn(testSession);
        when(mockUserRepository.getUserById(testUser.getId())).thenReturn(testUser);

        // Act
        boolean result = authViewModel.checkAuthenticationStatus();

        // Assert
        assertTrue(result);
        verify(authStateObserver).onChanged(argThat(authState -> 
            authState.isAuthenticated() && 
            authState.getUser().equals(testUser) &&
            authState.getSession().equals(testSession)
        ));
    }

    @Test
    public void testCheckAuthenticationStatus_NoSession_ReturnsFalse() {
        // Arrange
        when(mockUserRepository.isLoggedIn()).thenReturn(false);

        // Act
        boolean result = authViewModel.checkAuthenticationStatus();

        // Assert
        assertFalse(result);
        verify(authStateObserver).onChanged(argThat(authState -> !authState.isAuthenticated()));
    }

    @Test
    public void testCheckAuthenticationStatus_InvalidSession_ReturnsFalse() {
        // Arrange
        when(mockUserRepository.isLoggedIn()).thenReturn(true);
        when(mockUserRepository.getStoredSession()).thenReturn(null);

        // Act
        boolean result = authViewModel.checkAuthenticationStatus();

        // Assert
        assertFalse(result);
        verify(authStateObserver).onChanged(argThat(authState -> !authState.isAuthenticated()));
    }

    // Login Tests

    @Test
    public void testLogin_ValidCredentials_Success() throws InterruptedException {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        User testUser = new User("Test User", email, "hashedPassword");
        UserSession testSession = UserSession.fromUser(testUser);

        when(mockUserRepository.validateCredentials(email, password)).thenReturn(testUser);
        when(mockUserRepository.saveSession(testUser)).thenReturn(true);
        when(mockUserRepository.getStoredSession()).thenReturn(testSession);

        // Act
        authViewModel.login(email, password);
        Thread.sleep(100); // Wait for async operation

        // Assert
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(successMessageObserver).onChanged("Login successful");
        verify(authStateObserver).onChanged(argThat(authState -> 
            authState.isAuthenticated() && authState.getUser().equals(testUser)
        ));
    }

    @Test
    public void testLogin_InvalidCredentials_ShowsError() throws InterruptedException {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";

        when(mockUserRepository.validateCredentials(email, password)).thenReturn(null);

        // Act
        authViewModel.login(email, password);
        Thread.sleep(100); // Wait for async operation

        // Assert
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(any(AuthErrorHandler.ErrorResult.class));
        verify(errorMessageObserver).onChanged(anyString());
    }

    @Test
    public void testLogin_EmptyEmail_ShowsValidationError() {
        // Arrange
        String email = "";
        String password = "password123";

        // Act
        authViewModel.login(email, password);

        // Assert
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(argThat(error -> 
            error.getType() == AuthErrorHandler.ErrorType.VALIDATION_ERROR
        ));
        verify(errorMessageObserver).onChanged("Email is required");
    }

    @Test
    public void testLogin_EmptyPassword_ShowsValidationError() {
        // Arrange
        String email = "test@example.com";
        String password = "";

        // Act
        authViewModel.login(email, password);

        // Assert
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(argThat(error -> 
            error.getType() == AuthErrorHandler.ErrorType.VALIDATION_ERROR
        ));
        verify(errorMessageObserver).onChanged("Password is required");
    }

    @Test
    public void testLogin_InvalidEmailFormat_ShowsValidationError() {
        // Arrange
        String email = "invalid-email";
        String password = "password123";

        // Act
        authViewModel.login(email, password);

        // Assert
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(argThat(error -> 
            error.getType() == AuthErrorHandler.ErrorType.VALIDATION_ERROR
        ));
        verify(errorMessageObserver).onChanged("Please enter a valid email address");
    }

    // Signup Tests

    @Test
    public void testSignup_ValidData_Success() throws InterruptedException {
        // Arrange
        String name = "Test User";
        String email = "test@example.com";
        String password = "password123";
        String confirmPassword = "password123";
        User testUser = new User(name, email, "hashedPassword");
        UserSession testSession = UserSession.fromUser(testUser);

        when(mockUserRepository.registerUser(name, email, password)).thenReturn(testUser);
        when(mockUserRepository.saveSession(testUser)).thenReturn(true);
        when(mockUserRepository.getStoredSession()).thenReturn(testSession);

        // Act
        authViewModel.signup(name, email, password, confirmPassword);
        Thread.sleep(100); // Wait for async operation

        // Assert
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(successMessageObserver).onChanged("Account created successfully");
        verify(authStateObserver).onChanged(argThat(authState -> 
            authState.isAuthenticated() && authState.getUser().equals(testUser)
        ));
    }

    @Test
    public void testSignup_ExistingEmail_ShowsError() throws InterruptedException {
        // Arrange
        String name = "Test User";
        String email = "existing@example.com";
        String password = "password123";
        String confirmPassword = "password123";

        when(mockUserRepository.registerUser(name, email, password)).thenReturn(null);
        when(mockUserRepository.isEmailExists(email)).thenReturn(true);

        // Act
        authViewModel.signup(name, email, password, confirmPassword);
        Thread.sleep(100); // Wait for async operation

        // Assert
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(any(AuthErrorHandler.ErrorResult.class));
        verify(errorMessageObserver).onChanged("An account with this email already exists");
    }

    @Test
    public void testSignup_EmptyName_ShowsValidationError() {
        // Arrange
        String name = "";
        String email = "test@example.com";
        String password = "password123";
        String confirmPassword = "password123";

        // Act
        authViewModel.signup(name, email, password, confirmPassword);

        // Assert
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(argThat(error -> 
            error.getType() == AuthErrorHandler.ErrorType.VALIDATION_ERROR
        ));
        verify(errorMessageObserver).onChanged("Name is required");
    }

    @Test
    public void testSignup_ShortName_ShowsValidationError() {
        // Arrange
        String name = "A";
        String email = "test@example.com";
        String password = "password123";
        String confirmPassword = "password123";

        // Act
        authViewModel.signup(name, email, password, confirmPassword);

        // Assert
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(argThat(error -> 
            error.getType() == AuthErrorHandler.ErrorType.VALIDATION_ERROR
        ));
        verify(errorMessageObserver).onChanged("Name must be at least 2 characters");
    }

    @Test
    public void testSignup_WeakPassword_ShowsValidationError() {
        // Arrange
        String name = "Test User";
        String email = "test@example.com";
        String password = "123";
        String confirmPassword = "123";

        // Act
        authViewModel.signup(name, email, password, confirmPassword);

        // Assert
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(argThat(error -> 
            error.getType() == AuthErrorHandler.ErrorType.VALIDATION_ERROR
        ));
        verify(errorMessageObserver).onChanged("Password must be at least 6 characters");
    }

    @Test
    public void testSignup_PasswordMismatch_ShowsValidationError() {
        // Arrange
        String name = "Test User";
        String email = "test@example.com";
        String password = "password123";
        String confirmPassword = "differentpassword";

        // Act
        authViewModel.signup(name, email, password, confirmPassword);

        // Assert
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(argThat(error -> 
            error.getType() == AuthErrorHandler.ErrorType.VALIDATION_ERROR
        ));
        verify(errorMessageObserver).onChanged("Passwords do not match");
    }

    // Logout Tests

    @Test
    public void testLogout_Success() throws InterruptedException {
        // Arrange
        when(mockUserRepository.clearSession()).thenReturn(true);

        // Act
        authViewModel.logout();
        Thread.sleep(100); // Wait for async operation

        // Assert
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(successMessageObserver).onChanged("Logged out successfully");
        verify(authStateObserver).onChanged(argThat(authState -> !authState.isAuthenticated()));
    }

    @Test
    public void testLogout_Failure_ShowsError() throws InterruptedException {
        // Arrange
        when(mockUserRepository.clearSession()).thenReturn(false);

        // Act
        authViewModel.logout();
        Thread.sleep(100); // Wait for async operation

        // Assert
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(errorResultObserver).onChanged(argThat(error -> 
            error.getType() == AuthErrorHandler.ErrorType.SESSION_ERROR
        ));
        verify(errorMessageObserver).onChanged("Logout failed. Please try again.");
    }

    // Validation Tests

    @Test
    public void testValidateEmail_ValidEmails_ReturnsTrue() {
        assertTrue(authViewModel.validateEmail("test@example.com"));
        assertTrue(authViewModel.validateEmail("user.name@domain.co.uk"));
        assertTrue(authViewModel.validateEmail("test123@test-domain.com"));
    }

    @Test
    public void testValidateEmail_InvalidEmails_ReturnsFalse() {
        assertFalse(authViewModel.validateEmail(""));
        assertFalse(authViewModel.validateEmail("invalid-email"));
        assertFalse(authViewModel.validateEmail("@domain.com"));
        assertFalse(authViewModel.validateEmail("test@"));
        assertFalse(authViewModel.validateEmail(null));
    }

    @Test
    public void testValidatePassword_ValidPasswords_ReturnsTrue() {
        assertTrue(authViewModel.validatePassword("password123"));
        assertTrue(authViewModel.validatePassword("MySecurePass"));
        assertTrue(authViewModel.validatePassword("123456"));
    }

    @Test
    public void testValidatePassword_InvalidPasswords_ReturnsFalse() {
        assertFalse(authViewModel.validatePassword(""));
        assertFalse(authViewModel.validatePassword("12345")); // Too short
        assertFalse(authViewModel.validatePassword("pass word")); // Contains space
        assertFalse(authViewModel.validatePassword(null));
    }

    @Test
    public void testValidatePasswordMatch_MatchingPasswords_ReturnsTrue() {
        assertTrue(authViewModel.validatePasswordMatch("password", "password"));
        assertTrue(authViewModel.validatePasswordMatch("123456", "123456"));
    }

    @Test
    public void testValidatePasswordMatch_NonMatchingPasswords_ReturnsFalse() {
        assertFalse(authViewModel.validatePasswordMatch("password1", "password2"));
        assertFalse(authViewModel.validatePasswordMatch("password", ""));
        assertFalse(authViewModel.validatePasswordMatch("password", null));
    }

    @Test
    public void testValidateName_ValidNames_ReturnsTrue() {
        assertTrue(authViewModel.validateName("John Doe"));
        assertTrue(authViewModel.validateName("Jane"));
        assertTrue(authViewModel.validateName("Mary Jane Watson"));
    }

    @Test
    public void testValidateName_InvalidNames_ReturnsFalse() {
        assertFalse(authViewModel.validateName(""));
        assertFalse(authViewModel.validateName("J")); // Too short
        assertFalse(authViewModel.validateName(null));
    }

    // Detailed Validation Tests

    @Test
    public void testValidateEmailDetailed_ValidEmail_ReturnsValidResult() {
        ValidationUtils.ValidationResult result = authViewModel.validateEmailDetailed("test@example.com");
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    public void testValidateEmailDetailed_InvalidEmail_ReturnsErrorResult() {
        ValidationUtils.ValidationResult result = authViewModel.validateEmailDetailed("invalid-email");
        assertFalse(result.isValid());
        assertNotNull(result.getMessage());
    }

    @Test
    public void testValidatePasswordDetailed_ValidPassword_ReturnsValidResult() {
        ValidationUtils.ValidationResult result = authViewModel.validatePasswordDetailed("password123");
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    public void testValidatePasswordDetailed_InvalidPassword_ReturnsErrorResult() {
        ValidationUtils.ValidationResult result = authViewModel.validatePasswordDetailed("123");
        assertFalse(result.isValid());
        assertNotNull(result.getMessage());
    }

    @Test
    public void testGetPasswordStrength_WeakPassword_ReturnsWeakStrength() {
        ValidationUtils.PasswordStrength strength = authViewModel.getPasswordStrength("123456");
        assertEquals(ValidationUtils.PasswordStrength.Level.WEAK, strength.getLevel());
    }

    @Test
    public void testGetPasswordStrength_StrongPassword_ReturnsStrongStrength() {
        ValidationUtils.PasswordStrength strength = authViewModel.getPasswordStrength("Password1!");
        assertTrue(strength.getLevel().getValue() >= ValidationUtils.PasswordStrength.Level.STRONG.getValue());
    }

    // Form Validation Tests

    @Test
    public void testValidateLoginForm_ValidForm_ReturnsValidResult() {
        ValidationUtils.FormValidationResult result = authViewModel.validateLoginForm("test@example.com", "password123");
        assertTrue(result.isValid());
        assertTrue(result.getEmailResult().isValid());
        assertTrue(result.getPasswordResult().isValid());
    }

    @Test
    public void testValidateLoginForm_InvalidForm_ReturnsInvalidResult() {
        ValidationUtils.FormValidationResult result = authViewModel.validateLoginForm("invalid-email", "123");
        assertFalse(result.isValid());
        assertFalse(result.getEmailResult().isValid());
        assertFalse(result.getPasswordResult().isValid());
    }

    @Test
    public void testValidateSignupForm_ValidForm_ReturnsValidResult() {
        ValidationUtils.FormValidationResult result = authViewModel.validateSignupForm(
            "John Doe", "test@example.com", "password123", "password123");
        assertTrue(result.isValid());
        assertTrue(result.getNameResult().isValid());
        assertTrue(result.getEmailResult().isValid());
        assertTrue(result.getPasswordResult().isValid());
        assertTrue(result.getConfirmPasswordResult().isValid());
    }

    @Test
    public void testValidateSignupForm_InvalidForm_ReturnsInvalidResult() {
        ValidationUtils.FormValidationResult result = authViewModel.validateSignupForm(
            "J", "invalid-email", "123", "456");
        assertFalse(result.isValid());
        assertFalse(result.getNameResult().isValid());
        assertFalse(result.getEmailResult().isValid());
        assertFalse(result.getPasswordResult().isValid());
        assertFalse(result.getConfirmPasswordResult().isValid());
    }

    // Utility Methods Tests

    @Test
    public void testGetCurrentUser_Authenticated_ReturnsUser() {
        // Arrange
        User testUser = new User("Test User", "test@example.com", "hashedPassword");
        UserSession testSession = UserSession.fromUser(testUser);
        AuthState authState = AuthState.authenticated(testUser, testSession);
        
        when(mockUserRepository.isLoggedIn()).thenReturn(true);
        when(mockUserRepository.getStoredSession()).thenReturn(testSession);
        when(mockUserRepository.getUserById(testUser.getId())).thenReturn(testUser);
        
        authViewModel.checkAuthenticationStatus();

        // Act
        User currentUser = authViewModel.getCurrentUser();

        // Assert
        assertNotNull(currentUser);
        assertEquals(testUser, currentUser);
    }

    @Test
    public void testGetCurrentUser_NotAuthenticated_ReturnsNull() {
        // Arrange
        when(mockUserRepository.isLoggedIn()).thenReturn(false);
        authViewModel.checkAuthenticationStatus();

        // Act
        User currentUser = authViewModel.getCurrentUser();

        // Assert
        assertNull(currentUser);
    }

    @Test
    public void testIsAuthenticated_WithValidSession_ReturnsTrue() {
        // Arrange
        User testUser = new User("Test User", "test@example.com", "hashedPassword");
        UserSession testSession = UserSession.fromUser(testUser);
        
        when(mockUserRepository.isLoggedIn()).thenReturn(true);
        when(mockUserRepository.getStoredSession()).thenReturn(testSession);
        when(mockUserRepository.getUserById(testUser.getId())).thenReturn(testUser);
        
        authViewModel.checkAuthenticationStatus();

        // Act & Assert
        assertTrue(authViewModel.isAuthenticated());
    }

    @Test
    public void testIsAuthenticated_WithoutSession_ReturnsFalse() {
        // Arrange
        when(mockUserRepository.isLoggedIn()).thenReturn(false);
        authViewModel.checkAuthenticationStatus();

        // Act & Assert
        assertFalse(authViewModel.isAuthenticated());
    }

    @Test
    public void testClearMessages_ClearsAllMessages() {
        // Act
        authViewModel.clearMessages();

        // Assert
        verify(errorMessageObserver).onChanged(null);
        verify(successMessageObserver).onChanged(null);
        verify(errorResultObserver).onChanged(null);
    }

    @Test
    public void testRefreshSession_ValidSession_RefreshesSuccessfully() {
        // Arrange
        when(mockUserRepository.isLoggedIn()).thenReturn(true);
        when(mockUserRepository.refreshSession()).thenReturn(true);

        // Act
        authViewModel.refreshSession();

        // Assert
        verify(mockUserRepository).refreshSession();
        verify(mockUserRepository, times(2)).isLoggedIn(); // Once in refreshSession, once in checkAuthenticationStatus
    }
}