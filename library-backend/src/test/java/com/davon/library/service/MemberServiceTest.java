package com.davon.library.service;

import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.model.Loan;
import com.davon.library.model.Fine;
import com.davon.library.model.Transaction;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.LoanRepository;
import com.davon.library.repository.FineRepository;
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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class MemberServiceTest {

    @Inject
    MemberService memberService;

    @InjectMock
    MemberRepository memberRepository;

    @InjectMock
    BookRepository bookRepository;

    @InjectMock
    LoanRepository loanRepository;

    @InjectMock
    FineRepository fineRepository;

    @InjectMock
    TransactionService transactionService;

    @InjectMock
    InventoryService inventoryService;

    @InjectMock
    LoanService loanService;
    
    @InjectMock
    FineCalculationService fineCalculationService;

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
        testMember.setEmail("test@member.com");
        testMember.setPassword("password123");
        testMember.setRole("MEMBER");
        testMember.setMembershipNumber("MEM123456");
        testMember.setMembershipStart(new Date());
        
        // Set membership end date to 1 year from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        testMember.setMembershipEnd(cal.getTime());
        
        testMember.setBorrowedBookIds(new ArrayList<>());
        testMember.setFineHistory(new ArrayList<>());
        testMember.setTotalFines(BigDecimal.ZERO);

        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setStatus(Book.BookStatus.AVAILABLE);

        // Setup test loan
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setMember(testMember);
        testLoan.setBook(testBook);
        testLoan.setLoanDate(new Date());
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 14);
        testLoan.setDueDate(cal.getTime());
        testLoan.setStatus(Loan.LoanStatus.ACTIVE);

        // Setup test fine
        testFine = new Fine();
        testFine.setId(1L);
        testFine.setAmount(new BigDecimal("25.0"));
        testFine.setUser(testMember);
        testFine.setLoan(testLoan);
        testFine.setFineType(Fine.FineType.OVERDUE);
        testFine.setIssuedDate(new Date());
        testFine.setPaid(false);
        testFine.setStatus(Transaction.TransactionStatus.PENDING);
    }

    // ============ MEMBER REGISTRATION TESTS ============

    @Test
    @DisplayName("Should register member successfully with valid details")
    void testRegisterMember_Success() {
        // Given
        String name = "New Member";
        String email = "new@member.com";
        String password = "password123";
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        doNothing().when(memberRepository).persist(any(Member.class));

        // When
        Member result = memberService.registerMember(name, email, password);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email.toLowerCase(), result.getEmail());
        assertEquals(password, result.getPassword());
        assertEquals("MEMBER", result.getRole());
        assertNotNull(result.getMembershipNumber());
        assertTrue(result.getMembershipNumber().startsWith("MEM"));
        assertNotNull(result.getMembershipStart());
        assertNotNull(result.getMembershipEnd());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalFines()));
        
        verify(memberRepository).findByEmail(email);
        verify(memberRepository).persist(any(Member.class));
    }

    @Test
    @DisplayName("Should fail registration when name is null")
    void testRegisterMember_NullName() {
        // Given
        String name = null;
        String email = "test@member.com";
        String password = "password123";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.registerMember(name, email, password)
        );
        assertEquals("Invalid registration details", exception.getMessage());
        
        verify(memberRepository, never()).findByEmail(anyString());
        verify(memberRepository, never()).persist(any(Member.class));
    }

    @Test
    @DisplayName("Should fail registration when email is empty")
    void testRegisterMember_EmptyEmail() {
        // Given
        String name = "Test Member";
        String email = "";
        String password = "password123";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.registerMember(name, email, password)
        );
        assertEquals("Invalid registration details", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail registration when password is too short")
    void testRegisterMember_ShortPassword() {
        // Given
        String name = "Test Member";
        String email = "test@member.com";
        String password = "123";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.registerMember(name, email, password)
        );
        assertEquals("Invalid registration details", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void testRegisterMember_EmailExists() {
        // Given
        String name = "Test Member";
        String email = "existing@member.com";
        String password = "password123";
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberService.registerMember(name, email, password)
        );
        assertEquals("Member with email already exists", exception.getMessage());
        
        verify(memberRepository).findByEmail(email);
        verify(memberRepository, never()).persist(any(Member.class));
    }

    // ============ BOOK BORROWING TESTS ============

    @Test
    @DisplayName("Should borrow book successfully when all conditions are met")
    void testBorrowBook_Success() {
        // Given
        Long bookId = 1L;
        
        when(bookRepository.findByIdOptional(bookId)).thenReturn(Optional.of(testBook));
        when(fineRepository.calculateTotalUnpaidFines(testMember)).thenReturn(new BigDecimal("25.0"));
        when(loanRepository.countActiveLoansByMember(testMember)).thenReturn(2L);
        when(loanRepository.findActiveLoanByMemberAndBook(testMember, testBook)).thenReturn(Optional.empty());
        when(inventoryService.borrowCopy(anyLong())).thenReturn(true);
        doNothing().when(loanRepository).persist(any(Loan.class));

        // When
        boolean result = memberService.borrowBook(testMember, bookId);

        // Then
        assertTrue(result);
        assertTrue(testMember.getBorrowedBookIds().contains(bookId));
        assertEquals(Book.BookStatus.BORROWED, testBook.getStatus());
        
        verify(bookRepository).findByIdOptional(bookId);
        verify(fineRepository).calculateTotalUnpaidFines(testMember);
        verify(loanRepository).countActiveLoansByMember(testMember);
        verify(loanRepository).findActiveLoanByMemberAndBook(testMember, testBook);
        verify(loanRepository).persist(any(Loan.class));
    }

    @Test
    @DisplayName("Should fail to borrow book when member is null")
    void testBorrowBook_NullMember() {
        // Given
        Member nullMember = null;
        Long bookId = 1L;

        // When
        boolean result = memberService.borrowBook(nullMember, bookId);

        // Then
        assertFalse(result);
        verify(bookRepository, never()).findByIdOptional(anyLong());
    }

    @Test
    @DisplayName("Should fail to borrow book when book ID is null")
    void testBorrowBook_NullBookId() {
        // Given
        Long nullBookId = null;

        // When
        boolean result = memberService.borrowBook(testMember, nullBookId);

        // Then
        assertFalse(result);
        verify(bookRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to borrow book when membership is expired")
    void testBorrowBook_ExpiredMembership() {
        // Given
        Long bookId = 1L;
        
        // Set membership end date to past
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        testMember.setMembershipEnd(cal.getTime());

        // When
        boolean result = memberService.borrowBook(testMember, bookId);

        // Then
        assertFalse(result);
        verify(bookRepository, never()).findByIdOptional(anyLong());
    }

    @Test
    @DisplayName("Should fail to borrow book when fines exceed $50")
    void testBorrowBook_ExcessiveFines() {
        // Given
        Long bookId = 1L;
        
        when(fineRepository.calculateTotalUnpaidFines(testMember)).thenReturn(new BigDecimal("75.0"));

        // When
        boolean result = memberService.borrowBook(testMember, bookId);

        // Then
        assertFalse(result);
        verify(fineRepository).calculateTotalUnpaidFines(testMember);
        verify(bookRepository, never()).findByIdOptional(anyLong());
    }

    @Test
    @DisplayName("Should fail to borrow book when borrowing limit reached")
    void testBorrowBook_BorrowingLimitReached() {
        // Given
        Long bookId = 1L;
        
        when(fineRepository.calculateTotalUnpaidFines(testMember)).thenReturn(new BigDecimal("25.0"));
        when(loanRepository.countActiveLoansByMember(testMember)).thenReturn(5L);

        // When
        boolean result = memberService.borrowBook(testMember, bookId);

        // Then
        assertFalse(result);
        verify(fineRepository).calculateTotalUnpaidFines(testMember);
        verify(loanRepository).countActiveLoansByMember(testMember);
        verify(bookRepository, never()).findByIdOptional(anyLong());
    }

    @Test
    @DisplayName("Should fail to borrow book when book not found")
    void testBorrowBook_BookNotFound() {
        // Given
        Long bookId = 999L;
        
        when(fineRepository.calculateTotalUnpaidFines(testMember)).thenReturn(new BigDecimal("25.0"));
        when(loanRepository.countActiveLoansByMember(testMember)).thenReturn(2L);
        when(bookRepository.findByIdOptional(bookId)).thenReturn(Optional.empty());

        // When
        boolean result = memberService.borrowBook(testMember, bookId);

        // Then
        assertFalse(result);
        verify(bookRepository).findByIdOptional(bookId);
    }

    @Test
    @DisplayName("Should fail to borrow book when book is not available")
    void testBorrowBook_BookNotAvailable() {
        // Given
        Long bookId = 1L;
        testBook.setStatus(Book.BookStatus.BORROWED);
        
        when(fineRepository.calculateTotalUnpaidFines(testMember)).thenReturn(new BigDecimal("25.0"));
        when(loanRepository.countActiveLoansByMember(testMember)).thenReturn(2L);
        when(bookRepository.findByIdOptional(bookId)).thenReturn(Optional.of(testBook));

        // When
        boolean result = memberService.borrowBook(testMember, bookId);

        // Then
        assertFalse(result);
        verify(bookRepository).findByIdOptional(bookId);
    }

    @Test
    @DisplayName("Should fail to borrow book when member already has this book")
    void testBorrowBook_AlreadyBorrowed() {
        // Given
        Long bookId = 1L;
        
        when(fineRepository.calculateTotalUnpaidFines(testMember)).thenReturn(new BigDecimal("25.0"));
        when(loanRepository.countActiveLoansByMember(testMember)).thenReturn(2L);
        when(bookRepository.findByIdOptional(bookId)).thenReturn(Optional.of(testBook));
        when(loanRepository.findActiveLoanByMemberAndBook(testMember, testBook)).thenReturn(Optional.of(testLoan));

        // When
        boolean result = memberService.borrowBook(testMember, bookId);

        // Then
        assertFalse(result);
        verify(loanRepository).findActiveLoanByMemberAndBook(testMember, testBook);
    }

    // ============ BOOK RETURNING TESTS ============

    @Test
    @DisplayName("Should return book successfully when loan exists")
    void testReturnBook_Success() {
        // Given
        Long bookId = 1L;
        testMember.getBorrowedBookIds().add(bookId);
        
        when(bookRepository.findByIdOptional(bookId)).thenReturn(Optional.of(testBook));
        when(loanRepository.findActiveLoanByMemberAndBook(testMember, testBook)).thenReturn(Optional.of(testLoan));
        when(loanService.markAsReturned(testLoan.getId())).thenReturn(true);
        when(loanService.isOverdue(testLoan.getId())).thenReturn(false);
        when(inventoryService.returnCopy(anyLong())).thenReturn(true);

        // When
        boolean result = memberService.returnBook(testMember, bookId);

        // Then
        assertTrue(result);
        assertEquals(Book.BookStatus.AVAILABLE, testBook.getStatus());
        assertFalse(testMember.getBorrowedBookIds().contains(bookId));
        
        verify(bookRepository).findByIdOptional(bookId);
        verify(loanRepository).findActiveLoanByMemberAndBook(testMember, testBook);
        verify(loanService).markAsReturned(testLoan.getId());
        verify(loanService).isOverdue(testLoan.getId());
    }

    @Test
    @DisplayName("Should return book and create fine when overdue")
    void testReturnBook_OverdueWithFine() {
        // Given
        Long bookId = 1L;
        testMember.getBorrowedBookIds().add(bookId);
        BigDecimal fineAmount = new BigDecimal("15.0");
        
        // Mock the fine that FineCalculationService would return
        Fine calculatedFine = new Fine();
        calculatedFine.setAmount(fineAmount);
        calculatedFine.setUser(testMember);
        calculatedFine.setLoan(testLoan);
        calculatedFine.setFineType(Fine.FineType.OVERDUE);
        calculatedFine.setIssuedDate(new Date());
        calculatedFine.setPaid(false);
        calculatedFine.setStatus(Transaction.TransactionStatus.PENDING);
        
        when(bookRepository.findByIdOptional(bookId)).thenReturn(Optional.of(testBook));
        when(loanRepository.findActiveLoanByMemberAndBook(testMember, testBook)).thenReturn(Optional.of(testLoan));
        when(loanService.markAsReturned(testLoan.getId())).thenReturn(true);
        when(loanService.isOverdue(testLoan.getId())).thenReturn(true);
        when(fineCalculationService.calculateOverdueFine(testLoan)).thenReturn(calculatedFine);
        when(inventoryService.returnCopy(anyLong())).thenReturn(true);

        // When
        boolean result = memberService.returnBook(testMember, bookId);

        // Then
        assertTrue(result);
        assertEquals(Book.BookStatus.AVAILABLE, testBook.getStatus());
        assertFalse(testMember.getBorrowedBookIds().contains(bookId));
        assertEquals(0, fineAmount.compareTo(testMember.getTotalFines()));
        assertTrue(testMember.getFineHistory().size() > 0);
        
        verify(fineCalculationService).calculateOverdueFine(testLoan);
    }

    @Test
    @DisplayName("Should fail to return book when member is null")
    void testReturnBook_NullMember() {
        // Given
        Member nullMember = null;
        Long bookId = 1L;

        // When
        boolean result = memberService.returnBook(nullMember, bookId);

        // Then
        assertFalse(result);
        verify(bookRepository, never()).findByIdOptional(anyLong());
    }

    @Test
    @DisplayName("Should fail to return book when book not found")
    void testReturnBook_BookNotFound() {
        // Given
        Long bookId = 999L;
        
        when(bookRepository.findByIdOptional(bookId)).thenReturn(Optional.empty());

        // When
        boolean result = memberService.returnBook(testMember, bookId);

        // Then
        assertFalse(result);
        verify(bookRepository).findByIdOptional(bookId);
    }

    @Test
    @DisplayName("Should fail to return book when no active loan found")
    void testReturnBook_NoActiveLoan() {
        // Given
        Long bookId = 1L;
        
        when(bookRepository.findByIdOptional(bookId)).thenReturn(Optional.of(testBook));
        when(loanRepository.findActiveLoanByMemberAndBook(testMember, testBook)).thenReturn(Optional.empty());

        // When
        boolean result = memberService.returnBook(testMember, bookId);

        // Then
        assertFalse(result);
        verify(loanRepository).findActiveLoanByMemberAndBook(testMember, testBook);
    }

    // ============ FINE PAYMENT TESTS ============

    @Test
    @DisplayName("Should pay fine successfully with full payment")
    void testPayFine_FullPayment() {
        // Given
        Long fineId = 1L;
        BigDecimal paymentAmount = new BigDecimal("25.0");
        String description = "Full payment";
        testMember.setTotalFines(new BigDecimal("25.0"));
        
        when(fineRepository.findByIdOptional(fineId)).thenReturn(Optional.of(testFine));
        when(transactionService.markAsCompleted(fineId)).thenReturn(true);

        // When
        boolean result = memberService.payFine(testMember, fineId, paymentAmount, description);

        // Then
        assertTrue(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(testMember.getTotalFines()));
        assertTrue(testFine.isPaid());
        assertNotNull(testFine.getPaidDate());
        assertTrue(testMember.getFineHistory().size() > 0);
        
        verify(fineRepository).findByIdOptional(fineId);
        verify(transactionService).markAsCompleted(fineId);
    }

    @Test
    @DisplayName("Should pay fine successfully with partial payment")
    void testPayFine_PartialPayment() {
        // Given
        Long fineId = 1L;
        BigDecimal paymentAmount = new BigDecimal("15.0");
        String description = "Partial payment";
        testMember.setTotalFines(new BigDecimal("25.0"));
        
        when(fineRepository.findByIdOptional(fineId)).thenReturn(Optional.of(testFine));

        // When
        boolean result = memberService.payFine(testMember, fineId, paymentAmount, description);

        // Then
        assertTrue(result);
        assertEquals(0, new BigDecimal("10.0").compareTo(testMember.getTotalFines()));
        assertEquals(0, new BigDecimal("10.0").compareTo(testFine.getAmount()));
        assertFalse(testFine.isPaid());
        assertTrue(testMember.getFineHistory().size() > 0);
        
        verify(fineRepository).findByIdOptional(fineId);
        verify(transactionService, never()).markAsCompleted(anyLong());
    }

    @Test
    @DisplayName("Should fail to pay fine when member is null")
    void testPayFine_NullMember() {
        // Given
        Member nullMember = null;
        Long fineId = 1L;
        BigDecimal amount = new BigDecimal("25.0");
        String description = "Payment";

        // When
        boolean result = memberService.payFine(nullMember, fineId, amount, description);

        // Then
        assertFalse(result);
        verify(fineRepository, never()).findByIdOptional(anyLong());
    }

    @Test
    @DisplayName("Should fail to pay fine when fine not found")
    void testPayFine_FineNotFound() {
        // Given
        Long fineId = 999L;
        BigDecimal amount = new BigDecimal("25.0");
        String description = "Payment";
        
        when(fineRepository.findByIdOptional(fineId)).thenReturn(Optional.empty());

        // When
        boolean result = memberService.payFine(testMember, fineId, amount, description);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(fineId);
    }

    @Test
    @DisplayName("Should fail to pay fine when fine doesn't belong to member")
    void testPayFine_WrongMember() {
        // Given
        Long fineId = 1L;
        BigDecimal amount = new BigDecimal("25.0");
        String description = "Payment";
        
        Member otherMember = new Member();
        otherMember.setId(2L);
        testFine.setUser(otherMember);
        
        when(fineRepository.findByIdOptional(fineId)).thenReturn(Optional.of(testFine));

        // When
        boolean result = memberService.payFine(testMember, fineId, amount, description);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(fineId);
    }

    @Test
    @DisplayName("Should fail to pay fine when fine is already paid")
    void testPayFine_AlreadyPaid() {
        // Given
        Long fineId = 1L;
        BigDecimal amount = new BigDecimal("25.0");
        String description = "Payment";
        testFine.setPaid(true);
        testFine.setStatus(Transaction.TransactionStatus.COMPLETED);
        
        when(fineRepository.findByIdOptional(fineId)).thenReturn(Optional.of(testFine));

        // When
        boolean result = memberService.payFine(testMember, fineId, amount, description);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(fineId);
    }

    @Test
    @DisplayName("Should fail to pay fine when payment amount exceeds fine amount")
    void testPayFine_ExcessivePayment() {
        // Given
        Long fineId = 1L;
        BigDecimal amount = new BigDecimal("50.0"); // Fine amount is 25.0
        String description = "Payment";
        
        when(fineRepository.findByIdOptional(fineId)).thenReturn(Optional.of(testFine));

        // When
        boolean result = memberService.payFine(testMember, fineId, amount, description);

        // Then
        assertFalse(result);
        verify(fineRepository).findByIdOptional(fineId);
    }

    // ============ MEMBERSHIP RENEWAL TESTS ============

    @Test
    @DisplayName("Should renew membership successfully")
    void testRenewMembership_Success() {
        // Given
        int years = 2;
        Date originalEnd = testMember.getMembershipEnd();
        
        // When
        boolean result = memberService.renewMembership(testMember, years);

        // Then
        assertTrue(result);
        assertTrue(testMember.getMembershipEnd().after(originalEnd));
        assertNotNull(testMember.getUpdatedAt());
    }

    @Test
    @DisplayName("Should fail to renew membership when member is null")
    void testRenewMembership_NullMember() {
        // Given
        Member nullMember = null;
        int years = 1;

        // When
        boolean result = memberService.renewMembership(nullMember, years);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to renew membership when years is zero")
    void testRenewMembership_ZeroYears() {
        // Given
        int years = 0;

        // When
        boolean result = memberService.renewMembership(testMember, years);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to renew membership when years exceeds maximum")
    void testRenewMembership_ExcessiveYears() {
        // Given
        int years = 15; // Max is 10

        // When
        boolean result = memberService.renewMembership(testMember, years);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should renew membership from current end date when membership exists")
    void testRenewMembership_FromCurrentEndDate() {
        // Given
        int years = 1;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 6);
        Date futureEndDate = cal.getTime();
        testMember.setMembershipEnd(futureEndDate);

        // When
        boolean result = memberService.renewMembership(testMember, years);

        // Then
        assertTrue(result);
        assertTrue(testMember.getMembershipEnd().after(futureEndDate));
    }

    // ============ MEMBER STATISTICS TESTS ============

    @Test
    @DisplayName("Should get member statistics successfully")
    void testGetMemberStatistics_Success() {
        // Given
        when(loanRepository.countActiveLoansByMember(testMember)).thenReturn(3L);
        when(fineRepository.calculateTotalUnpaidFines(testMember)).thenReturn(new BigDecimal("45.0"));
        when(fineRepository.countUnpaidFinesByMember(testMember)).thenReturn(2L);

        // When & Then (method prints to console, so we just verify it doesn't throw)
        assertDoesNotThrow(() -> memberService.getMemberStatistics(testMember));
        
        verify(loanRepository).countActiveLoansByMember(testMember);
        verify(fineRepository).calculateTotalUnpaidFines(testMember);
        verify(fineRepository).countUnpaidFinesByMember(testMember);
    }

    @Test
    @DisplayName("Should handle null member in statistics")
    void testGetMemberStatistics_NullMember() {
        // Given
        Member nullMember = null;

        // When & Then
        assertDoesNotThrow(() -> memberService.getMemberStatistics(nullMember));
        
        verify(loanRepository, never()).countActiveLoansByMember(any());
        verify(fineRepository, never()).calculateTotalUnpaidFines(any());
        verify(fineRepository, never()).countUnpaidFinesByMember(any());
    }
} 