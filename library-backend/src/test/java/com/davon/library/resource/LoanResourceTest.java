package com.davon.library.resource;

import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.model.Book;
import com.davon.library.model.User;
import com.davon.library.repository.LoanRepository;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.LoanService;
import com.davon.library.service.MemberService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Calendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("LoanResource Integration Tests")
public class LoanResourceTest {

    @Inject
    LoanRepository loanRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    BookRepository bookRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    LoanService loanService;

    @Inject
    MemberService memberService;

    private User adminUser;
    private Member testMember;
    private Book testBook;
    private Loan testLoan;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data
        loanRepository.deleteAll();
        memberRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        // Create test admin user
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("admin123");
        adminUser.setRole("ADMIN");
        adminUser.setCreatedAt(new Date());
        userRepository.persist(adminUser);

        // Create test member
        testMember = new Member();
        testMember.setName("Test Member");
        testMember.setEmail("test@member.com");
        testMember.setPassword("member123");
        testMember.setRole("MEMBER");
        testMember.setCreatedAt(new Date());
        testMember.setMembershipNumber("MEM123456");
        testMember.setMembershipStart(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        testMember.setMembershipEnd(cal.getTime());
        memberRepository.persist(testMember);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        testBook.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.persist(testBook);

        // Create test loan
        testLoan = new Loan();
        testLoan.setMember(testMember);
        testLoan.setBook(testBook);
        testLoan.setLoanDate(new Date());
        
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 14);
        testLoan.setDueDate(cal.getTime());
        
