package com.davon.library.service;

import com.davon.library.model.Role;
import com.davon.library.model.User;
import com.davon.library.repository.RoleRepository;
import com.davon.library.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * RoleService - handles all role-related business operations
 */
@ApplicationScoped
public class RoleService {

    @Inject
    RoleRepository roleRepository;
    
    @Inject
    UserRepository userRepository;

    /**
     * Get all roles
     */
    public List<Role> getAllRoles() {
        return roleRepository.listAll();
    }

    /**
     * Get role by ID
     */
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findByIdOptional(id);
    }

    /**
     * Get role by name
     */
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    /**
     * Create new role
     */
    @Transactional
    public Role createRole(String name, List<String> permissions) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Role name cannot be empty");
            return null;
        }

        // Normalize name for consistency
        String normalizedName = name.trim().toUpperCase();

        // Check if role already exists
        if (roleRepository.roleNameExists(normalizedName)) {
            System.out.println("Role with name '" + normalizedName + "' already exists");
            return null;
        }

        Role role = new Role();
        role.setName(normalizedName);
        role.setPermissions(permissions != null ? permissions : new ArrayList<>());

        roleRepository.persist(role);

        System.out.println("Role created successfully: " + role.getName());
        return role;
    }

    /**
     * Update role information
     */
    @Transactional
    public boolean updateRole(Long roleId, String name, List<String> permissions) {
        if (roleId == null) {
            System.out.println("Role ID cannot be null");
            return false;
        }

        Optional<Role> roleOpt = roleRepository.findByIdOptional(roleId);
        if (roleOpt.isEmpty()) {
            System.out.println("Role not found with ID: " + roleId);
            return false;
        }

        Role role = roleOpt.get();
        boolean updated = false;

        if (name != null && !name.trim().isEmpty() && !name.equals(role.getName())) {
            // Normalize name for consistency
            String normalizedName = name.trim().toUpperCase();
            
            // Check if new name conflicts with existing role
            if (roleRepository.roleNameExists(normalizedName)) {
                System.out.println("Role name already exists: " + normalizedName);
                return false;
            }
            role.setName(normalizedName);
            updated = true;
        }

        if (permissions != null && !permissions.equals(role.getPermissions())) {
            role.setPermissions(permissions);
            updated = true;
        }

        if (updated) {
            System.out.println("Role updated successfully: " + role.getName());
        } else {
            System.out.println("No changes made to role");
        }

        return updated;
    }

    /**
     * Assign role to user
     */
    @Transactional
    public boolean assignRoleToUser(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            System.out.println("User ID and Role ID cannot be null");
            return false;
        }

        Optional<User> userOpt = userRepository.findByIdOptional(userId);
        if (userOpt.isEmpty()) {
            System.out.println("User not found");
            return false;
        }

        Optional<Role> roleOpt = roleRepository.findByIdOptional(roleId);
        if (roleOpt.isEmpty()) {
            System.out.println("Role not found");
            return false;
        }

        User user = userOpt.get();
        Role role = roleOpt.get();

        // Simple role assignment (assuming User has a role field)
        user.setRole(role.getName());

        System.out.println("Role '" + role.getName() + "' assigned to user '" + user.getName() + "'");
        return true;
    }

    /**
     * Check if role has permission
     */
    public boolean hasPermission(Long roleId, String permission) {
        if (roleId == null || permission == null) {
            return false;
        }

        Optional<Role> roleOpt = roleRepository.findByIdOptional(roleId);
        if (roleOpt.isEmpty()) {
            return false;
        }

        Role role = roleOpt.get();
        return role.getPermissions() != null && role.getPermissions().contains(permission);
    }

    /**
     * Add permission to role
     */
    @Transactional
    public boolean addPermissionToRole(Long roleId, String permission) {
        if (roleId == null || permission == null || permission.trim().isEmpty()) {
            System.out.println("Invalid parameters");
            return false;
        }

        Optional<Role> roleOpt = roleRepository.findByIdOptional(roleId);
        if (roleOpt.isEmpty()) {
            System.out.println("Role not found");
            return false;
        }

        Role role = roleOpt.get();

        if (role.getPermissions() == null) {
            role.setPermissions(new ArrayList<>());
        }

        if (!role.getPermissions().contains(permission)) {
            role.getPermissions().add(permission);
            System.out.println("Permission '" + permission + "' added to role '" + role.getName() + "'");
            return true;
        } else {
            System.out.println("Permission already exists");
            return false;
        }
    }

    /**
     * Remove permission from role
     */
    @Transactional
    public boolean removePermissionFromRole(Long roleId, String permission) {
        if (roleId == null || permission == null) {
            System.out.println("Invalid parameters");
            return false;
        }

        Optional<Role> roleOpt = roleRepository.findByIdOptional(roleId);
        if (roleOpt.isEmpty()) {
            System.out.println("Role not found");
            return false;
        }

        Role role = roleOpt.get();

        if (role.getPermissions() == null || !role.getPermissions().contains(permission)) {
            System.out.println("Permission not found in role");
            return false;
        }

        role.getPermissions().remove(permission);
        System.out.println("Permission '" + permission + "' removed from role '" + role.getName() + "'");
        return true;
    }

    /**
     * Delete role
     */
    @Transactional
    public boolean deleteRole(Long roleId) {
        if (roleId == null) {
            System.out.println("Role ID cannot be null");
            return false;
        }

        Optional<Role> roleOpt = roleRepository.findByIdOptional(roleId);
        if (roleOpt.isEmpty()) {
            System.out.println("Role not found");
            return false;
        }

        Role role = roleOpt.get();

        // Check if role is assigned to any users
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            System.out.println("Cannot delete role: " + role.getUsers().size() + " users have this role");
            return false;
        }

        roleRepository.delete(role);
        System.out.println("Role deleted successfully: " + role.getName());
        return true;
    }

    /**
     * Display role details
     */
    public void displayRoleDetails(Long roleId) {
        if (roleId == null) {
            System.out.println("Role ID cannot be null");
            return;
        }

        Optional<Role> roleOpt = roleRepository.findByIdOptional(roleId);
        if (roleOpt.isEmpty()) {
            System.out.println("Role not found");
            return;
        }

        Role role = roleOpt.get();

        System.out.println("=== Role Details ===");
        System.out.println("Name: " + role.getName());
        System.out.println("Permissions: " + (role.getPermissions() != null ? String.join(", ", role.getPermissions()) : "None"));
        System.out.println("Users Count: " + (role.getUsers() != null ? role.getUsers().size() : 0));
        System.out.println("===================");
    }
} 