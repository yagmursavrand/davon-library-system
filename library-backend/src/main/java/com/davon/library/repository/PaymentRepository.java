package com.davon.library.repository;

import com.davon.library.model.Payment;
import com.davon.library.model.User;
import com.davon.library.model.Fine;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Date;
import java.util.Optional;

@ApplicationScoped
public class PaymentRepository implements PanacheRepository<Payment> {

    public List<Payment> findByUser(User user) {
        return find("user", user).list();
    }

    public List<Payment> findByPaymentMethod(Payment.PaymentMethod method) {
        return find("paymentMethod", method).list();
    }

    public List<Payment> findByFine(Fine fine) {
        return find("fine", fine).list();
    }

    public Optional<Payment> findByReferenceNumber(String referenceNumber) {
        return find("referenceNumber", referenceNumber).firstResultOptional();
    }

    public Optional<Payment> findByConfirmationNumber(String confirmationNumber) {
        return find("confirmationNumber", confirmationNumber).firstResultOptional();
    }

    public List<Payment> findByDateRange(Date startDate, Date endDate) {
        return find("paymentDate >= ?1 and paymentDate <= ?2", startDate, endDate).list();
    }

    public List<Payment> findCompletedPayments() {
        return find("status", Payment.TransactionStatus.COMPLETED).list();
    }

    public List<Payment> findPendingPayments() {
        return find("status", Payment.TransactionStatus.PENDING).list();
    }

    public double getTotalAmountByMethod(Payment.PaymentMethod method) {
        Double result = find("SELECT SUM(amount) FROM Payment WHERE paymentMethod = ?1 AND status = ?2", 
                    method, Payment.TransactionStatus.COMPLETED)
                .project(Double.class).firstResult();
        return result != null ? result : 0.0;
    }

    public long countPaymentsByMethod(Payment.PaymentMethod method) {
        return count("paymentMethod = ?1 AND status = ?2", method, Payment.TransactionStatus.COMPLETED);
    }

    public List<Payment> findRecentPayments(int days) {
        Date cutoffDate = new Date(System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000));
        return find("paymentDate >= ?1", cutoffDate).list();
    }
} 