package com.davon.library.service;

import com.davon.library.model.Transaction;
import com.davon.library.model.Fine;
import com.davon.library.model.Payment;
import com.davon.library.model.User;
import com.davon.library.repository.TransactionRepository;
import com.davon.library.repository.FineRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("TransactionService Unit Tests")
public class TransactionServiceTest {

    @Inject
    TransactionService transactionService;

    @InjectMock
    TransactionRepository transactionRepository;

    @InjectMock
    FineRepository fineRepository;

    private Transaction testTransaction;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser.setCreatedAt(new Date());

        // Setup test transaction
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
        testTransaction.setAmount(25.0);
        testTransaction.setType("FINE");
        testTransaction.setDescription("Test transaction");
        testTransaction.setDate(new Date());
        testTransaction.setStatus(Transaction.TransactionStatus.PENDING);
    }

    // ===== MARK AS COMPLETED TESTS =====

    @Test
    @DisplayName("Should mark transaction as completed successfully")
    void testMarkAsCompleted_Success() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.markAsCompleted(1L);

        // Then
        assertTrue(result);
        assertEquals(Transaction.TransactionStatus.COMPLETED, testTransaction.getStatus());
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to mark transaction as completed when ID is null")
    void testMarkAsCompleted_NullId_Fails() {
        // When
        boolean result = transactionService.markAsCompleted(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should fail to mark transaction as completed when transaction not found")
    void testMarkAsCompleted_TransactionNotFound_Fails() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = transactionService.markAsCompleted(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    // ===== MARK AS CANCELLED TESTS =====

    @Test
    @DisplayName("Should mark transaction as cancelled successfully")
    void testMarkAsCancelled_Success() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.markAsCancelled(1L);

        // Then
        assertTrue(result);
        assertEquals(Transaction.TransactionStatus.CANCELLED, testTransaction.getStatus());
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to mark transaction as cancelled when ID is null")
    void testMarkAsCancelled_NullId_Fails() {
        // When
        boolean result = transactionService.markAsCancelled(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should fail to mark transaction as cancelled when transaction not found")
    void testMarkAsCancelled_TransactionNotFound_Fails() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = transactionService.markAsCancelled(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    // ===== MARK AS FAILED TESTS =====

    @Test
    @DisplayName("Should mark transaction as failed successfully")
    void testMarkAsFailed_Success() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.markAsFailed(1L);

        // Then
        assertTrue(result);
        assertEquals(Transaction.TransactionStatus.FAILED, testTransaction.getStatus());
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to mark transaction as failed when ID is null")
    void testMarkAsFailed_NullId_Fails() {
        // When
        boolean result = transactionService.markAsFailed(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should fail to mark transaction as failed when transaction not found")
    void testMarkAsFailed_TransactionNotFound_Fails() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = transactionService.markAsFailed(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    // ===== IS PENDING TESTS =====

    @Test
    @DisplayName("Should return true when transaction is pending")
    void testIsPending_True() {
        // Given
        testTransaction.setStatus(Transaction.TransactionStatus.PENDING);
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.isPending(1L);

        // Then
        assertTrue(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when transaction is not pending")
    void testIsPending_False() {
        // Given
        testTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.isPending(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when transaction ID is null")
    void testIsPending_NullId_False() {
        // When
        boolean result = transactionService.isPending(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should return false when transaction not found")
    void testIsPending_TransactionNotFound_False() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = transactionService.isPending(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    // ===== IS COMPLETED TESTS =====

    @Test
    @DisplayName("Should return true when transaction is completed")
    void testIsCompleted_True() {
        // Given
        testTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.isCompleted(1L);

        // Then
        assertTrue(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when transaction is not completed")
    void testIsCompleted_False() {
        // Given
        testTransaction.setStatus(Transaction.TransactionStatus.PENDING);
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.isCompleted(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when transaction ID is null")
    void testIsCompleted_NullId_False() {
        // When
        boolean result = transactionService.isCompleted(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should return false when transaction not found")
    void testIsCompleted_TransactionNotFound_False() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = transactionService.isCompleted(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    // ===== IS CANCELLED TESTS =====

    @Test
    @DisplayName("Should return true when transaction is cancelled")
    void testIsCancelled_True() {
        // Given
        testTransaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.isCancelled(1L);

        // Then
        assertTrue(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when transaction is not cancelled")
    void testIsCancelled_False() {
        // Given
        testTransaction.setStatus(Transaction.TransactionStatus.PENDING);
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.isCancelled(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when transaction ID is null")
    void testIsCancelled_NullId_False() {
        // When
        boolean result = transactionService.isCancelled(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should return false when transaction not found")
    void testIsCancelled_TransactionNotFound_False() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = transactionService.isCancelled(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    // ===== IS FAILED TESTS =====

    @Test
    @DisplayName("Should return true when transaction is failed")
    void testIsFailed_True() {
        // Given
        testTransaction.setStatus(Transaction.TransactionStatus.FAILED);
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.isFailed(1L);

        // Then
        assertTrue(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when transaction is not failed")
    void testIsFailed_False() {
        // Given
        testTransaction.setStatus(Transaction.TransactionStatus.PENDING);
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When
        boolean result = transactionService.isFailed(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when transaction ID is null")
    void testIsFailed_NullId_False() {
        // When
        boolean result = transactionService.isFailed(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should return false when transaction not found")
    void testIsFailed_TransactionNotFound_False() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = transactionService.isFailed(1L);

        // Then
        assertFalse(result);
        verify(transactionRepository).findByIdOptional(1L);
    }

    // ===== CREATE FINE TESTS =====

    @Test
    @DisplayName("Should create fine successfully")
    void testCreateFine_Success() {
        // Given
        doAnswer(invocation -> {
            Fine fine = invocation.getArgument(0);
            fine.setId(1L);
            return null;
        }).when(fineRepository).persist(any(Fine.class));

        // When
        Fine result = transactionService.createFine(testUser, 25.0, "Overdue book", Fine.FineType.OVERDUE);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(25.0, result.getAmount());
        assertEquals("FINE", result.getType());
        assertEquals("Overdue book", result.getDescription());
        assertEquals("Overdue book", result.getReason());
        assertEquals(Fine.FineType.OVERDUE, result.getFineType());
        assertEquals(Transaction.TransactionStatus.PENDING, result.getStatus());
        assertNotNull(result.getDate());
        assertNotNull(result.getIssuedDate());
        assertFalse(result.isPaid());
        
        verify(fineRepository).persist(any(Fine.class));
    }

    @Test
    @DisplayName("Should create fine with default type when type is null")
    void testCreateFine_DefaultType_Success() {
        // Given
        doAnswer(invocation -> {
            Fine fine = invocation.getArgument(0);
            fine.setId(1L);
            return null;
        }).when(fineRepository).persist(any(Fine.class));

        // When
        Fine result = transactionService.createFine(testUser, 25.0, "Test fine", null);

        // Then
        assertNotNull(result);
        assertEquals(Fine.FineType.OVERDUE, result.getFineType()); // Should default to OVERDUE
        verify(fineRepository).persist(any(Fine.class));
    }

    @Test
    @DisplayName("Should fail to create fine when user is null")
    void testCreateFine_NullUser_Fails() {
        // When
        Fine result = transactionService.createFine(null, 25.0, "Test fine", Fine.FineType.OVERDUE);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to create fine when amount is zero")
    void testCreateFine_ZeroAmount_Fails() {
        // When
        Fine result = transactionService.createFine(testUser, 0.0, "Test fine", Fine.FineType.OVERDUE);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to create fine when amount is negative")
    void testCreateFine_NegativeAmount_Fails() {
        // When
        Fine result = transactionService.createFine(testUser, -10.0, "Test fine", Fine.FineType.OVERDUE);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to create fine when reason is null")
    void testCreateFine_NullReason_Fails() {
        // When
        Fine result = transactionService.createFine(testUser, 25.0, null, Fine.FineType.OVERDUE);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    // ===== CREATE PAYMENT TESTS =====

    @Test
    @DisplayName("Should create payment successfully")
    void testCreatePayment_Success() {
        // Given
        doAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return null;
        }).when(transactionRepository).persist(any(Payment.class));

        // When
        Payment result = transactionService.createPayment(testUser, 25.0, "Fine payment", Payment.PaymentMethod.CREDIT_CARD);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(25.0, result.getAmount());
        assertEquals("PAYMENT", result.getType());
        assertEquals("Fine payment", result.getDescription());
        assertEquals(Payment.PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
        assertEquals(Transaction.TransactionStatus.PENDING, result.getStatus());
        assertNotNull(result.getDate());
        assertNotNull(result.getPaymentDate());
        assertNotNull(result.getReferenceNumber());
        assertTrue(result.getReferenceNumber().startsWith("PAY"));
        
        verify(transactionRepository).persist(any(Payment.class));
    }

    @Test
    @DisplayName("Should create payment with default method when method is null")
    void testCreatePayment_DefaultMethod_Success() {
        // Given
        doAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return null;
        }).when(transactionRepository).persist(any(Payment.class));

        // When
        Payment result = transactionService.createPayment(testUser, 25.0, "Test payment", null);

        // Then
        assertNotNull(result);
        assertEquals(Payment.PaymentMethod.CASH, result.getPaymentMethod()); // Should default to CASH
        verify(transactionRepository).persist(any(Payment.class));
    }

    @Test
    @DisplayName("Should fail to create payment when user is null")
    void testCreatePayment_NullUser_Fails() {
        // When
        Payment result = transactionService.createPayment(null, 25.0, "Test payment", Payment.PaymentMethod.CASH);

        // Then
        assertNull(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should fail to create payment when amount is zero")
    void testCreatePayment_ZeroAmount_Fails() {
        // When
        Payment result = transactionService.createPayment(testUser, 0.0, "Test payment", Payment.PaymentMethod.CASH);

        // Then
        assertNull(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should fail to create payment when amount is negative")
    void testCreatePayment_NegativeAmount_Fails() {
        // When
        Payment result = transactionService.createPayment(testUser, -10.0, "Test payment", Payment.PaymentMethod.CASH);

        // Then
        assertNull(result);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should fail to create payment when description is null")
    void testCreatePayment_NullDescription_Fails() {
        // When
        Payment result = transactionService.createPayment(testUser, 25.0, null, Payment.PaymentMethod.CASH);

        // Then
        assertNull(result);
        verifyNoInteractions(transactionRepository);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle multiple status changes on same transaction")
    void testMultipleStatusChanges_Success() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // When & Then
        assertTrue(transactionService.markAsCompleted(1L));
        assertEquals(Transaction.TransactionStatus.COMPLETED, testTransaction.getStatus());

        // Change to cancelled
        assertTrue(transactionService.markAsCancelled(1L));
        assertEquals(Transaction.TransactionStatus.CANCELLED, testTransaction.getStatus());

        // Change to failed
        assertTrue(transactionService.markAsFailed(1L));
        assertEquals(Transaction.TransactionStatus.FAILED, testTransaction.getStatus());

        verify(transactionRepository, times(3)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should handle all transaction status checks correctly")
    void testAllStatusChecks_Comprehensive() {
        // Given
        when(transactionRepository.findByIdOptional(1L)).thenReturn(Optional.of(testTransaction));

        // Test PENDING status
        testTransaction.setStatus(Transaction.TransactionStatus.PENDING);
        assertTrue(transactionService.isPending(1L));
        assertFalse(transactionService.isCompleted(1L));
        assertFalse(transactionService.isCancelled(1L));
        assertFalse(transactionService.isFailed(1L));

        // Test COMPLETED status
        testTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        assertFalse(transactionService.isPending(1L));
        assertTrue(transactionService.isCompleted(1L));
        assertFalse(transactionService.isCancelled(1L));
        assertFalse(transactionService.isFailed(1L));

        // Test CANCELLED status
        testTransaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        assertFalse(transactionService.isPending(1L));
        assertFalse(transactionService.isCompleted(1L));
        assertTrue(transactionService.isCancelled(1L));
        assertFalse(transactionService.isFailed(1L));

        // Test FAILED status
        testTransaction.setStatus(Transaction.TransactionStatus.FAILED);
        assertFalse(transactionService.isPending(1L));
        assertFalse(transactionService.isCompleted(1L));
        assertFalse(transactionService.isCancelled(1L));
        assertTrue(transactionService.isFailed(1L));
    }

    @Test
    @DisplayName("Should create fine with all fine types")
    void testCreateFine_AllFineTypes_Success() {
        // Given
        doAnswer(invocation -> {
            Fine fine = invocation.getArgument(0);
            fine.setId(1L);
            return null;
        }).when(fineRepository).persist(any(Fine.class));

        // Test each fine type
        Fine.FineType[] fineTypes = Fine.FineType.values();
        for (Fine.FineType fineType : fineTypes) {
            // When
            Fine result = transactionService.createFine(testUser, 25.0, "Test fine", fineType);

            // Then
            assertNotNull(result, "Fine creation failed for type: " + fineType);
            assertEquals(fineType, result.getFineType());
        }

        verify(fineRepository, times(fineTypes.length)).persist(any(Fine.class));
    }

    @Test
    @DisplayName("Should create payment with all payment methods")
    void testCreatePayment_AllPaymentMethods_Success() {
        // Given
        doAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return null;
        }).when(transactionRepository).persist(any(Payment.class));

        // Test each payment method
        Payment.PaymentMethod[] paymentMethods = Payment.PaymentMethod.values();
        for (Payment.PaymentMethod method : paymentMethods) {
            // When
            Payment result = transactionService.createPayment(testUser, 25.0, "Test payment", method);

            // Then
            assertNotNull(result, "Payment creation failed for method: " + method);
            assertEquals(method, result.getPaymentMethod());
        }

        verify(transactionRepository, times(paymentMethods.length)).persist(any(Payment.class));
    }

    @Test
    @DisplayName("Should generate unique reference numbers for payments")
    void testCreatePayment_UniqueReferenceNumbers() throws InterruptedException {
        // Given
        doAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return null;
        }).when(transactionRepository).persist(any(Payment.class));

        // When
        Payment payment1 = transactionService.createPayment(testUser, 25.0, "Payment 1", Payment.PaymentMethod.CASH);
        Thread.sleep(1); // Ensure different timestamps
        Payment payment2 = transactionService.createPayment(testUser, 30.0, "Payment 2", Payment.PaymentMethod.CASH);

        // Then
        assertNotNull(payment1);
        assertNotNull(payment2);
        assertNotEquals(payment1.getReferenceNumber(), payment2.getReferenceNumber());
        assertTrue(payment1.getReferenceNumber().startsWith("PAY"));
        assertTrue(payment2.getReferenceNumber().startsWith("PAY"));
    }
} 