package com.davon.library.service;

import com.davon.library.model.User;
import com.davon.library.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());
        testUser.setLoggedIn(false);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        // Given
        String email = "john@example.com";
        String password = "password123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.login(email, password);

        // Then
        assertTrue(result);
        assertTrue(testUser.isLoggedIn());
        assertNotNull(testUser.getLastLoginDate());
        
        // Verify repository was called
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should fail login with invalid password")
    void testLoginFailureInvalidPassword() {
        // Given
        String email = "john@example.com";
        String wrongPassword = "wrongpassword";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.login(email, wrongPassword);

        // Then
        assertFalse(result);
        assertFalse(testUser.isLoggedIn());
        assertNull(testUser.getLastLoginDate());
    }

    @Test
    @DisplayName("Should fail login with non-existent user")
    void testLoginFailureUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.login(email, password);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should fail login with null email")
    void testLoginFailureNullEmail() {
        // When
        boolean result = userService.login(null, "password123");

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should fail login with empty email")
    void testLoginFailureEmptyEmail() {
        // When
        boolean result = userService.login("", "password123");

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should fail login with null password")
    void testLoginFailureNullPassword() {
        // When
        boolean result = userService.login("john@example.com", null);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should logout successfully when user is logged in")
    void testLogoutSuccess() {
        // Given
        testUser.setLoggedIn(true);
        String email = "john@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.logout(email);

        // Then
        assertTrue(result);
        assertFalse(testUser.isLoggedIn());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should return false when logging out user who is not logged in")
    void testLogoutUserNotLoggedIn() {
        // Given
        testUser.setLoggedIn(false);
        String email = "john@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.logout(email);

        // Then
        assertFalse(result);
        assertFalse(testUser.isLoggedIn());
    }

    @Test
    @DisplayName("Should create user successfully with valid data")
    void testCreateUserSuccess() {
        // Given
        String name = "Jane Doe";
        String email = "jane@example.com";
        String password = "password123";
        String role = "USER";
        
        when(userRepository.emailExists(email)).thenReturn(false);

        // When
        User result = userService.createUser(name, email, password, role);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email.toLowerCase(), result.getEmail());
        assertEquals(password, result.getPassword());
        assertEquals(role, result.getRole());
        assertNotNull(result.getCreatedAt());
        assertFalse(result.isLoggedIn());
        
        verify(userRepository, times(1)).emailExists(email);
        verify(userRepository, times(1)).persist(any(User.class));
    }

    @Test
    @DisplayName("Should fail to create user with existing email")
    void testCreateUserFailureEmailExists() {
        // Given
        String name = "Jane Doe";
        String email = "existing@example.com";
        String password = "password123";
        String role = "USER";
        
        when(userRepository.emailExists(email)).thenReturn(true);

        // When
        User result = userService.createUser(name, email, password, role);

        // Then
        assertNull(result);
        verify(userRepository, times(1)).emailExists(email);
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    @DisplayName("Should fail to create user with invalid name")
    void testCreateUserFailureInvalidName() {
        // When
        User result = userService.createUser(null, "test@example.com", "password123", "USER");

        // Then
        assertNull(result);
        verify(userRepository, never()).emailExists(any());
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    @DisplayName("Should fail to create user with short password")
    void testCreateUserFailureShortPassword() {
        // When
        User result = userService.createUser("John Doe", "test@example.com", "123", "USER");

        // Then
        assertNull(result);
        verify(userRepository, never()).emailExists(any());
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    @DisplayName("Should update profile successfully")
    void testUpdateProfileSuccess() {
        // Given
        Long userId = 1L;
        String newName = "John Updated";
        String newEmail = "john.updated@example.com";
        
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());

        // When
        boolean result = userService.updateProfile(userId, newName, newEmail);

        // Then
        assertTrue(result);
        assertEquals(newName, testUser.getName());
        assertEquals(newEmail, testUser.getEmail());
        assertNotNull(testUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Should check user role correctly")
    void testHasRole() {
        // Given
        Long userId = 1L;
        testUser.setRole("ADMIN");
        
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertTrue(userService.hasRole(userId, "ADMIN"));
        assertTrue(userService.hasRole(userId, "admin")); // Case insensitive
        assertFalse(userService.hasRole(userId, "USER"));
    }

    @Test
    @DisplayName("Should check if user is admin")
    void testIsAdmin() {
        // Given
        Long userId = 1L;
        testUser.setRole("ADMIN");
        
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertTrue(userService.isAdmin(userId));
        
        // Test with non-admin user
        testUser.setRole("USER");
        assertFalse(userService.isAdmin(userId));
    }

    @Test
    @DisplayName("Should get user by email")
    void testGetUserByEmail() {
        // Given
        String email = "john@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should return empty for non-existent email")
    void testGetUserByEmailNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserByEmail(email);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should update login status")
    void testUpdateLoginStatus() {
        // Given
        Long userId = 1L;
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.updateLoginStatus(userId, true);

        // Then
        assertTrue(result);
        assertTrue(testUser.isLoggedIn());
        assertNotNull(testUser.getLastLoginDate());
    }

    // ===== ADDITIONAL MISSING TESTS =====

    @Test
    @DisplayName("Should fail logout with null email")
    void testLogoutFailureNullEmail() {
        // When
        boolean result = userService.logout(null);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should fail logout with empty email")
    void testLogoutFailureEmptyEmail() {
        // When
        boolean result = userService.logout("   ");

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should fail logout when user not found")
    void testLogoutFailureUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.logout(email);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should update profile with password successfully")
    void testUpdateProfileWithPasswordSuccess() {
        // Given
        Long userId = 1L;
        String newName = "John Updated";
        String newEmail = "john.updated@example.com";
        String newPassword = "newpassword123";
        
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());

        // When
        boolean result = userService.updateProfile(userId, newName, newEmail, newPassword);

        // Then
        assertTrue(result);
        assertEquals(newName, testUser.getName());
        assertEquals(newEmail, testUser.getEmail());
        assertEquals(newPassword, testUser.getPassword());
        assertNotNull(testUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Should fail to update profile with short password")
    void testUpdateProfileWithShortPasswordFailure() {
        // Given
        Long userId = 1L;
        String shortPassword = "123";
        
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.updateProfile(userId, "New Name", "new@email.com", shortPassword);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should update profile without password when password is null")
    void testUpdateProfileWithNullPassword() {
        // Given
        Long userId = 1L;
        String newName = "John Updated";
        String newEmail = "john.updated@example.com";
        
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());

        // When
        boolean result = userService.updateProfile(userId, newName, newEmail, null);

        // Then
        assertTrue(result);
        assertEquals(newName, testUser.getName());
        assertEquals(newEmail, testUser.getEmail());
        assertEquals("password123", testUser.getPassword()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should fail to update profile with null user ID")
    void testUpdateProfileFailureNullUserId() {
        // When
        boolean result = userService.updateProfile(null, "New Name", "new@email.com");

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to update profile when user not found")
    void testUpdateProfileFailureUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

        // When
        boolean result = userService.updateProfile(userId, "New Name", "new@email.com");

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).findByIdOptional(userId);
    }

    @Test
    @DisplayName("Should fail to update profile with invalid email format")
    void testUpdateProfileFailureInvalidEmail() {
        // Given
        Long userId = 1L;
        String invalidEmail = "invalid-email-format";
        
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.updateProfile(userId, "New Name", invalidEmail);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to update profile when email already exists for another user")
    void testUpdateProfileFailureEmailConflict() {
        // Given
        Long userId = 1L;
        String conflictEmail = "conflict@example.com";
        
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail(conflictEmail);
        
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(conflictEmail)).thenReturn(Optional.of(anotherUser));

        // When
        boolean result = userService.updateProfile(userId, "New Name", conflictEmail);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when updating login status with null user ID")
    void testUpdateLoginStatusFailureNullUserId() {
        // When
        boolean result = userService.updateLoginStatus(null, true);

        // Then
        assertFalse(result);
        verify(userRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should return false when updating login status for non-existent user")
    void testUpdateLoginStatusFailureUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

        // When
        boolean result = userService.updateLoginStatus(userId, true);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).findByIdOptional(userId);
    }

    @Test
    @DisplayName("Should handle logout status update correctly")
    void testUpdateLoginStatusLogout() {
        // Given
        Long userId = 1L;
        testUser.setLoggedIn(true);
        testUser.setLastLoginDate(new Date());
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.updateLoginStatus(userId, false);

        // Then
        assertTrue(result);
        assertFalse(testUser.isLoggedIn());
        // lastLoginDate should remain unchanged when logging out
        assertNotNull(testUser.getLastLoginDate());
    }

    @Test
    @DisplayName("Should handle getUserByEmail with null email")
    void testGetUserByEmailNullEmail() {
        // When
        Optional<User> result = userService.getUserByEmail(null);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should handle getUserByEmail with empty email")
    void testGetUserByEmailEmptyEmail() {
        // When
        Optional<User> result = userService.getUserByEmail("   ");

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Should handle hasRole with null user ID")
    void testHasRoleNullUserId() {
        // When & Then
        assertFalse(userService.hasRole(null, "ADMIN"));
        verify(userRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should handle hasRole with null required role")
    void testHasRoleNullRequiredRole() {
        // When & Then
        assertFalse(userService.hasRole(1L, null));
        verify(userRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should handle hasRole when user not found")
    void testHasRoleUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

        // When & Then
        assertFalse(userService.hasRole(userId, "ADMIN"));
        verify(userRepository, times(1)).findByIdOptional(userId);
    }

    @Test
    @DisplayName("Should handle hasRole when user has null role")
    void testHasRoleUserNullRole() {
        // Given
        Long userId = 1L;
        testUser.setRole(null);
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertFalse(userService.hasRole(userId, "ADMIN"));
    }

    @Test
    @DisplayName("Should handle isAdmin with null user ID")
    void testIsAdminNullUserId() {
        // When & Then
        assertFalse(userService.isAdmin(null));
        verify(userRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should create user with default role when role is null")
    void testCreateUserWithNullRole() {
        // Given
        String name = "Jane Doe";
        String email = "jane@example.com";
        String password = "password123";
        
        when(userRepository.emailExists(email)).thenReturn(false);

        // When
        User result = userService.createUser(name, email, password, null);

        // Then
        assertNotNull(result);
        assertEquals("USER", result.getRole()); // Should default to "USER"
        assertEquals(name, result.getName());
        assertEquals(email.toLowerCase(), result.getEmail());
    }

    @Test
    @DisplayName("Should fail to create user with empty name after trim")
    void testCreateUserFailureEmptyNameAfterTrim() {
        // When
        User result = userService.createUser("   ", "test@example.com", "password123", "USER");

        // Then
        assertNull(result);
        verify(userRepository, never()).emailExists(any());
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    @DisplayName("Should fail to create user with empty email after trim")
    void testCreateUserFailureEmptyEmailAfterTrim() {
        // When
        User result = userService.createUser("John Doe", "   ", "password123", "USER");

        // Then
        assertNull(result);
        verify(userRepository, never()).emailExists(any());
        verify(userRepository, never()).persist(any(User.class));
    }
} 