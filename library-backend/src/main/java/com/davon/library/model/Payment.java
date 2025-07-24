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
 * Payment entity - represents a payment transaction extending Transaction.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in TransactionService and PaymentService.
 * 
 * @see com.davon.library.service.TransactionService for business operations
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"fine"}) // Exclude to prevent circular references
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"fine"}, callSuper = true) // Exclude to prevent circular references
public class Payment extends Transaction {
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payment_date", nullable = false)
    private Date paymentDate = new Date();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(name = "confirmation_number", length = 100)
    private String confirmationNumber;
    
    // Many-to-One relationship with Fine (if this payment is for a fine)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fine_id")
    private Fine fine;
    
    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, PAYPAL, OTHER
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (paymentDate == null) {
            paymentDate = new Date();
        }
        if (getType() == null) {
            setType("PAYMENT");
        }
        if (referenceNumber == null) {
            referenceNumber = "PAY" + System.currentTimeMillis();
        }
    }
} 