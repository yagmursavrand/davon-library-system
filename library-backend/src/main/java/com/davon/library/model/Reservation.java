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
 * Reservation entity - represents a book reservation in the library system.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in ReservationService.
 * 
 * @see com.davon.library.service.ReservationService for business operations
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reservation_date", nullable = false)
    private Date reservationDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "notification_date")
    private Date notificationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
    

    
    @Column(name = "notes", length = 500)
    private String notes;
    
    // Many-to-One relationship with Book
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Book book;
    
    // Many-to-One relationship with Member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private Member member;
    
    // Reservation status enum
    public enum ReservationStatus {
        PENDING, ACTIVE, FULFILLED, EXPIRED, CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        if (reservationDate == null) {
            reservationDate = new Date();
        }
        if (expiryDate == null) {
            // BUG 1: Wrong expiry period - should be 7 days, but set to 5 days
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(reservationDate);
            cal.add(java.util.Calendar.DAY_OF_MONTH, 7); // BUG: Should be 7
            expiryDate = cal.getTime();
        }
    }
} 