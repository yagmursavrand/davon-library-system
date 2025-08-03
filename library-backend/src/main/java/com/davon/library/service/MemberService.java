package com.davon.library.service;

import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.model.Loan;
import com.davon.library.model.Fine;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.LoanRepository;
import com.davon.library.repository.FineRepository;
import com.davon.library.service.InventoryService;
import com.davon.library.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Optional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
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
    InventoryService inventoryService;
    
    @Inject
    TransactionService transactionService;
    
    @Inject
    LoanService loanService;
    
    @Inject
    FineCalculationService fineCalculationService;
    
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
        
        member.setMembershipNumber("MEM" + System.currentTimeMillis());
        member.setMembershipStart(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        member.setMembershipEnd(cal.getTime());
        
        member.setBorrowedBookIds(new ArrayList<>());
        member.setFineHistory(new ArrayList<>());
        member.setTotalFines(BigDecimal.ZERO);
        
        memberRepository.persist(member);
        System.out.println("Member registered successfully: " + member.getMembershipNumber());
        return member;
    }
    
    /**
     * Borrow a book for a member
     */
    @Transactional
    public Loan borrowBook(Member member, Long bookId) {
        if (member == null || bookId == null) {
            throw new WebApplicationException("Member and Book ID cannot be null.", Response.Status.BAD_REQUEST);
        }

        Book book = bookRepository.findByIdOptional(bookId)
            .orElseThrow(() -> new WebApplicationException("Book not found.", Response.Status.NOT_FOUND));
        
        if (book.getStatus() != Book.BookStatus.AVAILABLE) {
            throw new WebApplicationException("Book is not available for borrowing.", Response.Status.CONFLICT);
        }
        
        // Temporarily comment out other checks for debugging
        // if (!isMembershipActive(member)) {
        //     throw new WebApplicationException("Cannot borrow book: Membership is expired.", Response.Status.FORBIDDEN);
        // }
        
        // if (!canMemberBorrowDueToFines(member)) {
        //     throw new WebApplicationException("Cannot borrow book: Outstanding fines exceed limit.", Response.Status.FORBIDDEN);
        // }
        
        // if (!canMemberBorrowDueToLimit(member)) {
        //     throw new WebApplicationException("Cannot borrow book: Maximum borrowing limit (5 books) reached.", Response.Status.FORBIDDEN);
        // }
        
        // if (hasMemberAlreadyBorrowedBook(member, book)) {
        //     throw new WebApplicationException("You have already borrowed this book.", Response.Status.CONFLICT);
        // }
        
        book.setStatus(Book.BookStatus.BORROWED);
        
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        
        loanRepository.persist(loan);
        
        System.out.println("Book '" + book.getTitle() + "' borrowed successfully by " + member.getName());
        return loan;
    }
    
    private boolean isMembershipActive(Member member) {
        return member.getMembershipEnd() != null && member.getMembershipEnd().after(new Date());
    }
    
    private boolean canMemberBorrowDueToFines(Member member) {
        BigDecimal totalFines = fineRepository.calculateTotalUnpaidFines(member);
        return totalFines.compareTo(new BigDecimal("50.00")) <= 0;
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
        } else if (member.getTotalFines().compareTo(new BigDecimal("50.00")) > 0) {
            return "SUSPENDED";
        } else {
            return "ACTIVE";
        }
    }
    
    @Transactional
    public boolean returnBook(Member member, Long bookId) {
        if (member == null || bookId == null) {
            System.out.println("Member and book ID cannot be null");
            return false;
        }
        
        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found");
            return false;
        }
        
        Book book = bookOpt.get();
        
        Optional<Loan> loanOpt = loanRepository.findActiveLoanByMemberAndBook(member, book);
        if (loanOpt.isEmpty()) {
            System.out.println("No active loan found for this book and member");
            return false;
        }
        
        Loan loan = loanOpt.get();
        
        loanService.markAsReturned(loan.getId());
        book.setStatus(Book.BookStatus.AVAILABLE);
        
        if (book.getInventory() != null) {
            inventoryService.returnCopy(book.getInventory().getId());
        }
        
        member.getBorrowedBookIds().remove(bookId);
        
        if (loanService.isOverdue(loan.getId())) {
            Fine calculatedFine = fineCalculationService.calculateOverdueFine(loan);
            if (calculatedFine != null && calculatedFine.getAmount() != null) {
                member.setTotalFines(member.getTotalFines().add(calculatedFine.getAmount()));
                member.getFineHistory().add("Fine added: $" + calculatedFine.getAmount() + " - Overdue return on " + new Date());
                
                System.out.println("Overdue fine of $" + calculatedFine.getAmount() + " applied");
            }
        }
        
        System.out.println("Book '" + book.getTitle() + "' returned successfully by " + member.getName());
        return true;
    }
    
    @Transactional
    public boolean payFine(Member member, Long fineId, BigDecimal amount, String description) {
        if (member == null || fineId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Invalid payment parameters");
            return false;
        }
        
        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            System.out.println("Fine not found");
            return false;
        }
        
        Fine fine = fineOpt.get();
        
        if (!fine.getUser().getId().equals(member.getId())) {
            System.out.println("Fine does not belong to this member");
            return false;
        }
        
        if (fine.isPaid() || fine.getStatus() == Transaction.TransactionStatus.COMPLETED) {
            System.out.println("Fine is already paid");
            return false;
        }
        
        if (amount.compareTo(fine.getAmount()) > 0) {
            System.out.println("Payment amount exceeds fine amount");
            return false;
        }
        
        member.setTotalFines(member.getTotalFines().subtract(amount));
        member.getFineHistory().add("Paid $" + amount + " - " + description + " on " + new Date());
        
        if (amount.compareTo(fine.getAmount()) == 0) {
            fine.setPaid(true);
            fine.setPaidDate(new Date());
            transactionService.markAsCompleted(fine.getId());
        } else {
            fine.setAmount(fine.getAmount().subtract(amount));
        }
        
        System.out.println("Fine payment processed successfully. Amount: $" + amount);
        return true;
    }
    
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
    
    public void getMemberStatistics(Member member) {
        if (member == null) {
            System.out.println("Member cannot be null");
            return;
        }
        
        long activeLoanCount = loanRepository.countActiveLoansByMember(member);
        BigDecimal totalUnpaidFines = fineRepository.calculateTotalUnpaidFines(member);
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

    public com.davon.library.resource.MemberResource.DashboardDTO getDashboardData(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        List<Loan> allLoans = loanRepository.findByMember(member);
        
        long activeLoansCount = allLoans.stream()
            .filter(loan -> loan.getStatus() == Loan.LoanStatus.ACTIVE || loan.getStatus() == Loan.LoanStatus.RENEWED)
            .count();

        long overdueLoansCount = allLoans.stream()
            .filter(loan -> loan.getStatus() == Loan.LoanStatus.OVERDUE || 
                           (loan.getStatus() == Loan.LoanStatus.ACTIVE && loan.getDueDate().before(new Date())))
            .count();

        BigDecimal outstandingFines = fineRepository.calculateTotalUnpaidFines(member);

        List<com.davon.library.resource.MemberResource.UrgentLoanDTO> urgentLoans = allLoans.stream()
            .filter(loan -> loan.getStatus() == Loan.LoanStatus.ACTIVE || loan.getStatus() == Loan.LoanStatus.RENEWED || loan.getStatus() == Loan.LoanStatus.OVERDUE)
            .sorted(Comparator.comparing(Loan::getDueDate))
            .limit(3)
            .map(com.davon.library.resource.MemberResource.UrgentLoanDTO::fromEntity)
            .collect(java.util.stream.Collectors.toList());

        com.davon.library.resource.MemberResource.DashboardDTO dashboard = new com.davon.library.resource.MemberResource.DashboardDTO();
        dashboard.activeLoansCount = activeLoansCount;
        dashboard.overdueLoansCount = overdueLoansCount;
        dashboard.outstandingFines = outstandingFines.toPlainString();
        dashboard.urgentLoans = urgentLoans;

        return dashboard;
    }
}