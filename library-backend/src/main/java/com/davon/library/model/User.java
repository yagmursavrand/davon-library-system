package com.davon.library.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.Date;

import jakarta.persistence.*;

/**
 * User entity - represents a user in the library system.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in UserService.
 * 
 * @see com.davon.library.service.UserService for business operations
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "role", nullable = false, length = 50)
    private String role;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
    
    @Column(name = "is_logged_in")
    private boolean isLoggedIn = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login_date")
    private Date lastLoginDate;
    
    // One-to-One relationship with Profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Profile profile;
    
    // Many-to-One relationship with Role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnore
    private Role userRole;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
} 