package com.davon.library.service;

import com.davon.library.model.User;
import com.davon.library.model.Member;
import com.davon.library.repository.UserRepository;
import com.davon.library.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * UserService - handles all user-related business operations
 */
@ApplicationScoped
public class UserService {
    
    @Inject
    UserRepository userRepository;

    @Inject
    MemberRepository memberRepository;
    
    /**
     * Register a new user, who will always be a Member.
     */
    @Transactional
    public User registerUser(String name, String email, String password) {
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.length() < 6) {
            System.out.println("Invalid registration details: Name, email, and password (min 6 chars) are required.");
            return null;
        }

        // Check if email already exists
        if (userRepository.emailExists(email.trim().toLowerCase())) {
            System.out.println("Email already exists: " + email);
            return null;
        }

        // Create a Member object directly, which is also a User
        Member member = new Member();

        // Set User properties
        member.setName(name.trim());
        member.setEmail(email.trim().toLowerCase());
        member.setPassword(password); // In a real app, hash this!
        member.setRole("MEMBER"); 
        member.setCreatedAt(new Date());
        member.setLoggedIn(false);

        // Set Member-specific properties
        member.setMembershipStart(new Date());

        // Set membership to expire in one year
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        member.setMembershipEnd(cal.getTime());

        // Initialize collections to avoid issues
        member.setBorrowedBookIds(new ArrayList<>());
        member.setFineHistory(new ArrayList<>());
        
        // Persist the Member. Hibernate will handle creating both the User and Member records.
        memberRepository.persist(member);
        
        // The membership number can only be set after the ID is generated.
        member.setMembershipNumber("MEM-" + member.getId());

        System.out.println("User and Member registered successfully: " + member.getName() + " (ID: " + member.getId() + ")");

        return member;
    }

    /**
     * User login
     */
    @Transactional
    public boolean login(String email, String password) {
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            System.out.println("Email and password are required");
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            System.out.println("User not found with email: " + email);
            return false;
        }
        
        User user = userOpt.get();
        
        if (user.getPassword().equals(password)) {
            user.setLoggedIn(true);
            user.setLastLoginDate(new Date());
            System.out.println("User " + user.getEmail() + " logged in successfully at " + user.getLastLoginDate());
            return true;
        } else {
            System.out.println("Invalid password for user: " + user.getEmail());
            return false;
        }
    }
    
    /**
     * User logout
     */
    @Transactional
    public boolean logout(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email is required");
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            System.out.println("User not found with email: " + email);
            return false;
        }
        
        User user = userOpt.get();
        
        if (user.isLoggedIn()) {
            user.setLoggedIn(false);
            System.out.println("User " + user.getEmail() + " logged out successfully");
            return true;
        } else {
            System.out.println("User " + user.getEmail() + " was not logged in");
            return false;
        }
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public boolean updateProfile(Long userId, String newName, String newEmail) {
        if (userId == null) {
            System.out.println("User ID is required");
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByIdOptional(userId);
        if (userOpt.isEmpty()) {
            System.out.println("User not found");
            return false;
        }
        
        User user = userOpt.get();
        boolean updated = false;
        
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(user.getName())) {
            user.setName(newName);
            updated = true;
        }
        
        if (newEmail != null && !newEmail.trim().isEmpty() && !newEmail.equals(user.getEmail())) {
            // Basic email validation
            if (newEmail.contains("@") && newEmail.contains(".")) {
                // Check if email already exists for another user
                Optional<User> emailConflict = userRepository.findByEmail(newEmail);
                if (emailConflict.isPresent() && !emailConflict.get().getId().equals(userId)) {
                    System.out.println("Email already exists for another user");
                    return false;
                }
                user.setEmail(newEmail);
                updated = true;
            } else {
                System.out.println("Invalid email format: " + newEmail);
                return false;
            }
        }
        
        if (updated) {
            user.setUpdatedAt(new Date());
            System.out.println("Profile updated for user: " + user.getName());
        } else {
            System.out.println("No changes made to profile");
        }
        
        return updated;
    }
    
    /**
     * Update user profile with password
     */
    @Transactional
    public boolean updateProfile(Long userId, String newName, String newEmail, String newPassword) {
        boolean profileUpdated = updateProfile(userId, newName, newEmail);
        
        if (newPassword != null && !newPassword.trim().isEmpty() && newPassword.length() >= 6) {
            Optional<User> userOpt = userRepository.findByIdOptional(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(newPassword);
                user.setUpdatedAt(new Date());
                System.out.println("Password updated for user: " + user.getName());
                return true;
            }
        } else if (newPassword != null) {
            System.out.println("Password must be at least 6 characters long");
            return false;
        }
        
        return profileUpdated;
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(Long userId, String requiredRole) {
        if (userId == null || requiredRole == null) {
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByIdOptional(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        return user.getRole() != null && user.getRole().equalsIgnoreCase(requiredRole);
    }
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin(Long userId) {
        return hasRole(userId, "admin");
    }
    
    /**
     * Create new user
     */
    @Transactional
    public User createUser(String name, String email, String password, String role) {
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.length() < 6) {
            System.out.println("Invalid user details");
            return null;
        }
        
        // Check if email already exists
        if (userRepository.emailExists(email)) {
            System.out.println("Email already exists: " + email);
            return null;
        }
        
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(password);
        user.setRole(role != null ? role : "USER");
        user.setCreatedAt(new Date());
        user.setLoggedIn(false);
        
        userRepository.persist(user);
        
        System.out.println("User created successfully: " + user.getName() + " (" + user.getRole() + ")");
        return user;
    }
    
    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase());
    }
    
    /**
     * Update login status
     */
    @Transactional
    public boolean updateLoginStatus(Long userId, boolean isLoggedIn) {
        if (userId == null) {
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByIdOptional(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        user.setLoggedIn(isLoggedIn);
        
        if (isLoggedIn) {
            user.setLastLoginDate(new Date());
        }

        return true;
    }
}