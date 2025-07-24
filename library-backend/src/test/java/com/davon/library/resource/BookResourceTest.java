package com.davon.library.resource;

import com.davon.library.model.Book;
import com.davon.library.model.Author;
import com.davon.library.model.User;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.AuthorRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.BookService;
import com.davon.library.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("BookResource Integration Tests")
public class BookResourceTest {

    @Inject
    BookRepository bookRepository;

    @Inject
    AuthorRepository authorRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    BookService bookService;

    @Inject
    UserService userService;

    private User adminUser;
    private User regularUser;
    private Book testBook;
    private Author testAuthor;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        userRepository.deleteAll();

        // Create test admin user
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("admin123");
        adminUser.setRole("ADMIN");
        adminUser.setCreatedAt(new Date());
        userRepository.persist(adminUser);

        // Create test regular user
        regularUser = new User();
        regularUser.setName("Regular User");
        regularUser.setEmail("user@test.com");
        regularUser.setPassword("user123");
        regularUser.setRole("USER");
        regularUser.setCreatedAt(new Date());
        userRepository.persist(regularUser);

        // Create test author
        testAuthor = new Author();
        testAuthor.setName("Test Author");
        testAuthor.setBio("Test author biography");
        authorRepository.persist(testAuthor);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        testBook.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.persist(testBook);
    }

    // ===== PUBLIC ENDPOINTS TESTS =====

    @Test
    @DisplayName("Should get all books successfully")
    void testGetAllBooks_Success() {
        given()
        .when()
            .get("/api/books")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].title", equalTo("Test Book"))
            .body("[0].isbn", equalTo("978-0123456789"));
    }

    @Test
    @DisplayName("Should get book by ID successfully")
    void testGetBookById_Success() {
        given()
            .pathParam("id", testBook.getId())
        .when()
            .get("/api/books/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("title", equalTo("Test Book"))
            .body("isbn", equalTo("978-0123456789"))
            .body("status", equalTo("AVAILABLE"));
    }

    @Test
    @DisplayName("Should return 404 when book not found by ID")
    void testGetBookById_NotFound() {
        given()
            .pathParam("id", 99999L)
        .when()
            .get("/api/books/{id}")
        .then()
            .statusCode(404)
            .body(containsString("Book not found"));
    }

    @Test
    @DisplayName("Should search books by title successfully")
    void testSearchBooksByTitle_Success() {
        given()
            .pathParam("title", "Test")
        .when()
            .get("/api/books/search/title/{title}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].title", equalTo("Test Book"));
    }

    @Test
    @DisplayName("Should get books by genre successfully")
    void testGetBooksByGenre_Success() {
        given()
            .pathParam("genre", "Fiction")
        .when()
            .get("/api/books/genre/{genre}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].genre", equalTo("Fiction"));
    }

    @Test
    @DisplayName("Should get available books successfully")
    void testGetAvailableBooks_Success() {
        given()
        .when()
            .get("/api/books/available")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1))
            .body("[0].status", equalTo("AVAILABLE"));
    }

    @Test
    @DisplayName("Should search books with query parameter")
    void testSearchBooks_WithQuery_Success() {
        given()
            .queryParam("q", "Test")
        .when()
            .get("/api/books/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1));
    }

    @Test
    @DisplayName("Should return all books when search query is empty")
    void testSearchBooks_EmptyQuery_ReturnsAll() {
        given()
            .queryParam("q", "")
        .when()
            .get("/api/books/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1));
    }

    @Test
    @DisplayName("Should return all books when no search query provided")
    void testSearchBooks_NoQuery_ReturnsAll() {
        given()
        .when()
            .get("/api/books/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(1));
    }

    @Test
    @DisplayName("Should get book by ISBN successfully")
    void testGetBookByIsbn_Success() {
        given()
            .pathParam("isbn", "978-0123456789")
        .when()
            .get("/api/books/isbn/{isbn}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("isbn", equalTo("978-0123456789"))
            .body("title", equalTo("Test Book"));
    }

    @Test
    @DisplayName("Should return 404 when book not found by ISBN")
    void testGetBookByIsbn_NotFound() {
        given()
            .pathParam("isbn", "978-9999999999")
        .when()
            .get("/api/books/isbn/{isbn}")
        .then()
            .statusCode(404)
            .body(containsString("Book not found"));
    }

    // ===== ADMIN ENDPOINTS TESTS =====

    @Test
    @DisplayName("Should add book successfully as admin")
    void testAddBook_AsAdmin_Success() {
        String bookRequest = """
            {
                "title": "New Test Book",
                "isbn": "978-0987654321",
                "publicationYear": 2024,
                "genre": "Mystery"
            }
            """;

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(bookRequest)
        .when()
            .post("/api/books")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("title", equalTo("New Test Book"))
            .body("isbn", equalTo("978-0987654321"))
            .body("status", equalTo("AVAILABLE"));
    }

    @Test
    @DisplayName("Should fail to add book as regular user")
    void testAddBook_AsRegularUser_Forbidden() {
        String bookRequest = """
            {
                "title": "New Test Book",
                "isbn": "978-0987654321",
                "publicationYear": 2024,
                "genre": "Mystery"
            }
            """;

        given()
            .header("Authorization", "Bearer " + regularUser.getId())
            .contentType(ContentType.JSON)
            .body(bookRequest)
        .when()
            .post("/api/books")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should fail to add book without authorization")
    void testAddBook_NoAuth_Forbidden() {
        String bookRequest = """
            {
                "title": "New Test Book",
                "isbn": "978-0987654321",
                "publicationYear": 2024,
                "genre": "Mystery"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(bookRequest)
        .when()
            .post("/api/books")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should add book with authors successfully")
    @Transactional
    void testAddBook_WithAuthors_Success() {
        String bookRequest = """
            {
                "title": "Book with Author",
                "isbn": "978-1111111111",
                "publicationYear": 2024,
                "genre": "Science",
                "authorIds": [%d]
            }
            """.formatted(testAuthor.getId());

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(bookRequest)
        .when()
            .post("/api/books")
        .then()
            .statusCode(201)
            .body("title", equalTo("Book with Author"));
    }

    @Test
    @DisplayName("Should update book successfully as admin")
    void testUpdateBook_AsAdmin_Success() {
        String updateRequest = """
            {
                "title": "Updated Test Book",
                "genre": "Mystery",
                "publicationYear": 2024
            }
            """;

        given()
            .pathParam("id", testBook.getId())
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/books/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("title", equalTo("Updated Test Book"))
            .body("genre", equalTo("Mystery"));
    }

    @Test
    @DisplayName("Should fail to update book as regular user")
    void testUpdateBook_AsRegularUser_Forbidden() {
        String updateRequest = """
            {
                "title": "Updated Test Book"
            }
            """;

        given()
            .pathParam("id", testBook.getId())
            .header("Authorization", "Bearer " + regularUser.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/books/{id}")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent book")
    void testUpdateBook_NotFound() {
        String updateRequest = """
            {
                "title": "Updated Test Book"
            }
            """;

        given()
            .pathParam("id", 99999L)
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/books/{id}")
        .then()
            .statusCode(404)
            .body(containsString("Book not found"));
    }

    @Test
    @DisplayName("Should delete book successfully as admin")
    void testDeleteBook_AsAdmin_Success() {
        given()
            .pathParam("id", testBook.getId())
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .delete("/api/books/{id}")
        .then()
            .statusCode(200)
            .body(containsString("Book deleted successfully"));
    }

    @Test
    @DisplayName("Should fail to delete book as regular user")
    void testDeleteBook_AsRegularUser_Forbidden() {
        given()
            .pathParam("id", testBook.getId())
            .header("Authorization", "Bearer " + regularUser.getId())
        .when()
            .delete("/api/books/{id}")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent book")
    void testDeleteBook_NotFound() {
        given()
            .pathParam("id", 99999L)
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .delete("/api/books/{id}")
        .then()
            .statusCode(404)
            .body(containsString("Book not found"));
    }

    @Test
    @DisplayName("Should fail to delete borrowed book")
    @Transactional
    void testDeleteBook_BorrowedBook_BadRequest() {
        // Set book status to borrowed
        testBook.setStatus(Book.BookStatus.BORROWED);

        given()
            .pathParam("id", testBook.getId())
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .delete("/api/books/{id}")
        .then()
            .statusCode(400)
            .body(containsString("Cannot delete book: Currently borrowed"));
    }

    // ===== AUTHOR MANAGEMENT TESTS =====

    @Test
    @DisplayName("Should add author to book successfully as admin")
    void testAddAuthorToBook_AsAdmin_Success() {
        given()
            .pathParam("bookId", testBook.getId())
            .pathParam("authorId", testAuthor.getId())
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .post("/api/books/{bookId}/authors/{authorId}")
        .then()
            .statusCode(200)
            .body(containsString("Author added to book successfully"));
    }

    @Test
    @DisplayName("Should fail to add author to book as regular user")
    void testAddAuthorToBook_AsRegularUser_Forbidden() {
        given()
            .pathParam("bookId", testBook.getId())
            .pathParam("authorId", testAuthor.getId())
            .header("Authorization", "Bearer " + regularUser.getId())
        .when()
            .post("/api/books/{bookId}/authors/{authorId}")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    @Test
    @DisplayName("Should remove author from book successfully as admin")
    @Transactional
    void testRemoveAuthorFromBook_AsAdmin_Success() {
        // First add author to book
        bookService.addAuthorToBook(testBook.getId(), testAuthor.getId());

        given()
            .pathParam("bookId", testBook.getId())
            .pathParam("authorId", testAuthor.getId())
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .delete("/api/books/{bookId}/authors/{authorId}")
        .then()
            .statusCode(200)
            .body(containsString("Author removed from book successfully"));
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    @DisplayName("Should handle malformed JSON in add book request")
    void testAddBook_MalformedJSON() {
        String malformedRequest = "{invalid json}";

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(malformedRequest)
        .when()
            .post("/api/books")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle malformed JSON in update book request")
    void testUpdateBook_MalformedJSON() {
        String malformedRequest = "{invalid json}";

        given()
            .pathParam("id", testBook.getId())
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(malformedRequest)
        .when()
            .put("/api/books/{id}")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should handle invalid path parameters")
    void testGetBookById_InvalidId() {
        given()
            .pathParam("id", "invalid-id")
        .when()
            .get("/api/books/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should handle invalid authorization header")
    void testAddBook_InvalidAuth_Forbidden() {
        String bookRequest = """
            {
                "title": "New Test Book"
            }
            """;

        given()
            .header("Authorization", "Invalid-Token")
            .contentType(ContentType.JSON)
            .body(bookRequest)
        .when()
            .post("/api/books")
        .then()
            .statusCode(403)
            .body(containsString("Admin access required"));
    }

    // ===== CONTENT TYPE TESTS =====

    @Test
    @DisplayName("Should return JSON content type for successful requests")
    void testContentType_JSON() {
        given()
        .when()
            .get("/api/books")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should accept JSON content type for POST requests")
    void testAddBook_AcceptJSON() {
        String bookRequest = """
            {
                "title": "JSON Test Book",
                "isbn": "978-2222222222",
                "publicationYear": 2024,
                "genre": "Test"
            }
            """;

        given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(bookRequest)
        .when()
            .post("/api/books")
        .then()
            .statusCode(201);
    }

    // ===== INTEGRATION TESTS =====

    @Test
    @DisplayName("Should maintain data consistency across operations")
    @Transactional
    void testDataConsistency() {
        // First, verify book exists
        given()
            .pathParam("id", testBook.getId())
        .when()
            .get("/api/books/{id}")
        .then()
            .statusCode(200)
            .body("title", equalTo("Test Book"));

        // Update the book
        String updateRequest = """
            {
                "title": "Consistency Test Book"
            }
            """;

        given()
            .pathParam("id", testBook.getId())
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/books/{id}")
        .then()
            .statusCode(200);

        // Verify the update
        given()
            .pathParam("id", testBook.getId())
        .when()
            .get("/api/books/{id}")
        .then()
            .statusCode(200)
            .body("title", equalTo("Consistency Test Book"));
    }

    @Test
    @DisplayName("Should handle book lifecycle correctly")
    @Transactional
    void testBookLifecycle() {
        // 1. Create new book
        String bookRequest = """
            {
                "title": "Lifecycle Test Book",
                "isbn": "978-3333333333",
                "publicationYear": 2024,
                "genre": "Lifecycle"
            }
            """;

        Long newBookId = given()
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(bookRequest)
        .when()
            .post("/api/books")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // 2. Verify book was created
        given()
            .pathParam("id", newBookId)
        .when()
            .get("/api/books/{id}")
        .then()
            .statusCode(200)
            .body("title", equalTo("Lifecycle Test Book"));

        // 3. Update the book
        String updateRequest = """
            {
                "genre": "Updated Lifecycle"
            }
            """;

        given()
            .pathParam("id", newBookId)
            .header("Authorization", "Bearer " + adminUser.getId())
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/books/{id}")
        .then()
            .statusCode(200);

        // 4. Delete the book
        given()
            .pathParam("id", newBookId)
            .header("Authorization", "Bearer " + adminUser.getId())
        .when()
            .delete("/api/books/{id}")
        .then()
            .statusCode(200);

        // 5. Verify book was deleted
        given()
            .pathParam("id", newBookId)
        .when()
            .get("/api/books/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should handle search functionality correctly")
    @Transactional
    void testSearchFunctionality() {
        // Create additional books for search testing
        Book book1 = new Book();
        book1.setTitle("Java Programming");
        book1.setGenre("Programming");
        book1.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.persist(book1);

        Book book2 = new Book();
        book2.setTitle("Python Guide");
        book2.setGenre("Programming");
        book2.setStatus(Book.BookStatus.BORROWED);
        bookRepository.persist(book2);

        // Test search by title
        given()
            .pathParam("title", "Java")
        .when()
            .get("/api/books/search/title/{title}")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].title", containsString("Java"));

        // Test search by genre
        given()
            .pathParam("genre", "Programming")
        .when()
            .get("/api/books/genre/{genre}")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2));

        // Test available books only
        given()
        .when()
            .get("/api/books/available")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(1)); // Should include original test book + Java book
    }
} 