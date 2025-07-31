package com.davon.library.service;

import com.davon.library.model.Payment;
import com.davon.library.model.User;
import com.davon.library.model.Fine;
import com.davon.library.repository.PaymentRepository;
import com.davon.library.repository.FineRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("PaymentService Unit Tests")
public class PaymentServiceTest {

    @Inject
    PaymentService paymentService;

    @InjectMock
    PaymentRepository paymentRepository;

    @InjectMock
    FineRepository fineRepository;

    private Payment testPayment;
    private User testUser;
    private Fine testFine;

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

        // Setup test fine
        testFine = new Fine();
        testFine.setId(1L);
        testFine.setUser(testUser);
        testFine.setAmount(new BigDecimal("25.0"));
        testFine.setType("FINE");
        testFine.setDescription("Overdue book fine");
        testFine.setStatus(Fine.TransactionStatus.PENDING);
        testFine.setPaid(false);

        // Setup test payment
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setUser(testUser);
        testPayment.setAmount(new BigDecimal("25.0"));
        testPayment.setType("PAYMENT");
        testPayment.setDescription("Payment for fine");
        testPayment.setDate(new Date());
        testPayment.setStatus(Payment.TransactionStatus.PENDING);
        testPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        testPayment.setPaymentDate(new Date());
        testPayment.setReferenceNumber("PAY123456789");
        testPayment.setFine(testFine);
    }

    // ===== GET PAYMENTS BY USER TESTS =====

    @Test
    @DisplayName("Should get payments by user successfully")
    void testGetPaymentsByUser_Success() {
        // Given
        List<Payment> payments = List.of(testPayment);
        when(paymentRepository.findByUser(testUser)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment, result.get(0));
        verify(paymentRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("Should return empty list when user has no payments")
    void testGetPaymentsByUser_NoPayments() {
        // Given
        when(paymentRepository.findByUser(testUser)).thenReturn(List.of());

        // When
        List<Payment> result = paymentService.getPaymentsByUser(testUser);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository).findByUser(testUser);
    }

    // ===== GET PAYMENT BY ID TESTS =====

    @Test
    @DisplayName("Should get payment by ID successfully")
    void testGetPaymentById_Success() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        Optional<Payment> result = paymentService.getPaymentById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPayment, result.get());
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return empty when payment not found")
    void testGetPaymentById_NotFound() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        Optional<Payment> result = paymentService.getPaymentById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(paymentRepository).findByIdOptional(1L);
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
        }).when(paymentRepository).persist(any(Payment.class));

        // When
        Payment result = paymentService.createPayment(testUser, new BigDecimal("25.0"), Payment.PaymentMethod.CREDIT_CARD, testFine);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(0, new BigDecimal("25.0").compareTo(result.getAmount()));
        assertEquals("PAYMENT", result.getType());
        assertEquals("Payment for fine", result.getDescription());
        assertEquals(Payment.PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
        assertEquals(Payment.TransactionStatus.PENDING, result.getStatus());
        assertEquals(testFine, result.getFine());
        assertNotNull(result.getDate());
        assertNotNull(result.getPaymentDate());
        assertNotNull(result.getReferenceNumber());
        assertTrue(result.getReferenceNumber().startsWith("PAY"));
        
        verify(paymentRepository).persist(any(Payment.class));
    }

    @Test
    @DisplayName("Should create payment without fine")
    void testCreatePayment_WithoutFine_Success() {
        // Given
        doAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return null;
        }).when(paymentRepository).persist(any(Payment.class));

        // When
        Payment result = paymentService.createPayment(testUser, new BigDecimal("25.0"), Payment.PaymentMethod.CASH, null);

        // Then
        assertNotNull(result);
        assertEquals("Payment for service", result.getDescription());
        assertNull(result.getFine());
        verify(paymentRepository).persist(any(Payment.class));
    }

    @Test
    @DisplayName("Should fail to create payment when user is null")
    void testCreatePayment_NullUser_Fails() {
        // When
        Payment result = paymentService.createPayment(null, new BigDecimal("25.0"), Payment.PaymentMethod.CASH, testFine);

        // Then
        assertNull(result);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should fail to create payment when amount is zero")
    void testCreatePayment_ZeroAmount_Fails() {
        // When
        Payment result = paymentService.createPayment(testUser, BigDecimal.ZERO, Payment.PaymentMethod.CASH, testFine);

        // Then
        assertNull(result);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should fail to create payment when amount is negative")
    void testCreatePayment_NegativeAmount_Fails() {
        // When
        Payment result = paymentService.createPayment(testUser, new BigDecimal("-10.0"), Payment.PaymentMethod.CASH, testFine);

        // Then
        assertNull(result);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should fail to create payment when method is null")
    void testCreatePayment_NullMethod_Fails() {
        // When
        Payment result = paymentService.createPayment(testUser, new BigDecimal("25.0"), null, testFine);

        // Then
        assertNull(result);
        verifyNoInteractions(paymentRepository);
    }

    // ===== PROCESS PAYMENT TESTS =====

    @Test
    @DisplayName("Should process payment successfully")
    void testProcessPayment_Success() {
        // Given
        testPayment.setAmount(new BigDecimal("25.0"));
        testPayment.setStatus(Payment.TransactionStatus.PENDING);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = paymentService.processPayment(1L);

        // Then
        assertTrue(result);
        assertEquals(Payment.TransactionStatus.COMPLETED, testPayment.getStatus());
        assertNotNull(testPayment.getConfirmationNumber());
        assertTrue(testPayment.getConfirmationNumber().startsWith("CONF"));
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should process payment and mark fine as paid")
    void testProcessPayment_WithFine_Success() {
        // Given
        testPayment.setFine(testFine);
        testFine.setPaid(false);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = paymentService.processPayment(1L);

        // Then
        assertTrue(result);
        assertTrue(testFine.isPaid());
        assertNotNull(testFine.getPaidDate());
        assertEquals(Fine.TransactionStatus.COMPLETED, testFine.getStatus());
    }

    @Test
    @DisplayName("Should fail to process payment when ID is null")
    void testProcessPayment_NullId_Fails() {
        // When
        boolean result = paymentService.processPayment(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should fail to process payment when payment not found")
    void testProcessPayment_NotFound_Fails() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = paymentService.processPayment(1L);

        // Then
        assertFalse(result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to process payment when amount is invalid")
    void testProcessPayment_InvalidAmount_Fails() {
        // Given
        testPayment.setAmount(BigDecimal.ZERO);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = paymentService.processPayment(1L);

        // Then
        assertFalse(result);
        assertEquals(Payment.TransactionStatus.FAILED, testPayment.getStatus());
        verify(paymentRepository).findByIdOptional(1L);
    }

    // ===== REFUND PAYMENT TESTS =====

    @Test
    @DisplayName("Should refund payment successfully")
    void testRefundPayment_Success() {
        // Given
        String originalDescription = testPayment.getDescription();
        testPayment.setStatus(Payment.TransactionStatus.COMPLETED);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = paymentService.refundPayment(1L, "Customer request");

        // Then
        assertTrue(result);
        assertEquals(Payment.TransactionStatus.CANCELLED, testPayment.getStatus());
        assertTrue(testPayment.getDescription().contains("Refunded: Customer request"));
        assertTrue(testPayment.getDescription().startsWith(originalDescription));
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to refund payment when ID is null")
    void testRefundPayment_NullId_Fails() {
        // When
        boolean result = paymentService.refundPayment(null, "Test reason");

        // Then
        assertFalse(result);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should fail to refund payment when payment not found")
    void testRefundPayment_NotFound_Fails() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = paymentService.refundPayment(1L, "Test reason");

        // Then
        assertFalse(result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to refund payment when not completed")
    void testRefundPayment_NotCompleted_Fails() {
        // Given
        testPayment.setStatus(Payment.TransactionStatus.PENDING);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = paymentService.refundPayment(1L, "Test reason");

        // Then
        assertFalse(result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to refund payment when already cancelled")
    void testRefundPayment_AlreadyCancelled_Fails() {
        // Given
        testPayment.setStatus(Payment.TransactionStatus.CANCELLED);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = paymentService.refundPayment(1L, "Test reason");

        // Then
        assertFalse(result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    // ===== VALIDATE PAYMENT METHOD TESTS =====

    @Test
    @DisplayName("Should validate payment method correctly")
    void testIsValidPaymentMethod_Valid() {
        // When & Then
        assertTrue(paymentService.isValidPaymentMethod(Payment.PaymentMethod.CASH));
        assertTrue(paymentService.isValidPaymentMethod(Payment.PaymentMethod.CREDIT_CARD));
        assertTrue(paymentService.isValidPaymentMethod(Payment.PaymentMethod.DEBIT_CARD));
        assertTrue(paymentService.isValidPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER));
        assertTrue(paymentService.isValidPaymentMethod(Payment.PaymentMethod.PAYPAL));
        assertTrue(paymentService.isValidPaymentMethod(Payment.PaymentMethod.OTHER));
    }

    @Test
    @DisplayName("Should return false for null payment method")
    void testIsValidPaymentMethod_Null_Invalid() {
        // When & Then
        assertFalse(paymentService.isValidPaymentMethod(null));
    }

    // ===== IS ELECTRONIC PAYMENT TESTS =====

    @Test
    @DisplayName("Should identify electronic payments correctly")
    void testIsElectronicPayment_Electronic() {
        // Test credit card
        testPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        assertTrue(paymentService.isElectronicPayment(testPayment));

        // Test debit card
        testPayment.setPaymentMethod(Payment.PaymentMethod.DEBIT_CARD);
        assertTrue(paymentService.isElectronicPayment(testPayment));

        // Test bank transfer
        testPayment.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);
        assertTrue(paymentService.isElectronicPayment(testPayment));

        // Test PayPal
        testPayment.setPaymentMethod(Payment.PaymentMethod.PAYPAL);
        assertTrue(paymentService.isElectronicPayment(testPayment));
    }

    @Test
    @DisplayName("Should identify non-electronic payments correctly")
    void testIsElectronicPayment_NonElectronic() {
        // Test cash
        testPayment.setPaymentMethod(Payment.PaymentMethod.CASH);
        assertFalse(paymentService.isElectronicPayment(testPayment));

        // Test other
        testPayment.setPaymentMethod(Payment.PaymentMethod.OTHER);
        assertFalse(paymentService.isElectronicPayment(testPayment));
    }

    @Test
    @DisplayName("Should return false when payment is null")
    void testIsElectronicPayment_NullPayment_False() {
        // When & Then
        assertFalse(paymentService.isElectronicPayment(null));
    }

    @Test
    @DisplayName("Should return false when payment method is null")
    void testIsElectronicPayment_NullMethod_False() {
        // Given
        testPayment.setPaymentMethod(null);

        // When & Then
        assertFalse(paymentService.isElectronicPayment(testPayment));
    }

    // ===== GET PAYMENT STATUS TESTS =====

    @Test
    @DisplayName("Should return COMPLETED status")
    void testGetPaymentStatus_Completed() {
        // Given
        testPayment.setStatus(Payment.TransactionStatus.COMPLETED);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        String result = paymentService.getPaymentStatus(1L);

        // Then
        assertEquals("COMPLETED", result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return FAILED status")
    void testGetPaymentStatus_Failed() {
        // Given
        testPayment.setStatus(Payment.TransactionStatus.FAILED);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        String result = paymentService.getPaymentStatus(1L);

        // Then
        assertEquals("FAILED", result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return REFUNDED status")
    void testGetPaymentStatus_Refunded() {
        // Given
        testPayment.setStatus(Payment.TransactionStatus.CANCELLED);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        String result = paymentService.getPaymentStatus(1L);

        // Then
        assertEquals("REFUNDED", result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return PENDING status")
    void testGetPaymentStatus_Pending() {
        // Given
        testPayment.setStatus(Payment.TransactionStatus.PENDING);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        String result = paymentService.getPaymentStatus(1L);

        // Then
        assertEquals("PENDING", result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return INVALID when payment ID is null")
    void testGetPaymentStatus_NullId_Invalid() {
        // When
        String result = paymentService.getPaymentStatus(null);

        // Then
        assertEquals("INVALID", result);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when payment not found")
    void testGetPaymentStatus_NotFound() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        String result = paymentService.getPaymentStatus(1L);

        // Then
        assertEquals("NOT_FOUND", result);
        verify(paymentRepository).findByIdOptional(1L);
    }

    // ===== DISPLAY PAYMENT DETAILS TESTS =====

    @Test
    @DisplayName("Should display payment details without throwing exception")
    void testDisplayPaymentDetails_Success() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> paymentService.displayPaymentDetails(1L));
        verify(paymentRepository, atLeastOnce()).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should handle display payment details when ID is null")
    void testDisplayPaymentDetails_NullId() {
        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> paymentService.displayPaymentDetails(null));
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should handle display payment details when payment not found")
    void testDisplayPaymentDetails_NotFound() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> paymentService.displayPaymentDetails(1L));
        verify(paymentRepository).findByIdOptional(1L);
    }

    // ===== GET PROCESSING FEE TESTS =====

    @Test
    @DisplayName("Should calculate credit card processing fee correctly")
    void testGetProcessingFee_CreditCard() {
        // Given
        testPayment.setAmount(new BigDecimal("100.0"));
        testPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);

        // When
        BigDecimal result = paymentService.getProcessingFee(testPayment);

        // Then
        assertEquals(0, new BigDecimal("2.50").compareTo(result)); // 2.5% of $100 = $2.50
    }

    @Test
    @DisplayName("Should calculate debit card processing fee correctly")
    void testGetProcessingFee_DebitCard() {
        // Given
        testPayment.setPaymentMethod(Payment.PaymentMethod.DEBIT_CARD);

        // When
        BigDecimal result = paymentService.getProcessingFee(testPayment);

        // Then
        assertEquals(0, new BigDecimal("0.50").compareTo(result)); // Fixed $0.50 fee
    }

    @Test
    @DisplayName("Should calculate PayPal processing fee correctly")
    void testGetProcessingFee_PayPal() {
        // Given
        testPayment.setAmount(new BigDecimal("100.0"));
        testPayment.setPaymentMethod(Payment.PaymentMethod.PAYPAL);

        // When
        BigDecimal result = paymentService.getProcessingFee(testPayment);

        // Then
        assertEquals(0, new BigDecimal("3.20").compareTo(result)); // 2.9% of $100 + $0.30 = $3.20
    }

    @Test
    @DisplayName("Should calculate bank transfer processing fee correctly")
    void testGetProcessingFee_BankTransfer() {
        // Given
        testPayment.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);

        // When
        BigDecimal result = paymentService.getProcessingFee(testPayment);

        // Then
        assertEquals(0, new BigDecimal("1.00").compareTo(result)); // Fixed $1.00 fee
    }

    @Test
    @DisplayName("Should return zero fee for cash payments")
    void testGetProcessingFee_Cash() {
        // Given
        testPayment.setPaymentMethod(Payment.PaymentMethod.CASH);

        // When
        BigDecimal result = paymentService.getProcessingFee(testPayment);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result)); // No fee for cash
    }

    @Test
    @DisplayName("Should return zero fee for other payment methods")
    void testGetProcessingFee_Other() {
        // Given
        testPayment.setPaymentMethod(Payment.PaymentMethod.OTHER);

        // When
        BigDecimal result = paymentService.getProcessingFee(testPayment);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result)); // No fee for other
    }

    @Test
    @DisplayName("Should return zero fee when payment is null")
    void testGetProcessingFee_NullPayment() {
        // When
        BigDecimal result = paymentService.getProcessingFee(null);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    @DisplayName("Should return zero fee when payment method is null")
    void testGetProcessingFee_NullMethod() {
        // Given
        testPayment.setPaymentMethod(null);

        // When
        BigDecimal result = paymentService.getProcessingFee(testPayment);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    // ===== DELETE PAYMENT TESTS =====

    @Test
    @DisplayName("Should delete payment successfully")
    void testDeletePayment_Success() {
        // Given
        testPayment.setStatus(Payment.TransactionStatus.PENDING);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));
        doNothing().when(paymentRepository).delete(testPayment);

        // When
        boolean result = paymentService.deletePayment(1L);

        // Then
        assertTrue(result);
        verify(paymentRepository).findByIdOptional(1L);
        verify(paymentRepository).delete(testPayment);
    }

    @Test
    @DisplayName("Should fail to delete payment when ID is null")
    void testDeletePayment_NullId_Fails() {
        // When
        boolean result = paymentService.deletePayment(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should fail to delete payment when payment not found")
    void testDeletePayment_NotFound_Fails() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = paymentService.deletePayment(1L);

        // Then
        assertFalse(result);
        verify(paymentRepository).findByIdOptional(1L);
        verify(paymentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should fail to delete payment when payment is completed")
    void testDeletePayment_CompletedPayment_Fails() {
        // Given
        testPayment.setStatus(Payment.TransactionStatus.COMPLETED);
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // When
        boolean result = paymentService.deletePayment(1L);

        // Then
        assertFalse(result);
        verify(paymentRepository).findByIdOptional(1L);
        verify(paymentRepository, never()).delete(any());
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should create payment with all payment methods")
    void testCreatePayment_AllPaymentMethods_Success() {
        // Given
        doAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return null;
        }).when(paymentRepository).persist(any(Payment.class));

        // Test each payment method
        Payment.PaymentMethod[] paymentMethods = Payment.PaymentMethod.values();
        for (Payment.PaymentMethod method : paymentMethods) {
            // When
            Payment result = paymentService.createPayment(testUser, new BigDecimal("25.0"), method, testFine);

            // Then
            assertNotNull(result, "Payment creation failed for method: " + method);
            assertEquals(method, result.getPaymentMethod());
        }

        verify(paymentRepository, times(paymentMethods.length)).persist(any(Payment.class));
    }

    @Test
    @DisplayName("Should generate unique reference numbers for payments")
    void testCreatePayment_UniqueReferenceNumbers() throws InterruptedException {
        // Given
        doAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return null;
        }).when(paymentRepository).persist(any(Payment.class));

        // When
        Payment payment1 = paymentService.createPayment(testUser, new BigDecimal("25.0"), Payment.PaymentMethod.CASH, null);
        Thread.sleep(1); // Ensure different timestamps
        Payment payment2 = paymentService.createPayment(testUser, new BigDecimal("30.0"), Payment.PaymentMethod.CASH, null);

        // Then
        assertNotNull(payment1);
        assertNotNull(payment2);
        assertNotEquals(payment1.getReferenceNumber(), payment2.getReferenceNumber());
        assertTrue(payment1.getReferenceNumber().startsWith("PAY"));
        assertTrue(payment2.getReferenceNumber().startsWith("PAY"));
    }

    @Test
    @DisplayName("Should handle payment workflow correctly")
    void testPaymentWorkflow_Success() {
        // Given
        when(paymentRepository.findByIdOptional(1L)).thenReturn(Optional.of(testPayment));

        // Initially pending
        testPayment.setStatus(Payment.TransactionStatus.PENDING);
        assertEquals("PENDING", paymentService.getPaymentStatus(1L));

        // Process payment
        assertTrue(paymentService.processPayment(1L));
        assertEquals("COMPLETED", paymentService.getPaymentStatus(1L));

        // Refund payment
        assertTrue(paymentService.refundPayment(1L, "Customer request"));
        assertEquals("REFUNDED", paymentService.getPaymentStatus(1L));

        // Verify state changes
        assertEquals(Payment.TransactionStatus.CANCELLED, testPayment.getStatus());
        assertNotNull(testPayment.getConfirmationNumber());
        assertTrue(testPayment.getDescription().contains("Refunded"));
    }

    @Test
    @DisplayName("Should handle processing fee calculations for various amounts")
    void testProcessingFeeCalculations_VariousAmounts() {
        // Test credit card with different amounts
        testPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        
        testPayment.setAmount(new BigDecimal("10.0"));
        assertEquals(0, new BigDecimal("0.25").compareTo(paymentService.getProcessingFee(testPayment)));
        
        testPayment.setAmount(new BigDecimal("50.0"));
        assertEquals(0, new BigDecimal("1.25").compareTo(paymentService.getProcessingFee(testPayment)));
        
        testPayment.setAmount(new BigDecimal("200.0"));
        assertEquals(0, new BigDecimal("5.0").compareTo(paymentService.getProcessingFee(testPayment)));

        // Test PayPal with different amounts
        testPayment.setPaymentMethod(Payment.PaymentMethod.PAYPAL);
        
        testPayment.setAmount(new BigDecimal("10.0"));
        assertEquals(0, new BigDecimal("0.59").compareTo(paymentService.getProcessingFee(testPayment))); // 2.9% of $10 + $0.30
        
        testPayment.setAmount(new BigDecimal("100.0"));
        assertEquals(0, new BigDecimal("3.20").compareTo(paymentService.getProcessingFee(testPayment))); // 2.9% of $100 + $0.30
    }
} 