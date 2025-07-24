package com.davon.library.service;

import com.davon.library.model.Book;
import com.davon.library.model.Author;
import com.davon.library.model.Loan;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.AuthorRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("BookService Unit Tests")
public class BookServiceTest {

    @Inject
    BookService bookService;

    @InjectMock
    BookRepository bookRepository;

    @InjectMock
    AuthorRepository authorRepository;

    private Book testBook;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        testBook.setStatus(Book.BookStatus.AVAILABLE);
        testBook.setAuthors(new ArrayList<>());
        testBook.setLoans(new ArrayList<>());

        // Setup test author
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("Test Author");
        testAuthor.setBio("Test author biography");
        testAuthor.setBooks(new ArrayList<>());
    }

    // ===== ADD AUTHOR TO BOOK TESTS =====

    @Test
    @DisplayName("Should add author to book successfully")
    void testAddAuthorToBook_Success() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = bookService.addAuthorToBook(1L, 1L);

        // Then
        assertTrue(result);
        assertTrue(testBook.getAuthors().contains(testAuthor));
        assertTrue(testAuthor.getBooks().contains(testBook));
        
        verify(bookRepository).findByIdOptional(1L);
        verify(authorRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to add author when book ID is null")
    void testAddAuthorToBook_NullBookId_Fails() {
        // When
        boolean result = bookService.addAuthorToBook(null, 1L);

        // Then
        assertFalse(result);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should fail to add author when author ID is null")
    void testAddAuthorToBook_NullAuthorId_Fails() {
        // When
        boolean result = bookService.addAuthorToBook(1L, null);

        // Then
        assertFalse(result);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should fail to add author when book not found")
    void testAddAuthorToBook_BookNotFound_Fails() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = bookService.addAuthorToBook(1L, 1L);

        // Then
        assertFalse(result);
        verify(bookRepository).findByIdOptional(1L);
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should fail to add author when author not found")
    void testAddAuthorToBook_AuthorNotFound_Fails() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = bookService.addAuthorToBook(1L, 1L);

        // Then
        assertFalse(result);
        verify(bookRepository).findByIdOptional(1L);
        verify(authorRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to add author when already associated")
    void testAddAuthorToBook_AlreadyAssociated_Fails() {
        // Given - author already in book's authors list
        testBook.getAuthors().add(testAuthor);
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = bookService.addAuthorToBook(1L, 1L);

        // Then
        assertFalse(result);
        assertEquals(1, testBook.getAuthors().size()); // Should remain unchanged
    }

    // ===== REMOVE AUTHOR FROM BOOK TESTS =====

    @Test
    @DisplayName("Should remove author from book successfully")
    void testRemoveAuthorFromBook_Success() {
        // Given - author already associated with book
        testBook.getAuthors().add(testAuthor);
        testAuthor.getBooks().add(testBook);
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = bookService.removeAuthorFromBook(1L, 1L);

        // Then
        assertTrue(result);
        assertFalse(testBook.getAuthors().contains(testAuthor));
        assertFalse(testAuthor.getBooks().contains(testBook));
    }

    @Test
    @DisplayName("Should fail to remove author when not associated")
    void testRemoveAuthorFromBook_NotAssociated_Fails() {
        // Given - author not in book's authors list
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = bookService.removeAuthorFromBook(1L, 1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to remove author when book ID is null")
    void testRemoveAuthorFromBook_NullBookId_Fails() {
        // When
        boolean result = bookService.removeAuthorFromBook(null, 1L);

        // Then
        assertFalse(result);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should fail to remove author when author ID is null")
    void testRemoveAuthorFromBook_NullAuthorId_Fails() {
        // When
        boolean result = bookService.removeAuthorFromBook(1L, null);

        // Then
        assertFalse(result);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(authorRepository);
    }

    // ===== CREATE BOOK TESTS =====

    @Test
    @DisplayName("Should create book successfully")
    void testCreateBook_Success() {
        // Given
        when(bookRepository.findByIsbn("978-0123456789")).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return null;
        }).when(bookRepository).persist(any(Book.class));

        // When
        Book result = bookService.createBook("Test Book", "978-0123456789", 2023, "Fiction");

        // Then
        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        assertEquals("978-0123456789", result.getIsbn());
        assertEquals(2023, result.getPublicationYear());
        assertEquals("Fiction", result.getGenre());
        assertEquals(Book.BookStatus.AVAILABLE, result.getStatus());
        
        verify(bookRepository).findByIsbn("978-0123456789");
        verify(bookRepository).persist(any(Book.class));
    }

    @Test
    @DisplayName("Should fail to create book with null title")
    void testCreateBook_NullTitle_Fails() {
        // When
        Book result = bookService.createBook(null, "978-0123456789", 2023, "Fiction");

        // Then
        assertNull(result);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should fail to create book with empty title")
    void testCreateBook_EmptyTitle_Fails() {
        // When
        Book result = bookService.createBook("   ", "978-0123456789", 2023, "Fiction");

        // Then
        assertNull(result);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should fail to create book with duplicate ISBN")
    void testCreateBook_DuplicateIsbn_Fails() {
        // Given
        when(bookRepository.findByIsbn("978-0123456789")).thenReturn(Optional.of(testBook));

        // When
        Book result = bookService.createBook("Another Book", "978-0123456789", 2023, "Fiction");

        // Then
        assertNull(result);
        verify(bookRepository).findByIsbn("978-0123456789");
        verify(bookRepository, never()).persist(any(Book.class));
    }

    @Test
    @DisplayName("Should create book without ISBN")
    void testCreateBook_NoIsbn_Success() {
        // Given
        doAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return null;
        }).when(bookRepository).persist(any(Book.class));

        // When
        Book result = bookService.createBook("Test Book", null, 2023, "Fiction");

        // Then
        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        assertNull(result.getIsbn());
        
        verify(bookRepository, never()).findByIsbn(any());
        verify(bookRepository).persist(any(Book.class));
    }

    @Test
    @DisplayName("Should trim whitespace from title")
    void testCreateBook_TrimTitle_Success() {
        // Given
        when(bookRepository.findByIsbn(any())).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return null;
        }).when(bookRepository).persist(any(Book.class));

        // When
        Book result = bookService.createBook("  Test Book  ", "978-0123456789", 2023, "Fiction");

        // Then
        assertNotNull(result);
        assertEquals("Test Book", result.getTitle()); // Should be trimmed
    }

    // ===== UPDATE BOOK TESTS =====

    @Test
    @DisplayName("Should update book successfully")
    void testUpdateBook_Success() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findByIsbn("978-0987654321")).thenReturn(Optional.empty());

        // When
        boolean result = bookService.updateBook(1L, "Updated Title", "978-0987654321", 2024, "Mystery");

        // Then
        assertTrue(result);
        assertEquals("Updated Title", testBook.getTitle());
        assertEquals("978-0987654321", testBook.getIsbn());
        assertEquals(2024, testBook.getPublicationYear());
        assertEquals("Mystery", testBook.getGenre());
    }

    @Test
    @DisplayName("Should fail to update book when ID is null")
    void testUpdateBook_NullId_Fails() {
        // When
        boolean result = bookService.updateBook(null, "Updated Title", "978-0987654321", 2024, "Mystery");

        // Then
        assertFalse(result);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should fail to update book when not found")
    void testUpdateBook_BookNotFound_Fails() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = bookService.updateBook(1L, "Updated Title", "978-0987654321", 2024, "Mystery");

        // Then
        assertFalse(result);
        verify(bookRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to update book with conflicting ISBN")
    void testUpdateBook_ConflictingIsbn_Fails() {
        // Given
        Book anotherBook = new Book();
        anotherBook.setId(2L);
        anotherBook.setIsbn("978-0987654321");
        
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findByIsbn("978-0987654321")).thenReturn(Optional.of(anotherBook));

        // When
        boolean result = bookService.updateBook(1L, "Updated Title", "978-0987654321", 2024, "Mystery");

        // Then
        assertFalse(result);
        assertNotEquals("978-0987654321", testBook.getIsbn()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should update book with partial information")
    void testUpdateBook_PartialUpdate_Success() {
        // Given
        String originalTitle = testBook.getTitle();
        String originalIsbn = testBook.getIsbn();
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));

        // When - only update genre
        boolean result = bookService.updateBook(1L, null, null, 0, "Mystery");

        // Then
        assertTrue(result);
        assertEquals(originalTitle, testBook.getTitle()); // Should remain unchanged
        assertEquals(originalIsbn, testBook.getIsbn()); // Should remain unchanged
        assertEquals("Mystery", testBook.getGenre()); // Should be updated
    }

    @Test
    @DisplayName("Should trim whitespace from updated title")
    void testUpdateBook_TrimTitle_Success() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));

        // When
        boolean result = bookService.updateBook(1L, "  Updated Title  ", null, 0, null);

        // Then
        assertTrue(result);
        assertEquals("Updated Title", testBook.getTitle()); // Should be trimmed
    }

    @Test
    @DisplayName("Should allow updating book with same ISBN")
    void testUpdateBook_SameIsbn_Success() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findByIsbn(testBook.getIsbn())).thenReturn(Optional.of(testBook));

        // When
        boolean result = bookService.updateBook(1L, "Updated Title", testBook.getIsbn(), 2024, "Mystery");

        // Then
        assertTrue(result);
        assertEquals("Updated Title", testBook.getTitle());
        assertEquals(testBook.getIsbn(), testBook.getIsbn()); // Should remain the same
    }

    // ===== DELETE BOOK TESTS =====

    @Test
    @DisplayName("Should delete book successfully")
    void testDeleteBook_Success() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(testBook);

        // When
        boolean result = bookService.deleteBook(1L);

        // Then
        assertTrue(result);
        verify(bookRepository).findByIdOptional(1L);
        verify(bookRepository).delete(testBook);
    }

    @Test
    @DisplayName("Should fail to delete book when ID is null")
    void testDeleteBook_NullId_Fails() {
        // When
        boolean result = bookService.deleteBook(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should fail to delete book when not found")
    void testDeleteBook_BookNotFound_Fails() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = bookService.deleteBook(1L);

        // Then
        assertFalse(result);
        verify(bookRepository).findByIdOptional(1L);
        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should fail to delete book with active loans")
    void testDeleteBook_WithActiveLoans_Fails() {
        // Given - book has active loan
        Loan activeLoan = new Loan();
        activeLoan.setStatus(Loan.LoanStatus.ACTIVE);
        testBook.getLoans().add(activeLoan);
        
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));

        // When
        boolean result = bookService.deleteBook(1L);

        // Then
        assertFalse(result);
        verify(bookRepository).findByIdOptional(1L);
        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete book with only returned loans")
    void testDeleteBook_WithReturnedLoans_Success() {
        // Given - book has only returned loans
        Loan returnedLoan = new Loan();
        returnedLoan.setStatus(Loan.LoanStatus.RETURNED);
        testBook.getLoans().add(returnedLoan);
        
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(testBook);

        // When
        boolean result = bookService.deleteBook(1L);

        // Then
        assertTrue(result);
        verify(bookRepository).delete(testBook);
    }

    @Test
    @DisplayName("Should delete book with null loans list")
    void testDeleteBook_NullLoans_Success() {
        // Given - book has null loans list
        testBook.setLoans(null);
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(testBook);

        // When
        boolean result = bookService.deleteBook(1L);

        // Then
        assertTrue(result);
        verify(bookRepository).delete(testBook);
    }

    @Test
    @DisplayName("Should delete book with empty loans list")
    void testDeleteBook_EmptyLoans_Success() {
        // Given - book has empty loans list (already set in setUp)
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(testBook);

        // When
        boolean result = bookService.deleteBook(1L);

        // Then
        assertTrue(result);
        verify(bookRepository).delete(testBook);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle multiple authors on same book")
    void testAddMultipleAuthorsToBook_Success() {
        // Given
        Author secondAuthor = new Author();
        secondAuthor.setId(2L);
        secondAuthor.setName("Second Author");
        secondAuthor.setBooks(new ArrayList<>());
        
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.findByIdOptional(2L)).thenReturn(Optional.of(secondAuthor));

        // When
        boolean result1 = bookService.addAuthorToBook(1L, 1L);
        boolean result2 = bookService.addAuthorToBook(1L, 2L);

        // Then
        assertTrue(result1);
        assertTrue(result2);
        assertEquals(2, testBook.getAuthors().size());
        assertTrue(testBook.getAuthors().contains(testAuthor));
        assertTrue(testBook.getAuthors().contains(secondAuthor));
    }

    @Test
    @DisplayName("Should handle book creation with zero publication year")
    void testCreateBook_ZeroPublicationYear_Success() {
        // Given
        doAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return null;
        }).when(bookRepository).persist(any(Book.class));

        // When
        Book result = bookService.createBook("Test Book", null, 0, "Fiction");

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPublicationYear());
    }

    @Test
    @DisplayName("Should handle book update with negative publication year")
    void testUpdateBook_NegativePublicationYear_Ignored() {
        // Given
        int originalYear = testBook.getPublicationYear();
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));

        // When
        boolean result = bookService.updateBook(1L, null, null, -100, null);

        // Then
        assertTrue(result);
        assertEquals(originalYear, testBook.getPublicationYear()); // Should remain unchanged
    }
} 