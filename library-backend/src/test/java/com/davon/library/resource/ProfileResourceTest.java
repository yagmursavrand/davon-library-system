package com.davon.library.resource;

import jakarta.persistence.EntityManager;
import com.davon.library.model.Profile;
import com.davon.library.model.User;
import com.davon.library.repository.ProfileRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Calendar;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("ProfileResource Integration Tests")
class ProfileResourceTest {

    @Inject
    ProfileRepository profileRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserService userService;

    @Inject
    EntityManager entityManager;

    private User testUser;
    private User adminUser;
    private Profile testProfile;

    @BeforeEach
    @Transactional
    void setUp() {
        TestDatabaseCleanup.cleanupDatabase(entityManager);

        // Create test user
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());
        userRepository.persist(testUser);

        // Create admin user
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("adminpass123");
        adminUser.setRole("ADMIN");
        adminUser.setCreatedAt(new Date());
        userRepository.persist(adminUser);

        // Create test profile
        testProfile = new Profile();
        testProfile.setUser(testUser);
        testProfile.setAddress("123 Main St, City, State 12345");
        testProfile.setPhone("1234567890");
        
        Calendar cal = Calendar.getInstance();
        cal.set(1990, Calendar.JANUARY, 1);
        testProfile.setBirthDate(cal.getTime());
        testProfile.setLastUpdated(new Date());
        
