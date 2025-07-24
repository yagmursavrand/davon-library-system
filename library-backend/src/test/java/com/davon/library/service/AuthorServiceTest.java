package com.davon.library.service;

import com.davon.library.model.Author;
import com.davon.library.model.Book;
import com.davon.library.repository.AuthorRepository;
import com.davon.library.repository.BookRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("AuthorService Unit Tests")
public class AuthorServiceTest {

    @Inject
    AuthorService authorService;

    @InjectMock
    AuthorRepository authorRepository;

    @InjectMock
    BookRepository bookRepository;

    private Author testAuthor;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Setup test author
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("Test Author");
        testAuthor.setBio("Test author biography");
        testAuthor.setBooks(new ArrayList<>());

        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        testBook.setAuthors(new ArrayList<>());
    }

    // ===== GET ALL AUTHORS TESTS =====

    @Test
    @DisplayName("Should get all authors successfully")
    void testGetAllAuthors_Success() {
        // Given
        List<Author> authors = List.of(testAuthor);
        when(authorRepository.listAll()).thenReturn(authors);

        // When
        List<Author> result = authorService.getAllAuthors();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAuthor, result.get(0));
        
        verify(authorRepository).listAll();
    }

    @Test
    @DisplayName("Should return empty list when no authors exist")
    void testGetAllAuthors_EmptyList() {
        // Given
        when(authorRepository.listAll()).thenReturn(List.of());

        // When
        List<Author> result = authorService.getAllAuthors();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ===== GET AUTHOR BY ID TESTS =====

    @Test
    @DisplayName("Should get author by ID successfully")
    void testGetAuthorById_Success() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        Optional<Author> result = authorService.getAuthorById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAuthor, result.get());
        
        verify(authorRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should return empty when author not found")
    void testGetAuthorById_NotFound() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        Optional<Author> result = authorService.getAuthorById(1L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when ID is null")
    void testGetAuthorById_NullId() {
        // When
        Optional<Author> result = authorService.getAuthorById(null);

        // Then
        assertFalse(result.isPresent());
        verifyNoInteractions(authorRepository);
    }

    // ===== CREATE AUTHOR TESTS =====

    @Test
    @DisplayName("Should create author successfully")
    void testCreateAuthor_Success() {
        // Given
        when(authorRepository.findByName("New Author")).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(1L);
            return null;
        }).when(authorRepository).persist(any(Author.class));

        // When
        Author result = authorService.createAuthor("New Author", "New author bio");

        // Then
        assertNotNull(result);
        assertEquals("New Author", result.getName());
        assertEquals("New author bio", result.getBio());
        
        verify(authorRepository).findByName("New Author");
        verify(authorRepository).persist(any(Author.class));
    }

    @Test
    @DisplayName("Should fail to create author with null name")
    void testCreateAuthor_NullName_Fails() {
        // When
        Author result = authorService.createAuthor(null, "Bio");

        // Then
        assertNull(result);
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should fail to create author with empty name")
    void testCreateAuthor_EmptyName_Fails() {
        // When
        Author result = authorService.createAuthor("   ", "Bio");

        // Then
        assertNull(result);
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should return existing author when name already exists")
    void testCreateAuthor_ExistingName_ReturnsExisting() {
        // Given
        when(authorRepository.findByName("Existing Author")).thenReturn(Optional.of(testAuthor));

        // When
        Author result = authorService.createAuthor("Existing Author", "New bio");

        // Then
        assertNotNull(result);
        assertEquals(testAuthor, result);
        
        verify(authorRepository).findByName("Existing Author");
                 verify(authorRepository, never()).persist(any(Author.class));
    }

    @Test
    @DisplayName("Should trim whitespace from author name")
    void testCreateAuthor_TrimName_Success() {
        // Given
        when(authorRepository.findByName("Trimmed Author")).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(1L);
            return null;
        }).when(authorRepository).persist(any(Author.class));

        // When
        Author result = authorService.createAuthor("  Trimmed Author  ", "Bio");

        // Then
        assertNotNull(result);
        assertEquals("Trimmed Author", result.getName());
        
        verify(authorRepository).findByName("Trimmed Author");
    }

    @Test
    @DisplayName("Should create author with null bio")
    void testCreateAuthor_NullBio_Success() {
        // Given
        when(authorRepository.findByName("Author Without Bio")).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(1L);
            return null;
        }).when(authorRepository).persist(any(Author.class));

        // When
        Author result = authorService.createAuthor("Author Without Bio", null);

        // Then
        assertNotNull(result);
        assertEquals("Author Without Bio", result.getName());
        assertNull(result.getBio());
    }

    // ===== UPDATE AUTHOR TESTS =====

    @Test
    @DisplayName("Should update author successfully")
    void testUpdateAuthor_Success() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = authorService.updateAuthor(1L, "Updated Name", "Updated Bio");

        // Then
        assertTrue(result);
        assertEquals("Updated Name", testAuthor.getName());
        assertEquals("Updated Bio", testAuthor.getBio());
    }

    @Test
    @DisplayName("Should fail to update author when ID is null")
    void testUpdateAuthor_NullId_Fails() {
        // When
        boolean result = authorService.updateAuthor(null, "New Name", "New Bio");

        // Then
        assertFalse(result);
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should fail to update author when not found")
    void testUpdateAuthor_NotFound_Fails() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = authorService.updateAuthor(1L, "New Name", "New Bio");

        // Then
        assertFalse(result);
        verify(authorRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should update only name when bio is null")
    void testUpdateAuthor_OnlyName_Success() {
        // Given
        String originalBio = testAuthor.getBio();
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = authorService.updateAuthor(1L, "New Name", null);

        // Then
        assertTrue(result);
        assertEquals("New Name", testAuthor.getName());
        assertEquals(originalBio, testAuthor.getBio()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should update only bio when name is null")
    void testUpdateAuthor_OnlyBio_Success() {
        // Given
        String originalName = testAuthor.getName();
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = authorService.updateAuthor(1L, null, "New Bio");

        // Then
        assertTrue(result);
        assertEquals(originalName, testAuthor.getName()); // Should remain unchanged
        assertEquals("New Bio", testAuthor.getBio());
    }

    @Test
    @DisplayName("Should return false when no changes made")
    void testUpdateAuthor_NoChanges_ReturnsFalse() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When - update with same values
        boolean result = authorService.updateAuthor(1L, testAuthor.getName(), testAuthor.getBio());

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should trim whitespace from updated name")
    void testUpdateAuthor_TrimName_Success() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = authorService.updateAuthor(1L, "  Updated Name  ", null);

        // Then
        assertTrue(result);
        assertEquals("Updated Name", testAuthor.getName());
    }

    @Test
    @DisplayName("Should ignore empty name updates")
    void testUpdateAuthor_EmptyName_Ignored() {
        // Given
        String originalName = testAuthor.getName();
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = authorService.updateAuthor(1L, "   ", "New Bio");

        // Then
        assertTrue(result); // Should still return true because bio was updated
        assertEquals(originalName, testAuthor.getName()); // Name should remain unchanged
        assertEquals("New Bio", testAuthor.getBio());
    }

    // ===== DELETE AUTHOR TESTS =====

    @Test
    @DisplayName("Should delete author successfully")
    void testDeleteAuthor_Success() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        doNothing().when(authorRepository).delete(testAuthor);

        // When
        boolean result = authorService.deleteAuthor(1L);

        // Then
        assertTrue(result);
        verify(authorRepository).findByIdOptional(1L);
        verify(authorRepository).delete(testAuthor);
    }

    @Test
    @DisplayName("Should fail to delete author when ID is null")
    void testDeleteAuthor_NullId_Fails() {
        // When
        boolean result = authorService.deleteAuthor(null);

        // Then
        assertFalse(result);
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should fail to delete author when not found")
    void testDeleteAuthor_NotFound_Fails() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = authorService.deleteAuthor(1L);

        // Then
        assertFalse(result);
        verify(authorRepository).findByIdOptional(1L);
        verify(authorRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should fail to delete author with books")
    void testDeleteAuthor_WithBooks_Fails() {
        // Given - author has books
        testAuthor.getBooks().add(testBook);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = authorService.deleteAuthor(1L);

        // Then
        assertFalse(result);
        verify(authorRepository).findByIdOptional(1L);
        verify(authorRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete author with null books list")
    void testDeleteAuthor_NullBooks_Success() {
        // Given - author has null books list
        testAuthor.setBooks(null);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        doNothing().when(authorRepository).delete(testAuthor);

        // When
        boolean result = authorService.deleteAuthor(1L);

        // Then
        assertTrue(result);
        verify(authorRepository).delete(testAuthor);
    }

    @Test
    @DisplayName("Should delete author with empty books list")
    void testDeleteAuthor_EmptyBooks_Success() {
        // Given - author has empty books list (already set in setUp)
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        doNothing().when(authorRepository).delete(testAuthor);

        // When
        boolean result = authorService.deleteAuthor(1L);

        // Then
        assertTrue(result);
        verify(authorRepository).delete(testAuthor);
    }

    // ===== ADD BOOK TO AUTHOR TESTS =====

    @Test
    @DisplayName("Should add book to author successfully")
    void testAddBookToAuthor_Success() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));

        // When
        boolean result = authorService.addBookToAuthor(1L, 1L);

        // Then
        assertTrue(result);
        assertTrue(testAuthor.getBooks().contains(testBook));
        assertTrue(testBook.getAuthors().contains(testAuthor));
        
        verify(authorRepository).findByIdOptional(1L);
        verify(bookRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to add book when author ID is null")
    void testAddBookToAuthor_NullAuthorId_Fails() {
        // When
        boolean result = authorService.addBookToAuthor(null, 1L);

        // Then
        assertFalse(result);
        verifyNoInteractions(authorRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should fail to add book when book ID is null")
    void testAddBookToAuthor_NullBookId_Fails() {
        // When
        boolean result = authorService.addBookToAuthor(1L, null);

        // Then
        assertFalse(result);
        verifyNoInteractions(authorRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should fail to add book when author not found")
    void testAddBookToAuthor_AuthorNotFound_Fails() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = authorService.addBookToAuthor(1L, 1L);

        // Then
        assertFalse(result);
        verify(authorRepository).findByIdOptional(1L);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should fail to add book when book not found")
    void testAddBookToAuthor_BookNotFound_Fails() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = authorService.addBookToAuthor(1L, 1L);

        // Then
        assertFalse(result);
        verify(authorRepository).findByIdOptional(1L);
        verify(bookRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should fail to add book when already associated")
    void testAddBookToAuthor_AlreadyAssociated_Fails() {
        // Given - book already associated with author
        testAuthor.getBooks().add(testBook);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));

        // When
        boolean result = authorService.addBookToAuthor(1L, 1L);

        // Then
        assertFalse(result);
        assertEquals(1, testAuthor.getBooks().size()); // Should remain unchanged
    }

    // ===== REMOVE BOOK FROM AUTHOR TESTS =====

    @Test
    @DisplayName("Should remove book from author successfully")
    void testRemoveBookFromAuthor_Success() {
        // Given - book already associated with author
        testAuthor.getBooks().add(testBook);
        testBook.getAuthors().add(testAuthor);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));

        // When
        boolean result = authorService.removeBookFromAuthor(1L, 1L);

        // Then
        assertTrue(result);
        assertFalse(testAuthor.getBooks().contains(testBook));
        assertFalse(testBook.getAuthors().contains(testAuthor));
    }

    @Test
    @DisplayName("Should fail to remove book when not associated")
    void testRemoveBookFromAuthor_NotAssociated_Fails() {
        // Given - book not associated with author
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));

        // When
        boolean result = authorService.removeBookFromAuthor(1L, 1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to remove book when author ID is null")
    void testRemoveBookFromAuthor_NullAuthorId_Fails() {
        // When
        boolean result = authorService.removeBookFromAuthor(null, 1L);

        // Then
        assertFalse(result);
        verifyNoInteractions(authorRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should fail to remove book when book ID is null")
    void testRemoveBookFromAuthor_NullBookId_Fails() {
        // When
        boolean result = authorService.removeBookFromAuthor(1L, null);

        // Then
        assertFalse(result);
        verifyNoInteractions(authorRepository);
        verifyNoInteractions(bookRepository);
    }

    // ===== GET BOOKS BY AUTHOR TESTS =====

    @Test
    @DisplayName("Should get books by author successfully")
    void testGetBooksByAuthor_Success() {
        // Given
        testAuthor.getBooks().add(testBook);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        List<Book> result = authorService.getBooksByAuthor(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testBook));
    }

    @Test
    @DisplayName("Should return empty list when author has no books")
    void testGetBooksByAuthor_NoBooks_EmptyList() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When
        List<Book> result = authorService.getBooksByAuthor(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when author not found")
    void testGetBooksByAuthor_AuthorNotFound_EmptyList() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        List<Book> result = authorService.getBooksByAuthor(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when author ID is null")
    void testGetBooksByAuthor_NullId_EmptyList() {
        // When
        List<Book> result = authorService.getBooksByAuthor(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(authorRepository);
    }

    // ===== SEARCH AUTHORS BY NAME TESTS =====

    @Test
    @DisplayName("Should search authors by name successfully")
    void testSearchAuthorsByName_Success() {
        // Given
        List<Author> authors = List.of(testAuthor);
        @SuppressWarnings("unchecked")
        PanacheQuery<Author> mockQuery = mock(PanacheQuery.class);
        when(authorRepository.find("name LIKE ?1", "%Test%")).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(authors);

        // When
        List<Author> result = authorService.searchAuthorsByName("Test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAuthor, result.get(0));
    }

    @Test
    @DisplayName("Should return empty list when search term is null")
    void testSearchAuthorsByName_NullTerm_EmptyList() {
        // When
        List<Author> result = authorService.searchAuthorsByName(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should return empty list when search term is empty")
    void testSearchAuthorsByName_EmptyTerm_EmptyList() {
        // When
        List<Author> result = authorService.searchAuthorsByName("   ");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should trim search term")
    void testSearchAuthorsByName_TrimTerm_Success() {
        // Given
        List<Author> authors = List.of(testAuthor);
        @SuppressWarnings("unchecked")
        PanacheQuery<Author> mockQuery = mock(PanacheQuery.class);
        when(authorRepository.find("name LIKE ?1", "%Test%")).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(authors);

        // When
        List<Author> result = authorService.searchAuthorsByName("  Test  ");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(authorRepository, times(1)).find("name LIKE ?1", "%Test%");
    }

    // ===== GET AUTHOR STATISTICS TESTS =====

    @Test
    @DisplayName("Should get author statistics successfully")
    void testGetAuthorStatistics_Success() {
        // Given
        testAuthor.getBooks().add(testBook);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When & Then - this method prints to console, so we just verify it doesn't throw
        assertDoesNotThrow(() -> authorService.getAuthorStatistics(1L));
        
        verify(authorRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should handle author statistics when ID is null")
    void testGetAuthorStatistics_NullId() {
        // When & Then - should not throw, just print error
        assertDoesNotThrow(() -> authorService.getAuthorStatistics(null));
        
        verifyNoInteractions(authorRepository);
    }

    @Test
    @DisplayName("Should handle author statistics when author not found")
    void testGetAuthorStatistics_AuthorNotFound() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When & Then - should not throw, just print error
        assertDoesNotThrow(() -> authorService.getAuthorStatistics(1L));
        
        verify(authorRepository).findByIdOptional(1L);
    }

    @Test
    @DisplayName("Should handle author with null bio in statistics")
    void testGetAuthorStatistics_NullBio() {
        // Given
        testAuthor.setBio(null);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When & Then - should not throw
        assertDoesNotThrow(() -> authorService.getAuthorStatistics(1L));
    }

    @Test
    @DisplayName("Should handle author with null books in statistics")
    void testGetAuthorStatistics_NullBooks() {
        // Given
        testAuthor.setBooks(null);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When & Then - should not throw
        assertDoesNotThrow(() -> authorService.getAuthorStatistics(1L));
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle multiple books on same author")
    void testAddMultipleBooksToAuthor_Success() {
        // Given
        Book secondBook = new Book();
        secondBook.setId(2L);
        secondBook.setTitle("Second Book");
        secondBook.setAuthors(new ArrayList<>());
        
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findByIdOptional(2L)).thenReturn(Optional.of(secondBook));

        // When
        boolean result1 = authorService.addBookToAuthor(1L, 1L);
        boolean result2 = authorService.addBookToAuthor(1L, 2L);

        // Then
        assertTrue(result1);
        assertTrue(result2);
        assertEquals(2, testAuthor.getBooks().size());
        assertTrue(testAuthor.getBooks().contains(testBook));
        assertTrue(testAuthor.getBooks().contains(secondBook));
    }

    @Test
    @DisplayName("Should handle author creation with very long bio")
    void testCreateAuthor_LongBio_Success() {
        // Given
        String longBio = "A".repeat(1000); // Very long bio
        when(authorRepository.findByName("Author with Long Bio")).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(1L);
            return null;
        }).when(authorRepository).persist(any(Author.class));

        // When
        Author result = authorService.createAuthor("Author with Long Bio", longBio);

        // Then
        assertNotNull(result);
        assertEquals("Author with Long Bio", result.getName());
        assertEquals(longBio, result.getBio());
    }

    @Test
    @DisplayName("Should handle updating author with same name")
    void testUpdateAuthor_SameName_NoChanges() {
        // Given
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAuthor));

        // When - update with same name and bio
        boolean result = authorService.updateAuthor(1L, testAuthor.getName(), testAuthor.getBio());

        // Then
        assertFalse(result); // No changes made
    }
} 