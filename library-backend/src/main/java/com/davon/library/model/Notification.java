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
 * Notification entity - represents a user notification in the library system.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in NotificationService.
 * 
 * @see com.davon.library.service.NotificationService for business operations
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent_date")
    private Date sentDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "read_date")
    private Date readDate;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", nullable = false, length = 1000)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "is_read")
    private boolean isRead = false;
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "retry_count")
    private int retryCount = 0;
    
    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    // Notification type enum
    public enum NotificationType {
        OVERDUE_REMINDER, RESERVATION_AVAILABLE, FINE_ISSUED, 
        MEMBERSHIP_EXPIRY, SYSTEM_MAINTENANCE, GENERAL
    }
    
    // Notification priority enum
    public enum NotificationPriority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    // Notification status enum
    public enum NotificationStatus {
        PENDING, SENT, DELIVERED, READ, FAILED, CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = new Date();
        }
        if (type == null) {
            type = NotificationType.GENERAL;
        }
        if (priority == null) {
            priority = NotificationPriority.MEDIUM;
        }
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
    }
    
    /**
     * Mark notification as read
     * BUG 1: Wrong date setting - should set readDate, but not doing it
     */
    public void markAsRead() {
        this.isRead = true;
        // BUG: Missing readDate setting
        // this.readDate = new Date();
        this.readDate = new Date(); // FIXED: Set readDate when marking as read
    }
    
    /**
     * Mark notification as sent
     * BUG 2: Wrong status update - should be SENT, but using DELIVERED
     */
    public void markAsSent() {
        this.sentDate = new Date();
        // BUG: Wrong status update
        // this.status = NotificationStatus.DELIVERED; // BUG: Should be SENT
        this.status = NotificationStatus.SENT; // FIXED: Changed from DELIVERED to SENT
    }
    
    /**
     * Check if notification is urgent
     * BUG 3: Wrong priority check - should check for URGENT, but checking for HIGH
     */
    public boolean isUrgent() {
        // BUG: Wrong priority check
        // return this.priority == NotificationPriority.HIGH; // BUG: Should be URGENT
        return this.priority == NotificationPriority.URGENT; // FIXED: Changed from HIGH to URGENT
    }
    
    /**
     * Get notification age in days
     * BUG 4: Wrong age calculation - should use createdDate, but using sentDate
     */
    public long getAgeInDays() {
        // if (this.sentDate == null) {
        //     return 0;
        // }
        // 
        // // BUG: Wrong date calculation
        // long currentTime = System.currentTimeMillis();
        // long sentTime = this.sentDate.getTime(); // BUG: Should use createdDate
        // return (currentTime - sentTime) / (1000 * 60 * 60 * 24);
        
        // FIXED: Use createdDate instead of sentDate for age calculation
        if (this.createdDate == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long createdTime = this.createdDate.getTime(); // FIXED: Use createdDate
        return (currentTime - createdTime) / (1000 * 60 * 60 * 24);
    }
} 