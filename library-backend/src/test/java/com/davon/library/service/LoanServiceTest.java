package com.davon.library.service;

import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.repository.LoanRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Calendar;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("LoanService Unit Tests")
public class LoanServiceTest {

    @Inject
    LoanService loanService;

    @InjectMock
    LoanRepository loanRepository;

    private Loan testLoan;
    private Member testMember;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Setup test member
        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("Test Member");
        testMember.setEmail("test@member.com");

        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");

        // Setup test loan (active, due in 7 days)
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setMember(testMember);
        testLoan.setBook(testBook);
        testLoan.setLoanDate(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7); // Due in 7 days
        testLoan.setDueDate(cal.getTime());
        
        testLoan.setStatus(Loan.LoanStatus.ACTIVE);
        testLoan.setFineAmount(0.0);
        testLoan.setReturnDate(null);
    }

    // ===== CALCULATE FINE TESTS =====

    @Test
    @DisplayName("Should calculate fine correctly for overdue loan")
    void testCalculateFine_OverdueLoan_Success() {
        // Given - loan overdue by 5 days
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5); // Due 5 days ago
        testLoan.setDueDate(cal.getTime());
        testLoan.setReturnDate(null); // Not returned yet
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        double fine = loanService.calculateFine(1L);

        // Then
        assertEquals(5.0, fine, 0.01); // $1 per day * 5 days = $5
    }

    @Test
    @DisplayName("Should return zero fine for non-overdue loan")
    void testCalculateFine_NotOverdue_ZeroFine() {
        // Given - loan due in future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3); // Due in 3 days
        testLoan.setDueDate(cal.getTime());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        double fine = loanService.calculateFine(1L);

        // Then
        assertEquals(0.0, fine, 0.01);
    }

    @Test
    @DisplayName("Should return zero fine for returned loan")
    void testCalculateFine_ReturnedLoan_ZeroFine() {
        // Given - loan already returned
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5); // Was due 5 days ago
        testLoan.setDueDate(cal.getTime());
        testLoan.setReturnDate(new Date()); // But returned
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        double fine = loanService.calculateFine(1L);

        // Then
        assertEquals(0.0, fine, 0.01);
    }

    @Test
    @DisplayName("Should cap fine at maximum amount")
    void testCalculateFine_MaximumFine_Capped() {
        // Given - loan overdue by 60 days (more than $50 max)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -60); // Due 60 days ago
        testLoan.setDueDate(cal.getTime());
        testLoan.setReturnDate(null);
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        double fine = loanService.calculateFine(1L);

        // Then
        assertEquals(50.0, fine, 0.01); // Capped at $50 maximum
    }

    @Test
    @DisplayName("Should return zero fine when loan ID is null")
    void testCalculateFine_NullLoanId_ZeroFine() {
        // When
        double fine = loanService.calculateFine(null);

        // Then
        assertEquals(0.0, fine, 0.01);
        verify(loanRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should return zero fine when loan not found")
    void testCalculateFine_LoanNotFound_ZeroFine() {
        // Given
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        double fine = loanService.calculateFine(1L);

        // Then
        assertEquals(0.0, fine, 0.01);
    }

    // ===== IS OVERDUE TESTS =====

    @Test
    @DisplayName("Should return true for overdue loan")
    void testIsOverdue_OverdueLoan_True() {
        // Given - loan overdue by 2 days
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        testLoan.setDueDate(cal.getTime());
        testLoan.setReturnDate(null);
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean isOverdue = loanService.isOverdue(1L);

        // Then
        assertTrue(isOverdue);
    }

    @Test
    @DisplayName("Should return false for non-overdue loan")
    void testIsOverdue_NotOverdue_False() {
        // Given - loan due in future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 5);
        testLoan.setDueDate(cal.getTime());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean isOverdue = loanService.isOverdue(1L);

        // Then
        assertFalse(isOverdue);
    }

    @Test
    @DisplayName("Should return false for returned loan")
    void testIsOverdue_ReturnedLoan_False() {
        // Given - returned loan (even if was overdue)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        testLoan.setDueDate(cal.getTime());
        testLoan.setReturnDate(new Date());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean isOverdue = loanService.isOverdue(1L);

        // Then
        assertFalse(isOverdue);
    }

    @Test
    @DisplayName("Should return false when loan ID is null")
    void testIsOverdue_NullLoanId_False() {
        // When
        boolean isOverdue = loanService.isOverdue(null);

        // Then
        assertFalse(isOverdue);
        verify(loanRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should return false when loan not found")
    void testIsOverdue_LoanNotFound_False() {
        // Given
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean isOverdue = loanService.isOverdue(1L);

        // Then
        assertFalse(isOverdue);
    }

    // ===== MARK AS RETURNED TESTS =====

    @Test
    @DisplayName("Should mark loan as returned successfully")
    void testMarkAsReturned_Success() {
        // Given - active loan
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean result = loanService.markAsReturned(1L);

        // Then
        assertTrue(result);
        assertNotNull(testLoan.getReturnDate());
        assertEquals(Loan.LoanStatus.RETURNED, testLoan.getStatus());
    }

    @Test
    @DisplayName("Should calculate fine when marking overdue loan as returned")
    void testMarkAsReturned_OverdueLoan_CalculatesFine() {
        // Given - overdue loan
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3); // Due 3 days ago
        testLoan.setDueDate(cal.getTime());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean result = loanService.markAsReturned(1L);

        // Then
        assertTrue(result);
        assertNotNull(testLoan.getReturnDate());
        assertEquals(Loan.LoanStatus.RETURNED, testLoan.getStatus());
        assertEquals(3.0, testLoan.getFineAmount(), 0.01); // $3 fine for 3 days overdue
    }

    @Test
    @DisplayName("Should fail to mark as returned when already returned")
    void testMarkAsReturned_AlreadyReturned_Fails() {
        // Given - already returned loan
        testLoan.setReturnDate(new Date());
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean result = loanService.markAsReturned(1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to mark as returned when loan ID is null")
    void testMarkAsReturned_NullLoanId_Fails() {
        // When
        boolean result = loanService.markAsReturned(null);

        // Then
        assertFalse(result);
        verify(loanRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to mark as returned when loan not found")
    void testMarkAsReturned_LoanNotFound_Fails() {
        // Given
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = loanService.markAsReturned(1L);

        // Then
        assertFalse(result);
    }

    // ===== RENEW LOAN TESTS =====

    @Test
    @DisplayName("Should renew loan successfully")
    void testRenewLoan_Success() {
        // Given - active loan not overdue
        Date originalDueDate = testLoan.getDueDate();
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean result = loanService.renewLoan(1L, 7);

        // Then
        assertTrue(result);
        assertEquals(Loan.LoanStatus.RENEWED, testLoan.getStatus());
        assertTrue(testLoan.getDueDate().after(originalDueDate));
        
        // Check that due date was extended by 7 days
        Calendar cal = Calendar.getInstance();
        cal.setTime(originalDueDate);
        cal.add(Calendar.DAY_OF_MONTH, 7);
        assertEquals(cal.getTime(), testLoan.getDueDate());
    }

    @Test
    @DisplayName("Should fail to renew overdue loan")
    void testRenewLoan_OverdueLoan_Fails() {
        // Given - overdue loan
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2); // Due 2 days ago
        testLoan.setDueDate(cal.getTime());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean result = loanService.renewLoan(1L, 7);

        // Then
        assertFalse(result);
        assertEquals(Loan.LoanStatus.ACTIVE, testLoan.getStatus()); // Status unchanged
    }

    @Test
    @DisplayName("Should fail to renew returned loan")
    void testRenewLoan_ReturnedLoan_Fails() {
        // Given - returned loan
        testLoan.setReturnDate(new Date());
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean result = loanService.renewLoan(1L, 7);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to renew with invalid renewal period")
    void testRenewLoan_InvalidPeriod_Fails() {
        // Given
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When & Then - test various invalid periods
        assertFalse(loanService.renewLoan(1L, 0));   // Zero days
        assertFalse(loanService.renewLoan(1L, -5));  // Negative days
        assertFalse(loanService.renewLoan(1L, 15));  // More than 14 days
        
        verify(loanRepository, times(3)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to renew when loan ID is null")
    void testRenewLoan_NullLoanId_Fails() {
        // When
        boolean result = loanService.renewLoan(null, 7);

        // Then
        assertFalse(result);
        verify(loanRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to renew when loan not found")
    void testRenewLoan_LoanNotFound_Fails() {
        // Given
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = loanService.renewLoan(1L, 7);

        // Then
        assertFalse(result);
    }

    // ===== GET DAYS UNTIL DUE TESTS =====

    @Test
    @DisplayName("Should calculate days until due correctly")
    void testGetDaysUntilDue_Success() {
        // Given - loan due in 5 days
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 5);
        testLoan.setDueDate(cal.getTime());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        long daysUntilDue = loanService.getDaysUntilDue(1L);

        // Then
        assertEquals(5L, daysUntilDue);
    }

    @Test
    @DisplayName("Should return zero for overdue loan")
    void testGetDaysUntilDue_OverdueLoan_Zero() {
        // Given - overdue loan
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        testLoan.setDueDate(cal.getTime());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        long daysUntilDue = loanService.getDaysUntilDue(1L);

        // Then
        assertEquals(0L, daysUntilDue);
    }

    @Test
    @DisplayName("Should return zero for returned loan")
    void testGetDaysUntilDue_ReturnedLoan_Zero() {
        // Given - returned loan
        testLoan.setReturnDate(new Date());
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        long daysUntilDue = loanService.getDaysUntilDue(1L);

        // Then
        assertEquals(0L, daysUntilDue);
    }

    @Test
    @DisplayName("Should return zero when loan ID is null")
    void testGetDaysUntilDue_NullLoanId_Zero() {
        // When
        long daysUntilDue = loanService.getDaysUntilDue(null);

        // Then
        assertEquals(0L, daysUntilDue);
        verify(loanRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should return zero when loan not found")
    void testGetDaysUntilDue_LoanNotFound_Zero() {
        // Given
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        long daysUntilDue = loanService.getDaysUntilDue(1L);

        // Then
        assertEquals(0L, daysUntilDue);
    }

    // ===== GET DAYS OVERDUE TESTS =====

    @Test
    @DisplayName("Should calculate days overdue correctly")
    void testGetDaysOverdue_OverdueLoan_Success() {
        // Given - loan overdue by 4 days
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -4);
        testLoan.setDueDate(cal.getTime());
        testLoan.setReturnDate(null);
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        long daysOverdue = loanService.getDaysOverdue(1L);

        // Then
        assertEquals(4L, daysOverdue);
    }

    @Test
    @DisplayName("Should return zero for non-overdue loan")
    void testGetDaysOverdue_NotOverdue_Zero() {
        // Given - loan due in future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        testLoan.setDueDate(cal.getTime());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        long daysOverdue = loanService.getDaysOverdue(1L);

        // Then
        assertEquals(0L, daysOverdue);
    }

    @Test
    @DisplayName("Should return zero for returned loan")
    void testGetDaysOverdue_ReturnedLoan_Zero() {
        // Given - returned loan (even if was overdue)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5);
        testLoan.setDueDate(cal.getTime());
        testLoan.setReturnDate(new Date());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        long daysOverdue = loanService.getDaysOverdue(1L);

        // Then
        assertEquals(0L, daysOverdue);
    }

    // ===== CREATE LOAN TESTS =====

    @Test
    @DisplayName("Should create loan successfully")
    void testCreateLoan_Success() {
        // Given
        doAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(1L);
            return null;
        }).when(loanRepository).persist(any(Loan.class));

        // When
        Loan result = loanService.createLoan(testMember, testBook, 14);

        // Then
        assertNotNull(result);
        assertEquals(testMember, result.getMember());
        assertEquals(testBook, result.getBook());
        assertEquals(Loan.LoanStatus.ACTIVE, result.getStatus());
        assertEquals(0.0, result.getFineAmount(), 0.01);
        assertNotNull(result.getLoanDate());
        assertNotNull(result.getDueDate());
        assertNull(result.getReturnDate());
        
        // Check due date is 14 days from loan date
        Calendar cal = Calendar.getInstance();
        cal.setTime(result.getLoanDate());
        cal.add(Calendar.DAY_OF_MONTH, 14);
        assertEquals(cal.getTime(), result.getDueDate());
        
        verify(loanRepository).persist(any(Loan.class));
    }

    @Test
    @DisplayName("Should create loan with default period when invalid period provided")
    void testCreateLoan_InvalidPeriod_UsesDefault() {
        // Given
        doAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(1L);
            return null;
        }).when(loanRepository).persist(any(Loan.class));

        // When
        Loan result = loanService.createLoan(testMember, testBook, 0); // Invalid period

        // Then
        assertNotNull(result);
        
        // Check due date is 14 days (default) from loan date
        Calendar cal = Calendar.getInstance();
        cal.setTime(result.getLoanDate());
        cal.add(Calendar.DAY_OF_MONTH, 14);
        assertEquals(cal.getTime(), result.getDueDate());
    }

    @Test
    @DisplayName("Should fail to create loan when member is null")
    void testCreateLoan_NullMember_Fails() {
        // When
        Loan result = loanService.createLoan(null, testBook, 14);

        // Then
        assertNull(result);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("Should fail to create loan when book is null")
    void testCreateLoan_NullBook_Fails() {
        // When
        Loan result = loanService.createLoan(testMember, null, 14);

        // Then
        assertNull(result);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    @DisplayName("Should create loan with custom period")
    void testCreateLoan_CustomPeriod_Success() {
        // Given
        doAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(1L);
            return null;
        }).when(loanRepository).persist(any(Loan.class));

        // When
        Loan result = loanService.createLoan(testMember, testBook, 21); // 21 days

        // Then
        assertNotNull(result);
        
        // Check due date is 21 days from loan date
        Calendar cal = Calendar.getInstance();
        cal.setTime(result.getLoanDate());
        cal.add(Calendar.DAY_OF_MONTH, 21);
        assertEquals(cal.getTime(), result.getDueDate());
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle loan due exactly today")
    void testIsOverdue_DueToday_NotOverdue() {
        // Given - loan due today but later in the day (end of day)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        testLoan.setDueDate(cal.getTime());
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean isOverdue = loanService.isOverdue(1L);

        // Then
        assertFalse(isOverdue); // Should not be overdue if due later today
    }

    @Test
    @DisplayName("Should handle very large fine calculation")
    void testCalculateFine_VeryLargeFine_CappedCorrectly() {
        // Given - loan overdue by 100 days
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -100);
        testLoan.setDueDate(cal.getTime());
        testLoan.setReturnDate(null);
        
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        double fine = loanService.calculateFine(1L);

        // Then
        assertEquals(50.0, fine, 0.01); // Should be capped at $50
    }

    @Test
    @DisplayName("Should handle renewal at maximum allowed period")
    void testRenewLoan_MaximumPeriod_Success() {
        // Given
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLoan));

        // When
        boolean result = loanService.renewLoan(1L, 14); // Maximum 14 days

        // Then
        assertTrue(result);
        assertEquals(Loan.LoanStatus.RENEWED, testLoan.getStatus());
    }
} 