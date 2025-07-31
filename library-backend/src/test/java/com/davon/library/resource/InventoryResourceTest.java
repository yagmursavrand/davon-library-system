package com.davon.library.resource;

import jakarta.persistence.EntityManager;
import com.davon.library.model.Inventory;
import com.davon.library.model.User;
import com.davon.library.model.Book;
import com.davon.library.model.Library;
import com.davon.library.repository.InventoryRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.LibraryRepository;
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
@DisplayName("InventoryResource Integration Tests")
public class InventoryResourceTest {

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    BookRepository bookRepository;

    @Inject
    LibraryRepository libraryRepository;

    @Inject
    UserService userService;

    @Inject
    EntityManager entityManager;

    private User adminUser;
    private User regularUser;
    private Inventory testInventory;
    private Book testBook;
    private Library testLibrary;

    @BeforeEach
    @Transactional
    void setUp() {
        TestDatabaseCleanup.cleanupDatabase(entityManager);

        // Create test admin user
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("admin123");
        adminUser.setRole("ADMIN");
        adminUser.setCreatedAt(new Date());
        userRepository.persist(adminUser);

        // Create test regular user
        regularUser = new User();
        regularUser.setName("Regular User");
        regularUser.setEmail("user@test.com");
        regularUser.setPassword("user123");
        regularUser.setRole("USER");
        regularUser.setCreatedAt(new Date());
        userRepository.persist(regularUser);

        // Create test library
        testLibrary = new Library();
        testLibrary.setName("Test Library");
        testLibrary.setAddress("123 Test St");
        testLibrary.setPhone("555-0123");
        libraryRepository.persist(testLibrary);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        bookRepository.persist(testBook);

        // Create test inventory
        testInventory = new Inventory();
        testInventory.setBook(testBook);
        testInventory.setLibrary(testLibrary);
        testInventory.setTotalCopies(10);
        testInventory.setAvailableCopies(8);
        testInventory.setReservedCopies(1);
        testInventory.setDamagedCopies(1);
        inventoryRepository.persist(testInventory);
    }

    // ===== GET ALL INVENTORY TESTS =====

