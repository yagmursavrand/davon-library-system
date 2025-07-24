package com.davon.library;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple User model test without Lombok
 */
class SimpleUserModelTest {

    // Simple User class for testing (without Lombok)
    static class SimpleUser {
        private Long id;
        private String name;
        private String email;
        private String password;
        private String role;
        private boolean isLoggedIn;
        
        // Constructors
        public SimpleUser() {}
        
        public SimpleUser(String name, String email, String password, String role) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.isLoggedIn = false;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public boolean isLoggedIn() { return isLoggedIn; }
        public void setLoggedIn(boolean loggedIn) { this.isLoggedIn = loggedIn; }
        
        // Business logic methods
        public boolean login(String inputPassword) {
            if (inputPassword != null && inputPassword.equals(this.password)) {
                this.isLoggedIn = true;
                return true;
            }
            return false;
        }
        
        public void logout() {
            this.isLoggedIn = false;
        }
        
        public boolean hasRole(String requiredRole) {
            return this.role != null && this.role.equalsIgnoreCase(requiredRole);
        }
        
        public boolean isAdmin() {
            return hasRole("ADMIN");
        }
    }

    @Test
    @DisplayName("Should create user with constructor")
    void testUserCreation() {
        // Given & When
        SimpleUser user = new SimpleUser("John Doe", "john@example.com", "password123", "USER");
        
        // Then
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("USER", user.getRole());
        assertFalse(user.isLoggedIn());
    }

    @Test
    @DisplayName("Should login with correct password")
    void testLoginSuccess() {
        // Given
        SimpleUser user = new SimpleUser("John Doe", "john@example.com", "password123", "USER");
        
        // When
        boolean result = user.login("password123");
        
        // Then
        assertTrue(result);
        assertTrue(user.isLoggedIn());
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void testLoginFailure() {
        // Given
        SimpleUser user = new SimpleUser("John Doe", "john@example.com", "password123", "USER");
        
        // When
        boolean result = user.login("wrongpassword");
        
        // Then
        assertFalse(result);
        assertFalse(user.isLoggedIn());
    }

    @Test
    @DisplayName("Should logout user")
    void testLogout() {
        // Given
        SimpleUser user = new SimpleUser("John Doe", "john@example.com", "password123", "USER");
        user.setLoggedIn(true);
        
        // When
        user.logout();
        
        // Then
        assertFalse(user.isLoggedIn());
    }

    @Test
    @DisplayName("Should check user role")
    void testHasRole() {
        // Given
        SimpleUser user = new SimpleUser("John Doe", "john@example.com", "password123", "ADMIN");
        
        // When & Then
        assertTrue(user.hasRole("ADMIN"));
        assertTrue(user.hasRole("admin")); // Case insensitive
        assertFalse(user.hasRole("USER"));
    }

    @Test
    @DisplayName("Should check if user is admin")
    void testIsAdmin() {
        // Given
        SimpleUser adminUser = new SimpleUser("Admin User", "admin@example.com", "admin123", "ADMIN");
        SimpleUser regularUser = new SimpleUser("Regular User", "user@example.com", "user123", "USER");
        
        // When & Then
        assertTrue(adminUser.isAdmin());
        assertFalse(regularUser.isAdmin());
    }

    @Test
    @DisplayName("Should handle null password in login")
    void testLoginWithNullPassword() {
        // Given
        SimpleUser user = new SimpleUser("John Doe", "john@example.com", "password123", "USER");
        
        // When
        boolean result = user.login(null);
        
        // Then
        assertFalse(result);
        assertFalse(user.isLoggedIn());
    }

    @Test
    @DisplayName("Should handle null role in hasRole")
    void testHasRoleWithNullRole() {
        // Given
        SimpleUser user = new SimpleUser("John Doe", "john@example.com", "password123", null);
        
        // When & Then
        assertFalse(user.hasRole("ADMIN"));
        assertFalse(user.isAdmin());
    }
} 