package com.davon.library.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Calendar;

import jakarta.persistence.*;

/**
 * Loan entity - represents a book loan in the library system.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in LoanService.
 * 
 * @see com.davon.library.service.LoanService for business operations
 */
@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "loan_date", nullable = false)
    private Date loanDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_date", nullable = false)
    private Date dueDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "return_date")
    private Date returnDate;
    
    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status = LoanStatus.ACTIVE;
    
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
    
    // Loan status enum
    public enum LoanStatus {
        ACTIVE, RETURNED, OVERDUE, RENEWED, LOST
    }
    
    @PrePersist
    protected void onCreate() {
        if (loanDate == null) {
            loanDate = new Date();
        }
        if (dueDate == null) {
            // Default loan period is 14 days
            Calendar cal = Calendar.getInstance();
            cal.setTime(loanDate);
            cal.add(Calendar.DAY_OF_MONTH, 14);
            dueDate = cal.getTime();
        }
    }
} 