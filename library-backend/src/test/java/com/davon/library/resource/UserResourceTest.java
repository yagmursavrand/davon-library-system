package com.davon.library.resource;

import com.davon.library.model.User;
import com.davon.library.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserResourceTest {

    @Inject
    UserRepository userRepository;

    private User testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean database
        userRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());
        testUser.setLoggedIn(false);
        
        userRepository.persist(testUser);
    }

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        given()
            .when()
                .get("/api/users")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].name", equalTo("John Doe"))
                .body("[0].email", equalTo("john@example.com"));
    }

    @Test
    @DisplayName("Should get user by ID")
    void testGetUserById() {
        given()
            .pathParam("id", testUser.getId())
            .when()
                .get("/api/users/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(testUser.getId().intValue()))
                .body("name", equalTo("John Doe"))
                .body("email", equalTo("john@example.com"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent user ID")
    void testGetUserByIdNotFound() {
        given()
            .pathParam("id", 999L)
            .when()
                .get("/api/users/{id}")
            .then()
                .statusCode(404)
                .body(equalTo("User not found"));
    }

    @Test
    @DisplayName("Should get user by email")
    void testGetUserByEmail() {
        given()
            .pathParam("email", "john@example.com")
            .when()
                .get("/api/users/email/{email}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("John Doe"))
                .body("email", equalTo("john@example.com"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent email")
    void testGetUserByEmailNotFound() {
        given()
            .pathParam("email", "nonexistent@example.com")
            .when()
                .get("/api/users/email/{email}")
            .then()
                .statusCode(404)
                .body(equalTo("User not found"));
    }

    @Test
    @DisplayName("Should get users by role")
    void testGetUsersByRole() {
        given()
            .pathParam("role", "USER")
            .when()
                .get("/api/users/role/{role}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].role", equalTo("USER"));
    }

    @Test
    @DisplayName("Should create new user")
    void testCreateUser() {
        String newUserJson = """
            {
                "name": "Jane Smith",
                "email": "jane@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(newUserJson)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Jane Smith"))
                .body("email", equalTo("jane@example.com"))
                .body("role", equalTo("USER"));
    }

    @Test
    @DisplayName("Should return 400 when creating user with existing email")
    void testCreateUserEmailExists() {
        String duplicateUserJson = """
            {
                "name": "Another John",
                "email": "john@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(duplicateUserJson)
            .when()
                .post("/api/users")
            .then()
                .statusCode(400)
                .body(containsString("Email already exists"));
    }

    @Test
    @DisplayName("Should update user")
    void testUpdateUser() {
        String updateJson = """
            {
                "name": "John Updated",
                "email": "john.updated@example.com",
                "password": "newpassword123",
                "role": "ADMIN"
            }
            """;

        given()
            .pathParam("id", testUser.getId())
            .contentType(ContentType.JSON)
            .body(updateJson)
            .when()
                .put("/api/users/{id}")
            .then()
                .statusCode(200)
                .body(equalTo("User updated successfully"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent user")
    void testUpdateUserNotFound() {
        String updateJson = """
            {
                "name": "Non Existent",
                "email": "nonexistent@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

        given()
            .pathParam("id", 999L)
            .contentType(ContentType.JSON)
            .body(updateJson)
            .when()
                .put("/api/users/{id}")
            .then()
                .statusCode(404)
                .body(equalTo("User not found"));
    }

    @Test
    @DisplayName("Should delete user")
    void testDeleteUser() {
        given()
            .pathParam("id", testUser.getId())
            .when()
                .delete("/api/users/{id}")
            .then()
                .statusCode(200)
                .body(equalTo("User deleted successfully"));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    void testDeleteUserNotFound() {
        given()
            .pathParam("id", 999L)
            .when()
                .delete("/api/users/{id}")
            .then()
                .statusCode(404)
                .body(equalTo("User not found"));
    }
} 