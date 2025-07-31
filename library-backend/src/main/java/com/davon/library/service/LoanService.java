package com.davon.library.service;

import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.repository.LoanRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Calendar;
import java.util.Optional;

/**
 * LoanService - handles essential loan operations
 * Simplified to focus only on core loan business logic
 */
@ApplicationScoped
public class LoanService {
    
    @Inject
    LoanRepository loanRepository;
    
    /**
     * Check if loan is overdue
     */
    public boolean isOverdue(Long loanId) {
        if (loanId == null) {
            return false;
        }
        
        Optional<Loan> loanOpt = loanRepository.findByIdOptional(loanId);
        if (loanOpt.isEmpty()) {
            return false;
        }
        
        Loan loan = loanOpt.get();
        
        if (loan.getDueDate() != null && loan.getReturnDate() == null) {
            Calendar now = Calendar.getInstance();
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(loan.getDueDate());
            
            return now.after(dueDate);
        }
        return false;
    }
    
    /**
     * Mark loan as returned - simplified
     */
    @Transactional
    public boolean markAsReturned(Long loanId) {
        if (loanId == null) {
            return false;
        }
        
        Optional<Loan> loanOpt = loanRepository.findByIdOptional(loanId);
        if (loanOpt.isEmpty()) {
            return false;
        }
        
        Loan loan = loanOpt.get();
        
        if (loan.getReturnDate() != null) {
            return false; // Already returned
        }
        
        // Simply mark as returned
        loan.setReturnDate(new Date());
        loan.setStatus(Loan.LoanStatus.RETURNED);
        
        return true;
    }
    
    /**
     * Create new loan
     */
    @Transactional
    public Loan createLoan(Member member, Book book, int loanPeriodDays) {
        if (member == null || book == null) {
            return null;
        }
        
        if (loanPeriodDays <= 0) {
            loanPeriodDays = 14; // Default 14 days
        }
        
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setLoanDate(new Date());
        
        // Set due date
        Calendar cal = Calendar.getInstance();
        cal.setTime(loan.getLoanDate());
        cal.add(Calendar.DAY_OF_MONTH, loanPeriodDays);
        loan.setDueDate(cal.getTime());
        
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loan.setFineAmount(BigDecimal.ZERO);
        
        loanRepository.persist(loan);
        
        return loan;
    }
    
    /**
     * Get days until due
     */
    public long getDaysUntilDue(Long loanId) {
        if (loanId == null) {
            return 0;
        }
        
        Optional<Loan> loanOpt = loanRepository.findByIdOptional(loanId);
        if (loanOpt.isEmpty()) {
            return 0;
        }
        
        Loan loan = loanOpt.get();
        
        if (loan.getReturnDate() != null) {
            return 0; // Already returned
        }
        
        long diffInMillies = loan.getDueDate().getTime() - new Date().getTime();
        return Math.max(0, diffInMillies / (24 * 60 * 60 * 1000));
    }
    
    /**
     * Get days overdue
     */
    public long getDaysOverdue(Long loanId) {
        if (!isOverdue(loanId)) {
            return 0;
        }
        
        Optional<Loan> loanOpt = loanRepository.findByIdOptional(loanId);
        if (loanOpt.isEmpty()) {
            return 0;
        }
        
        Loan loan = loanOpt.get();
        
        long diffInMillies = new Date().getTime() - loan.getDueDate().getTime();
        return diffInMillies / (24 * 60 * 60 * 1000);
    }
} 
 