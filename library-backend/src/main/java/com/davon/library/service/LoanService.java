package com.davon.library.service;

import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.repository.LoanRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.Calendar;
import java.util.Optional;

/**
 * LoanService - handles all loan-related business operations
 */
@ApplicationScoped
public class LoanService {
    
    @Inject
    LoanRepository loanRepository;
    
    /**
     * Calculate fine for loan
     */
    public double calculateFine(Long loanId) {
        if (loanId == null) {
            return 0.0;
        }
        
        Optional<Loan> loanOpt = loanRepository.findByIdOptional(loanId);
        if (loanOpt.isEmpty()) {
            return 0.0;
        }
        
        Loan loan = loanOpt.get();
        
        if (loan.getReturnDate() != null || !isOverdue(loanId)) {
            return 0.0;
        }
        
        // Calculate days overdue
        long diffInMillies = new Date().getTime() - loan.getDueDate().getTime();
        long daysOverdue = diffInMillies / (24 * 60 * 60 * 1000);
        
        // Fine rate: $1 per day overdue
        double calculatedFine = daysOverdue * 1.0;
        
        // Maximum fine: $50
        return Math.min(calculatedFine, 50.0);
    }
    
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
            // Use Calendar to compare dates properly
            // A loan is overdue only if current date is AFTER the due date
            // If due date is today but later in the day, it's not overdue yet
            Calendar now = Calendar.getInstance();
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(loan.getDueDate());
            
            return now.after(dueDate);
        }
        return false;
    }
    
    /**
     * Mark loan as returned
     */
    @Transactional
    public boolean markAsReturned(Long loanId) {
        if (loanId == null) {
            System.out.println("Loan ID cannot be null");
            return false;
        }
        
        Optional<Loan> loanOpt = loanRepository.findByIdOptional(loanId);
        if (loanOpt.isEmpty()) {
            System.out.println("Loan not found");
            return false;
        }
        
        Loan loan = loanOpt.get();
        
        if (loan.getReturnDate() != null) {
            System.out.println("Loan already returned");
            return false;
        }
        
        // Calculate final fine if overdue BEFORE setting return date
        boolean wasOverdue = isOverdue(loanId);
        if (wasOverdue) {
            double fine = calculateFine(loanId);
            loan.setFineAmount(fine);
        }
        
        // Now set return date and status
        loan.setReturnDate(new Date());
        loan.setStatus(Loan.LoanStatus.RETURNED);
        
        System.out.println("Loan marked as returned. Fine amount: $" + loan.getFineAmount());
        return true;
    }
    
    /**
     * Renew loan
     */
    @Transactional
    public boolean renewLoan(Long loanId, int additionalDays) {
        if (loanId == null) {
            System.out.println("Loan ID cannot be null");
            return false;
        }
        
        Optional<Loan> loanOpt = loanRepository.findByIdOptional(loanId);
        if (loanOpt.isEmpty()) {
            System.out.println("Loan not found");
            return false;
        }
        
        if (additionalDays <= 0 || additionalDays > 14) {
            System.out.println("Invalid renewal period: Must be 1-14 days");
            return false;
        }
        
        Loan loan = loanOpt.get();
        
        if (loan.getReturnDate() != null) {
            System.out.println("Cannot renew: Book already returned");
            return false;
        }
        
        if (isOverdue(loanId)) {
            System.out.println("Cannot renew: Loan is overdue");
            return false;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(loan.getDueDate());
        cal.add(Calendar.DAY_OF_MONTH, additionalDays);
        loan.setDueDate(cal.getTime());
        loan.setStatus(Loan.LoanStatus.RENEWED);
        
        System.out.println("Loan renewed. New due date: " + loan.getDueDate());
        return true;
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
    
    /**
     * Create new loan
     */
    @Transactional
    public Loan createLoan(Member member, Book book, int loanPeriodDays) {
        if (member == null || book == null) {
            System.out.println("Member and book cannot be null");
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
        loan.setFineAmount(0.0);
        
        loanRepository.persist(loan);
        
        System.out.println("Loan created for book '" + book.getTitle() + "' to member '" + member.getName() + "'. Due: " + loan.getDueDate());
        return loan;
    }
} 
 