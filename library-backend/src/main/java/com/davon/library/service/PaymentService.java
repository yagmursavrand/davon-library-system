package com.davon.library.service;

import com.davon.library.model.Payment;
import com.davon.library.model.User;
import com.davon.library.model.Fine;
import com.davon.library.repository.PaymentRepository;
import com.davon.library.repository.FineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * PaymentService - handles all payment-related business operations
 */
@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;
    
    @Inject
    FineRepository fineRepository;

    /**
     * Get payments by user
     */
    public List<Payment> getPaymentsByUser(User user) {
        return paymentRepository.findByUser(user);
    }

    /**
     * Get payment by ID
     */
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findByIdOptional(id);
    }

    /**
     * Create new payment
     */
    @Transactional
    public Payment createPayment(User user, double amount, Payment.PaymentMethod method, Fine fine) {
        if (user == null || amount <= 0 || method == null) {
            System.out.println("Invalid payment parameters");
            return null;
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setType("PAYMENT");
        payment.setDescription("Payment for " + (fine != null ? "fine" : "service"));
        payment.setDate(new Date());
        payment.setStatus(Payment.TransactionStatus.PENDING);
        payment.setPaymentMethod(method);
        payment.setPaymentDate(new Date());
        payment.setReferenceNumber("PAY" + System.currentTimeMillis());
        payment.setFine(fine);

        paymentRepository.persist(payment);

        System.out.println("Payment created: $" + amount + " from user " + user.getName());
        return payment;
    }

    /**
     * Process payment
     */
    @Transactional
    public boolean processPayment(Long paymentId) {
        if (paymentId == null) {
            System.out.println("Payment ID cannot be null");
            return false;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByIdOptional(paymentId);
        if (paymentOpt.isEmpty()) {
            System.out.println("Payment not found");
            return false;
        }

        Payment payment = paymentOpt.get();

        if (payment.getAmount() <= 0) {
            System.out.println("Invalid payment amount");
            payment.setStatus(Payment.TransactionStatus.FAILED);
            return false;
        }

        try {
            // Simulate payment processing
            Thread.sleep(100); // Simulate processing time
            
            payment.setConfirmationNumber("CONF" + System.currentTimeMillis());
            payment.setStatus(Payment.TransactionStatus.COMPLETED);
            
            // If this payment is for a fine, mark the fine as paid
            if (payment.getFine() != null) {
                Fine fine = payment.getFine();
                fine.setPaid(true);
                fine.setPaidDate(new Date());
                fine.setStatus(Fine.TransactionStatus.COMPLETED);
            }
            
            System.out.println("Payment processed successfully. Confirmation: " + payment.getConfirmationNumber());
            return true;
            
        } catch (Exception e) {
            System.out.println("Payment processing failed: " + e.getMessage());
            payment.setStatus(Payment.TransactionStatus.FAILED);
            return false;
        }
    }

    /**
     * Refund payment
     */
    @Transactional
    public boolean refundPayment(Long paymentId, String reason) {
        if (paymentId == null) {
            System.out.println("Payment ID cannot be null");
            return false;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByIdOptional(paymentId);
        if (paymentOpt.isEmpty()) {
            System.out.println("Payment not found");
            return false;
        }

        Payment payment = paymentOpt.get();

        if (payment.getStatus() != Payment.TransactionStatus.COMPLETED) {
            System.out.println("Cannot refund: Payment not completed");
            return false;
        }
        
        if (payment.getStatus() == Payment.TransactionStatus.CANCELLED) {
            System.out.println("Payment already cancelled/refunded");
            return false;
        }
        
        payment.setStatus(Payment.TransactionStatus.CANCELLED);
        payment.setDescription(payment.getDescription() + " (Refunded: " + reason + " on " + new Date() + ")");
        
        System.out.println("Payment " + paymentId + " refunded. Reason: " + reason);
        return true;
    }

    /**
     * Validate payment method
     */
    public boolean isValidPaymentMethod(Payment.PaymentMethod method) {
        return method != null;
    }

    /**
     * Check if payment is electronic
     */
    public boolean isElectronicPayment(Payment payment) {
        if (payment == null || payment.getPaymentMethod() == null) {
            return false;
        }
        
        return payment.getPaymentMethod() == Payment.PaymentMethod.CREDIT_CARD || 
               payment.getPaymentMethod() == Payment.PaymentMethod.DEBIT_CARD || 
               payment.getPaymentMethod() == Payment.PaymentMethod.BANK_TRANSFER || 
               payment.getPaymentMethod() == Payment.PaymentMethod.PAYPAL;
    }

    /**
     * Get payment status summary
     */
    public String getPaymentStatus(Long paymentId) {
        if (paymentId == null) {
            return "INVALID";
        }

        Optional<Payment> paymentOpt = paymentRepository.findByIdOptional(paymentId);
        if (paymentOpt.isEmpty()) {
            return "NOT_FOUND";
        }

        Payment payment = paymentOpt.get();

        if (payment.getStatus() == Payment.TransactionStatus.COMPLETED) {
            return "COMPLETED";
        } else if (payment.getStatus() == Payment.TransactionStatus.FAILED) {
            return "FAILED";
        } else if (payment.getStatus() == Payment.TransactionStatus.CANCELLED) {
            return "REFUNDED";
        } else {
            return "PENDING";
        }
    }

    /**
     * Display payment details
     */
    public void displayPaymentDetails(Long paymentId) {
        if (paymentId == null) {
            System.out.println("Payment ID cannot be null");
            return;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByIdOptional(paymentId);
        if (paymentOpt.isEmpty()) {
            System.out.println("Payment not found");
            return;
        }

        Payment payment = paymentOpt.get();

        System.out.println("=== Payment Details ===");
        System.out.println("Payment ID: " + payment.getId());
        System.out.println("Amount: $" + payment.getAmount());
        System.out.println("Method: " + payment.getPaymentMethod());
        System.out.println("Payment Date: " + payment.getPaymentDate());
        System.out.println("Reference Number: " + payment.getReferenceNumber());
        System.out.println("Confirmation Number: " + (payment.getConfirmationNumber() != null ? payment.getConfirmationNumber() : "N/A"));
        System.out.println("Status: " + getPaymentStatus(paymentId));
        if (payment.getFine() != null) {
            System.out.println("Fine ID: " + payment.getFine().getId());
        }
        System.out.println("Electronic Payment: " + (isElectronicPayment(payment) ? "Yes" : "No"));
        System.out.println("=====================");
    }

    /**
     * Get processing fee based on payment method
     */
    public double getProcessingFee(Payment payment) {
        if (payment == null || payment.getPaymentMethod() == null) {
            return 0.0;
        }
        
        switch (payment.getPaymentMethod()) {
            case CREDIT_CARD:
                return payment.getAmount() * 0.025; // 2.5% fee
            case DEBIT_CARD:
                return 0.50; // Fixed $0.50 fee
            case PAYPAL:
                return payment.getAmount() * 0.029 + 0.30; // 2.9% + $0.30
            case BANK_TRANSFER:
                return 1.00; // Fixed $1.00 fee
            case CASH:
            case OTHER:
            default:
                return 0.0; // No fee
        }
    }

    /**
     * Delete payment
     */
    @Transactional
    public boolean deletePayment(Long paymentId) {
        if (paymentId == null) {
            System.out.println("Payment ID cannot be null");
            return false;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByIdOptional(paymentId);
        if (paymentOpt.isEmpty()) {
            System.out.println("Payment not found");
            return false;
        }

        Payment payment = paymentOpt.get();

        if (payment.getStatus() == Payment.TransactionStatus.COMPLETED) {
            System.out.println("Cannot delete completed payment");
            return false;
        }

        paymentRepository.delete(payment);
        System.out.println("Payment deleted successfully: " + paymentId);
        return true;
    }
} 