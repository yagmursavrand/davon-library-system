package com.davon.library;

import com.davon.library.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple User model test without dependencies
 */
class SimpleUserTest {

    @Test
    @DisplayName("Should create User with Lombok generated methods")
    void testUserCreation() {
        // Given
        User user = new User();
        
        // When
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole("USER");
        user.setCreatedAt(new Date());
        user.setLoggedIn(false);
        
        // Then
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("USER", user.getRole());
        assertNotNull(user.getCreatedAt());
        assertFalse(user.isLoggedIn());
    }

    @Test
    @DisplayName("Should test User equals and hashCode")
    void testUserEquality() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setName("John Doe");
        user1.setEmail("john@example.com");
        
        User user2 = new User();
        user2.setId(1L);
        user2.setName("John Doe");
        user2.setEmail("john@example.com");
        
        User user3 = new User();
        user3.setId(2L);
        user3.setName("Jane Doe");
        user3.setEmail("jane@example.com");
        
        // Then - Lombok should generate equals/hashCode
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should test User toString")
    void testUserToString() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        
        // When
        String userString = user.toString();
        
        // Then - Lombok should generate toString
        assertNotNull(userString);
        assertTrue(userString.contains("John Doe"));
        assertTrue(userString.contains("john@example.com"));
    }

    @Test
    @DisplayName("Should test User constructors")
    void testUserConstructors() {
        // Test no-args constructor
        User user1 = new User();
        assertNotNull(user1);
        
        // Test all-args constructor (if Lombok generates it)
        Date now = new Date();
        User user2 = new User(1L, "John", "john@test.com", "pass", "USER", now, null, false, null, null, null);
        
        assertEquals(1L, user2.getId());
        assertEquals("John", user2.getName());
        assertEquals("john@test.com", user2.getEmail());
        assertEquals("pass", user2.getPassword());
        assertEquals("USER", user2.getRole());
        assertEquals(now, user2.getCreatedAt());
        assertFalse(user2.isLoggedIn());
    }
} 