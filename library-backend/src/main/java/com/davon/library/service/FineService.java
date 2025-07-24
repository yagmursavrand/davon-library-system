package com.davon.library.service;

import com.davon.library.model.Fine;
import com.davon.library.model.User;
import com.davon.library.model.Loan;
import com.davon.library.repository.FineRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.repository.LoanRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * FineService - handles all fine-related business operations
 */
@ApplicationScoped
public class FineService {

    @Inject
    FineRepository fineRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    LoanRepository loanRepository;

    /**
     * Get fines by user
     */
    public List<Fine> getFinesByUser(User user) {
        return fineRepository.findByUser(user);
    }

    /**
     * Get fine by ID
     */
    public Optional<Fine> getFineById(Long id) {
        return fineRepository.findByIdOptional(id);
    }

    /**
     * Create new fine
     */
    @Transactional
    public Fine createFine(User user, double amount, String reason, Fine.FineType fineType, Loan loan) {
        if (user == null || amount <= 0 || reason == null || reason.trim().isEmpty()) {
            System.out.println("Invalid fine parameters");
            return null;
        }

        Fine fine = new Fine();
        fine.setUser(user);
        fine.setAmount(amount);
        fine.setType("FINE");
        fine.setDescription(reason);
        fine.setDate(new Date());
        fine.setStatus(Fine.TransactionStatus.PENDING);
        fine.setReason(reason.trim());
        fine.setFineType(fineType != null ? fineType : Fine.FineType.OVERDUE);
        fine.setIssuedDate(new Date());
        fine.setPaid(false);
        fine.setLoan(loan);

        fineRepository.persist(fine);

        System.out.println("Fine created: $" + amount + " for user " + user.getName() + " - " + reason);
        return fine;
    }

    /**
     * Check if fine is paid (implements Fine.isPaid())
     */
    public boolean isFinesPaid(Long fineId) {
        if (fineId == null) {
            return false;
        }

        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            return false;
        }

        Fine fine = fineOpt.get();
        return fine.isPaid() && fine.getPaidDate() != null;
    }

    /**
     * Mark fine as paid (implements Fine.markAsPaid())
     */
    @Transactional
    public boolean markFineAsPaid(Long fineId) {
        if (fineId == null) {
            System.out.println("Fine ID cannot be null");
            return false;
        }

        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            System.out.println("Fine not found");
            return false;
        }

        Fine fine = fineOpt.get();

        if (fine.isPaid()) {
            System.out.println("Fine is already paid");
            return false;
        }

        fine.setPaid(true);
        fine.setPaidDate(new Date());
        fine.setStatus(Fine.TransactionStatus.COMPLETED);

        System.out.println("Fine " + fineId + " marked as paid on " + fine.getPaidDate());
        return true;
    }

    /**
     * Calculate days since fine was issued
     */
    public long getDaysSinceIssued(Long fineId) {
        if (fineId == null) {
            return 0;
        }

        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty() || fineOpt.get().getIssuedDate() == null) {
            return 0;
        }

        Date issuedDate = fineOpt.get().getIssuedDate();
        long diffInMillies = new Date().getTime() - issuedDate.getTime();
        return diffInMillies / (24 * 60 * 60 * 1000);
    }

    /**
     * Check if fine is overdue (not paid within 30 days)
     */
    public boolean isFineOverdue(Long fineId) {
        if (fineId == null) {
            return false;
        }

        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            return false;
        }

        Fine fine = fineOpt.get();
        return !fine.isPaid() && getDaysSinceIssued(fineId) > 30;
    }

    /**
     * Get fine status summary
     */
    public String getFineStatus(Long fineId) {
        if (fineId == null) {
            return "INVALID";
        }

        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            return "NOT_FOUND";
        }

        Fine fine = fineOpt.get();

        if (fine.isPaid()) {
            return "PAID";
        } else if (isFineOverdue(fineId)) {
            return "OVERDUE";
        } else if (fine.getStatus() == Fine.TransactionStatus.CANCELLED) {
            return "CANCELLED";
        } else {
            return "PENDING";
        }
    }

    /**
     * Apply late payment penalty (additional fine)
     */
    @Transactional
    public boolean applyLatePenalty(Long fineId, double penaltyAmount) {
        if (fineId == null || penaltyAmount <= 0) {
            System.out.println("Invalid parameters for late penalty");
            return false;
        }

        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            System.out.println("Fine not found");
            return false;
        }

        Fine fine = fineOpt.get();

        if (fine.isPaid()) {
            System.out.println("Cannot apply penalty to paid fine");
            return false;
        }

        fine.setAmount(fine.getAmount() + penaltyAmount);
        fine.setDescription(fine.getDescription() + " (Late penalty: $" + penaltyAmount + " applied on " + new Date() + ")");
        
        System.out.println("Late penalty of $" + penaltyAmount + " applied to fine " + fineId);
        return true;
    }

    /**
     * Get all unpaid fines for a user
     */
    public List<Fine> getUnpaidFinesByUser(User user) {
        if (user == null) {
            return List.of();
        }

        return fineRepository.findUnpaidFines(user);
    }

    /**
     * Calculate total unpaid fines for a user
     */
    public double calculateTotalUnpaidFines(User user) {
        if (user == null) {
            return 0.0;
        }

        return fineRepository.calculateTotalUnpaidFines(user);
    }

    /**
     * Get formatted fine details
     */
    public void displayFineDetails(Long fineId) {
        if (fineId == null) {
            System.out.println("Fine ID cannot be null");
            return;
        }

        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            System.out.println("Fine not found");
            return;
        }

        Fine fine = fineOpt.get();

        System.out.println("=== Fine Details ===");
        System.out.println("Fine ID: " + fine.getId());
        System.out.println("Amount: $" + fine.getAmount());
        System.out.println("Type: " + fine.getFineType());
        System.out.println("Reason: " + (fine.getReason() != null ? fine.getReason() : "No reason specified"));
        System.out.println("Issued Date: " + fine.getIssuedDate());
        System.out.println("Status: " + getFineStatus(fineId));
        if (fine.isPaid()) {
            System.out.println("Paid Date: " + fine.getPaidDate());
        }
        System.out.println("Days Since Issued: " + getDaysSinceIssued(fineId));
        System.out.println("===================");
    }

    /**
     * Delete fine
     */
    @Transactional
    public boolean deleteFine(Long fineId) {
        if (fineId == null) {
            System.out.println("Fine ID cannot be null");
            return false;
        }

        Optional<Fine> fineOpt = fineRepository.findByIdOptional(fineId);
        if (fineOpt.isEmpty()) {
            System.out.println("Fine not found");
            return false;
        }

        Fine fine = fineOpt.get();

        if (fine.isPaid()) {
            System.out.println("Cannot delete paid fine");
            return false;
        }

        fineRepository.delete(fine);
        System.out.println("Fine deleted successfully: " + fineId);
        return true;
    }
} 