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

import java.math.BigDecimal;
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
        testLoan.setFineAmount(BigDecimal.ZERO);
        testLoan.setReturnDate(null);
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

    @Test
    @DisplayName("Should return zero when loan ID is null")
    void testGetDaysOverdue_NullLoanId_Zero() {
        // When
        long daysOverdue = loanService.getDaysOverdue(null);

        // Then
        assertEquals(0L, daysOverdue);
        verify(loanRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should return zero when loan not found")
    void testGetDaysOverdue_LoanNotFound_Zero() {
        // Given
        when(loanRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

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
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getFineAmount()));
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
} 