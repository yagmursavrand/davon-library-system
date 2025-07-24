package com.davon.library.repository;

import com.davon.library.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
    
    /**
     * Find users by role
     */
    public List<User> findByRole(String role) {
        return find("role", role).list();
    }
    
    /**
     * Find active users (logged in)
     */
    public List<User> findActiveUsers() {
        return find("isLoggedIn", true).list();
    }
    
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return count("email", email) > 0;
    }
    
    /**
     * Find users created after a date
     */
    public List<User> findUsersCreatedAfter(java.util.Date date) {
        return find("createdAt > ?1", date).list();
    }
    
    /**
     * Update user login status
     */
    public void updateLoginStatus(Long userId, boolean isLoggedIn) {
        update("isLoggedIn = ?1, lastLoginDate = ?2 where id = ?3", 
               isLoggedIn, new java.util.Date(), userId);
    }
} 