        testLoan.setStatus(Loan.LoanStatus.ACTIVE);
        testLoan.setFineAmount(0.0);
        loanRepository.persist(testLoan);
    }

    // ===== GET ALL LOANS TESTS =====

    @Test
    @DisplayName("Should get all loans successfully")
    void testGetAllLoans_Success() {
        given()
        .when()
            .get("/api/loans")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].status", equalTo("ACTIVE"));
    }

    // ===== GET LOAN BY ID TESTS =====

    @Test
    @DisplayName("Should get loan by ID successfully")
    void testGetLoanById_Success() {
        given()
            .pathParam("id", testLoan.getId())
        .when()
            .get("/api/loans/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("ACTIVE"))
            .body("fineAmount", equalTo(0.0f));
    }

    @Test
    @DisplayName("Should return 404 when loan not found")
    void testGetLoanById_NotFound() {
        given()
            .pathParam("id", 99999L)
        .when()
            .get("/api/loans/{id}")
        .then()
            .statusCode(404)
            .body(containsString("Loan not found"));
    }

    // ===== GET LOANS BY MEMBER TESTS =====

    @Test
    @DisplayName("Should get loans by member ID successfully")
    void testGetLoansByMember_Success() {
        given()
            .pathParam("memberId", testMember.getId())
        .when()
            .get("/api/loans/member/{memberId}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Should return 404 when member not found for loan lookup")
    void testGetLoansByMember_MemberNotFound() {
        given()
            .pathParam("memberId", 99999L)
        .when()
            .get("/api/loans/member/{memberId}")
        .then()
            .statusCode(404)
            .body(containsString("Member not found"));
    }

    // ===== GET ACTIVE LOANS BY MEMBER TESTS =====

    @Test
    @DisplayName("Should get active loans by member successfully")
    void testGetActiveLoansByMember_Success() {
        given()
            .pathParam("memberId", testMember.getId())
        .when()
            .get("/api/loans/member/{memberId}/active")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Should return empty list when member has no active loans")
    @Transactional
    void testGetActiveLoansByMember_NoActiveLoans() {
        // Mark the loan as returned
        testLoan.setReturnDate(new Date());
        testLoan.setStatus(Loan.LoanStatus.RETURNED);

        given()
            .pathParam("memberId", testMember.getId())
        .when()
            .get("/api/loans/member/{memberId}/active")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(0));
    }

    // ===== BORROW BOOK TESTS =====

    @Test
    @DisplayName("Should borrow book successfully")
    void testBorrowBook_Success() {
        // Create a new book for borrowing (since testBook is already borrowed)
        Book newBook = new Book();
        newBook.setTitle("New Test Book");
        newBook.setIsbn("978-0987654321");
        newBook.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.persist(newBook);

        String borrowRequest = """
            {
                "memberId": %d,
                "bookId": %d
            }
            """.formatted(testMember.getId(), newBook.getId());

        given()
            .contentType(ContentType.JSON)
            .body(borrowRequest)
        .when()
            .post("/api/loans/borrow")
        .then()
            .statusCode(201)
            .body(containsString("Book borrowed successfully"));
    }

    @Test
    @DisplayName("Should fail to borrow book when member not found")
    void testBorrowBook_MemberNotFound() {
        String borrowRequest = """
            {
                "memberId": 99999,
                "bookId": %d
            }
            """.formatted(testBook.getId());

        given()
            .contentType(ContentType.JSON)
            .body(borrowRequest)
        .when()
            .post("/api/loans/borrow")
        .then()
            .statusCode(404)
            .body(containsString("Member not found"));
    }

    @Test
    @DisplayName("Should handle borrow book failure gracefully")
    void testBorrowBook_Failure() {
        // Try to borrow the same book that's already borrowed
        String borrowRequest = """
            {
                "memberId": %d,
                "bookId": %d
            }
            """.formatted(testMember.getId(), testBook.getId());

        given()
            .contentType(ContentType.JSON)
            .body(borrowRequest)
        .when()
            .post("/api/loans/borrow")
        .then()
            .statusCode(400)
            .body(containsString("Failed to borrow book"));
    }

    // ===== RETURN BOOK TESTS =====

    @Test
    @DisplayName("Should return book successfully")
    void testReturnBook_Success() {
        given()
            .pathParam("loanId", testLoan.getId())
        .when()
            .put("/api/loans/{loanId}/return")
        .then()
            .statusCode(200)
            .body(containsString("Book returned successfully"));
    }

    @Test
    @DisplayName("Should fail to return book when loan not found")
    void testReturnBook_LoanNotFound() {
        given()
            .pathParam("loanId", 99999L)
        .when()
            .put("/api/loans/{loanId}/return")
        .then()
            .statusCode(400)
            .body(containsString("Failed to return book"));
    }

    @Test
    @DisplayName("Should fail to return already returned book")
    @Transactional
    void testReturnBook_AlreadyReturned() {
        // Mark loan as already returned
        testLoan.setReturnDate(new Date());
        testLoan.setStatus(Loan.LoanStatus.RETURNED);

        given()
            .pathParam("loanId", testLoan.getId())
        .when()
            .put("/api/loans/{loanId}/return")
        .then()
            .statusCode(400)
            .body(containsString("Failed to return book"));
    }

    // ===== RENEW LOAN TESTS =====

    @Test
    @DisplayName("Should renew loan successfully")
    void testRenewLoan_Success() {
        String renewRequest = """
            {
                "additionalDays": 7
            }
            """;

        given()
            .pathParam("loanId", testLoan.getId())
            .contentType(ContentType.JSON)
            .body(renewRequest)
        .when()
            .put("/api/loans/{loanId}/renew")
        .then()
            .statusCode(200)
            .body(containsString("Loan renewed successfully"));
    }

    @Test
    @DisplayName("Should fail to renew loan with invalid period")
    void testRenewLoan_InvalidPeriod() {
        String renewRequest = """
            {
                "additionalDays": 20
            }
            """;

        given()
            .pathParam("loanId", testLoan.getId())
            .contentType(ContentType.JSON)
            .body(renewRequest)
        .when()
            .put("/api/loans/{loanId}/renew")
        .then()
            .statusCode(400)
            .body(containsString("Failed to renew loan"));
    }

    @Test
    @DisplayName("Should fail to renew overdue loan")
    @Transactional
    void testRenewLoan_OverdueLoan() {
        // Make loan overdue
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -5); // Due 5 days ago
        testLoan.setDueDate(cal.getTime());

        String renewRequest = """
            {
                "additionalDays": 7
            }
            """;

        given()
            .pathParam("loanId", testLoan.getId())
            .contentType(ContentType.JSON)
            .body(renewRequest)
        .when()
            .put("/api/loans/{loanId}/renew")
        .then()
            .statusCode(400)
            .body(containsString("Failed to renew loan"));
    }

    @Test
    @DisplayName("Should fail to renew loan when loan not found")
    void testRenewLoan_LoanNotFound() {
        String renewRequest = """
            {
                "additionalDays": 7
            }
            """;

        given()
            .pathParam("loanId", 99999L)
            .contentType(ContentType.JSON)
            .body(renewRequest)
        .when()
            .put("/api/loans/{loanId}/renew")
        .then()
            .statusCode(400)
            .body(containsString("Failed to renew loan"));
    }

    // ===== GET OVERDUE LOANS TESTS =====

    @Test
    @DisplayName("Should get overdue loans successfully")
    @Transactional
    void testGetOverdueLoans_Success() {
        // Make the test loan overdue
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3); // Due 3 days ago
        testLoan.setDueDate(cal.getTime());

        given()
        .when()
            .get("/api/loans/overdue")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Should return empty list when no overdue loans")
    void testGetOverdueLoans_NoOverdueLoans() {
        // Test loan is not overdue (due in future)
        given()
        .when()
            .get("/api/loans/overdue")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(0));
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    @DisplayName("Should handle malformed JSON in borrow request")
    void testBorrowBook_MalformedJSON() {
        String malformedRequest = "{invalid json}";

        given()
            .contentType(ContentType.JSON)
            .body(malformedRequest)
        .when()
            .post("/api/loans/borrow")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle malformed JSON in renew request")
    void testRenewLoan_MalformedJSON() {
        String malformedRequest = "{invalid json}";

        given()
            .pathParam("loanId", testLoan.getId())
            .contentType(ContentType.JSON)
            .body(malformedRequest)
        .when()
            .put("/api/loans/{loanId}/renew")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle invalid path parameters")
    void testGetLoanById_InvalidId() {
        given()
            .pathParam("id", "invalid-id")
        .when()
            .get("/api/loans/{id}")
        .then()
            .statusCode(404);
    }

    // ===== CONTENT TYPE TESTS =====

    @Test
    @DisplayName("Should return JSON content type for successful requests")
    void testContentType_JSON() {
        given()
        .when()
            .get("/api/loans")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should accept JSON content type for POST requests")
    void testBorrowBook_AcceptJSON() {
        Book newBook = new Book();
        newBook.setTitle("Another Test Book");
        newBook.setIsbn("978-1111111111");
        newBook.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.persist(newBook);

        String borrowRequest = """
            {
                "memberId": %d,
                "bookId": %d
            }
            """.formatted(testMember.getId(), newBook.getId());

        given()
            .contentType(ContentType.JSON)
            .body(borrowRequest)
        .when()
            .post("/api/loans/borrow")
        .then()
            .statusCode(201);
    }

    // ===== INTEGRATION TESTS =====

    @Test
    @DisplayName("Should maintain data consistency across operations")
    @Transactional
    void testDataConsistency() {
        // First, verify loan exists
        given()
            .pathParam("id", testLoan.getId())
        .when()
            .get("/api/loans/{id}")
        .then()
            .statusCode(200)
            .body("status", equalTo("ACTIVE"));

        // Return the book
        given()
            .pathParam("loanId", testLoan.getId())
        .when()
            .put("/api/loans/{loanId}/return")
        .then()
            .statusCode(200);

        // Verify loan status changed
        given()
            .pathParam("id", testLoan.getId())
        .when()
            .get("/api/loans/{id}")
        .then()
            .statusCode(200)
            .body("status", equalTo("RETURNED"));
    }

    @Test
    @DisplayName("Should handle loan lifecycle correctly")
    @Transactional
    void testLoanLifecycle() {
        // Create a new book for this test
        Book lifecycleBook = new Book();
        lifecycleBook.setTitle("Lifecycle Test Book");
        lifecycleBook.setIsbn("978-2222222222");
        lifecycleBook.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.persist(lifecycleBook);

        // 1. Borrow book
        String borrowRequest = """
            {
                "memberId": %d,
                "bookId": %d
            }
            """.formatted(testMember.getId(), lifecycleBook.getId());

        given()
            .contentType(ContentType.JSON)
            .body(borrowRequest)
        .when()
            .post("/api/loans/borrow")
        .then()
            .statusCode(201);

        // 2. Verify loan was created (check active loans)
        given()
            .pathParam("memberId", testMember.getId())
        .when()
            .get("/api/loans/member/{memberId}/active")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(1)); // Should have original loan + new loan
    }
} 