package com.davon.library.service;

import com.davon.library.model.Transaction;
import com.davon.library.model.Fine;
import com.davon.library.model.Payment;
import com.davon.library.model.User;
import com.davon.library.repository.TransactionRepository;
import com.davon.library.repository.FineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * TransactionService - handles all transaction-related business operations
 */
@ApplicationScoped
public class TransactionService {
    
    @Inject
    TransactionRepository transactionRepository;
    
    @Inject
    FineRepository fineRepository;
    
    /**
     * Mark transaction as completed
     */
    @Transactional
    public boolean markAsCompleted(Long transactionId) {
        if (transactionId == null) {
            System.out.println("Transaction ID cannot be null");
            return false;
        }
        
        Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
        if (transactionOpt.isEmpty()) {
            System.out.println("Transaction not found");
            return false;
        }
        
        Transaction transaction = transactionOpt.get();
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        
        System.out.println("Transaction " + transaction.getId() + " marked as completed");
        return true;
    }
    
    /**
     * Mark transaction as cancelled
     */
    @Transactional
    public boolean markAsCancelled(Long transactionId) {
        if (transactionId == null) {
            System.out.println("Transaction ID cannot be null");
            return false;
        }
        
        Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
        if (transactionOpt.isEmpty()) {
            System.out.println("Transaction not found");
            return false;
        }
        
        Transaction transaction = transactionOpt.get();
        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        
        System.out.println("Transaction " + transaction.getId() + " cancelled");
        return true;
    }
    
    /**
     * Mark transaction as failed
     */
    @Transactional
    public boolean markAsFailed(Long transactionId) {
        if (transactionId == null) {
            System.out.println("Transaction ID cannot be null");
            return false;
        }
        
        Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
        if (transactionOpt.isEmpty()) {
            System.out.println("Transaction not found");
            return false;
        }
        
        Transaction transaction = transactionOpt.get();
        transaction.setStatus(Transaction.TransactionStatus.FAILED);
        
        System.out.println("Transaction " + transaction.getId() + " failed");
        return true;
    }
    
    /**
     * Check if transaction is pending
     */
    public boolean isPending(Long transactionId) {
        if (transactionId == null) {
            return false;
        }
        
        Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
        if (transactionOpt.isEmpty()) {
            return false;
        }
        
        return Transaction.TransactionStatus.PENDING.equals(transactionOpt.get().getStatus());
    }
    
    /**
     * Check if transaction is completed
     */
    public boolean isCompleted(Long transactionId) {
        if (transactionId == null) {
            return false;
        }
        
        Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
        if (transactionOpt.isEmpty()) {
            return false;
        }
        
        return Transaction.TransactionStatus.COMPLETED.equals(transactionOpt.get().getStatus());
    }
    
    /**
     * Check if transaction is cancelled
     */
    public boolean isCancelled(Long transactionId) {
        if (transactionId == null) {
            return false;
        }
        
        Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
        if (transactionOpt.isEmpty()) {
            return false;
        }
        
        return Transaction.TransactionStatus.CANCELLED.equals(transactionOpt.get().getStatus());
    }
    
    /**
     * Check if transaction is failed
     */
    public boolean isFailed(Long transactionId) {
        if (transactionId == null) {
            return false;
        }
        
        Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
        if (transactionOpt.isEmpty()) {
            return false;
        }
        
        return Transaction.TransactionStatus.FAILED.equals(transactionOpt.get().getStatus());
    }
    
    /**
     * Create new fine transaction
     */
    @Transactional
    public Fine createFine(User user, double amount, String reason, Fine.FineType fineType) {
        if (user == null || amount <= 0 || reason == null) {
            System.out.println("Invalid fine parameters");
            return null;
        }
        
        Fine fine = new Fine();
        fine.setUser(user);
        fine.setAmount(amount);
        fine.setType("FINE");
        fine.setDescription(reason);
        fine.setDate(new Date());
        fine.setStatus(Transaction.TransactionStatus.PENDING);
        fine.setReason(reason);
        fine.setFineType(fineType != null ? fineType : Fine.FineType.OVERDUE);
        fine.setIssuedDate(new Date());
        fine.setPaid(false);
        
        fineRepository.persist(fine);
        
        System.out.println("Fine created: $" + amount + " for user " + user.getName() + " - " + reason);
        return fine;
    }
    
    /**
     * Create new payment transaction
     */
    @Transactional
    public Payment createPayment(User user, double amount, String description, Payment.PaymentMethod method) {
        if (user == null || amount <= 0 || description == null) {
            System.out.println("Invalid payment parameters");
            return null;
        }
        
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setType("PAYMENT");
        payment.setDescription(description);
        payment.setDate(new Date());
        payment.setStatus(Transaction.TransactionStatus.PENDING);
        payment.setPaymentMethod(method != null ? method : Payment.PaymentMethod.CASH);
        payment.setPaymentDate(new Date());
        payment.setReferenceNumber("PAY" + System.currentTimeMillis());
        
        transactionRepository.persist(payment);
        
        System.out.println("Payment created: $" + amount + " from user " + user.getName() + " - " + description);
        return payment;
    }
} 