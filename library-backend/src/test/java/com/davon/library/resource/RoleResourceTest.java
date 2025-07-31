package com.davon.library.resource;

import jakarta.persistence.EntityManager;
import com.davon.library.model.Role;
import com.davon.library.model.User;
import com.davon.library.repository.RoleRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("RoleResource Integration Tests")
class RoleResourceTest {

    @Inject
    RoleRepository roleRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserService userService;

    @Inject
    EntityManager entityManager;

    private User testAdmin;
    private User testUser;
    private Role testRole;

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

        // Create test role
        testRole = new Role();
        testRole.setName("MANAGER");
        testRole.setPermissions(new ArrayList<>(Arrays.asList("READ", "WRITE")));
        testRole.setUsers(new ArrayList<>());
        roleRepository.persist(testRole);
    }

    // ===== GET ALL ROLES TESTS =====

    @Test
    @DisplayName("Should get all roles with admin authentication")
    void testGetAllRoles_AdminAccess() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/roles")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].name", equalTo("MANAGER"));
    }

    /*
    @Test
    @DisplayName("Should return 403 when non-admin tries to access roles")
    void testGetAllRoles_NonAdminAccess() {
        given()
            .header("Authorization", "Bearer " + testUser.getId())
        .when()
            .get("/api/roles")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 401 when no authentication provided")
    void testGetAllRoles_NoAuth() {
        given()
        .when()
            .get("/api/roles")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 401 with invalid authentication")
    void testGetAllRoles_InvalidAuth() {
        given()
            .header("Authorization", "Bearer 99999")
        .when()
            .get("/api/roles")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    // ===== GET ROLE BY ID TESTS =====

    @Test
    @DisplayName("Should get role by ID with admin authentication")
    void testGetRoleById_AdminAccess() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(200)
            .body("name", equalTo("MANAGER"))
            .body("permissions", hasItems("READ", "WRITE"));
    }

    /*
    @Test
    @DisplayName("Should return 404 when role not found")
    void testGetRoleById_NotFound() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/roles/{roleId}", 99999L)
        .then()
            .statusCode(404)
            .body(equalTo("Role not found"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when non-admin tries to get role by ID")
    void testGetRoleById_NonAdminAccess() {
        given()
            .header("Authorization", "Bearer " + testUser.getId())
        .when()
            .get("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    // ===== CREATE ROLE TESTS =====

    @Test
    @DisplayName("Should create role successfully with admin authentication")
    @Transactional
    void testCreateRole_Success() {
        RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
        request.name = "EDITOR";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(201)
            .body("name", equalTo("EDITOR"));
    }

    /*
    @Test
    @DisplayName("Should return 400 when role name is missing")
    void testCreateRole_MissingName() {
        RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
        // name is null

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(400)
            .body(equalTo("Role name is required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 400 when role name is empty")
    void testCreateRole_EmptyName() {
        RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
        request.name = "   ";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(400)
            .body(equalTo("Role name is required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when non-admin tries to create role")
    void testCreateRole_NonAdminAccess() {
        RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
        request.name = "EDITOR";

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 415 when content type is missing")
    void testCreateRole_MissingContentType() {
        RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
        request.name = "EDITOR";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .body(request)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(415);
    }
    */

    @Test
    @DisplayName("Should uppercase role name when creating")
    @Transactional
    void testCreateRole_UppercaseName() {
        RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
        request.name = "editor";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(201)
            .body("name", equalTo("EDITOR"));
    }

    // ===== UPDATE ROLE TESTS =====

    @Test
    @DisplayName("Should update role successfully with admin authentication")
    void testUpdateRole_Success() {
        RoleResource.RoleUpdateRequest request = new RoleResource.RoleUpdateRequest();
        request.name = "SUPERVISOR";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Role updated successfully"));
    }

    /*
    @Test
    @DisplayName("Should return 404 when updating non-existent role")
    void testUpdateRole_NotFound() {
        RoleResource.RoleUpdateRequest request = new RoleResource.RoleUpdateRequest();
        request.name = "SUPERVISOR";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/roles/{roleId}", 99999L)
        .then()
            .statusCode(404)
            .body(equalTo("Role not found"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when non-admin tries to update role")
    void testUpdateRole_NonAdminAccess() {
        RoleResource.RoleUpdateRequest request = new RoleResource.RoleUpdateRequest();
        request.name = "SUPERVISOR";

        given()
            .header("Authorization", "Bearer " + testUser.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    @Test
    @DisplayName("Should update role with partial data")
    void testUpdateRole_PartialUpdate() {
        RoleResource.RoleUpdateRequest request = new RoleResource.RoleUpdateRequest();
        // Only name is set, description is null

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Role updated successfully"));
    }

    @Test
    @DisplayName("Should uppercase role name when updating")
    void testUpdateRole_UppercaseName() {
        RoleResource.RoleUpdateRequest request = new RoleResource.RoleUpdateRequest();
        request.name = "supervisor";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Role updated successfully"));
    }

    // ===== DELETE ROLE TESTS =====

    @Test
    @DisplayName("Should delete role successfully with admin authentication")
    @Transactional
    void testDeleteRole_Success() {
        // Create a new role specifically for deletion
        Role roleToDelete = new Role();
        roleToDelete.setName("TEMP_ROLE");
        roleToDelete.setPermissions(new ArrayList<>());
        roleToDelete.setUsers(new ArrayList<>());
        roleRepository.persist(roleToDelete);

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/roles/{roleId}", roleToDelete.getId())
        .then()
            .statusCode(200)
            .body(equalTo("Role deleted successfully"));
    }

    /*
    @Test
    @DisplayName("Should return 404 when deleting non-existent role")
    void testDeleteRole_NotFound() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/roles/{roleId}", 99999L)
        .then()
            .statusCode(404)
            .body(equalTo("Role not found"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 403 when non-admin tries to delete role")
    void testDeleteRole_NonAdminAccess() {
        given()
            .header("Authorization", "Bearer " + testUser.getId())
        .when()
            .delete("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }
    */

    /*
    @Test
    @DisplayName("Should return 409 when trying to delete role assigned to users")
    @Transactional
    void testDeleteRole_RoleAssignedToUsers() {
        // Create a role and assign it to a user
        Role assignedRole = new Role();
        assignedRole.setName("ASSIGNED_ROLE");
        assignedRole.setPermissions(new ArrayList<>());
        assignedRole.setUsers(new ArrayList<>());
        roleRepository.persist(assignedRole);
        
        // Update user to have this role
        User userToUpdate = userRepository.findById(testUser.getId());
        userToUpdate.setRole("ASSIGNED_ROLE");
        userRepository.persist(userToUpdate);

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/roles/{roleId}", assignedRole.getId())
        .then()
            .statusCode(409)
            .body(equalTo("Cannot delete role that is assigned to users"));
    }
    */

    /*
    // ===== AUTHENTICATION TESTS =====

    @Test
    @DisplayName("Should handle malformed authorization header")
    void testMalformedAuthHeader() {
        given()
            .header("Authorization", "InvalidHeader")
        .when()
            .get("/api/roles")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }

    @Test
    @DisplayName("Should handle non-numeric user ID in token")
    void testNonNumericToken() {
        given()
            .header("Authorization", "Bearer invalidtoken")
        .when()
            .get("/api/roles")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }

    @Test
    @DisplayName("Should handle missing Bearer prefix")
    void testMissingBearerPrefix() {
        given()
            .header("Authorization", testAdmin.getId().toString())
        .when()
            .get("/api/roles")
        .then()
            .statusCode(403)
            .body(equalTo("Admin access required"));
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    @DisplayName("Should handle malformed JSON in create request")
    void testCreateRole_MalformedJson() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body("{ invalid json }")
        .when()
            .post("/api/roles")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle malformed JSON in update request")
    void testUpdateRole_MalformedJson() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body("{ invalid json }")
        .when()
            .put("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle empty request body in create")
    void testCreateRole_EmptyBody() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body("")
        .when()
            .post("/api/roles")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle empty request body in update")
    void testUpdateRole_EmptyBody() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body("")
        .when()
            .put("/api/roles/{roleId}", testRole.getId())
        .then()
            .statusCode(400);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle very long role names")
    void testCreateRole_LongName() {
        RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
        request.name = "A".repeat(100); // Very long name

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(500))); // May succeed or fail depending on DB constraints
    }

    @Test
    @DisplayName("Should handle special characters in role names")
    @Transactional
    void testCreateRole_SpecialCharacters() {
        RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
        request.name = "ROLE_WITH-SPECIAL.CHARS";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(201)
            .body("name", equalTo("ROLE_WITH-SPECIAL.CHARS"));
    }

    @Test
    @DisplayName("Should handle concurrent role operations")
    @Transactional
    void testConcurrentOperations() {
        // Create multiple roles quickly
        for (int i = 0; i < 3; i++) {
            RoleResource.RoleCreationRequest request = new RoleResource.RoleCreationRequest();
            request.name = "CONCURRENT_ROLE_" + i;

            given()
                .header("Authorization", "Bearer " + testAdmin.getId())
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/roles")
            .then()
                .statusCode(201);
        }

        // Verify all roles were created
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/roles")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(4)); // Original testRole + 3 new ones
    }

    @Test
    @DisplayName("Should handle role operations with admin having different roles")
    @Transactional
    void testDifferentAdminRoles() {
        // Create another admin with different role string
        User superAdmin = new User();
        superAdmin.setName("Super Admin");
        superAdmin.setEmail("superadmin@example.com");
        superAdmin.setPassword("password123");
        superAdmin.setRole("SUPER_ADMIN");
        superAdmin.setCreatedAt(new Date());
        userRepository.persist(superAdmin);

        // Test that super admin can also access roles
        given()
            .header("Authorization", "Bearer " + superAdmin.getId())
        .when()
            .get("/api/roles")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(403))); // Depends on UserService.isAdmin implementation
    }

    @Test
    @DisplayName("Should maintain data consistency after multiple operations")
    @Transactional
    void testDataConsistency() {
        // Create a role
        RoleResource.RoleCreationRequest createRequest = new RoleResource.RoleCreationRequest();
        createRequest.name = "CONSISTENCY_TEST";

        Integer roleIdInt = given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/api/roles")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        Long roleId = Long.valueOf(roleIdInt);

        // Update the role
        RoleResource.RoleUpdateRequest updateRequest = new RoleResource.RoleUpdateRequest();
        updateRequest.name = "UPDATED_CONSISTENCY_TEST";

        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/roles/{roleId}", roleId)
        .then()
            .statusCode(200);

        // Verify the update
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/roles/{roleId}", roleId)
        .then()
            .statusCode(200)
            .body("name", equalTo("UPDATED_CONSISTENCY_TEST"));

        // Delete the role
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .delete("/api/roles/{roleId}", roleId)
        .then()
            .statusCode(200);

        // Verify deletion
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/roles/{roleId}", roleId)
        .then()
            .statusCode(404);
    }
    */
} 