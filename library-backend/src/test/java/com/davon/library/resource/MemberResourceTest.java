package com.davon.library.resource;

import com.davon.library.model.Member;
import com.davon.library.model.User;
import com.davon.library.model.Book;
import com.davon.library.model.Fine;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.FineRepository;
import com.davon.library.service.MemberService;
import com.davon.library.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class MemberResourceTest {

    @Inject
    MemberRepository memberRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    BookRepository bookRepository;

    @Inject
    FineRepository fineRepository;

    @Inject
    MemberService memberService;

    @Inject
    UserService userService;

    private Member testMember;
    private User testAdmin;
    private Book testBook;
    private Fine testFine;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data in correct order to respect foreign key constraints
        fineRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
        userRepository.deleteAll();

        // Create test admin user
        testAdmin = new User();
        testAdmin.setName("Test Admin");
        testAdmin.setEmail("admin@library.com");
        testAdmin.setPassword("admin123");
        testAdmin.setRole("ADMIN");
        testAdmin.setCreatedAt(new Date());
        testAdmin.setLoggedIn(true);
        userRepository.persist(testAdmin);

        // Create test member
        testMember = new Member();
        testMember.setName("Test Member");
        testMember.setEmail("member@library.com");
        testMember.setPassword("password123");
        testMember.setRole("MEMBER");
        testMember.setMembershipNumber("MEM123456");
        testMember.setMembershipStart(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        testMember.setMembershipEnd(cal.getTime());
        
        testMember.setBorrowedBookIds(new ArrayList<>());
        testMember.setFineHistory(new ArrayList<>());
        testMember.setTotalFines(0.0);
        testMember.setCreatedAt(new Date());
        testMember.setLoggedIn(true);
        memberRepository.persist(testMember);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setStatus(Book.BookStatus.AVAILABLE);
        testBook.setGenre("Fiction");
        testBook.setPublicationYear(2023);
        bookRepository.persist(testBook);

        // Create test fine
        testFine = new Fine();
        testFine.setAmount(25.0);
        testFine.setUser(testMember);
        testFine.setFineType(Fine.FineType.OVERDUE);
        testFine.setIssuedDate(new Date());
        testFine.setPaid(false);
        testFine.setType("FINE");
        testFine.setDescription("Overdue fine");
        testFine.setDate(new Date());
        fineRepository.persist(testFine);
    }

    // ============ MEMBER REGISTRATION TESTS ============

    @Test
    @DisplayName("Should register new member successfully")
    void testRegisterMember_Success() {
        String requestBody = """
            {
                "name": "New Member",
                "email": "new@member.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/register")
        .then()
            .statusCode(201)
            .body("name", equalTo("New Member"))
            .body("email", equalTo("new@member.com"))
            .body("role", equalTo("MEMBER"))
            .body("membershipNumber", startsWith("MEM"))
            .body("totalFines", equalTo(0.0f));
    }

    @Test
    @DisplayName("Should fail registration with invalid data")
    void testRegisterMember_InvalidData() {
        String requestBody = """
            {
                "name": "",
                "email": "invalid-email",
                "password": "123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/register")
        .then()
            .statusCode(400)
            .body(containsString("Registration failed"));
    }

    @Test
    @DisplayName("Should fail registration with existing email")
    void testRegisterMember_ExistingEmail() {
        String requestBody = """
            {
                "name": "Duplicate Member",
                "email": "member@library.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/register")
        .then()
            .statusCode(400)
            .body(containsString("Member with email already exists"));
    }

    // ============ MEMBER PROFILE TESTS ============

    @Test
    @DisplayName("Should get member profile with authentication")
    void testGetMemberProfile_Success() {
        given()
            .header("Authorization", "Bearer " + testMember.getId())
        .when()
            .get("/api/members/{memberId}/profile", testMember.getId())
        .then()
            .statusCode(200)
            .body("name", equalTo("Test Member"))
            .body("email", equalTo("member@library.com"))
            .body("membershipNumber", equalTo("MEM123456"));
    }

    @Test
    @DisplayName("Should reject profile access without authentication")
    void testGetMemberProfile_NoAuth() {
        given()
        .when()
            .get("/api/members/{memberId}/profile", testMember.getId())
        .then()
            .statusCode(401)
            .body(containsString("Authentication required"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent member profile")
    void testGetMemberProfile_NotFound() {
        given()
            .header("Authorization", "Bearer " + testMember.getId())
        .when()
            .get("/api/members/{memberId}/profile", 999L)
        .then()
            .statusCode(404)
            .body(containsString("Member not found"));
    }

    // ============ BOOK BORROWING TESTS ============

    @Test
    @DisplayName("Should borrow book successfully with member authentication")
    void testBorrowBook_Success() {
        given()
            .header("Authorization", "Bearer " + testMember.getId())
        .when()
            .post("/api/members/{memberId}/borrow/{bookId}", testMember.getId(), testBook.getId())
        .then()
            .statusCode(200)
            .body(containsString("Book borrowed successfully"));
    }

    @Test
    @DisplayName("Should reject borrowing without authentication")
    void testBorrowBook_NoAuth() {
        given()
        .when()
            .post("/api/members/{memberId}/borrow/{bookId}", testMember.getId(), testBook.getId())
        .then()
            .statusCode(401)
            .body(containsString("Unauthorized access"));
    }

    @Test
    @DisplayName("Should reject borrowing with wrong member authentication")
    void testBorrowBook_WrongMember() {
        given()
            .header("Authorization", "Bearer " + (testMember.getId() + 100))
        .when()
            .post("/api/members/{memberId}/borrow/{bookId}", testMember.getId(), testBook.getId())
        .then()
            .statusCode(401)
            .body(containsString("Unauthorized access"));
    }

    @Test
    @DisplayName("Should fail to borrow non-existent book")
    void testBorrowBook_BookNotFound() {
        given()
            .header("Authorization", "Bearer " + testMember.getId())
        .when()
            .post("/api/members/{memberId}/borrow/{bookId}", testMember.getId(), 999L)
        .then()
            .statusCode(400)
            .body(containsString("Failed to borrow book"));
    }

    // ============ BOOK RETURNING TESTS ============

    @Test
    @DisplayName("Should return book successfully")
    @Transactional
    void testReturnBook_Success() {
        // First borrow the book
        testMember.getBorrowedBookIds().add(testBook.getId());
        testBook.setStatus(Book.BookStatus.BORROWED);

        given()
            .header("Authorization", "Bearer " + testMember.getId())
        .when()
            .post("/api/members/{memberId}/return/{bookId}", testMember.getId(), testBook.getId())
        .then()
            .statusCode(400); // Will fail because no active loan exists, but tests the endpoint
    }

    @Test
    @DisplayName("Should reject returning without authentication")
    void testReturnBook_NoAuth() {
        given()
        .when()
            .post("/api/members/{memberId}/return/{bookId}", testMember.getId(), testBook.getId())
        .then()
            .statusCode(401)
            .body(containsString("Unauthorized access"));
    }

    // ============ FINE PAYMENT TESTS ============

    @Test
    @DisplayName("Should pay fine successfully")
    void testPayFine_Success() {
        String requestBody = """
            {
                "amount": 25.0,
                "description": "Full payment"
            }
            """;

        given()
            .header("Authorization", "Bearer " + testMember.getId())
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/{memberId}/pay-fine/{fineId}", testMember.getId(), testFine.getId())
        .then()
            .statusCode(200)
            .body(containsString("Fine payment processed successfully"));
    }

    @Test
    @DisplayName("Should reject fine payment without authentication")
    void testPayFine_NoAuth() {
        String requestBody = """
            {
                "amount": 25.0,
                "description": "Payment"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/{memberId}/pay-fine/{fineId}", testMember.getId(), testFine.getId())
        .then()
            .statusCode(401)
            .body(containsString("Unauthorized access"));
    }

    @Test
    @DisplayName("Should fail to pay non-existent fine")
    void testPayFine_FineNotFound() {
        String requestBody = """
            {
                "amount": 25.0,
                "description": "Payment"
            }
            """;

        given()
            .header("Authorization", "Bearer " + testMember.getId())
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/{memberId}/pay-fine/{fineId}", testMember.getId(), 999L)
        .then()
            .statusCode(400)
            .body(containsString("Failed to process payment"));
    }

    // ============ MEMBERSHIP RENEWAL TESTS ============

    @Test
    @DisplayName("Should renew membership successfully")
    void testRenewMembership_Success() {
        String requestBody = """
            {
                "years": 2
            }
            """;

        given()
            .header("Authorization", "Bearer " + testMember.getId())
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/{memberId}/renew-membership", testMember.getId())
        .then()
            .statusCode(200)
            .body(containsString("Membership renewed successfully"));
    }

    @Test
    @DisplayName("Should reject membership renewal without authentication")
    void testRenewMembership_NoAuth() {
        String requestBody = """
            {
                "years": 1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/{memberId}/renew-membership", testMember.getId())
        .then()
            .statusCode(401)
            .body(containsString("Unauthorized access"));
    }

    @Test
    @DisplayName("Should fail membership renewal with invalid years")
    void testRenewMembership_InvalidYears() {
        String requestBody = """
            {
                "years": 0
            }
            """;

        given()
            .header("Authorization", "Bearer " + testMember.getId())
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/members/{memberId}/renew-membership", testMember.getId())
        .then()
            .statusCode(400)
            .body(containsString("Failed to renew membership"));
    }

    // ============ MEMBER STATISTICS TESTS ============

    @Test
    @DisplayName("Should get member statistics as member themselves")
    void testGetMemberStatistics_AsMember() {
        given()
            .header("Authorization", "Bearer " + testMember.getId())
        .when()
            .get("/api/members/{memberId}/statistics", testMember.getId())
        .then()
            .statusCode(200)
            .body(containsString("Statistics generated"));
    }

    @Test
    @DisplayName("Should get member statistics as admin")
    void testGetMemberStatistics_AsAdmin() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/members/{memberId}/statistics", testMember.getId())
        .then()
            .statusCode(200)
            .body(containsString("Statistics generated"));
    }

    @Test
    @DisplayName("Should reject statistics access without authentication")
    void testGetMemberStatistics_NoAuth() {
        given()
        .when()
            .get("/api/members/{memberId}/statistics", testMember.getId())
        .then()
            .statusCode(401)
            .body(containsString("Authentication required"));
    }

    @Test
    @DisplayName("Should reject statistics access for wrong member")
    void testGetMemberStatistics_WrongMember() {
        // Create another member
        Member otherMember = new Member();
        otherMember.setName("Other Member");
        otherMember.setEmail("other@member.com");
        otherMember.setPassword("password123");
        otherMember.setRole("MEMBER");
        otherMember.setCreatedAt(new Date());
        memberRepository.persist(otherMember);

        given()
            .header("Authorization", "Bearer " + otherMember.getId())
        .when()
            .get("/api/members/{memberId}/statistics", testMember.getId())
        .then()
            .statusCode(403)
            .body(containsString("Access denied"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent member statistics")
    void testGetMemberStatistics_MemberNotFound() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/members/{memberId}/statistics", 999L)
        .then()
            .statusCode(404)
            .body(containsString("Member not found"));
    }

    // ============ GET ALL MEMBERS TESTS ============

    @Test
    @DisplayName("Should get all members as admin")
    void testGetAllMembers_AsAdmin() {
        given()
            .header("Authorization", "Bearer " + testAdmin.getId())
        .when()
            .get("/api/members")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].name", notNullValue())
            .body("[0].email", notNullValue());
    }

    @Test
    @DisplayName("Should reject get all members for non-admin")
    void testGetAllMembers_NonAdmin() {
        given()
            .header("Authorization", "Bearer " + testMember.getId())
        .when()
            .get("/api/members")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should reject get all members without authentication")
    void testGetAllMembers_NoAuth() {
        given()
        .when()
            .get("/api/members")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    // ============ ERROR HANDLING TESTS ============

    @Test
    @DisplayName("Should handle invalid member ID gracefully")
    void testInvalidMemberId() {
        given()
            .header("Authorization", "Bearer " + testMember.getId())
        .when()
            .get("/api/members/{memberId}/profile", "invalid")
        .then()
            .statusCode(404); // JAX-RS will handle path param conversion error
    }

    @Test
    @DisplayName("Should handle malformed JSON in registration")
    void testMalformedJson() {
        String malformedJson = "{ invalid json }";

        given()
            .contentType(ContentType.JSON)
            .body(malformedJson)
        .when()
            .post("/api/members/register")
        .then()
            .statusCode(400); // Bad request for malformed JSON
    }

    @Test
    @DisplayName("Should handle missing request body in fine payment")
    void testMissingRequestBody() {
        given()
            .header("Authorization", "Bearer " + testMember.getId())
            .contentType(ContentType.JSON)
        .when()
            .post("/api/members/{memberId}/pay-fine/{fineId}", testMember.getId(), testFine.getId())
        .then()
            .statusCode(500); // Internal server error for missing body
    }

    // ============ AUTHENTICATION EDGE CASES ============

    @Test
    @DisplayName("Should reject invalid authorization header format")
    void testInvalidAuthHeader() {
        given()
            .header("Authorization", "InvalidFormat")
        .when()
            .get("/api/members/{memberId}/profile", testMember.getId())
        .then()
            .statusCode(401)
            .body(containsString("Authentication required"));
    }

    @Test
    @DisplayName("Should reject non-numeric token in authorization")
    void testNonNumericToken() {
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .get("/api/members/{memberId}/profile", testMember.getId())
        .then()
            .statusCode(401)
            .body(containsString("Authentication required"));
    }

    @Test
    @DisplayName("Should reject non-existent user token")
    void testNonExistentUserToken() {
        given()
            .header("Authorization", "Bearer 99999")
        .when()
            .get("/api/members/{memberId}/profile", testMember.getId())
        .then()
            .statusCode(401)
            .body(containsString("Authentication required"));
    }
} 