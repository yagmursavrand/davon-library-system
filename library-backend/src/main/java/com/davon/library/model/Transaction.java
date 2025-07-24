package com.davon.library.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.Date;

import jakarta.persistence.*;

/**
 * Transaction entity - represents a financial transaction.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in TransactionService.
 * 
 * @see com.davon.library.service.TransactionService for business operations
 */
@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"}) // Exclude to prevent circular references
@EqualsAndHashCode(exclude = {"user"}) // Exclude to prevent circular references
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date", nullable = false)
    private Date date = new Date();
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "amount", nullable = false)
    private double amount;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    // Many-to-One relationship with User (who initiated the transaction)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    public enum TransactionStatus {
        PENDING, COMPLETED, CANCELLED, FAILED
    }
    
    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = new Date();
        }
    }
} 