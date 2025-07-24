package com.davon.library.service;

import com.davon.library.model.Admin;
import com.davon.library.model.User;
import com.davon.library.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class AdminServiceTest {

    @Inject
    AdminService adminService;

    @InjectMock
    UserRepository userRepository;

    private Admin testAdmin;
    private User testUser;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setName("Admin User");
        testAdmin.setEmail("admin@library.com");
        testAdmin.setRole("ADMIN");
        testAdmin.setAdminLevel("SUPER_ADMIN");
        testAdmin.setDepartment("IT");
        testAdmin.setCreatedAt(new Date());

        testUser = new User();
        testUser.setId(2L);
        testUser.setName("Regular User");
        testUser.setEmail("user@library.com");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());
    }

    @Test
    @DisplayName("Should add user successfully when admin has permission")
    void testAddUserSuccess() {
        // Given
        when(userRepository.emailExists(testUser.getEmail())).thenReturn(false);

        // When
        boolean result = adminService.addUser(testAdmin, testUser);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).emailExists(testUser.getEmail());
    }

    @Test
    @DisplayName("Should fail to add user when admin is null")
    void testAddUserFailsWhenAdminIsNull() {
        // When
        boolean result = adminService.addUser(null, testUser);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to add user when user is null")
    void testAddUserFailsWhenUserIsNull() {
        // When
        boolean result = adminService.addUser(testAdmin, null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to add user when user name is empty")
    void testAddUserFailsWhenUserNameIsEmpty() {
        // Given
        testUser.setName("");

        // When
        boolean result = adminService.addUser(testAdmin, testUser);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to add user when email is invalid")
    void testAddUserFailsWhenEmailIsInvalid() {
        // Given
        testUser.setEmail("invalid-email");

        // When
        boolean result = adminService.addUser(testAdmin, testUser);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to add user when email already exists")
    void testAddUserFailsWhenEmailExists() {
        // Given
        when(userRepository.emailExists(testUser.getEmail())).thenReturn(true);

        // When
        boolean result = adminService.addUser(testAdmin, testUser);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).emailExists(testUser.getEmail());
    }

    @Test
    @DisplayName("Should remove user successfully when admin has permission")
    void testRemoveUserSuccess() {
        // Given
        when(userRepository.findByIdOptional(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        boolean result = adminService.removeUser(testAdmin, testUser.getId());

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).findByIdOptional(testUser.getId());
    }

    @Test
    @DisplayName("Should fail to remove user when user not found")
    void testRemoveUserFailsWhenUserNotFound() {
        // Given
        when(userRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // When
        boolean result = adminService.removeUser(testAdmin, 999L);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).findByIdOptional(999L);
    }

    @Test
    @DisplayName("Should fail to remove user when admin tries to remove themselves")
    void testRemoveUserFailsWhenAdminTriesToRemoveThemselves() {
        // Given
        when(userRepository.findByIdOptional(testAdmin.getId())).thenReturn(Optional.of(testAdmin));

        // When
        boolean result = adminService.removeUser(testAdmin, testAdmin.getId());

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should update user successfully when admin has permission")
    void testUpdateUserSuccess() {
        // Given
        when(userRepository.findByIdOptional(testUser.getId())).thenReturn(Optional.of(testUser));
        
        User updatedUser = new User();
        updatedUser.setName("Updated User Name");
        updatedUser.setEmail("updated@library.com");

        // When
        boolean result = adminService.updateUser(testAdmin, testUser.getId(), updatedUser);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).findByIdOptional(testUser.getId());
    }

    @Test
    @DisplayName("Should fail to update user when user not found")
    void testUpdateUserFailsWhenUserNotFound() {
        // Given
        when(userRepository.findByIdOptional(999L)).thenReturn(Optional.empty());
        
        User updatedUser = new User();
        updatedUser.setName("New Name");
        updatedUser.setEmail("new@email.com");

        // When
        boolean result = adminService.updateUser(testAdmin, 999L, updatedUser);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).findByIdOptional(999L);
    }

    @Test
    @DisplayName("Should check admin permissions correctly")
    void testHasPermission() {
        // Test SUPER_ADMIN permissions
        assertTrue(adminService.hasPermission(testAdmin, "ADD_USER"));
        assertTrue(adminService.hasPermission(testAdmin, "DELETE_USER"));
        assertTrue(adminService.hasPermission(testAdmin, "UPDATE_USER"));

        // Test SENIOR admin permissions
        testAdmin.setAdminLevel("SENIOR");
        assertTrue(adminService.hasPermission(testAdmin, "ADD_USER"));
        assertTrue(adminService.hasPermission(testAdmin, "UPDATE_USER"));
        assertFalse(adminService.hasPermission(testAdmin, "DELETE_ADMIN"));

        // Test STANDARD admin permissions  
        testAdmin.setAdminLevel("STANDARD");
        assertTrue(adminService.hasPermission(testAdmin, "ADD_USER"));
        assertTrue(adminService.hasPermission(testAdmin, "UPDATE_USER"));
        assertTrue(adminService.hasPermission(testAdmin, "VIEW_USERS"));
        assertFalse(adminService.hasPermission(testAdmin, "DELETE_USER"));
    }

    @Test
    @DisplayName("Should handle admin permissions based on admin level")
    void testAdminLevelPermissions() {
        // Test different admin levels through hasPermission method
        testAdmin.setAdminLevel("SUPER_ADMIN");
        assertTrue(adminService.hasPermission(testAdmin, "ADD_USER"));
        assertTrue(adminService.hasPermission(testAdmin, "DELETE_ADMIN"));
        
        testAdmin.setAdminLevel("SENIOR");
        assertTrue(adminService.hasPermission(testAdmin, "UPDATE_USER"));
        assertFalse(adminService.hasPermission(testAdmin, "DELETE_ADMIN"));
        
        testAdmin.setAdminLevel("STANDARD");
        assertTrue(adminService.hasPermission(testAdmin, "ADD_USER"));
        assertFalse(adminService.hasPermission(testAdmin, "DELETE_USER"));
    }
} 