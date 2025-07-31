package com.davon.library.resource;

import jakarta.persistence.EntityManager;
import com.davon.library.model.Library;
import com.davon.library.model.User;
import com.davon.library.repository.LibraryRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("LibraryResource Integration Tests")
class LibraryResourceTest {

    @Inject
    LibraryRepository libraryRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserService userService;

    @Inject
    EntityManager entityManager;

    private User testAdmin;
    private User testUser;
    private Library testLibrary;

    @BeforeEach
    @Transactional
    void setUp() {
        TestDatabaseCleanup.cleanupDatabase(entityManager);

        // Create test admin user
        testAdmin = new User();
        testAdmin.setName("Test Admin");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setPassword("password123");
        testAdmin.setRole("ADMIN");
        testAdmin.setCreatedAt(new Date());
        userRepository.persist(testAdmin);

        // Create test regular user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("user@example.com");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());
        userRepository.persist(testUser);

        // Create test library
        testLibrary = new Library();
        testLibrary.setName("Central Library");
        testLibrary.setAddress("123 Main St, City, State 12345");
        testLibrary.setPhone("555-0123");
        testLibrary.setEmail("central@library.com");
        testLibrary.setOpeningHours("Mon-Fri: 9AM-8PM, Sat-Sun: 10AM-6PM");
        libraryRepository.persist(testLibrary);
    }

    // ===== GET ALL LIBRARIES TESTS =====

