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
 * Fine entity - represents a fine transaction extending Transaction.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in TransactionService and FineService.
 * 
 * @see com.davon.library.service.TransactionService for business operations
 */
@Entity
@Table(name = "fines")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"loan"}) // Exclude to prevent circular references
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"loan"}, callSuper = true) // Exclude to prevent circular references
public class Fine extends Transaction {
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued_date", nullable = false)
    private Date issuedDate = new Date();
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "paid_date")
    private Date paidDate;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fine_type", nullable = false)
    private FineType fineType = FineType.OVERDUE;
    
    @Column(name = "is_paid")
    private boolean paid = false;
    
    // Many-to-One relationship with Loan (the loan that caused this fine)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;
    
    public enum FineType {
        OVERDUE, DAMAGE, LOST_BOOK, LATE_RETURN, OTHER
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (issuedDate == null) {
            issuedDate = new Date();
        }
        if (getType() == null) {
            setType("FINE");
        }
    }
} 