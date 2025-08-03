package com.davon.library.repository;

import com.davon.library.model.Fine;
import com.davon.library.model.User;
import com.davon.library.model.Member;
import com.davon.library.model.Loan;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FineRepository implements PanacheRepository<Fine> {

    public List<Fine> findByUser(User user) {
        return find("user", user).list();
    }

    public List<Fine> findByMember(Member member) {
        return find("user", member).list();
    }

    public List<Fine> findByLoan(Loan loan) {
        return find("loan", loan).list();
    }

    public Fine findFirstByLoan(Loan loan) {
        return find("loan", loan).firstResult();
    }

    public List<Fine> findByFineType(Fine.FineType fineType) {
        return find("fineType", fineType).list();
    }

    public List<Fine> findPaidFines() {
        return find("paid", true).list();
    }

    public List<Fine> findUnpaidFines() {
        return find("paid", false).list();
    }

    public List<Fine> findUnpaidFines(User user) {
        return find("user = ?1 and paid = false", user).list();
    }

    public List<Fine> findOverdueFines() {
        return find("paid = false and issuedDate < ?1", 
                   new java.util.Date(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000))).list();
    }

    public BigDecimal calculateTotalUnpaidFines(Member member) {
        BigDecimal total = getEntityManager().createQuery(
                "SELECT SUM(f.amount) FROM Fine f WHERE f.user = :user AND f.paid = false", BigDecimal.class)
                .setParameter("user", member)
                .getSingleResult();
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalUnpaidFines(User user) {
        BigDecimal total = getEntityManager().createQuery(
                "SELECT SUM(f.amount) FROM Fine f WHERE f.user = :user AND f.paid = false", BigDecimal.class)
                .setParameter("user", user)
                .getSingleResult();
        return total != null ? total : BigDecimal.ZERO;
    }

    public long countUnpaidFinesByMember(Member member) {
        return count("user = ?1 and paid = false", member);
    }

    public List<Fine> findRecentFines(int days) {
        java.util.Date cutoffDate = new java.util.Date(System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000));
        return find("issuedDate >= ?1", cutoffDate).list();
    }
}