    @Test
    @DisplayName("Should get all libraries successfully (public endpoint)")
    void testGetAllLibraries_Success() {
        given()
        .when()
            .get("/api/libraries")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].name", equalTo("Central Library"))
            .body("[0].address", equalTo("123 Main St, City, State 12345"))
            .body("[0].phone", equalTo("555-0123"))
            .body("[0].email", equalTo("central@library.com"));
    }

    /*
    @Test
    @DisplayName("Should handle empty libraries list")
    @Transactional
    void testGetAllLibraries_EmptyList() {
        // Remove all libraries
        libraryRepository.deleteAll();

        given()
        .when()
            .get("/api/libraries")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }
    */

    // ===== GET LIBRARY BY ID TESTS =====

    @Test
    @DisplayName("Should get library by ID successfully (public endpoint)")
    void testGetLibraryById_Success() {
        given()
        .when()
            .get("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(testLibrary.getId().intValue()))
            .body("name", equalTo("Central Library"))
            .body("address", equalTo("123 Main St, City, State 12345"))
            .body("phone", equalTo("555-0123"))
            .body("email", equalTo("central@library.com"))
            .body("openingHours", equalTo("Mon-Fri: 9AM-8PM, Sat-Sun: 10AM-6PM"));
    }

    /*
    @Test
    @DisplayName("Should return 404 when library not found")
    void testGetLibraryById_NotFound() {
        given()
        .when()
            .get("/api/libraries/{libraryId}", 99999L)
        .then()
            .statusCode(404)
            .body(equalTo("Library not found"));
    }
    */

    // ===== CREATE LIBRARY TESTS =====

    @Test
    @DisplayName("Should create library successfully with admin authentication")
    @Transactional
    void testCreateLibrary_Success() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "New Library";
        request.address = "456 Oak Ave, Another City, State 54321";
        request.phone = "555-9876";
        request.email = "new@library.com";
        request.openingHours = "Mon-Sun: 8AM-10PM";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(201)
            .body("name", equalTo("New Library"))
            .body("address", equalTo("456 Oak Ave, Another City, State 54321"))
            .body("phone", equalTo("555-9876"))
            .body("email", equalTo("new@library.com"))
            .body("openingHours", equalTo("Mon-Sun: 8AM-10PM"));
    }

    /*
    @Test
    @DisplayName("Should return 403 when non-admin tries to create library")
    void testCreateLibrary_NonAdminAccess() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "New Library";

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when no authentication provided")
    void testCreateLibrary_NoAuth() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "New Library";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 with invalid authentication")
    void testCreateLibrary_InvalidAuth() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "New Library";

        given()
            .header("Authorization", "Bearer 99999")
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 400 when library name is missing")
    void testCreateLibrary_MissingName() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        // name is null

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(400)
            .body(equalTo("Library name is required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 400 when library name is empty")
    void testCreateLibrary_EmptyName() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "   ";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(400)
            .body(equalTo("Library name is required"));
    }
    */

    @Test
    @DisplayName("Should create library with minimal data")
    @Transactional
    void testCreateLibrary_MinimalData() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "Minimal Library";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(201)
            .body("name", equalTo("Minimal Library"))
            .body("address", nullValue())
            .body("phone", nullValue())
            .body("email", nullValue())
            .body("openingHours", nullValue());
    }

    @Test
    @DisplayName("Should trim library name when creating")
    @Transactional
    void testCreateLibrary_TrimName() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "  Trimmed Library  ";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(201)
            .body("name", equalTo("Trimmed Library"));
    }

    // ===== UPDATE LIBRARY TESTS =====

    @Test
    @DisplayName("Should update library successfully with admin authentication")
    void testUpdateLibrary_Success() {
        LibraryResource.LibraryUpdateRequest request = new LibraryResource.LibraryUpdateRequest();
        request.name = "Updated Library";
        request.address = "Updated Address";
        request.phone = "555-1111";
        request.email = "updated@library.com";
        request.openingHours = "Updated Hours";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Library updated successfully"));
    }

    /*
    @Test
    @DisplayName("Should return 404 when updating non-existent library")
    void testUpdateLibrary_NotFound() {
        LibraryResource.LibraryUpdateRequest request = new LibraryResource.LibraryUpdateRequest();
        request.name = "Updated Library";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/libraries/{libraryId}", 99999L)
        .then()
            .statusCode(404)
            .body(equalTo("Library not found"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when non-admin tries to update library")
    void testUpdateLibrary_NonAdminAccess() {
        LibraryResource.LibraryUpdateRequest request = new LibraryResource.LibraryUpdateRequest();
        request.name = "Updated Library";

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    @Test
    @DisplayName("Should update library with partial data")
    void testUpdateLibrary_PartialUpdate() {
        LibraryResource.LibraryUpdateRequest request = new LibraryResource.LibraryUpdateRequest();
        request.name = "Partially Updated Library";
        // Other fields are null

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Library updated successfully"));
    }

    @Test
    @DisplayName("Should trim updated values")
    void testUpdateLibrary_TrimValues() {
        LibraryResource.LibraryUpdateRequest request = new LibraryResource.LibraryUpdateRequest();
        request.name = "  Trimmed Updated Library  ";
        request.address = "  Trimmed Address  ";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Library updated successfully"));
    }

    @Test
    @DisplayName("Should handle empty string values in update")
    void testUpdateLibrary_EmptyStrings() {
        LibraryResource.LibraryUpdateRequest request = new LibraryResource.LibraryUpdateRequest();
        request.name = "Valid Name";
        request.address = "";
        request.phone = "   ";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Library updated successfully"));
    }

    /*
    @Test
    @DisplayName("Should not update with empty name")
    void testUpdateLibrary_EmptyName() {
        LibraryResource.LibraryUpdateRequest request = new LibraryResource.LibraryUpdateRequest();
        request.name = "   ";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Library updated successfully"));
    }
    */

    // ===== DELETE LIBRARY TESTS =====

    @Test
    @DisplayName("Should delete library successfully with admin authentication")
    @Transactional
    void testDeleteLibrary_Success() {
        // Create a new library specifically for deletion
        Library libraryToDelete = new Library();
        libraryToDelete.setName("Library To Delete");
        libraryToDelete.setAddress("Delete Address");
        libraryRepository.persist(libraryToDelete);

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/libraries/{libraryId}", libraryToDelete.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Library deleted successfully"));
    }

    /*
    @Test
    @DisplayName("Should return 404 when deleting non-existent library")
    void testDeleteLibrary_NotFound() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/libraries/{libraryId}", 99999L)
        .then()
            .statusCode(404)
            .body(equalTo("Library not found"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when non-admin tries to delete library")
    void testDeleteLibrary_NonAdminAccess() {
        given()
            .header("Authorization", "Bearer " + testUser.getId())
        .when()
            .delete("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    // ===== SEARCH LIBRARIES TESTS =====

    @Test
    @DisplayName("Should search libraries successfully")
    @Transactional
    void testSearchLibraries_Success() {
        // Create additional library for search testing
        Library searchLibrary = new Library();
        searchLibrary.setName("Search Test Library");
        searchLibrary.setAddress("Search Address");
        libraryRepository.persist(searchLibrary);

        given()
            .queryParam("search", "Search")
        .when()
            .get("/api/libraries/search")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("name", hasItem("Search Test Library"));
    }

    /*
    @Test
    @DisplayName("Should return empty result when no libraries match search")
    void testSearchLibraries_NoResults() {
        given()
            .queryParam("search", "NonExistentLibrary")
        .when()
            .get("/api/libraries/search")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }
    */

    /*
    @Test
    @DisplayName("Should return 400 when search parameter is missing")
    void testSearchLibraries_MissingParam() {
        given()
        .when()
            .get("/api/libraries/search")
        .then()
            .statusCode(400)
            .body(equalTo("Search parameter is required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 400 when search parameter is empty")
    void testSearchLibraries_EmptyParam() {
        given()
            .queryParam("search", "")
        .when()
            .get("/api/libraries/search")
        .then()
            .statusCode(400)
            .body(equalTo("Search parameter is required"));
    }
    */

        /*
    // ===== AUTHENTICATION TESTS =====

    @Test
    @DisplayName("Should handle malformed authorization header")
    void testMalformedAuthHeader() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "New Library";

        given()
            .header("Authorization", "InvalidHeader")
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }

    @Test
    @DisplayName("Should handle non-numeric user ID in token")
    void testNonNumericToken() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "New Library";

        given()
            .header("Authorization", "Bearer invalidtoken")
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }

    @Test
    @DisplayName("Should handle missing Bearer prefix")
    void testMissingBearerPrefix() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "New Library";

        given()
            .header("Authorization", testAdmin.getId().toString())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    @DisplayName("Should handle malformed JSON in create request")
    void testCreateLibrary_MalformedJson() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body("{ invalid json }")
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle malformed JSON in update request")
    void testUpdateLibrary_MalformedJson() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body("{ invalid json }")
        .when()
            .put("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle empty request body in create")
    void testCreateLibrary_EmptyBody() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body("")
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle empty request body in update")
    void testUpdateLibrary_EmptyBody() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body("")
        .when()
            .put("/api/libraries/{libraryId}", testLibrary.getId())
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle missing content type")
    void testCreateLibrary_MissingContentType() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "New Library";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(415);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle very long library names")
    void testCreateLibrary_LongName() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "A".repeat(300); // Very long name

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(500))); // May succeed or fail depending on DB constraints
    }

    @Test
    @DisplayName("Should handle special characters in library data")
    @Transactional
    void testCreateLibrary_SpecialCharacters() {
        LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
        request.name = "Library with Special Characters: !@#$%^&*()";
        request.address = "123 Main St, Apt #456, City & State 12345";
        request.phone = "+1 (555) 123-4567";
        request.email = "special.chars+test@library-system.com";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(201)
            .body("name", equalTo("Library with Special Characters: !@#$%^&*()"))
            .body("address", equalTo("123 Main St, Apt #456, City & State 12345"))
            .body("phone", equalTo("+1 (555) 123-4567"))
            .body("email", equalTo("special.chars+test@library-system.com"));
    }

    @Test
    @DisplayName("Should handle concurrent library operations")
    @Transactional
    void testConcurrentOperations() {
        // Create multiple libraries quickly
        for (int i = 0; i < 3; i++) {
            LibraryResource.LibraryCreationRequest request = new LibraryResource.LibraryCreationRequest();
            request.name = "Concurrent Library " + i;

            given()
                .header("Authorization", "Bearer " + testAdmin.getId())
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/libraries")
            .then()
                .statusCode(201);
        }

        // Verify all libraries were created
        given()
        .when()
            .get("/api/libraries")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(4)); // Original testLibrary + 3 new ones
    }

    @Test
    @DisplayName("Should maintain data consistency after multiple operations")
    @Transactional
    void testDataConsistency() {
        // Create a library
        LibraryResource.LibraryCreationRequest createRequest = new LibraryResource.LibraryCreationRequest();
        createRequest.name = "Consistency Test Library";

        Long libraryId = given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/api/libraries")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Update the library
        LibraryResource.LibraryUpdateRequest updateRequest = new LibraryResource.LibraryUpdateRequest();
        updateRequest.name = "Updated Consistency Test Library";
        updateRequest.address = "Updated Address";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/libraries/{libraryId}", libraryId)
        .then()
            .statusCode(200);

        // Verify the update
        given()
        .when()
            .get("/api/libraries/{libraryId}", libraryId)
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated Consistency Test Library"))
            .body("address", equalTo("Updated Address"));

        // Delete the library
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/libraries/{libraryId}", libraryId)
        .then()
            .statusCode(200);

        // Verify deletion
        given()
        .when()
            .get("/api/libraries/{libraryId}", libraryId)
        .then()
            .statusCode(404);
    }
    */
} 