        profileRepository.persist(testProfile);
        testUser.setProfile(testProfile);
    }

    // ===== GET PROFILE TESTS =====

    @Test
    @DisplayName("Should get profile successfully for authenticated user")
    void testGetProfileByUserId_Success() {
        given()
            .header("Authorization", "Bearer " + testUser.getId())
        .when()
            .get("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(testProfile.getId().intValue()))
            .body("address", equalTo(testProfile.getAddress()))
            .body("phone", equalTo(testProfile.getPhone()));
    }

    @Test
    @DisplayName("Should get profile successfully for admin user")
    void testGetProfileByUserId_AdminAccess() {
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .get("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(testProfile.getId().intValue()))
            .body("address", equalTo(testProfile.getAddress()));
    }

    /*
    @Test
    @DisplayName("Should return 401 when not authenticated")
    void testGetProfileByUserId_Unauthorized() {
        given()
        .when()
            .get("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(401)
            .body(equalTo("Authentication required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 401 with invalid auth header")
    void testGetProfileByUserId_InvalidAuth() {
        given()
            .header("Authorization", "InvalidToken")
        .when()
            .get("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(401)
            .body(equalTo("Authentication required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when user tries to access another user's profile")
    @Transactional
    void testGetProfileByUserId_Forbidden() {
        // Create another user
        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password123");
        anotherUser.setRole("USER");
        anotherUser.setCreatedAt(new Date());
        userRepository.persist(anotherUser);

        given()
            .header("Authorization", "Bearer " + anotherUser.getId())
        .when()
            .get("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(403)
            .body(equalTo("Access denied"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 404 when profile not found")
    @Transactional
    void testGetProfileByUserId_NotFound() {
        // Create user without profile
        User userWithoutProfile = new User();
        userWithoutProfile.setName("No Profile User");
        userWithoutProfile.setEmail("noprofile@example.com");
        userWithoutProfile.setPassword("password123");
        userWithoutProfile.setRole("USER");
        userWithoutProfile.setCreatedAt(new Date());
        userRepository.persist(userWithoutProfile);

        given()
            .header("Authorization", "Bearer " + userWithoutProfile.getId())
        .when()
            .get("/api/profiles/user/{userId}", userWithoutProfile.getId())
        .then()
            .statusCode(404)
            .body(equalTo("Profile not found"));
    }
    */

    // ===== UPDATE PROFILE TESTS =====

    @Test
    @DisplayName("Should update profile successfully")
    @Transactional
    void testUpdateProfile_Success() {
        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.address = "456 Oak Ave, New City, State 54321";
        request.phone = "9876543210";
        
        Calendar cal = Calendar.getInstance();
        cal.set(1985, Calendar.JUNE, 15);
        request.birthDate = cal.getTime();

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Profile updated successfully"));
    }

    @Test
    @DisplayName("Should create new profile when user has none")
    @Transactional
    void testUpdateProfile_CreateNew() {
        // Create user without profile
        User userWithoutProfile = new User();
        userWithoutProfile.setName("No Profile User");
        userWithoutProfile.setEmail("noprofile@example.com");
        userWithoutProfile.setPassword("password123");
        userWithoutProfile.setRole("USER");
        userWithoutProfile.setCreatedAt(new Date());
        userRepository.persist(userWithoutProfile);

        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.address = "789 Pine St, Another City, State 98765";
        request.phone = "5555551234";
        
        Calendar cal = Calendar.getInstance();
        cal.set(1988, Calendar.MARCH, 20);
        request.birthDate = cal.getTime();

        given()
            .header("Authorization", "Bearer " + userWithoutProfile.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", userWithoutProfile.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Profile updated successfully"));
    }

    /*
    @Test
    @DisplayName("Should return 401 when updating without authentication")
    void testUpdateProfile_Unauthorized() {
        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.address = "New Address";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(401)
            .body(equalTo("Authentication required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when user tries to update another user's profile")
    @Transactional
    void testUpdateProfile_Forbidden() {
        // Create another user
        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password123");
        anotherUser.setRole("USER");
        anotherUser.setCreatedAt(new Date());
        userRepository.persist(anotherUser);

        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.address = "Unauthorized Address";

        given()
            .header("Authorization", "Bearer " + anotherUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(403)
            .body(equalTo("Access denied"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when updating profile for non-existent user (user can't access other profiles)")
    void testUpdateProfile_UserNotFound() {
        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.address = "New Address";

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", 99999L)
        .then()
            .statusCode(403) // Changed from 404 to 403 as user can't access other profiles
            .body(equalTo("Access denied"));
    }
    */

    @Test
    @DisplayName("Should update profile with partial data")
    @Transactional
    void testUpdateProfile_PartialUpdate() {
        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.phone = "5551234567"; // Only update phone

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Profile updated successfully"));
    }

    @Test
    @DisplayName("Should handle empty string values correctly")
    @Transactional
    void testUpdateProfile_EmptyStrings() {
        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.phone = ""; // Empty string should be converted to null
        request.address = "   "; // Whitespace should be converted to null

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Profile updated successfully"));
    }

    /*
    @Test
    @DisplayName("Should return 500 when authentication token is invalid format")
    void testUpdateProfile_InvalidTokenFormat() {
        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.address = "New Address";

        given()
            .header("Authorization", "Bearer invalidtoken")
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(401)
            .body(equalTo("Authentication required"));
    }
    */

    /*
    // ===== ERROR HANDLING TESTS =====

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void testUpdateProfile_MalformedJson() {
        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body("{invalid json}")
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(500))); // Either bad request or server error is acceptable
    }

    @Test
    @DisplayName("Should handle missing content type")
    void testUpdateProfile_MissingContentType() {
        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.address = "New Address";

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .body(request) // No content type specified
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(anyOf(equalTo(415), equalTo(400), equalTo(500))); // Unsupported media type or other error
    }

    @Test
    @DisplayName("Should handle authentication with non-numeric user ID")
    void testGetProfile_NonNumericUserId() {
        given()
            .header("Authorization", "Bearer abc123")
        .when()
            .get("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(401)
            .body(equalTo("Authentication required"));
    }

    @Test
    @DisplayName("Should handle very long address")
    @Transactional
    void testUpdateProfile_LongAddress() {
        ProfileResource.ProfileUpdateRequest request = new ProfileResource.ProfileUpdateRequest();
        request.address = "A".repeat(600); // Very long address

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(500))); // Could succeed or fail depending on validation
    }

    @Test
    @DisplayName("Should handle null request body")
    void testUpdateProfile_NullBody() {
        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
        .when()
            .put("/api/profiles/user/{userId}", testUser.getId())
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(500))); // Bad request or server error
    }
    */
} 