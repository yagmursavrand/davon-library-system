package com.davon.library.service;

import com.davon.library.model.Fine;
import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.repository.FineRepository;
import com.davon.library.repository.LoanRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("FineCalculationService Unit Tests")
public class FineServiceTest {

    @Inject
    FineCalculationService fineCalculationService;

    @InjectMock
    FineRepository fineRepository;

    @InjectMock
    LoanRepository loanRepository;

    private Member testMember;
    private Book testBook;
    private Loan testLoan;
    private Fine testFine;

    @BeforeEach
    void setUp() {
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
        testFine.setUser(testMember);
        testFine.setAmount(new BigDecimal("25.0"));
        testFine.setType("FINE");
        testFine.setDescription("Overdue fine for book: Test Book");
        testFine.setDate(new Date());
        testFine.setStatus(Fine.TransactionStatus.PENDING);
        testFine.setReason("Book returned 5 days late");
        testFine.setFineType(Fine.FineType.OVERDUE);
        testFine.setIssuedDate(new Date());
        testFine.setPaid(false);
        testFine.setLoan(testLoan);
    }

    // ===== CALCULATE OVERDUE FINE TESTS =====

    @Test
    @DisplayName("Should calculate overdue fine successfully")
    void testCalculateOverdueFine_Success() {
        // Given - loan is overdue
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5); // 5 days ago
        testLoan.setDueDate(cal.getTime());
        
        when(fineRepository.findFirstByLoan(any(Loan.class))).thenReturn(null);
        doAnswer(invocation -> {
            Fine fine = invocation.getArgument(0);
            fine.setId(2L); // Simulate saving to the database
            return fine;
        }).when(fineRepository).persist(any(Fine.class));

        // When
        Fine result = fineCalculationService.calculateOverdueFine(testLoan);

        // Then
        assertNotNull(result, "Expected a new fine to be created.");
        assertEquals(testMember, result.getUser());
        assertEquals(0, new BigDecimal("10.0").compareTo(result.getAmount()));
        assertEquals("FINE", result.getType());
        assertEquals("Overdue fine for book: Test Book", result.getDescription());
        assertEquals(Fine.FineType.OVERDUE, result.getFineType());
        assertEquals(Fine.TransactionStatus.PENDING, result.getStatus());
        assertEquals(testLoan, result.getLoan());
        assertNotNull(result.getDate());
        assertNotNull(result.getIssuedDate());
        assertFalse(result.isPaid());

