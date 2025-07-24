package com.davon.library.service;

import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.model.Loan;
import com.davon.library.model.Fine;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.LoanRepository;
import com.davon.library.repository.FineRepository;
import com.davon.library.service.TransactionService;
import com.davon.library.service.InventoryService;
import com.davon.library.service.LoanService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.Calendar;
import java.util.Optional;
import java.util.ArrayList;
import com.davon.library.model.Transaction;

@ApplicationScoped
public class MemberService {
    
    @Inject
    MemberRepository memberRepository;
    
    @Inject
    BookRepository bookRepository;
    
    @Inject
    LoanRepository loanRepository;
    
    @Inject
    FineRepository fineRepository;
    
    @Inject
    TransactionService transactionService;
    
    @Inject
    InventoryService inventoryService;
    
    @Inject
    LoanService loanService;
    
    /**
     * Register a new member (implements Member.register())
     */
    @Transactional
    public Member registerMember(String name, String email, String password) {
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.length() < 6) {
            throw new IllegalArgumentException("Invalid registration details");
        }
        
        // Check if member already exists
        Optional<Member> existingMember = memberRepository.findByEmail(email);
        if (existingMember.isPresent()) {
            throw new IllegalArgumentException("Member with email already exists");
        }
        
        Member member = new Member();
        member.setName(name.trim());
        member.setEmail(email.trim().toLowerCase());
        member.setPassword(password);
        member.setRole("MEMBER");
        member.setCreatedAt(new Date());
        
        // Initialize membership details
        member.setMembershipNumber("MEM" + System.currentTimeMillis());
        member.setMembershipStart(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1); // 1 year membership
        member.setMembershipEnd(cal.getTime());
        
        // Initialize collections (Lombok doesn't initialize with values)
        member.setBorrowedBookIds(new ArrayList<>());
        member.setFineHistory(new ArrayList<>());
        member.setTotalFines(0.0);
        
