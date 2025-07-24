package com.davon.library.repository;

import com.davon.library.model.Transaction;
import com.davon.library.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Date;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {
    
    public List<Transaction> findByUser(User user) {
        return find("user", user).list();
    }
    
    public List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return find("status", status).list();
    }
    
    public List<Transaction> findByType(String type) {
        return find("type", type).list();
    }
    
    public List<Transaction> findByDateRange(Date startDate, Date endDate) {
        return find("date >= ?1 and date <= ?2", startDate, endDate).list();
    }
    
    public List<Transaction> findPendingTransactions() {
        return find("status", Transaction.TransactionStatus.PENDING).list();
    }
    
    public List<Transaction> findCompletedTransactions() {
        return find("status", Transaction.TransactionStatus.COMPLETED).list();
    }
    
    public double getTotalAmountByStatus(Transaction.TransactionStatus status) {
        Double result = find("SELECT SUM(amount) FROM Transaction WHERE status = ?1", status)
            .project(Double.class).firstResult();
        return result != null ? result : 0.0;
    }
} 