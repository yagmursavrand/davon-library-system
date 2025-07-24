package com.davon.library;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple manual test to verify testing framework works
 */
class UserManualTest {

    @Test
    @DisplayName("Should test basic functionality")
    void testBasicFunctionality() {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        
        // When
        boolean isValidEmail = email.contains("@");
        boolean isValidName = name != null && !name.trim().isEmpty();
        
        // Then
        assertTrue(isValidEmail);
        assertTrue(isValidName);
        assertEquals("John Doe", name);
        assertEquals("john@example.com", email);
    }

    @Test
    @DisplayName("Should test string operations")
    void testStringOperations() {
        // Given
        String input = "  Test String  ";
        
        // When
        String trimmed = input.trim();
        String upperCase = trimmed.toUpperCase();
        
        // Then
        assertEquals("Test String", trimmed);
        assertEquals("TEST STRING", upperCase);
        assertNotEquals(input, trimmed);
    }

    @Test
    @DisplayName("Should test boolean logic")
    void testBooleanLogic() {
        // Given
        boolean userExists = true;
        boolean passwordCorrect = true;
        boolean accountActive = true;
        
        // When
        boolean canLogin = userExists && passwordCorrect && accountActive;
        
        // Then
        assertTrue(canLogin);
        
        // Test failure case
        boolean accountInactive = false;
        boolean cannotLogin = userExists && passwordCorrect && accountInactive;
        assertFalse(cannotLogin);
    }
} 