    @Test
    @DisplayName("Should get all inventory items with admin authentication")
    void testGetAllInventory_AdminAuth_Success() {
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].totalCopies", equalTo(10))
            .body("[0].availableCopies", equalTo(8));
    }

    /*
    @Test
    @DisplayName("Should fail to get inventory without authentication")
    void testGetAllInventory_NoAuth_Forbidden() {
        given()
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should fail to get inventory with regular user authentication")
    void testGetAllInventory_RegularUserAuth_Forbidden() {
        given()
            .header("Authorization", "Bearer " + regularUser.getId())
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should fail to get inventory with invalid auth token")
    void testGetAllInventory_InvalidAuth_Forbidden() {
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should fail to get inventory with malformed auth header")
    void testGetAllInventory_MalformedAuth_Forbidden() {
        given()
            .header("Authorization", "InvalidFormat " + adminUser.getId())
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
    */

    // ===== GET INVENTORY BY ID TESTS =====

    @Test
    @DisplayName("Should get inventory by ID with admin authentication")
    void testGetInventoryById_AdminAuth_Success() {
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
        .when()
            .get("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("totalCopies", equalTo(10))
            .body("availableCopies", equalTo(8))
            .body("reservedCopies", equalTo(1))
            .body("damagedCopies", equalTo(1));
    }

    /*
    @Test
    @DisplayName("Should fail to get inventory by ID without authentication")
    void testGetInventoryById_NoAuth_Forbidden() {
        given()
            .pathParam("inventoryId", testInventory.getId())
        .when()
            .get("/api/inventory/{inventoryId}")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should fail to get inventory by ID with regular user authentication")
    void testGetInventoryById_RegularUserAuth_Forbidden() {
        given()
            .header("Authorization", "Bearer " + regularUser.getId())
            .pathParam("inventoryId", testInventory.getId())
        .when()
            .get("/api/inventory/{inventoryId}")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 404 when inventory not found")
    void testGetInventoryById_NotFound() {
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", 99999L)
        .when()
            .get("/api/inventory/{inventoryId}")
        .then()
            .statusCode(404)
            .body(containsString("Inventory not found"));
    }
    */

    // ===== UPDATE INVENTORY TESTS =====

    @Test
    @DisplayName("Should update inventory with admin authentication")
    void testUpdateInventory_AdminAuth_Success() {
        String updateRequest = """
            {
                "totalCopies": 15,
                "availableCopies": 12,
                "reservedCopies": 2,
                "damagedCopies": 1
            }
            """;

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200)
            .body(containsString("Inventory updated successfully"));
    }

    @Test
    @DisplayName("Should update inventory with partial data")
    void testUpdateInventory_PartialUpdate_Success() {
        String updateRequest = """
            {
                "totalCopies": 20,
                "availableCopies": 18
            }
            """;

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200)
            .body(containsString("Inventory updated successfully"));
    }

    /*
    @Test
    @DisplayName("Should fail to update inventory without authentication")
    void testUpdateInventory_NoAuth_Forbidden() {
        String updateRequest = """
            {
                "totalCopies": 15
            }
            """;

        given()
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should fail to update inventory with regular user authentication")
    void testUpdateInventory_RegularUserAuth_Forbidden() {
        String updateRequest = """
            {
                "totalCopies": 15
            }
            """;

        given()
            .header("Authorization", "Bearer " + regularUser.getId())
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 404 when updating non-existent inventory")
    void testUpdateInventory_NotFound() {
        String updateRequest = """
            {
                "totalCopies": 15
            }
            """;

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", 99999L)
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(404)
            .body(containsString("Inventory not found"));
    }
    */

    /*
    @Test
    @DisplayName("Should handle empty update request")
    void testUpdateInventory_EmptyRequest_Success() {
        String updateRequest = "{}";

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200)
            .body(containsString("Inventory updated successfully"));
    }
    */

    /*
    @Test
    @DisplayName("Should handle malformed JSON in update request")
    void testUpdateInventory_MalformedJSON_BadRequest() {
        String malformedRequest = "{invalid json}";

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(malformedRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(400);
    }
    */

    /*
    // ===== AUTHENTICATION EDGE CASES =====

    @Test
    @DisplayName("Should handle non-numeric user ID in auth token")
    void testAuthentication_NonNumericUserId_Forbidden() {
        given()
            .header("Authorization", "Bearer non-numeric-id")
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should handle non-existent user ID in auth token")
    void testAuthentication_NonExistentUserId_Forbidden() {
        given()
            .header("Authorization", "Bearer 99999")
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should handle empty auth token")
    void testAuthentication_EmptyToken_Forbidden() {
        given()
            .header("Authorization", "Bearer ")
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    @DisplayName("Should handle database errors gracefully")
    void testDatabaseError_Handling() {
        // This test would require mocking database failures
        // For now, we test with invalid path parameters
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", "invalid-id")
        .when()
            .get("/api/inventory/{inventoryId}")
        .then()
            .statusCode(404);
    }

    // ===== CONTENT TYPE TESTS =====

    @Test
    @DisplayName("Should return JSON content type for successful requests")
    void testContentType_JSON() {
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should accept JSON content type for update requests")
    void testUpdateInventory_AcceptJSON() {
        String updateRequest = """
            {
                "totalCopies": 15
            }
            """;

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200);
    }
    */

    // ===== INTEGRATION WITH BUSINESS LOGIC =====

    @Test
    @DisplayName("Should reflect inventory changes in database")
    @Transactional
    void testInventoryUpdate_DatabaseIntegration() {
        String updateRequest = """
            {
                "totalCopies": 25,
                "availableCopies": 20,
                "reservedCopies": 3,
                "damagedCopies": 2
            }
            """;

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200);

        // Verify changes are persisted
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
        .when()
            .get("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200)
            .body("totalCopies", equalTo(25))
            .body("availableCopies", equalTo(20))
            .body("reservedCopies", equalTo(3))
            .body("damagedCopies", equalTo(2));
    }

    @Test
    @DisplayName("Should maintain data consistency across requests")
    @Transactional
    void testDataConsistency() {
        // First, get initial state
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
        .when()
            .get("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200)
            .body("totalCopies", equalTo(10));

        // Update inventory
        String updateRequest = """
            {
                "totalCopies": 30
            }
            """;

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .pathParam("inventoryId", testInventory.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/inventory/{inventoryId}")
        .then()
            .statusCode(200);

        // Verify consistency in list endpoint
        given()
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .get("/api/inventory")
        .then()
            .statusCode(200)
            .body("[0].totalCopies", equalTo(30));
    }
} 