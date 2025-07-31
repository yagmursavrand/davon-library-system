package com.davon.library.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.Date;

import jakarta.persistence.*;

/**
 * Admin entity - represents an admin user extending User.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in AdminService.
 * 
 * @see com.davon.library.service.AdminService for business operations
 */
@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {
    
    @Column(name = "admin_level", length = 20)
    private String adminLevel = "STANDARD";
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_admin_action")
    private Date lastAdminAction;
    
    // Admin level enum for reference
    public enum AdminLevel {
        STANDARD, SENIOR, SUPER_ADMIN
    }
} 