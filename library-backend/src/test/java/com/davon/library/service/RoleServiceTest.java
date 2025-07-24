package com.davon.library.service;

import com.davon.library.model.Role;
import com.davon.library.model.User;
import com.davon.library.repository.RoleRepository;
import com.davon.library.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Inject
    RoleService roleService;

    @InjectMock
    RoleRepository roleRepository;

    @InjectMock
    UserRepository userRepository;

    private Role testRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test role
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ADMIN");
        testRole.setPermissions(new ArrayList<>(Arrays.asList("READ", "WRITE", "DELETE")));
        testRole.setUsers(new ArrayList<>());

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
    }

    // ===== GET ALL ROLES TESTS =====

    @Test
    @DisplayName("Should get all roles successfully")
    void testGetAllRoles_Success() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        when(roleRepository.listAll()).thenReturn(roles);

        // Act
        List<Role> result = roleService.getAllRoles();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRole, result.get(0));
        verify(roleRepository).listAll();
    }

    @Test
    @DisplayName("Should return empty list when no roles exist")
    void testGetAllRoles_EmptyList() {
        // Arrange
        when(roleRepository.listAll()).thenReturn(new ArrayList<>());

        // Act
        List<Role> result = roleService.getAllRoles();

        // Assert
        assertTrue(result.isEmpty());
        verify(roleRepository).listAll();
    }

    // ===== GET ROLE BY ID TESTS =====

    @Test
    @DisplayName("Should get role by ID successfully")
    void testGetRoleById_Success() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        Optional<Role> result = roleService.getRoleById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRole, result.get());
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return empty when role not found")
    void testGetRoleById_NotFound() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleService.getRoleById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(roleRepository).findByIdOptional(1L);
    }

    // ===== GET ROLE BY NAME TESTS =====

    @Test
    @DisplayName("Should get role by name successfully")
    void testGetRoleByName_Success() {
        // Arrange
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(testRole));

        // Act
        Optional<Role> result = roleService.getRoleByName("ADMIN");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRole, result.get());
        verify(roleRepository).findByName("ADMIN");
    }

    @Test
    @DisplayName("Should return empty when role name not found")
    void testGetRoleByName_NotFound() {
        // Arrange
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleService.getRoleByName("NONEXISTENT");

        // Assert
        assertFalse(result.isPresent());
        verify(roleRepository).findByName("NONEXISTENT");
    }

    // ===== CREATE ROLE TESTS =====

    @Test
    @DisplayName("Should create role successfully")
    void testCreateRole_Success() {
        // Arrange
        List<String> permissions = Arrays.asList("READ", "WRITE");
        when(roleRepository.roleNameExists("MANAGER")).thenReturn(false);
        doNothing().when(roleRepository).persist(any(Role.class));

        // Act
        Role result = roleService.createRole("manager", permissions);

        // Assert
        assertNotNull(result);
        assertEquals("MANAGER", result.getName());
        assertEquals(permissions, result.getPermissions());
        verify(roleRepository).roleNameExists("MANAGER");
        verify(roleRepository).persist(any(Role.class));
    }

    @Test
    @DisplayName("Should create role with null permissions")
    void testCreateRole_NullPermissions() {
        // Arrange
        when(roleRepository.roleNameExists("MANAGER")).thenReturn(false);
        doNothing().when(roleRepository).persist(any(Role.class));

        // Act
        Role result = roleService.createRole("manager", null);

        // Assert
        assertNotNull(result);
        assertEquals("MANAGER", result.getName());
        assertNotNull(result.getPermissions());
        assertTrue(result.getPermissions().isEmpty());
        verify(roleRepository).roleNameExists("MANAGER");
        verify(roleRepository).persist(any(Role.class));
    }

    @Test
    @DisplayName("Should fail to create role when name is null")
    void testCreateRole_NullName() {
        // Act
        Role result = roleService.createRole(null, Arrays.asList("READ"));

        // Assert
        assertNull(result);
        verify(roleRepository, never()).roleNameExists(any());
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    @DisplayName("Should fail to create role when name is empty")
    void testCreateRole_EmptyName() {
        // Act
        Role result = roleService.createRole("   ", Arrays.asList("READ"));

        // Assert
        assertNull(result);
        verify(roleRepository, never()).roleNameExists(any());
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    @DisplayName("Should fail to create role when name already exists")
    void testCreateRole_NameExists() {
        // Arrange
        when(roleRepository.roleNameExists("ADMIN")).thenReturn(true);

        // Act
        Role result = roleService.createRole("admin", Arrays.asList("READ"));

        // Assert
        assertNull(result);
        verify(roleRepository).roleNameExists("ADMIN");
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should trim and uppercase role name when creating")
    void testCreateRole_TrimAndUppercase() {
        // Arrange
        when(roleRepository.roleNameExists("MANAGER")).thenReturn(false);
        doNothing().when(roleRepository).persist(any(Role.class));

        // Act
        Role result = roleService.createRole("  manager  ", Arrays.asList("READ"));

        // Assert
        assertNotNull(result);
        assertEquals("MANAGER", result.getName());
        verify(roleRepository).roleNameExists("MANAGER");
        verify(roleRepository).persist(any(Role.class));
    }

    // ===== UPDATE ROLE TESTS =====

    @Test
    @DisplayName("Should update role successfully")
    void testUpdateRole_Success() {
        // Arrange
        List<String> newPermissions = Arrays.asList("READ", "WRITE", "EXECUTE");
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.roleNameExists("MANAGER")).thenReturn(false);

        // Act
        boolean result = roleService.updateRole(1L, "manager", newPermissions);

        // Assert
        assertTrue(result);
        assertEquals("MANAGER", testRole.getName());
        assertEquals(newPermissions, testRole.getPermissions());
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository).roleNameExists("MANAGER");
    }

    @Test
    @DisplayName("Should fail to update role when ID is null")
    void testUpdateRole_NullId() {
        // Act
        boolean result = roleService.updateRole(null, "MANAGER", Arrays.asList("READ"));

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to update role when role not found")
    void testUpdateRole_RoleNotFound() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = roleService.updateRole(1L, "MANAGER", Arrays.asList("READ"));

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository, never()).roleNameExists(any());
    }

    @Test
    @DisplayName("Should fail to update role when new name already exists")
    void testUpdateRole_NameExists() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.roleNameExists("MANAGER")).thenReturn(true);

        // Act
        boolean result = roleService.updateRole(1L, "manager", Arrays.asList("READ"));

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository).roleNameExists("MANAGER");
    }

    @Test
    @DisplayName("Should update only permissions when name is same")
    void testUpdateRole_OnlyPermissions() {
        // Arrange
        List<String> newPermissions = Arrays.asList("READ", "EXECUTE");
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.updateRole(1L, "ADMIN", newPermissions);

        // Assert
        assertTrue(result);
        assertEquals("ADMIN", testRole.getName()); // Unchanged
        assertEquals(newPermissions, testRole.getPermissions()); // Changed
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository, never()).roleNameExists(any());
    }

    @Test
    @DisplayName("Should return false when no changes made")
    void testUpdateRole_NoChanges() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.updateRole(1L, "ADMIN", testRole.getPermissions());

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository, never()).roleNameExists(any());
    }

    // ===== ASSIGN ROLE TO USER TESTS =====

    @Test
    @DisplayName("Should assign role to user successfully")
    void testAssignRoleToUser_Success() {
        // Arrange
        when(userRepository.findByIdOptional(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.assignRoleToUser(1L, 1L);

        // Assert
        assertTrue(result);
        assertEquals("ADMIN", testUser.getRole());
        verify(userRepository).findByIdOptional(1L);
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to assign role when user ID is null")
    void testAssignRoleToUser_NullUserId() {
        // Act
        boolean result = roleService.assignRoleToUser(null, 1L);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).findByIdOptional(any());
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to assign role when role ID is null")
    void testAssignRoleToUser_NullRoleId() {
        // Act
        boolean result = roleService.assignRoleToUser(1L, null);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).findByIdOptional(any());
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to assign role when user not found")
    void testAssignRoleToUser_UserNotFound() {
        // Arrange
        when(userRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = roleService.assignRoleToUser(1L, 1L);

        // Assert
        assertFalse(result);
        verify(userRepository).findByIdOptional(1L);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to assign role when role not found")
    void testAssignRoleToUser_RoleNotFound() {
        // Arrange
        when(userRepository.findByIdOptional(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = roleService.assignRoleToUser(1L, 1L);

        // Assert
        assertFalse(result);
        verify(userRepository).findByIdOptional(1L);
        verify(roleRepository).findByIdOptional(1L);
    }

    // ===== HAS PERMISSION TESTS =====

    @Test
    @DisplayName("Should return true when role has permission")
    void testHasPermission_True() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.hasPermission(1L, "READ");

        // Assert
        assertTrue(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when role doesn't have permission")
    void testHasPermission_False() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.hasPermission(1L, "EXECUTE");

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when role ID is null")
    void testHasPermission_NullRoleId() {
        // Act
        boolean result = roleService.hasPermission(null, "READ");

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should return false when permission is null")
    void testHasPermission_NullPermission() {
        // Act
        boolean result = roleService.hasPermission(1L, null);

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should return false when role not found")
    void testHasPermission_RoleNotFound() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = roleService.hasPermission(1L, "READ");

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when role has null permissions")
    void testHasPermission_NullPermissions() {
        // Arrange
        testRole.setPermissions(null);
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.hasPermission(1L, "READ");

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    // ===== ADD PERMISSION TO ROLE TESTS =====

    @Test
    @DisplayName("Should add permission to role successfully")
    void testAddPermissionToRole_Success() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.addPermissionToRole(1L, "EXECUTE");

        // Assert
        assertTrue(result);
        assertTrue(testRole.getPermissions().contains("EXECUTE"));
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to add permission when role ID is null")
    void testAddPermissionToRole_NullRoleId() {
        // Act
        boolean result = roleService.addPermissionToRole(null, "EXECUTE");

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to add permission when permission is null")
    void testAddPermissionToRole_NullPermission() {
        // Act
        boolean result = roleService.addPermissionToRole(1L, null);

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to add permission when permission is empty")
    void testAddPermissionToRole_EmptyPermission() {
        // Act
        boolean result = roleService.addPermissionToRole(1L, "   ");

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to add permission when role not found")
    void testAddPermissionToRole_RoleNotFound() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = roleService.addPermissionToRole(1L, "EXECUTE");

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to add permission when permission already exists")
    void testAddPermissionToRole_PermissionExists() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.addPermissionToRole(1L, "READ");

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should initialize permissions list when null")
    void testAddPermissionToRole_InitializePermissions() {
        // Arrange
        testRole.setPermissions(null);
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.addPermissionToRole(1L, "EXECUTE");

        // Assert
        assertTrue(result);
        assertNotNull(testRole.getPermissions());
        assertTrue(testRole.getPermissions().contains("EXECUTE"));
        verify(roleRepository).findByIdOptional(1L);
    }

    // ===== REMOVE PERMISSION FROM ROLE TESTS =====

    @Test
    @DisplayName("Should remove permission from role successfully")
    void testRemovePermissionFromRole_Success() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.removePermissionFromRole(1L, "READ");

        // Assert
        assertTrue(result);
        assertFalse(testRole.getPermissions().contains("READ"));
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to remove permission when role ID is null")
    void testRemovePermissionFromRole_NullRoleId() {
        // Act
        boolean result = roleService.removePermissionFromRole(null, "READ");

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to remove permission when permission is null")
    void testRemovePermissionFromRole_NullPermission() {
        // Act
        boolean result = roleService.removePermissionFromRole(1L, null);

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to remove permission when role not found")
    void testRemovePermissionFromRole_RoleNotFound() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = roleService.removePermissionFromRole(1L, "READ");

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to remove permission when permissions list is null")
    void testRemovePermissionFromRole_NullPermissions() {
        // Arrange
        testRole.setPermissions(null);
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.removePermissionFromRole(1L, "READ");

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to remove permission when permission not found")
    void testRemovePermissionFromRole_PermissionNotFound() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.removePermissionFromRole(1L, "EXECUTE");

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
    }

    // ===== DELETE ROLE TESTS =====

    @Test
    @DisplayName("Should delete role successfully")
    void testDeleteRole_Success() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));
        doNothing().when(roleRepository).delete(testRole);

        // Act
        boolean result = roleService.deleteRole(1L);

        // Assert
        assertTrue(result);
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository).delete(testRole);
    }

    @Test
    @DisplayName("Should fail to delete role when ID is null")
    void testDeleteRole_NullId() {
        // Act
        boolean result = roleService.deleteRole(null);

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).findByIdOptional(any());
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should fail to delete role when role not found")
    void testDeleteRole_RoleNotFound() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = roleService.deleteRole(1L);

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should fail to delete role when role has users")
    void testDeleteRole_RoleHasUsers() {
        // Arrange
        testRole.getUsers().add(testUser);
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean result = roleService.deleteRole(1L);

        // Assert
        assertFalse(result);
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete role when users list is null")
    void testDeleteRole_NullUsers() {
        // Arrange
        testRole.setUsers(null);
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));
        doNothing().when(roleRepository).delete(testRole);

        // Act
        boolean result = roleService.deleteRole(1L);

        // Assert
        assertTrue(result);
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository).delete(testRole);
    }

    @Test
    @DisplayName("Should delete role when users list is empty")
    void testDeleteRole_EmptyUsers() {
        // Arrange
        testRole.setUsers(new ArrayList<>());
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));
        doNothing().when(roleRepository).delete(testRole);

        // Act
        boolean result = roleService.deleteRole(1L);

        // Assert
        assertTrue(result);
        verify(roleRepository).findByIdOptional(1L);
        verify(roleRepository).delete(testRole);
    }

    // ===== DISPLAY ROLE DETAILS TESTS =====

    @Test
    @DisplayName("Should display role details successfully")
    void testDisplayRoleDetails_Success() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> roleService.displayRoleDetails(1L));
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should handle null role ID in display")
    void testDisplayRoleDetails_NullId() {
        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> roleService.displayRoleDetails(null));
        verify(roleRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should handle role not found in display")
    void testDisplayRoleDetails_RoleNotFound() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> roleService.displayRoleDetails(1L));
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should display role with null permissions")
    void testDisplayRoleDetails_NullPermissions() {
        // Arrange
        testRole.setPermissions(null);
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> roleService.displayRoleDetails(1L));
        verify(roleRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should display role with null users")
    void testDisplayRoleDetails_NullUsers() {
        // Arrange
        testRole.setUsers(null);
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> roleService.displayRoleDetails(1L));
        verify(roleRepository).findByIdOptional(1L);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle role with empty permissions list")
    void testRoleWithEmptyPermissions() {
        // Arrange
        testRole.setPermissions(new ArrayList<>());
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean hasPermission = roleService.hasPermission(1L, "READ");
        boolean addResult = roleService.addPermissionToRole(1L, "NEW_PERMISSION");

        // Assert
        assertFalse(hasPermission);
        assertTrue(addResult);
        assertTrue(testRole.getPermissions().contains("NEW_PERMISSION"));
        verify(roleRepository, times(2)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should handle multiple permission operations")
    void testMultiplePermissionOperations() {
        // Arrange
        when(roleRepository.findByIdOptional(1L)).thenReturn(Optional.of(testRole));

        // Act
        boolean add1 = roleService.addPermissionToRole(1L, "EXECUTE");
        boolean add2 = roleService.addPermissionToRole(1L, "ADMIN");
        boolean remove1 = roleService.removePermissionFromRole(1L, "READ");
        boolean hasExecute = roleService.hasPermission(1L, "EXECUTE");
        boolean hasRead = roleService.hasPermission(1L, "READ");

        // Assert
        assertTrue(add1);
        assertTrue(add2);
        assertTrue(remove1);
        assertTrue(hasExecute);
        assertFalse(hasRead);
        assertTrue(testRole.getPermissions().contains("EXECUTE"));
        assertTrue(testRole.getPermissions().contains("ADMIN"));
        assertFalse(testRole.getPermissions().contains("READ"));
        verify(roleRepository, times(5)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should handle case sensitivity in role names")
    void testRoleNameCaseSensitivity() {
        // Arrange
        when(roleRepository.roleNameExists("MANAGER")).thenReturn(false);
        doNothing().when(roleRepository).persist(any(Role.class));

        // Act
        Role result1 = roleService.createRole("manager", Arrays.asList("READ"));
        Role result2 = roleService.createRole("MANAGER", Arrays.asList("WRITE"));
        Role result3 = roleService.createRole("Manager", Arrays.asList("DELETE"));

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals("MANAGER", result1.getName());
        assertEquals("MANAGER", result2.getName());
        assertEquals("MANAGER", result3.getName());
        // Note: In real scenario, subsequent calls would fail due to name exists check
        verify(roleRepository, times(3)).roleNameExists("MANAGER");
        verify(roleRepository, times(3)).persist(any(Role.class));
    }
} 