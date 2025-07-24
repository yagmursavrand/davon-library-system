package com.davon.library.service;

import com.davon.library.model.Profile;
import com.davon.library.model.User;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("ProfileService Unit Tests")
class ProfileServiceTest {

    @Inject
    ProfileService profileService;

    private Profile testProfile;
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
        
        // Create test profile
        testProfile = new Profile();
        testProfile.setId(1L);
        testProfile.setUser(testUser);
        testProfile.setAddress("123 Main St, City, State 12345");
        testProfile.setPhone("1234567890");
        
        Calendar cal = Calendar.getInstance();
        cal.set(1990, Calendar.JANUARY, 1);
        testProfile.setBirthDate(cal.getTime());
        testProfile.setLastUpdated(new Date());
        
        testUser.setProfile(testProfile);
    }

    // ===== GET AGE TESTS =====

    @Test
    @DisplayName("Should calculate age correctly for valid birth date")
    void testGetAge_Success() {
        // Arrange
        Calendar birthCal = Calendar.getInstance();
        birthCal.set(1990, Calendar.JANUARY, 1);
        testProfile.setBirthDate(birthCal.getTime());

        // Act
        int age = profileService.getAge(testProfile);

        // Assert
        Calendar now = Calendar.getInstance();
        int expectedAge = now.get(Calendar.YEAR) - 1990;
        if (now.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
            expectedAge--;
        }
        assertEquals(expectedAge, age);
        assertTrue(age >= 0);
    }

    @Test
    @DisplayName("Should calculate age correctly for recent birth date")
    void testGetAge_RecentBirthDate() {
        // Arrange - 25 years old
        Calendar birthCal = Calendar.getInstance();
        birthCal.add(Calendar.YEAR, -25);
        testProfile.setBirthDate(birthCal.getTime());

        // Act
        int age = profileService.getAge(testProfile);

        // Assert
        assertTrue(age == 24 || age == 25); // Could be 24 or 25 depending on exact date
    }

    @Test
    @DisplayName("Should return -1 for null profile")
    void testGetAge_NullProfile() {
        // Act
        int age = profileService.getAge(null);

        // Assert
        assertEquals(-1, age);
    }

    @Test
    @DisplayName("Should return -1 for profile with null birth date")
    void testGetAge_NullBirthDate() {
        // Arrange
        testProfile.setBirthDate(null);

        // Act
        int age = profileService.getAge(testProfile);

        // Assert
        assertEquals(-1, age);
    }

    @Test
    @DisplayName("Should calculate age correctly for leap year birth date")
    void testGetAge_LeapYearBirthDate() {
        // Arrange - Born on leap day 2000
        Calendar birthCal = Calendar.getInstance();
        birthCal.set(2000, Calendar.FEBRUARY, 29);
        testProfile.setBirthDate(birthCal.getTime());

        // Act
        int age = profileService.getAge(testProfile);

        // Assert
        Calendar now = Calendar.getInstance();
        int expectedAge = now.get(Calendar.YEAR) - 2000;
        assertTrue(age >= expectedAge - 1 && age <= expectedAge);
    }

    // ===== PROFILE COMPLETION TESTS =====

    @Test
    @DisplayName("Should return true for complete profile")
    void testIsProfileComplete_Complete() {
        // Act
        boolean isComplete = profileService.isProfileComplete(testProfile);

        // Assert
        assertTrue(isComplete);
    }

    @Test
    @DisplayName("Should return false for profile with missing address")
    void testIsProfileComplete_MissingAddress() {
        // Arrange
        testProfile.setAddress(null);

        // Act
        boolean isComplete = profileService.isProfileComplete(testProfile);

        // Assert
        assertFalse(isComplete);
    }

    @Test
    @DisplayName("Should return false for profile with empty address")
    void testIsProfileComplete_EmptyAddress() {
        // Arrange
        testProfile.setAddress("   ");

        // Act
        boolean isComplete = profileService.isProfileComplete(testProfile);

        // Assert
        assertFalse(isComplete);
    }

    @Test
    @DisplayName("Should return false for profile with missing phone")
    void testIsProfileComplete_MissingPhone() {
        // Arrange
        testProfile.setPhone(null);

        // Act
        boolean isComplete = profileService.isProfileComplete(testProfile);

        // Assert
        assertFalse(isComplete);
    }

    @Test
    @DisplayName("Should return false for profile with empty phone")
    void testIsProfileComplete_EmptyPhone() {
        // Arrange
        testProfile.setPhone("   ");

        // Act
        boolean isComplete = profileService.isProfileComplete(testProfile);

        // Assert
        assertFalse(isComplete);
    }

    @Test
    @DisplayName("Should return false for profile with missing birth date")
    void testIsProfileComplete_MissingBirthDate() {
        // Arrange
        testProfile.setBirthDate(null);

        // Act
        boolean isComplete = profileService.isProfileComplete(testProfile);

        // Assert
        assertFalse(isComplete);
    }

    @Test
    @DisplayName("Should return false for null profile")
    void testIsProfileComplete_NullProfile() {
        // Act
        boolean isComplete = profileService.isProfileComplete(null);

        // Assert
        assertFalse(isComplete);
    }

    @Test
    @DisplayName("Should return true for profile with minimal valid data")
    void testIsProfileComplete_MinimalValidData() {
        // Arrange
        testProfile.setAddress("123 Main St");
        testProfile.setPhone("1234567890");
        testProfile.setBirthDate(new Date());

        // Act
        boolean isComplete = profileService.isProfileComplete(testProfile);

        // Assert
        assertTrue(isComplete);
    }

    // ===== FORMATTED PHONE TESTS =====

    @Test
    @DisplayName("Should format 10-digit phone number correctly")
    void testGetFormattedPhone_TenDigits() {
        // Arrange
        testProfile.setPhone("1234567890");

        // Act
        String formatted = profileService.getFormattedPhone(testProfile);

        // Assert
        assertEquals("(123) 456-7890", formatted);
    }

    @Test
    @DisplayName("Should format phone with existing formatting")
    void testGetFormattedPhone_WithExistingFormatting() {
        // Arrange
        testProfile.setPhone("(123) 456-7890");

        // Act
        String formatted = profileService.getFormattedPhone(testProfile);

        // Assert
        assertEquals("(123) 456-7890", formatted);
    }

    @Test
    @DisplayName("Should format phone with dashes")
    void testGetFormattedPhone_WithDashes() {
        // Arrange
        testProfile.setPhone("123-456-7890");

        // Act
        String formatted = profileService.getFormattedPhone(testProfile);

        // Assert
        assertEquals("(123) 456-7890", formatted);
    }

    @Test
    @DisplayName("Should return original phone for non-10-digit numbers")
    void testGetFormattedPhone_NonTenDigits() {
        // Arrange
        String originalPhone = "123-456-7890-123";
        testProfile.setPhone(originalPhone);

        // Act
        String formatted = profileService.getFormattedPhone(testProfile);

        // Assert
        assertEquals(originalPhone, formatted);
    }

    @Test
    @DisplayName("Should return original phone for short numbers")
    void testGetFormattedPhone_ShortNumber() {
        // Arrange
        String shortPhone = "12345";
        testProfile.setPhone(shortPhone);

        // Act
        String formatted = profileService.getFormattedPhone(testProfile);

        // Assert
        assertEquals(shortPhone, formatted);
    }

    @Test
    @DisplayName("Should return null for null profile")
    void testGetFormattedPhone_NullProfile() {
        // Act
        String formatted = profileService.getFormattedPhone(null);

        // Assert
        assertNull(formatted);
    }

    @Test
    @DisplayName("Should return null for profile with null phone")
    void testGetFormattedPhone_NullPhone() {
        // Arrange
        testProfile.setPhone(null);

        // Act
        String formatted = profileService.getFormattedPhone(testProfile);

        // Assert
        assertNull(formatted);
    }

    @Test
    @DisplayName("Should handle phone with spaces and special characters")
    void testGetFormattedPhone_WithSpecialCharacters() {
        // Arrange
        testProfile.setPhone("1 2 3 - 4 5 6 - 7 8 9 0");

        // Act
        String formatted = profileService.getFormattedPhone(testProfile);

        // Assert
        assertEquals("(123) 456-7890", formatted);
    }

    // ===== CREATE PROFILE TESTS =====

    @Test
    @DisplayName("Should create profile successfully")
    void testCreateProfile_Success() {
        // Arrange
        User newUser = new User();
        newUser.setId(2L);
        newUser.setName("Jane Smith");
        newUser.setEmail("jane@example.com");
        newUser.setProfile(null);
        
        String address = "456 Elm St, City, State 67890";
        String phone = "9876543210";
        Calendar cal = Calendar.getInstance();
        cal.set(1995, Calendar.JULY, 10);
        Date birthDate = cal.getTime();

        // Act
        Profile createdProfile = profileService.createProfile(newUser, address, phone, birthDate);

        // Assert
        assertNotNull(createdProfile);
        assertEquals(newUser, createdProfile.getUser());
        assertEquals(address, createdProfile.getAddress());
        assertEquals(phone, createdProfile.getPhone());
        assertEquals(birthDate, createdProfile.getBirthDate());
        assertNotNull(createdProfile.getLastUpdated());
        assertEquals(createdProfile, newUser.getProfile());
    }

    @Test
    @DisplayName("Should create profile with null optional fields")
    void testCreateProfile_NullOptionalFields() {
        // Arrange
        User newUser = new User();
        newUser.setId(3L);
        newUser.setName("Bob Johnson");
        newUser.setEmail("bob@example.com");
        newUser.setProfile(null);

        // Act
        Profile createdProfile = profileService.createProfile(newUser, null, null, null);

        // Assert
        assertNotNull(createdProfile);
        assertEquals(newUser, createdProfile.getUser());
        assertNull(createdProfile.getAddress());
        assertNull(createdProfile.getPhone());
        assertNull(createdProfile.getBirthDate());
        assertNotNull(createdProfile.getLastUpdated());
        assertEquals(createdProfile, newUser.getProfile());
    }

    @Test
    @DisplayName("Should return null when creating profile for null user")
    void testCreateProfile_NullUser() {
        // Act
        Profile createdProfile = profileService.createProfile(null, "Address", "Phone", new Date());

        // Assert
        assertNull(createdProfile);
    }

    @Test
    @DisplayName("Should return existing profile when user already has one")
    void testCreateProfile_UserAlreadyHasProfile() {
        // Arrange
        testUser.setProfile(testProfile);

        // Act
        Profile createdProfile = profileService.createProfile(testUser, "New Address", "New Phone", new Date());

        // Assert
        assertEquals(testProfile, createdProfile);
    }

    // ===== DISPLAY PROFILE TESTS =====

    @Test
    @DisplayName("Should display profile without throwing exception")
    void testDisplayProfile_Success() {
        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> profileService.displayProfile(testProfile));
    }

    @Test
    @DisplayName("Should handle null profile in display")
    void testDisplayProfile_NullProfile() {
        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> profileService.displayProfile(null));
    }

    @Test
    @DisplayName("Should display incomplete profile without throwing exception")
    void testDisplayProfile_IncompleteProfile() {
        // Arrange
        testProfile.setAddress(null);
        testProfile.setPhone(null);
        testProfile.setBirthDate(null);

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> profileService.displayProfile(testProfile));
    }

    @Test
    @DisplayName("Should display profile with minimal data")
    void testDisplayProfile_MinimalData() {
        // Arrange
        Profile minimalProfile = new Profile();
        minimalProfile.setUser(testUser);

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> profileService.displayProfile(minimalProfile));
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle profile with very old birth date")
    void testGetAge_VeryOldBirthDate() {
        // Arrange - Born in 1900
        Calendar birthCal = Calendar.getInstance();
        birthCal.set(1900, Calendar.JANUARY, 1);
        testProfile.setBirthDate(birthCal.getTime());

        // Act
        int age = profileService.getAge(testProfile);

        // Assert
        assertTrue(age > 100);
        assertTrue(age < 150); // Reasonable upper bound
    }

    @Test
    @DisplayName("Should handle profile completion with whitespace-only fields")
    void testIsProfileComplete_WhitespaceFields() {
        // Arrange
        testProfile.setAddress("   \t\n   ");
        testProfile.setPhone("   \t\n   ");

        // Act
        boolean isComplete = profileService.isProfileComplete(testProfile);

        // Assert
        assertFalse(isComplete);
    }

    @Test
    @DisplayName("Should format international phone number")
    void testGetFormattedPhone_InternationalNumber() {
        // Arrange
        testProfile.setPhone("+1-234-567-8901");

        // Act
        String formatted = profileService.getFormattedPhone(testProfile);

        // Assert
        // Should return original since it's not exactly 10 digits after cleaning
        assertEquals("+1-234-567-8901", formatted);
    }
} 