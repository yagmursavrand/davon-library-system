package com.davon.library.resource;

import com.davon.library.model.Admin;
import com.davon.library.model.User;
import com.davon.library.repository.AdminRepository;
import com.davon.library.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AdminResourceTest {

    @Inject
    AdminRepository adminRepository;

    @Inject
    UserRepository userRepository;

    private Admin testAdmin;
    private User testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up
        adminRepository.deleteAll();
        userRepository.deleteAll();

        // Create test admin
        testAdmin = new Admin();
        testAdmin.setName("Test Admin");
        testAdmin.setEmail("admin@library.com");
        testAdmin.setPassword("admin123");
        testAdmin.setRole("ADMIN");
        testAdmin.setAdminLevel("SUPER_ADMIN");
        testAdmin.setDepartment("IT");
        testAdmin.setCreatedAt(new Date());
        adminRepository.persist(testAdmin);

        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("user@library.com");
        testUser.setPassword("user123");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());
        userRepository.persist(testUser);
    }

    @Test
    @DisplayName("Should get all admins with admin authentication")
    void testGetAllAdmins() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/admins")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Should reject get all admins without authentication")
    void testGetAllAdminsWithoutAuth() {
        given()
        .when()
            .get("/api/admins")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should get admin by ID with proper authentication")
    void testGetAdminById() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/admins/" + testAdmin.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("name", equalTo("Test Admin"))
            .body("email", equalTo("admin@library.com"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent admin")
    void testGetAdminByIdNotFound() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/admins/999")
        .then()
            .statusCode(404)
            .body(containsString("Admin not found"));
    }

    @Test
    @DisplayName("Should create new admin with valid request")
    void testCreateAdmin() {
        String requestBody = """
            {
                "email": "user@library.com",
                "adminLevel": "ADMIN",
                "department": "Library Services"
            }
            """;

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/admins")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("role", equalTo("ADMIN"))
            .body("department", equalTo("Library Services"));
    }

    @Test
    @DisplayName("Should reject admin creation for non-existent user")
    void testCreateAdminUserNotFound() {
        String requestBody = """
            {
                "email": "nonexistent@library.com",
                "adminLevel": "ADMIN",
                "department": "IT"
            }
            """;

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/admins")
        .then()
            .statusCode(404)
            .body(containsString("User not found"));
    }

    @Test
    @DisplayName("Should update admin details")
    void testUpdateAdmin() {
        String requestBody = """
            {
                "adminLevel": "BASIC_ADMIN",
                "department": "Updated Department"
            }
            """;

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/api/admins/" + testAdmin.getId())
        .then()
            .statusCode(200)
            .body(containsString("Admin updated successfully"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent admin")
    void testUpdateAdminNotFound() {
        String requestBody = """
            {
                "adminLevel": "ADMIN",
                "department": "IT"
            }
            """;

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/api/admins/999")
        .then()
            .statusCode(404)
            .body(containsString("Admin not found"));
    }

    @Test
    @DisplayName("Should delete admin successfully")
    @Transactional
    void testDeleteAdmin() {
        // Create another admin to delete
        Admin adminToDelete = new Admin();
        adminToDelete.setName("Admin To Delete");
        adminToDelete.setEmail("delete@library.com");
        adminToDelete.setPassword("password");
        adminToDelete.setRole("ADMIN");
        adminToDelete.setAdminLevel("ADMIN");
        adminToDelete.setDepartment("Test");
        adminToDelete.setCreatedAt(new Date());
        adminRepository.persist(adminToDelete);

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/admins/" + adminToDelete.getId())
        .then()
            .statusCode(200)
            .body(containsString("Admin privileges removed successfully"));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent admin")
    void testDeleteAdminNotFound() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/admins/999")
        .then()
            .statusCode(404)
            .body(containsString("Admin not found"));
    }

    @Test
    @DisplayName("Should get admins by department")
    void testGetAdminsByDepartment() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/admins/department/IT")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Should get admins by level")
    void testGetAdminsByLevel() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/admins/level/SUPER_ADMIN")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Should reject requests without admin privileges")
    void testNonAdminAccess() {
        // Test with regular user token
        given()
            .header("Authorization", "Bearer " + testUser.getId())
        .when()
            .get("/api/admins")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should reject requests with invalid authentication")
    void testInvalidAuthentication() {
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .get("/api/admins")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }
} 