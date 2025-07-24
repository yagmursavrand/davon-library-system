package com.davon.library.repository;

import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.model.Book;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Date;
import java.util.Optional;

@ApplicationScoped
public class LoanRepository implements PanacheRepository<Loan> {
    
    /**
     * Find loans by member
     */
    public List<Loan> findByMember(Member member) {
        return find("member", member).list();
    }
    
    /**
     * Find loans by book
     */
    public List<Loan> findByBook(Book book) {
        return find("book", book).list();
    }
    
    /**
     * Find active loans (not returned)
     */
    public List<Loan> findActiveLoans() {
        return find("returnDate IS NULL").list();
    }
    
    /**
     * Find overdue loans
     */
    public List<Loan> findOverdueLoans() {
        return find("dueDate < ?1 AND returnDate IS NULL", new Date()).list();
    }
    
    /**
     * Find loans by status
     */
    public List<Loan> findByStatus(Loan.LoanStatus status) {
        return find("status", status).list();
    }
    
    /**
     * Find loans due within days
     */
    public List<Loan> findLoansDueWithinDays(int days) {
        Date futureDate = new Date(System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000));
        return find("dueDate BETWEEN ?1 AND ?2 AND returnDate IS NULL", new Date(), futureDate).list();
    }
    
    /**
     * Find loans by member ID
     */
    public List<Loan> findByMemberId(Long memberId) {
        return find("member.id", memberId).list();
    }
    
    /**
     * Find active loan for specific book and member
     */
    public Optional<Loan> findActiveLoanByMemberAndBook(Member member, Book book) {
        return find("member = ?1 AND book = ?2 AND returnDate IS NULL", member, book).firstResultOptional();
    }
    
    /**
     * Count active loans by member
     */
    public long countActiveLoansByMember(Member member) {
        return count("member = ?1 AND returnDate IS NULL", member);
    }
    
    /**
     * Find active loans by member (not returned yet)
     */
    public List<Loan> findActiveLoansByMember(Member member) {
        return find("member = ?1 AND returnDate IS NULL", member).list();
    }
    
    /**
     * Find loans with fines
     */
    public List<Loan> findLoansWithFines() {
        return find("fineAmount > 0").list();
    }
} 