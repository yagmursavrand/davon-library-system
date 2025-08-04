package com.davon.library.service;

import com.davon.library.model.Admin;
import com.davon.library.model.User;
import com.davon.library.model.Fine;
import com.davon.library.repository.FineRepository;
import com.davon.library.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Date;



@ApplicationScoped
public class AdminService {
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    FineRepository fineRepository;

    public List<Fine> getAllUnpaidFines() {
        return fineRepository.findUnpaidFines();
    }

    /**
     * Add a new user to the system (Admin functionality)
     */
    @Transactional
    public boolean addUser(Admin admin, User user) {
        if (admin == null || user == null) {
            System.out.println("Admin and user cannot be null");
            return false;
        }
        
        if (!hasPermission(admin, "ADD_USER")) {
            System.out.println("Admin does not have permission to add users");
            return false;
        }
        
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            System.out.println("User name is required");
            return false;
        }
        
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            System.out.println("Valid email is required");
            return false;
        }
        
        // Check if user already exists
        if (userRepository.emailExists(user.getEmail())) {
            System.out.println("User with email " + user.getEmail() + " already exists");
            return false;
        }
        
        // Set creation date
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(new Date());
        }
        
        // Persist user
        userRepository.persist(user);
        
        // Update admin's last action
        admin.setLastAdminAction(new Date());
        
        System.out.println("Admin " + admin.getName() + " added user: " + user.getName() + " (" + user.getEmail() + ")");
        return true;
    }
    
    /**
     * Remove user from the system (Admin functionality)
     */
    @Transactional
    public boolean removeUser(Admin admin, Long userId) {
        if (admin == null || userId == null) {
            System.out.println("Admin and user ID cannot be null");
            return false;
        }
        
        if (!hasPermission(admin, "DELETE_USER")) {
            System.out.println("Admin does not have permission to delete users");
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByIdOptional(userId);
        if (userOpt.isEmpty()) {
            System.out.println("User with ID " + userId + " not found");
            return false;
        }
        
        User userToRemove = userOpt.get();
        
        // Prevent admin from removing themselves
        if (userId.equals(admin.getId())) {
            System.out.println("Admin cannot remove themselves");
            return false;
        }
        
        // Delete user
        userRepository.delete(userToRemove);
        
        // Update admin's last action
        admin.setLastAdminAction(new Date());
        
        System.out.println("Admin " + admin.getName() + " removed user: " + userToRemove.getName());
        return true;
    }
    
    /**
     * Update existing user information (Admin functionality)
     */
    @Transactional
    public boolean updateUser(Admin admin, Long userId, User updatedUser) {
        if (admin == null || userId == null || updatedUser == null) {
            System.out.println("Admin, user ID, and updated user data are required");
            return false;
        }
        
        if (!hasPermission(admin, "UPDATE_USER")) {
            System.out.println("Admin does not have permission to update users");
            return false;
        }
        
        Optional<User> existingUserOpt = userRepository.findByIdOptional(userId);
        if (existingUserOpt.isEmpty()) {
            System.out.println("User with ID " + userId + " not found");
            return false;
        }
        
        User existingUser = existingUserOpt.get();
        
        if (updatedUser.getName() == null || updatedUser.getName().trim().isEmpty()) {
            System.out.println("User name cannot be empty");
            return false;
        }
        
        if (updatedUser.getEmail() == null || !updatedUser.getEmail().contains("@")) {
            System.out.println("Valid email is required");
            return false;
        }
        
        // Check if new email conflicts with existing user (except current user)
        Optional<User> emailConflict = userRepository.findByEmail(updatedUser.getEmail());
        if (emailConflict.isPresent() && !emailConflict.get().getId().equals(userId)) {
            System.out.println("Email already exists for another user");
            return false;
        }
        
        // Update user fields
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(updatedUser.getPassword());
        }
        existingUser.setUpdatedAt(new Date());
        
        // Update admin's last action
        admin.setLastAdminAction(new Date());
        
        System.out.println("Admin " + admin.getName() + " updated user: " + existingUser.getName());
        return true;
    }
    
    /**
     * View all users in the system (Admin functionality)
     */
    public List<User> viewAllUsers(Admin admin) {
        if (admin == null) {
            System.out.println("Admin cannot be null");
            return List.of();
        }
        
                if (!hasPermission(admin, "VIEW_USERS")) {
            System.out.println("Admin does not have permission to view users");
            return List.of();
        }

        List<User> users = userRepository.listAll();
        
        System.out.println("Admin " + admin.getName() + " is viewing all users:");
        System.out.println("Total users in system: " + users.size());
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            System.out.println((i + 1) + ". " + user.getName() + " (" + user.getEmail() + ") - Role: " + user.getRole());
        }
        
        // Update admin's last action
        admin.setLastAdminAction(new Date());
        
        return users;
    }
    
    /**
     * Get user statistics (Admin functionality)
     */
    public void getUserStatistics(Admin admin) {
        if (admin == null) {
            System.out.println("Admin cannot be null");
            return;
        }
        
        if (!hasPermission(admin, "VIEW_USERS")) {
            System.out.println("Admin does not have permission to view statistics");
            return;
        }
        
        long totalUsers = userRepository.count();
        long adminCount = userRepository.findByRole("ADMIN").size();
        long memberCount = userRepository.findByRole("MEMBER").size();
        long userCount = userRepository.findByRole("USER").size();
        long activeUsers = userRepository.findActiveUsers().size();
        
        System.out.println("=== User Statistics ===");
        System.out.println("Total Users: " + totalUsers);
        System.out.println("Admins: " + adminCount);
        System.out.println("Members: " + memberCount);
        System.out.println("Regular Users: " + userCount);
        System.out.println("Currently Active: " + activeUsers);
        System.out.println("Requested by: " + admin.getName());
        System.out.println("=======================");
        
        // Update admin's last action
        admin.setLastAdminAction(new Date());
    }
    
    /**
     * Find user by email (Admin functionality)
     */
    public Optional<User> findUserByEmail(Admin admin, String email) {
        if (admin == null || !hasPermission(admin, "VIEW_USERS")) {
            System.out.println("Admin does not have permission");
            return Optional.empty();
        }
        
        admin.setLastAdminAction(new Date());
        return userRepository.findByEmail(email);
    }
    
    /**
     * Check if admin has permission for specific action
     */
    public boolean hasPermission(Admin admin, String action) {
        if (admin == null || action == null || action.trim().isEmpty()) {
            return false;
        }
        
        // Super admin has all permissions
        if ("SUPER_ADMIN".equals(admin.getAdminLevel())) {
            return true;
        }
        
        // Senior admin has most permissions
        if ("SENIOR".equals(admin.getAdminLevel())) {
            return !action.equals("DELETE_ADMIN") && !action.equals("SYSTEM_CONFIG");
        }
        
        // Standard admin has basic permissions
        return action.equals("VIEW_USERS") || action.equals("ADD_USER") || action.equals("UPDATE_USER");
    }
    
    /**
     * Promote admin level (only super admin can do this)
     */
    @Transactional
    public boolean promoteAdmin(Admin requestingAdmin, Admin targetAdmin, String newLevel) {
        if (requestingAdmin == null || targetAdmin == null || newLevel == null) {
            System.out.println("All parameters are required");
            return false;
        }
        
        if (!"SUPER_ADMIN".equals(requestingAdmin.getAdminLevel())) {
            System.out.println("Only super admins can promote other admins");
            return false;
        }
        
        // Validate new level
        if (!isValidAdminLevel(newLevel)) {
            System.out.println("Invalid admin level: " + newLevel);
            return false;
        }
        
        targetAdmin.setAdminLevel(newLevel);
        targetAdmin.setUpdatedAt(new Date());
        requestingAdmin.setLastAdminAction(new Date());
        
        System.out.println("Admin " + targetAdmin.getName() + " promoted to " + newLevel + " by " + requestingAdmin.getName());
        return true;
    }
    
    /**
     * Get admin statistics
     */
    public void getAdminStatistics(Admin admin) {
        if (admin == null) {
            System.out.println("Admin cannot be null");
            return;
        }
        
        System.out.println("=== Admin Statistics ===");
        System.out.println("Admin: " + admin.getName());
        System.out.println("Level: " + admin.getAdminLevel());
        System.out.println("Department: " + (admin.getDepartment() != null ? admin.getDepartment() : "Not specified"));
        System.out.println("Last Action: " + (admin.getLastAdminAction() != null ? admin.getLastAdminAction() : "No recent actions"));
        System.out.println("Login Status: " + (admin.isLoggedIn() ? "Logged In" : "Logged Out"));
        System.out.println("========================");
        
        admin.setLastAdminAction(new Date());
    }
    
    /**
     * Create new admin account
     */
    @Transactional
    public Admin createAdmin(String name, String email, String password, String level, String department) {
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.length() < 6) {
            System.out.println("Invalid admin details");
            return null;
        }
        
        // Check if email already exists
        if (userRepository.emailExists(email)) {
            System.out.println("Email already exists: " + email);
            return null;
        }
        
        Admin admin = new Admin();
        admin.setName(name.trim());
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(password);
        admin.setRole("ADMIN");
        admin.setCreatedAt(new Date());
        admin.setAdminLevel(level != null ? level : "STANDARD");
        admin.setDepartment(department);
        
        userRepository.persist(admin);
        
        System.out.println("Admin created successfully: " + admin.getName() + " (" + admin.getAdminLevel() + ")");
        return admin;
    }
    
    // Helper methods
    private boolean isValidAdminLevel(String level) {
        return "STANDARD".equals(level) || "SENIOR".equals(level) || "SUPER_ADMIN".equals(level);
    }
} 