        verify(fineRepository).persist(any(Fine.class));
    }

    @Test
    @DisplayName("Should return null when loan is null")
    void testCalculateOverdueFine_NullLoan() {
        // When
        Fine result = fineCalculationService.calculateOverdueFine(null);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should return null when loan due date is null")
    void testCalculateOverdueFine_NullDueDate() {
        // Given
        testLoan.setDueDate(null);

        // When
        Fine result = fineCalculationService.calculateOverdueFine(testLoan);

        // Then
        assertNull(result);
        verifyNoInteractions(fineRepository);
    }

    @Test
    @DisplayName("Should return null when book is not overdue yet")
    void testCalculateOverdueFine_NotOverdue() {
        // Given - loan is not overdue (due date is in the future)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 5); // 5 days in the future
        testLoan.setDueDate(cal.getTime());

        // When
        Fine result = fineCalculationService.calculateOverdueFine(testLoan);

        // Then
        assertNull(result, "No fine should be created for a loan that is not overdue.");
        verify(fineRepository, never()).persist(any(Fine.class));
    }

    @Test
    @DisplayName("Should apply maximum fine limit")
    void testCalculateOverdueFine_MaximumLimit() {
        // Given - loan is very overdue (100 days)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -100); // 100 days ago
        testLoan.setDueDate(cal.getTime());
        
        when(fineRepository.findFirstByLoan(any(Loan.class))).thenReturn(null);
        doAnswer(invocation -> {
            Fine fine = invocation.getArgument(0);
            fine.setId(2L); // Simulate saving to the database
            return fine;
        }).when(fineRepository).persist(any(Fine.class));

        // When
        Fine result = fineCalculationService.calculateOverdueFine(testLoan);

        // Then
        assertNotNull(result, "A new fine should be created for a very overdue loan.");
        assertEquals(0, new BigDecimal("50.0").compareTo(result.getAmount())); // Should be capped at $50
        verify(fineRepository).persist(any(Fine.class));
    }

    // ===== CALCULATE TOTAL FINES FOR MEMBER TESTS =====

    @Test
    @DisplayName("Should calculate total fines for member successfully")
    void testCalculateTotalFinesForMember_Success() {
        // Given
        List<Fine> fines = List.of(testFine);
        when(fineRepository.findByUser(testMember)).thenReturn(fines);

        // When
        BigDecimal result = fineCalculationService.calculateTotalFinesForMember(testMember);

        // Then
        assertEquals(0, new BigDecimal("25.0").compareTo(result));
        verify(fineRepository).findByUser(testMember);
    }

    @Test
    @DisplayName("Should return 0 when member has no fines")
    void testCalculateTotalFinesForMember_NoFines() {
        // Given
        when(fineRepository.findByUser(testMember)).thenReturn(List.of());

        // When
        BigDecimal result = fineCalculationService.calculateTotalFinesForMember(testMember);

        // Then
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
        verify(fineRepository).findByUser(testMember);
    }

    @Test
    @DisplayName("Should only count unpaid fines")
    void testCalculateTotalFinesForMember_OnlyUnpaid() {
        // Given
        Fine paidFine = new Fine();
        paidFine.setAmount(new BigDecimal("15.0"));
        paidFine.setPaid(true);
        
        Fine unpaidFine = new Fine();
        unpaidFine.setAmount(new BigDecimal("25.0"));
        unpaidFine.setPaid(false);
        
        List<Fine> fines = List.of(paidFine, unpaidFine);
        when(fineRepository.findByUser(testMember)).thenReturn(fines);

        // When
        BigDecimal result = fineCalculationService.calculateTotalFinesForMember(testMember);

        // Then
        assertEquals(0, new BigDecimal("25.0").compareTo(result)); // Only unpaid fine should be counted
        verify(fineRepository).findByUser(testMember);
    }

    // ===== GET OVERDUE BOOKS FOR MEMBER TESTS =====

    @Test
    @DisplayName("Should get overdue books for member successfully")
    void testGetOverdueBooksForMember_Success() {
        // Given - make loan overdue
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5); // 5 days ago
        testLoan.setDueDate(cal.getTime());
        
        List<Loan> overdueLoans = List.of(testLoan);
        when(loanRepository.findByMember(testMember)).thenReturn(overdueLoans);

        // When
        List<Loan> result = fineCalculationService.getOverdueBooksForMember(testMember);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLoan, result.get(0));
        verify(loanRepository).findByMember(testMember);
    }

    @Test
    @DisplayName("Should return empty list when member has no loans")
    void testGetOverdueBooksForMember_NoLoans() {
        // Given
        when(loanRepository.findByMember(testMember)).thenReturn(List.of());

        // When
        List<Loan> result = fineCalculationService.getOverdueBooksForMember(testMember);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loanRepository).findByMember(testMember);
    }

    @Test
    @DisplayName("Should filter only active loans")
    void testGetOverdueBooksForMember_OnlyActiveLoans() {
        // Given
        Loan activeLoan = new Loan();
        activeLoan.setStatus(Loan.LoanStatus.ACTIVE);
        activeLoan.setDueDate(createOverdueDate(5));
        
        Loan returnedLoan = new Loan();
        returnedLoan.setStatus(Loan.LoanStatus.RETURNED);
        returnedLoan.setDueDate(createOverdueDate(5));
        
        List<Loan> loans = List.of(activeLoan, returnedLoan);
        when(loanRepository.findByMember(testMember)).thenReturn(loans);

        // When
        List<Loan> result = fineCalculationService.getOverdueBooksForMember(testMember);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only active loan should be included
        assertEquals(activeLoan, result.get(0));
        verify(loanRepository).findByMember(testMember);
    }

    // ===== HAS OUTSTANDING FINES TESTS =====

    @Test
    @DisplayName("Should return true when member has outstanding fines above threshold")
    void testHasOutstandingFines_True() {
        // Given - member has $30 in unpaid fines (above $25 threshold)
        Fine highFine = new Fine();
        highFine.setAmount(new BigDecimal("30.0"));
        highFine.setPaid(false);
        
        List<Fine> fines = List.of(highFine);
        when(fineRepository.findByUser(testMember)).thenReturn(fines);

        // When
        boolean result = fineCalculationService.hasOutstandingFines(testMember);

        // Then
        assertTrue(result);
        verify(fineRepository).findByUser(testMember);
    }

    @Test
    @DisplayName("Should return false when member has fines below threshold")
    void testHasOutstandingFines_False() {
        // Given - member has $20 in unpaid fines (below $25 threshold)
        Fine lowFine = new Fine();
        lowFine.setAmount(new BigDecimal("20.0"));
        lowFine.setPaid(false);
        
        List<Fine> fines = List.of(lowFine);
        when(fineRepository.findByUser(testMember)).thenReturn(fines);

        // When
        boolean result = fineCalculationService.hasOutstandingFines(testMember);

        // Then
        assertFalse(result);
        verify(fineRepository).findByUser(testMember);
    }

    @Test
    @DisplayName("Should return false when member has no fines")
    void testHasOutstandingFines_NoFines() {
        // Given
        when(fineRepository.findByUser(testMember)).thenReturn(List.of());

        // When
        boolean result = fineCalculationService.hasOutstandingFines(testMember);

        // Then
        assertFalse(result);
        verify(fineRepository).findByUser(testMember);
    }

    // ===== HELPER METHODS =====

    private Date createOverdueDate(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
        return cal.getTime();
    }
} 