package com.davon.library.service;

import com.davon.library.model.Fine;
import com.davon.library.model.User;
import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.repository.FineRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.repository.LoanRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("FineService Unit Tests")
public class FineServiceTest {

    @Inject
    FineService fineService;

    @InjectMock
    FineRepository fineRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    LoanRepository loanRepository;

    private Fine testFine;
    private User testUser;
    private Loan testLoan;
    private Book testBook;
    private Member testMember;

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

        // Setup test member
        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("Test Member");
        testMember.setEmail("member@example.com");
        testMember.setMembershipNumber("MEM001");

        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");

        // Setup test loan
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setMember(testMember);
        testLoan.setBook(testBook);
        testLoan.setLoanDate(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 14);
        testLoan.setDueDate(cal.getTime());
        testLoan.setStatus(Loan.LoanStatus.ACTIVE);

        // Setup test fine
        testFine = new Fine();
        testFine.setId(1L);
        testFine.setUser(testUser);
        testFine.setAmount(25.0);
        testFine.setType("FINE");
        testFine.setDescription("Overdue book fine");
        testFine.setDate(new Date());
        testFine.setStatus(Fine.TransactionStatus.PENDING);
        testFine.setReason("Book returned late");
        testFine.setFineType(Fine.FineType.OVERDUE);
        testFine.setIssuedDate(new Date());
        testFine.setPaid(false);
        testFine.setLoan(testLoan);
    }

    // ===== GET FINES BY USER TESTS =====

    @Test
    @DisplayName("Should get fines by user successfully")
    void testGetFinesByUser_Success() {
        // Given
        List<Fine> fines = List.of(testFine);
        when(fineRepository.findByUser(testUser)).thenReturn(fines);

        // When
        List<Fine> result = fineService.getFinesByUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFine, result.get(0));
        verify(fineRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("Should return empty list when user has no fines")
    void testGetFinesByUser_NoFines() {
        // Given
        when(fineRepository.findByUser(testUser)).thenReturn(List.of());

        // When
        List<Fine> result = fineService.getFinesByUser(testUser);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(fineRepository).findByUser(testUser);
    }

    // ===== GET FINE BY ID TESTS =====

    @Test
    @DisplayName("Should get fine by ID successfully")
    void testGetFineById_Success() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        Optional<Fine> result = fineService.getFineById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testFine, result.get());
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return empty when fine not found")
    void testGetFineById_NotFound() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        Optional<Fine> result = fineService.getFineById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(fineRepository).findByIdOptional(1L);
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
        Fine result = fineService.createFine(testUser, 25.0, "Overdue book", Fine.FineType.OVERDUE, testLoan);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(25.0, result.getAmount());
        assertEquals("FINE", result.getType());
        assertEquals("Overdue book", result.getDescription());
        assertEquals("Overdue book", result.getReason());
        assertEquals(Fine.FineType.OVERDUE, result.getFineType());
        assertEquals(Fine.TransactionStatus.PENDING, result.getStatus());
        assertEquals(testLoan, result.getLoan());
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
        Fine result = fineService.createFine(testUser, 25.0, "Test fine", null, testLoan);

        // Then
        assertNotNull(result);
        assertEquals(Fine.FineType.OVERDUE, result.getFineType()); // Should default to OVERDUE
        verify(fineRepository).persist(any(Fine.class));
    }

    @Test
    @DisplayName("Should create fine with null loan")
    void testCreateFine_NullLoan_Success() {
        // Given
        doAnswer(invocation -> {
            Fine fine = invocation.getArgument(0);
            fine.setId(1L);
            return null;
        }).when(fineRepository).persist(any(Fine.class));

        // When
        Fine result = fineService.createFine(testUser, 25.0, "Test fine", Fine.FineType.DAMAGE, null);

        // Then
        assertNotNull(result);
        assertNull(result.getLoan());
        verify(fineRepository).persist(any(Fine.class));
    }

    @Test
    @DisplayName("Should fail to create fine when user is null")
    void testCreateFine_NullUser_Fails() {
        // When
        Fine result = fineService.createFine(null, 25.0, "Test fine", Fine.FineType.OVERDUE, testLoan);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to create fine when amount is zero")
    void testCreateFine_ZeroAmount_Fails() {
        // When
        Fine result = fineService.createFine(testUser, 0.0, "Test fine", Fine.FineType.OVERDUE, testLoan);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to create fine when amount is negative")
    void testCreateFine_NegativeAmount_Fails() {
        // When
        Fine result = fineService.createFine(testUser, -10.0, "Test fine", Fine.FineType.OVERDUE, testLoan);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to create fine when reason is null")
    void testCreateFine_NullReason_Fails() {
        // When
        Fine result = fineService.createFine(testUser, 25.0, null, Fine.FineType.OVERDUE, testLoan);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to create fine when reason is empty")
    void testCreateFine_EmptyReason_Fails() {
        // When
        Fine result = fineService.createFine(testUser, 25.0, "   ", Fine.FineType.OVERDUE, testLoan);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should trim reason when creating fine")
    void testCreateFine_TrimReason_Success() {
        // Given
        doAnswer(invocation -> {
            Fine fine = invocation.getArgument(0);
            fine.setId(1L);
            return null;
        }).when(fineRepository).persist(any(Fine.class));

        // When
        Fine result = fineService.createFine(testUser, 25.0, "  Test fine  ", Fine.FineType.OVERDUE, testLoan);

        // Then
        assertNotNull(result);
        assertEquals("Test fine", result.getReason());
        verify(fineRepository).persist(any(Fine.class));
    }

    // ===== IS FINES PAID TESTS =====

    @Test
    @DisplayName("Should return true when fine is paid")
    void testIsFinesPaid_True() {
        // Given
        testFine.setPaid(true);
        testFine.setPaidDate(new Date());
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.isFinesPaid(1L);

        // Then
        assertTrue(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when fine is not paid")
    void testIsFinesPaid_False() {
        // Given
        testFine.setPaid(false);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.isFinesPaid(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when fine is paid but no paid date")
    void testIsFinesPaid_PaidButNoPaidDate_False() {
        // Given
        testFine.setPaid(true);
        testFine.setPaidDate(null);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.isFinesPaid(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when fine ID is null")
    void testIsFinesPaid_NullId_False() {
        // When
        boolean result = fineService.isFinesPaid(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should return false when fine not found")
    void testIsFinesPaid_NotFound_False() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = fineService.isFinesPaid(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    // ===== MARK FINE AS PAID TESTS =====

    @Test
    @DisplayName("Should mark fine as paid successfully")
    void testMarkFineAsPaid_Success() {
        // Given
        testFine.setPaid(false);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.markFineAsPaid(1L);

        // Then
        assertTrue(result);
        assertTrue(testFine.isPaid());
        assertNotNull(testFine.getPaidDate());
        assertEquals(Fine.TransactionStatus.COMPLETED, testFine.getStatus());
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to mark fine as paid when already paid")
    void testMarkFineAsPaid_AlreadyPaid_Fails() {
        // Given
        testFine.setPaid(true);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.markFineAsPaid(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to mark fine as paid when ID is null")
    void testMarkFineAsPaid_NullId_Fails() {
        // When
        boolean result = fineService.markFineAsPaid(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to mark fine as paid when fine not found")
    void testMarkFineAsPaid_NotFound_Fails() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = fineService.markFineAsPaid(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    // ===== GET DAYS SINCE ISSUED TESTS =====

    @Test
    @DisplayName("Should calculate days since issued correctly")
    void testGetDaysSinceIssued_Success() {
        // Given - fine issued 5 days ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5);
        testFine.setIssuedDate(cal.getTime());
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        long result = fineService.getDaysSinceIssued(1L);

        // Then
        assertEquals(5, result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return 0 when fine ID is null")
    void testGetDaysSinceIssued_NullId_Zero() {
        // When
        long result = fineService.getDaysSinceIssued(null);

        // Then
        assertEquals(0, result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should return 0 when fine not found")
    void testGetDaysSinceIssued_NotFound_Zero() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        long result = fineService.getDaysSinceIssued(1L);

        // Then
        assertEquals(0, result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return 0 when issued date is null")
    void testGetDaysSinceIssued_NullIssuedDate_Zero() {
        // Given
        testFine.setIssuedDate(null);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        long result = fineService.getDaysSinceIssued(1L);

        // Then
        assertEquals(0, result);
        verify(fineRepository).findByIdOptional(1L);
    }

    // ===== IS FINE OVERDUE TESTS =====

    @Test
    @DisplayName("Should return true when fine is overdue (>30 days and not paid)")
    void testIsFineOverdue_True() {
        // Given - fine issued 35 days ago and not paid
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -35);
        testFine.setIssuedDate(cal.getTime());
        testFine.setPaid(false);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.isFineOverdue(1L);

        // Then
        assertTrue(result);
        verify(fineRepository, times(2)).findByIdOptional(1L); // Called twice: once for overdue check, once for days calculation
    }

    @Test
    @DisplayName("Should return false when fine is not overdue (<30 days)")
    void testIsFineOverdue_NotOverdue_False() {
        // Given - fine issued 20 days ago and not paid
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -20);
        testFine.setIssuedDate(cal.getTime());
        testFine.setPaid(false);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.isFineOverdue(1L);

        // Then
        assertFalse(result);
        verify(fineRepository, times(2)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when fine is paid even if >30 days")
    void testIsFineOverdue_PaidFine_False() {
        // Given - fine issued 35 days ago but paid
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -35);
        testFine.setIssuedDate(cal.getTime());
        testFine.setPaid(true);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.isFineOverdue(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return false when fine ID is null")
    void testIsFineOverdue_NullId_False() {
        // When
        boolean result = fineService.isFineOverdue(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should return false when fine not found")
    void testIsFineOverdue_NotFound_False() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = fineService.isFineOverdue(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    // ===== GET FINE STATUS TESTS =====

    @Test
    @DisplayName("Should return PAID status when fine is paid")
    void testGetFineStatus_Paid() {
        // Given
        testFine.setPaid(true);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        String result = fineService.getFineStatus(1L);

        // Then
        assertEquals("PAID", result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return OVERDUE status when fine is overdue")
    void testGetFineStatus_Overdue() {
        // Given - fine issued 35 days ago and not paid
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -35);
        testFine.setIssuedDate(cal.getTime());
        testFine.setPaid(false);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        String result = fineService.getFineStatus(1L);

        // Then
        assertEquals("OVERDUE", result);
        verify(fineRepository, times(3)).findByIdOptional(1L); // Called multiple times for status checks
    }

    @Test
    @DisplayName("Should return CANCELLED status when fine is cancelled")
    void testGetFineStatus_Cancelled() {
        // Given
        testFine.setPaid(false);
        testFine.setStatus(Fine.TransactionStatus.CANCELLED);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        String result = fineService.getFineStatus(1L);

        // Then
        assertEquals("CANCELLED", result);
        verify(fineRepository, times(3)).findByIdOptional(1L); // Called 3 times: main call + isFineOverdue calls
    }

    @Test
    @DisplayName("Should return PENDING status when fine is pending")
    void testGetFineStatus_Pending() {
        // Given - fine issued recently and not paid
        testFine.setPaid(false);
        testFine.setStatus(Fine.TransactionStatus.PENDING);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        String result = fineService.getFineStatus(1L);

        // Then
        assertEquals("PENDING", result);
        verify(fineRepository, times(3)).findByIdOptional(1L); // Called 3 times: main call + isFineOverdue calls
    }

    @Test
    @DisplayName("Should return INVALID when fine ID is null")
    void testGetFineStatus_NullId_Invalid() {
        // When
        String result = fineService.getFineStatus(null);

        // Then
        assertEquals("INVALID", result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when fine not found")
    void testGetFineStatus_NotFound() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        String result = fineService.getFineStatus(1L);

        // Then
        assertEquals("NOT_FOUND", result);
        verify(fineRepository).findByIdOptional(1L);
    }

    // ===== APPLY LATE PENALTY TESTS =====

    @Test
    @DisplayName("Should apply late penalty successfully")
    void testApplyLatePenalty_Success() {
        // Given
        double originalAmount = testFine.getAmount();
        String originalDescription = testFine.getDescription();
        testFine.setPaid(false);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.applyLatePenalty(1L, 10.0);

        // Then
        assertTrue(result);
        assertEquals(originalAmount + 10.0, testFine.getAmount());
        assertTrue(testFine.getDescription().contains("Late penalty: $10.0"));
        assertTrue(testFine.getDescription().startsWith(originalDescription));
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to apply late penalty when fine ID is null")
    void testApplyLatePenalty_NullId_Fails() {
        // When
        boolean result = fineService.applyLatePenalty(null, 10.0);

        // Then
        assertFalse(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to apply late penalty when penalty amount is zero")
    void testApplyLatePenalty_ZeroPenalty_Fails() {
        // When
        boolean result = fineService.applyLatePenalty(1L, 0.0);

        // Then
        assertFalse(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to apply late penalty when penalty amount is negative")
    void testApplyLatePenalty_NegativePenalty_Fails() {
        // When
        boolean result = fineService.applyLatePenalty(1L, -5.0);

        // Then
        assertFalse(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to apply late penalty when fine not found")
    void testApplyLatePenalty_NotFound_Fails() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = fineService.applyLatePenalty(1L, 10.0);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to apply late penalty when fine is already paid")
    void testApplyLatePenalty_PaidFine_Fails() {
        // Given
        testFine.setPaid(true);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.applyLatePenalty(1L, 10.0);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
    }

    // ===== GET UNPAID FINES BY USER TESTS =====

    @Test
    @DisplayName("Should get unpaid fines by user successfully")
    void testGetUnpaidFinesByUser_Success() {
        // Given
        List<Fine> unpaidFines = List.of(testFine);
        when(fineRepository.findUnpaidFines(testUser)).thenReturn(unpaidFines);

        // When
        List<Fine> result = fineService.getUnpaidFinesByUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFine, result.get(0));
        verify(fineRepository).findUnpaidFines(testUser);
    }

    @Test
    @DisplayName("Should return empty list when user is null")
    void testGetUnpaidFinesByUser_NullUser_EmptyList() {
        // When
        List<Fine> result = fineService.getUnpaidFinesByUser(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(fineRepository);
    }

    // ===== CALCULATE TOTAL UNPAID FINES TESTS =====

    @Test
    @DisplayName("Should calculate total unpaid fines successfully")
    void testCalculateTotalUnpaidFines_Success() {
        // Given
        when(fineRepository.calculateTotalUnpaidFines(testUser)).thenReturn(75.0);

        // When
        double result = fineService.calculateTotalUnpaidFines(testUser);

        // Then
        assertEquals(75.0, result);
        verify(fineRepository).calculateTotalUnpaidFines(testUser);
    }

    @Test
    @DisplayName("Should return 0.0 when user is null")
    void testCalculateTotalUnpaidFines_NullUser_Zero() {
        // When
        double result = fineService.calculateTotalUnpaidFines(null);

        // Then
        assertEquals(0.0, result);
        verifyNoInteractions(fineRepository);
    }

    // ===== DISPLAY FINE DETAILS TESTS =====

    @Test
    @DisplayName("Should display fine details without throwing exception")
    void testDisplayFineDetails_Success() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> fineService.displayFineDetails(1L));
        verify(fineRepository, atLeastOnce()).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should handle display fine details when ID is null")
    void testDisplayFineDetails_NullId() {
        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> fineService.displayFineDetails(null));
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should handle display fine details when fine not found")
    void testDisplayFineDetails_NotFound() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> fineService.displayFineDetails(1L));
        verify(fineRepository).findByIdOptional(1L);
    }

    // ===== DELETE FINE TESTS =====

    @Test
    @DisplayName("Should delete fine successfully")
    void testDeleteFine_Success() {
        // Given
        testFine.setPaid(false);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));
        doNothing().when(fineRepository).delete(testFine);

        // When
        boolean result = fineService.deleteFine(1L);

        // Then
        assertTrue(result);
        verify(fineRepository).findByIdOptional(1L);
        verify(fineRepository).delete(testFine);
    }

    @Test
    @DisplayName("Should fail to delete fine when ID is null")
    void testDeleteFine_NullId_Fails() {
        // When
        boolean result = fineService.deleteFine(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should fail to delete fine when fine not found")
    void testDeleteFine_NotFound_Fails() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = fineService.deleteFine(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
        verify(fineRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should fail to delete fine when fine is paid")
    void testDeleteFine_PaidFine_Fails() {
        // Given
        testFine.setPaid(true);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        boolean result = fineService.deleteFine(1L);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(1L);
        verify(fineRepository, never()).delete(any());
    }

    // ===== EDGE CASE TESTS =====

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
            Fine result = fineService.createFine(testUser, 25.0, "Test fine", fineType, testLoan);

            // Then
            assertNotNull(result, "Fine creation failed for type: " + fineType);
            assertEquals(fineType, result.getFineType());
        }

        verify(fineRepository, times(fineTypes.length)).persist(any(Fine.class));
    }

    @Test
    @DisplayName("Should handle fine status transitions correctly")
    void testFineStatusTransitions_Success() {
        // Given
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // Initially pending
        testFine.setStatus(Fine.TransactionStatus.PENDING);
        testFine.setPaid(false);
        assertEquals("PENDING", fineService.getFineStatus(1L));

        // Mark as paid
        assertTrue(fineService.markFineAsPaid(1L));
        assertEquals("PAID", fineService.getFineStatus(1L));

        // Verify state changes
        assertTrue(testFine.isPaid());
        assertNotNull(testFine.getPaidDate());
        assertEquals(Fine.TransactionStatus.COMPLETED, testFine.getStatus());
    }

    @Test
    @DisplayName("Should handle multiple penalty applications")
    void testMultiplePenaltyApplications_Success() {
        // Given
        double originalAmount = 25.0;
        testFine.setAmount(originalAmount);
        testFine.setPaid(false);
        when(fineRepository.findByIdOptional(1L)).thenReturn(Optional.of(testFine));

        // When
        assertTrue(fineService.applyLatePenalty(1L, 10.0));
        assertTrue(fineService.applyLatePenalty(1L, 5.0));

        // Then
        assertEquals(originalAmount + 15.0, testFine.getAmount()); // 25 + 10 + 5 = 40
        assertTrue(testFine.getDescription().contains("Late penalty: $10.0"));
        assertTrue(testFine.getDescription().contains("Late penalty: $5.0"));
    }
} 