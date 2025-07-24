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
class RealUserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user with Lombok working!
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
} 