        memberRepository.persist(member);
        System.out.println("Member registered successfully: " + member.getMembershipNumber());
        return member;
    }
    
    /**
     * Borrow a book for a member
     */
    @Transactional
    public boolean borrowBook(Member member, Long bookId) {
        if (member == null || bookId == null) {
            System.out.println("Member and book ID cannot be null");
            return false;
        }
        
        // Business validation: Check membership status
        if (!isMembershipActive(member)) {
            System.out.println("Cannot borrow book: Membership is expired");
            return false;
        }
        
        // Business validation: Check outstanding fines
        if (!canMemberBorrowDueToFines(member)) {
            System.out.println("Cannot borrow book: Outstanding fines exceed $50");
            return false;
        }
        
        // Business validation: Check borrowing limit
        if (!canMemberBorrowDueToLimit(member)) {
            System.out.println("Cannot borrow book: Maximum borrowing limit (5 books) reached");
            return false;
        }
        
        // Get book
        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found");
            return false;
        }
        
        Book book = bookOpt.get();
        
        // Business validation: Check if book is available
        if (book.getStatus() != Book.BookStatus.AVAILABLE) {
            System.out.println("Book is not available for borrowing");
            return false;
        }
        
        // Business validation: Check if member already has this book
        if (hasMemberAlreadyBorrowedBook(member, book)) {
            System.out.println("Member already has this book borrowed");
            return false;
        }
        
        // Business operation: Update member's borrowed books
        member.getBorrowedBookIds().add(bookId);
        
        // Business operation: Update book status
        book.setStatus(Book.BookStatus.BORROWED);
        
        // Update inventory
        if (book.getInventory() != null) {
            inventoryService.borrowCopy(book.getInventory().getId());
        }
        
        // Business operation: Create loan record
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setLoanDate(new Date());
        
        // Set due date (14 days from now)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 14);
        loan.setDueDate(cal.getTime());
        
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        
        loanRepository.persist(loan);
        
        System.out.println("Book '" + book.getTitle() + "' borrowed successfully by " + member.getName());
        return true;
    }
    
    // Business logic helper methods
    private boolean isMembershipActive(Member member) {
        return member.getMembershipEnd() != null && member.getMembershipEnd().after(new Date());
    }
    
    private boolean canMemberBorrowDueToFines(Member member) {
        double totalFines = fineRepository.calculateTotalUnpaidFines(member);
        return totalFines <= 50.0;
    }
    
    private boolean canMemberBorrowDueToLimit(Member member) {
        long activeLoanCount = loanRepository.countActiveLoansByMember(member);
        return activeLoanCount < 5;
    }
    
    private boolean hasMemberAlreadyBorrowedBook(Member member, Book book) {
        Optional<Loan> existingLoan = loanRepository.findActiveLoanByMemberAndBook(member, book);
        return existingLoan.isPresent();
    }
    
    private boolean isMembershipExpiringSoon(Member member) {
        if (member.getMembershipEnd() == null) return false;
        
        Calendar thirtyDaysFromNow = Calendar.getInstance();
        thirtyDaysFromNow.add(Calendar.DAY_OF_MONTH, 30);
        
        return member.getMembershipEnd().before(thirtyDaysFromNow.getTime()) && isMembershipActive(member);
    }
    
    private String getMemberStatus(Member member) {
        if (!isMembershipActive(member)) {
            return "EXPIRED";
        } else if (isMembershipExpiringSoon(member)) {
            return "EXPIRING_SOON";
        } else if (member.getTotalFines() > 50.0) {
            return "SUSPENDED";
        } else {
            return "ACTIVE";
        }
    }
    
    /**
     * Return a book (implements Member.returnBook())
     */
    @Transactional
    public boolean returnBook(Member member, Long bookId) {
        if (member == null || bookId == null) {
            System.out.println("Member and book ID cannot be null");
            return false;
        }
        
        // Get book
        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found");
            return false;
        }
        
        Book book = bookOpt.get();
        
        // Find active loan
        Optional<Loan> loanOpt = loanRepository.findActiveLoanByMemberAndBook(member, book);
        if (loanOpt.isEmpty()) {
            System.out.println("No active loan found for this book and member");
            return false;
        }
        
        Loan loan = loanOpt.get();
        
        // Mark loan as returned
        loanService.markAsReturned(loan.getId());
        
        // Update book status
        book.setStatus(Book.BookStatus.AVAILABLE);
        
        // Update inventory
        if (book.getInventory() != null) {
            inventoryService.returnCopy(book.getInventory().getId());
        }
        
        // Remove from member's borrowed books list
        member.getBorrowedBookIds().remove(bookId);
        
        // Check if overdue and create fine if necessary
        if (loanService.isOverdue(loan.getId())) {
            double fineAmount = loanService.calculateFine(loan.getId());
            if (fineAmount > 0) {
                Fine fine = new Fine();
                fine.setAmount(fineAmount);
                fine.setDescription("Overdue return");
                fine.setType("FINE");
                fine.setUser(member);
                fine.setReason("Overdue return");
                fine.setFineType(Fine.FineType.OVERDUE);
                fine.setLoan(loan);
                fine.setIssuedDate(new Date());
                fine.setPaid(false);
                fine.setStatus(Transaction.TransactionStatus.PENDING);
                fine.setDate(new Date());
                
                fineRepository.persist(fine);
                
                // Business operation: Add fine to member
                member.setTotalFines(member.getTotalFines() + fineAmount);
                member.getFineHistory().add("Fine added: $" + fineAmount + " - Overdue return on " + new Date());
                
                System.out.println("Overdue fine of $" + fineAmount + " applied");
            }
        }
        
        System.out.println("Book '" + book.getTitle() + "' returned successfully by " + member.getName());
        return true;
    }
    
    /**
     * Pay fine (implements Member.payFine())
     */
    @Transactional
    public boolean payFine(Member member, Long fineId, double amount, String description) {
        if (member == null || fineId == null || amount <= 0) {
            System.out.println("Invalid payment parameters");
            return false;
        }
        
        // Get fine
        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            System.out.println("Fine not found");
            return false;
        }
        
        Fine fine = fineOpt.get();
        
        // Check if fine belongs to member
        if (!fine.getUser().getId().equals(member.getId())) {
            System.out.println("Fine does not belong to this member");
            return false;
        }
        
        // Check if fine is already paid
        if (fine.isPaid() || fine.getStatus() == Transaction.TransactionStatus.COMPLETED) {
            System.out.println("Fine is already paid");
            return false;
        }
        
        // Check payment amount
        if (amount > fine.getAmount()) {
            System.out.println("Payment amount exceeds fine amount");
            return false;
        }
        
        // Business operation: Apply payment to member
        member.setTotalFines(member.getTotalFines() - amount);
        member.getFineHistory().add("Paid $" + amount + " - " + description + " on " + new Date());
        
        // Process payment in fine entity
        if (amount == fine.getAmount()) {
            // Full payment
            fine.setPaid(true);
            fine.setPaidDate(new Date());
            transactionService.markAsCompleted(fine.getId());
        } else {
            // Partial payment - reduce fine amount
            fine.setAmount(fine.getAmount() - amount);
        }
        
        System.out.println("Fine payment processed successfully. Amount: $" + amount);
        return true;
    }
    
    /**
     * Renew membership (implements Member.renewMembership())
     */
    @Transactional
    public boolean renewMembership(Member member, int years) {
        if (member == null || years <= 0 || years > 10) {
            System.out.println("Invalid renewal parameters");
            return false;
        }
        
        Date currentEnd = member.getMembershipEnd() != null ? member.getMembershipEnd() : new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentEnd);
        cal.add(Calendar.YEAR, years);
        
        member.setMembershipEnd(cal.getTime());
        member.setUpdatedAt(new Date());
        
        System.out.println("Membership renewed for " + member.getName() + " until: " + member.getMembershipEnd());
        return true;
    }
    
    /**
     * Get member statistics
     */
    public void getMemberStatistics(Member member) {
        if (member == null) {
            System.out.println("Member cannot be null");
            return;
        }
        
        long activeLoanCount = loanRepository.countActiveLoansByMember(member);
        double totalUnpaidFines = fineRepository.calculateTotalUnpaidFines(member);
        long totalFineCount = fineRepository.countUnpaidFinesByMember(member);
        
        System.out.println("=== Member Statistics ===");
        System.out.println("Name: " + member.getName());
        System.out.println("Membership Number: " + member.getMembershipNumber());
        System.out.println("Status: " + getMemberStatus(member));
        System.out.println("Active Loans: " + activeLoanCount);
        System.out.println("Unpaid Fines: $" + totalUnpaidFines + " (" + totalFineCount + " fines)");
        System.out.println("Membership Expires: " + member.getMembershipEnd());
        System.out.println("========================");
    }
} 