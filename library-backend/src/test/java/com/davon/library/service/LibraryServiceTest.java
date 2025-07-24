package com.davon.library.service;

import com.davon.library.model.Library;
import com.davon.library.model.Book;
import com.davon.library.model.Member;
import com.davon.library.model.Inventory;
import com.davon.library.model.Author;
import com.davon.library.repository.LibraryRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.InventoryRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("LibraryService Unit Tests")
class LibraryServiceTest {

    @Inject
    LibraryService libraryService;

    @InjectMock
    LibraryRepository libraryRepository;

    @InjectMock
    BookRepository bookRepository;

    @InjectMock
    MemberRepository memberRepository;

    @InjectMock
    InventoryRepository inventoryRepository;

    @InjectMock
    InventoryService inventoryService;

    @InjectMock
    MemberService memberService;

    private Library testLibrary;
    private Book testBook;
    private Member testMember;
    private Inventory testInventory;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        // Create test library
        testLibrary = new Library();
        testLibrary.setId(1L);
        testLibrary.setName("Central Library");
        testLibrary.setAddress("123 Main St, City, State 12345");
        testLibrary.setPhone("555-0123");
        testLibrary.setEmail("central@library.com");
        testLibrary.setOpeningHours("Mon-Fri: 9AM-8PM, Sat-Sun: 10AM-6PM");
        testLibrary.setInventories(new ArrayList<>());
        testLibrary.setMembers(new ArrayList<>());

        // Create test author
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("Test Author");
        testAuthor.setBio("A test author");

        // Create test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setPublicationYear(2020);
        testBook.setGenre("Fiction");
        testBook.setStatus(Book.BookStatus.AVAILABLE);
        testBook.setAuthors(Arrays.asList(testAuthor));

        // Create test member
        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("Test Member");
        testMember.setEmail("test@member.com");
        testMember.setPassword("password123");
        testMember.setRole("MEMBER");
        testMember.setMembershipNumber("MEM123456");
        testMember.setMembershipStart(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        testMember.setMembershipEnd(cal.getTime());

        // Create test inventory
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setBook(testBook);
        testInventory.setLibrary(testLibrary);
        testInventory.setTotalCopies(5);
        testInventory.setAvailableCopies(3);
        testInventory.setReservedCopies(1);
        testInventory.setDamagedCopies(1);
    }

    // ===== ADD BOOK TO LIBRARY TESTS =====

    @Test
    @DisplayName("Should add book to library successfully with new inventory")
    void testAddBookToLibrary_NewInventory_Success() {
        // Given
        int copies = 3;
        when(inventoryService.addCopy(anyLong(), eq(copies))).thenReturn(true);
        doNothing().when(bookRepository).persist(testBook);

        // When
        boolean result = libraryService.addBookToLibrary(testLibrary, testBook, copies);

        // Then
        assertTrue(result);
        assertEquals(1, testLibrary.getInventories().size());
        
        Inventory addedInventory = testLibrary.getInventories().get(0);
        assertEquals(testBook, addedInventory.getBook());
        assertEquals(testLibrary, addedInventory.getLibrary());
        assertEquals(copies, addedInventory.getTotalCopies());
        assertEquals(copies, addedInventory.getAvailableCopies());
    }

    @Test
    @DisplayName("Should add copies to existing inventory")
    void testAddBookToLibrary_ExistingInventory_Success() {
        // Given
        int newCopies = 2;
        testLibrary.getInventories().add(testInventory);
        when(inventoryService.addCopy(testInventory.getId(), newCopies)).thenReturn(true);

        // When
        boolean result = libraryService.addBookToLibrary(testLibrary, testBook, newCopies);

        // Then
        assertTrue(result);
        verify(inventoryService).addCopy(testInventory.getId(), newCopies);
    }

    @Test
    @DisplayName("Should fail to add book when library is null")
    void testAddBookToLibrary_NullLibrary() {
        // When
        boolean result = libraryService.addBookToLibrary(null, testBook, 3);

        // Then
        assertFalse(result);
        verify(bookRepository, never()).persist(any(Book.class));
        verify(inventoryService, never()).addCopy(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should fail to add book when book is null")
    void testAddBookToLibrary_NullBook() {
        // When
        boolean result = libraryService.addBookToLibrary(testLibrary, null, 3);

        // Then
        assertFalse(result);
        verify(bookRepository, never()).persist(any(Book.class));
        verify(inventoryService, never()).addCopy(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should fail to add book when copies is zero or negative")
    void testAddBookToLibrary_InvalidCopies() {
        // When
        boolean result1 = libraryService.addBookToLibrary(testLibrary, testBook, 0);
        boolean result2 = libraryService.addBookToLibrary(testLibrary, testBook, -1);

        // Then
        assertFalse(result1);
        assertFalse(result2);
        verify(bookRepository, never()).persist(any(Book.class));
        verify(inventoryService, never()).addCopy(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should persist book when ID is null")
    void testAddBookToLibrary_PersistNewBook() {
        // Given
        testBook.setId(null);
        int copies = 3;
        doNothing().when(bookRepository).persist(testBook);

        // When
        boolean result = libraryService.addBookToLibrary(testLibrary, testBook, copies);

        // Then
        assertTrue(result);
        verify(bookRepository).persist(testBook);
    }

    // ===== REMOVE BOOK FROM LIBRARY TESTS =====

    @Test
    @DisplayName("Should remove book copies successfully")
    void testRemoveBookFromLibrary_Success() {
        // Given
        int copiesToRemove = 2;
        testLibrary.getInventories().add(testInventory);
        when(inventoryService.removeCopy(testInventory.getId(), copiesToRemove)).thenReturn(true);

        // When
        boolean result = libraryService.removeBookFromLibrary(testLibrary, testBook, copiesToRemove);

        // Then
        assertTrue(result);
        verify(inventoryService).removeCopy(testInventory.getId(), copiesToRemove);
    }

    @Test
    @DisplayName("Should remove inventory when all copies are removed")
    void testRemoveBookFromLibrary_RemoveInventory() {
        // Given
        int copiesToRemove = 5;
        testInventory.setTotalCopies(0); // Simulate all copies removed
        testLibrary.getInventories().add(testInventory);
        when(inventoryService.removeCopy(testInventory.getId(), copiesToRemove)).thenReturn(true);

        // When
        boolean result = libraryService.removeBookFromLibrary(testLibrary, testBook, copiesToRemove);

        // Then
        assertTrue(result);
        assertFalse(testLibrary.getInventories().contains(testInventory));
        verify(inventoryService).removeCopy(testInventory.getId(), copiesToRemove);
    }

    @Test
    @DisplayName("Should fail to remove book when library is null")
    void testRemoveBookFromLibrary_NullLibrary() {
        // When
        boolean result = libraryService.removeBookFromLibrary(null, testBook, 2);

        // Then
        assertFalse(result);
        verify(inventoryService, never()).removeCopy(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should fail to remove book when book is null")
    void testRemoveBookFromLibrary_NullBook() {
        // When
        boolean result = libraryService.removeBookFromLibrary(testLibrary, null, 2);

        // Then
        assertFalse(result);
        verify(inventoryService, never()).removeCopy(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should fail to remove book when copies is zero or negative")
    void testRemoveBookFromLibrary_InvalidCopies() {
        // When
        boolean result1 = libraryService.removeBookFromLibrary(testLibrary, testBook, 0);
        boolean result2 = libraryService.removeBookFromLibrary(testLibrary, testBook, -1);

        // Then
        assertFalse(result1);
        assertFalse(result2);
        verify(inventoryService, never()).removeCopy(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should fail to remove book when book not found in inventory")
    void testRemoveBookFromLibrary_BookNotFound() {
        // Given
        testLibrary.getInventories().clear(); // No inventories

        // When
        boolean result = libraryService.removeBookFromLibrary(testLibrary, testBook, 2);

        // Then
        assertFalse(result);
        verify(inventoryService, never()).removeCopy(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should fail when inventory service fails to remove copies")
    void testRemoveBookFromLibrary_InventoryServiceFails() {
        // Given
        int copiesToRemove = 2;
        testLibrary.getInventories().add(testInventory);
        when(inventoryService.removeCopy(testInventory.getId(), copiesToRemove)).thenReturn(false);

        // When
        boolean result = libraryService.removeBookFromLibrary(testLibrary, testBook, copiesToRemove);

        // Then
        assertFalse(result);
        verify(inventoryService).removeCopy(testInventory.getId(), copiesToRemove);
    }

    // ===== REGISTER MEMBER TO LIBRARY TESTS =====

    @Test
    @DisplayName("Should register member to library successfully")
    void testRegisterMemberToLibrary_Success() {
        // Given
        when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(Optional.empty());
        doNothing().when(memberRepository).persist(testMember);

        // When
        boolean result = libraryService.registerMemberToLibrary(testLibrary, testMember);

        // Then
        assertTrue(result);
        assertNotNull(testMember.getLibrary());
        assertEquals(1, testLibrary.getMembers().size());
        verify(memberRepository).findByEmail(testMember.getEmail());
        verify(memberRepository).persist(testMember);
    }

    @Test
    @DisplayName("Should fail to register member when library is null")
    void testRegisterMemberToLibrary_NullLibrary() {
        // When
        boolean result = libraryService.registerMemberToLibrary(null, testMember);

        // Then
        assertFalse(result);
        verify(memberRepository, never()).findByEmail(anyString());
        verify(memberRepository, never()).persist(any(Member.class));
    }

    @Test
    @DisplayName("Should fail to register member when member is null")
    void testRegisterMemberToLibrary_NullMember() {
        // When
        boolean result = libraryService.registerMemberToLibrary(testLibrary, null);

        // Then
        assertFalse(result);
        verify(memberRepository, never()).findByEmail(anyString());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("Should fail to register member when email already exists")
    void testRegisterMemberToLibrary_EmailExists() {
        // Given
        Member existingMember = new Member();
        existingMember.setEmail(testMember.getEmail());
        existingMember.setLibrary(testLibrary);
        when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(Optional.of(existingMember));

        // When
        boolean result = libraryService.registerMemberToLibrary(testLibrary, testMember);

        // Then
        assertFalse(result);
        verify(memberRepository).findByEmail(testMember.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("Should not persist member when ID is already set")
    void testRegisterMemberToLibrary_ExistingMember() {
        // Given
        testMember.setId(1L); // Member already has ID
        when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(Optional.empty());

        // When
        boolean result = libraryService.registerMemberToLibrary(testLibrary, testMember);

        // Then
        assertTrue(result);
        assertNotNull(testMember.getLibrary());
        assertEquals(1, testLibrary.getMembers().size());
        verify(memberRepository).findByEmail(testMember.getEmail());
        verify(memberRepository, never()).persist(testMember); // Should not persist existing member
    }

    // ===== SEARCH BOOKS IN LIBRARY TESTS =====

    @Test
    @DisplayName("Should search books by title successfully")
    void testSearchBooksInLibrary_ByTitle() {
        // Given
        testLibrary.getInventories().add(testInventory);
        String searchTerm = "test";

        // When
        List<Book> result = libraryService.searchBooksInLibrary(testLibrary, searchTerm);

        // Then
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
    }

    @Test
    @DisplayName("Should search books by author name successfully")
    void testSearchBooksInLibrary_ByAuthor() {
        // Given
        testLibrary.getInventories().add(testInventory);
        String searchTerm = "author";

        // When
        List<Book> result = libraryService.searchBooksInLibrary(testLibrary, searchTerm);

        // Then
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
    }

    @Test
    @DisplayName("Should return empty list when library is null")
    void testSearchBooksInLibrary_NullLibrary() {
        // When
        List<Book> result = libraryService.searchBooksInLibrary(null, "test");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when search term is null")
    void testSearchBooksInLibrary_NullSearchTerm() {
        // When
        List<Book> result = libraryService.searchBooksInLibrary(testLibrary, null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when search term is empty")
    void testSearchBooksInLibrary_EmptySearchTerm() {
        // When
        List<Book> result = libraryService.searchBooksInLibrary(testLibrary, "   ");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when no books match search term")
    void testSearchBooksInLibrary_NoMatches() {
        // Given
        testLibrary.getInventories().add(testInventory);
        String searchTerm = "nonexistent";

        // When
        List<Book> result = libraryService.searchBooksInLibrary(testLibrary, searchTerm);

        // Then
        assertTrue(result.isEmpty());
    }

    // ===== GET AVAILABLE BOOKS IN LIBRARY TESTS =====

    @Test
    @DisplayName("Should get available books successfully")
    void testGetAvailableBooksInLibrary_Success() {
        // Given
        testLibrary.getInventories().add(testInventory); // Has 3 available copies

        // When
        List<Book> result = libraryService.getAvailableBooksInLibrary(testLibrary);

        // Then
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
    }

    @Test
    @DisplayName("Should return empty list when no books available")
    void testGetAvailableBooksInLibrary_NoAvailable() {
        // Given
        testInventory.setAvailableCopies(0);
        testLibrary.getInventories().add(testInventory);

        // When
        List<Book> result = libraryService.getAvailableBooksInLibrary(testLibrary);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when library is null")
    void testGetAvailableBooksInLibrary_NullLibrary() {
        // When
        List<Book> result = libraryService.getAvailableBooksInLibrary(null);

        // Then
        assertTrue(result.isEmpty());
    }

    // ===== GET BOOKS BY GENRE IN LIBRARY TESTS =====

    @Test
    @DisplayName("Should get books by genre successfully")
    void testGetBooksByGenreInLibrary_Success() {
        // Given
        testLibrary.getInventories().add(testInventory);
        String genre = "Fiction";

        // When
        List<Book> result = libraryService.getBooksByGenreInLibrary(testLibrary, genre);

        // Then
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
    }

    @Test
    @DisplayName("Should handle case insensitive genre search")
    void testGetBooksByGenreInLibrary_CaseInsensitive() {
        // Given
        testLibrary.getInventories().add(testInventory);
        String genre = "fiction"; // lowercase

        // When
        List<Book> result = libraryService.getBooksByGenreInLibrary(testLibrary, genre);

        // Then
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
    }

    @Test
    @DisplayName("Should return empty list when library is null")
    void testGetBooksByGenreInLibrary_NullLibrary() {
        // When
        List<Book> result = libraryService.getBooksByGenreInLibrary(null, "Fiction");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when genre is null")
    void testGetBooksByGenreInLibrary_NullGenre() {
        // When
        List<Book> result = libraryService.getBooksByGenreInLibrary(testLibrary, null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when genre is empty")
    void testGetBooksByGenreInLibrary_EmptyGenre() {
        // When
        List<Book> result = libraryService.getBooksByGenreInLibrary(testLibrary, "   ");

        // Then
        assertTrue(result.isEmpty());
    }

    // ===== FIND MEMBER IN LIBRARY TESTS =====

    @Test
    @DisplayName("Should find member by email successfully")
    void testFindMemberInLibrary_Success() {
        // Given
        testLibrary.getMembers().add(testMember);
        String email = testMember.getEmail();

        // When
        Optional<Member> result = libraryService.findMemberInLibrary(testLibrary, email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testMember, result.get());
    }

    @Test
    @DisplayName("Should handle case insensitive email search")
    void testFindMemberInLibrary_CaseInsensitive() {
        // Given
        testLibrary.getMembers().add(testMember);
        String email = testMember.getEmail().toUpperCase();

        // When
        Optional<Member> result = libraryService.findMemberInLibrary(testLibrary, email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testMember, result.get());
    }

    @Test
    @DisplayName("Should return empty when library is null")
    void testFindMemberInLibrary_NullLibrary() {
        // When
        Optional<Member> result = libraryService.findMemberInLibrary(null, "test@email.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when email is null")
    void testFindMemberInLibrary_NullEmail() {
        // When
        Optional<Member> result = libraryService.findMemberInLibrary(testLibrary, null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when email is empty")
    void testFindMemberInLibrary_EmptyEmail() {
        // When
        Optional<Member> result = libraryService.findMemberInLibrary(testLibrary, "   ");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when member not found")
    void testFindMemberInLibrary_NotFound() {
        // Given
        testLibrary.getMembers().add(testMember);

        // When
        Optional<Member> result = libraryService.findMemberInLibrary(testLibrary, "notfound@email.com");

        // Then
        assertFalse(result.isPresent());
    }

    // ===== FIND MEMBER BY MEMBERSHIP NUMBER TESTS =====

    @Test
    @DisplayName("Should find member by membership number successfully")
    void testFindMemberByNumberInLibrary_Success() {
        // Given
        testLibrary.getMembers().add(testMember);
        String membershipNumber = testMember.getMembershipNumber();

        // When
        Optional<Member> result = libraryService.findMemberByNumberInLibrary(testLibrary, membershipNumber);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testMember, result.get());
    }

    @Test
    @DisplayName("Should return empty when library is null")
    void testFindMemberByNumberInLibrary_NullLibrary() {
        // When
        Optional<Member> result = libraryService.findMemberByNumberInLibrary(null, "MEM123");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when membership number is null")
    void testFindMemberByNumberInLibrary_NullNumber() {
        // When
        Optional<Member> result = libraryService.findMemberByNumberInLibrary(testLibrary, null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when membership number is empty")
    void testFindMemberByNumberInLibrary_EmptyNumber() {
        // When
        Optional<Member> result = libraryService.findMemberByNumberInLibrary(testLibrary, "   ");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when member not found")
    void testFindMemberByNumberInLibrary_NotFound() {
        // Given
        testLibrary.getMembers().add(testMember);

        // When
        Optional<Member> result = libraryService.findMemberByNumberInLibrary(testLibrary, "NOTFOUND");

        // Then
        assertFalse(result.isPresent());
    }

    // ===== GET LIBRARY STATISTICS TESTS =====

    @Test
    @DisplayName("Should get library statistics successfully")
    void testGetLibraryStatistics_Success() {
        // Given
        testLibrary.getInventories().add(testInventory);
        testLibrary.getMembers().add(testMember);

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> libraryService.getLibraryStatistics(testLibrary));
    }

    @Test
    @DisplayName("Should handle null library in statistics")
    void testGetLibraryStatistics_NullLibrary() {
        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> libraryService.getLibraryStatistics(null));
    }

    @Test
    @DisplayName("Should handle library with no inventories or members")
    void testGetLibraryStatistics_EmptyLibrary() {
        // Given
        Library emptyLibrary = new Library();
        emptyLibrary.setName("Empty Library");
        emptyLibrary.setInventories(new ArrayList<>());
        emptyLibrary.setMembers(new ArrayList<>());

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> libraryService.getLibraryStatistics(emptyLibrary));
    }

    @Test
    @DisplayName("Should handle members with expired membership")
    void testGetLibraryStatistics_ExpiredMembers() {
        // Given
        Member expiredMember = new Member();
        expiredMember.setName("Expired Member");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1); // Set to past date
        expiredMember.setMembershipEnd(cal.getTime());
        
        testLibrary.getMembers().add(testMember); // Active member
        testLibrary.getMembers().add(expiredMember); // Expired member

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> libraryService.getLibraryStatistics(testLibrary));
    }

    // ===== IS LIBRARY OPERATIONAL TESTS =====

    @Test
    @DisplayName("Should return true when library is operational")
    void testIsLibraryOperational_True() {
        // When
        boolean result = libraryService.isLibraryOperational(testLibrary);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when library is null")
    void testIsLibraryOperational_NullLibrary() {
        // When
        boolean result = libraryService.isLibraryOperational(null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when library name is null")
    void testIsLibraryOperational_NullName() {
        // Given
        testLibrary.setName(null);

        // When
        boolean result = libraryService.isLibraryOperational(testLibrary);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when library name is empty")
    void testIsLibraryOperational_EmptyName() {
        // Given
        testLibrary.setName("   ");

        // When
        boolean result = libraryService.isLibraryOperational(testLibrary);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when library address is null")
    void testIsLibraryOperational_NullAddress() {
        // Given
        testLibrary.setAddress(null);

        // When
        boolean result = libraryService.isLibraryOperational(testLibrary);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when library address is empty")
    void testIsLibraryOperational_EmptyAddress() {
        // Given
        testLibrary.setAddress("   ");

        // When
        boolean result = libraryService.isLibraryOperational(testLibrary);

        // Then
        assertFalse(result);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle multiple books in inventory")
    void testMultipleBooksInInventory() {
        // Given
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Another Book");
        book2.setGenre("Science");

        Inventory inventory2 = new Inventory();
        inventory2.setId(2L);
        inventory2.setBook(book2);
        inventory2.setLibrary(testLibrary);
        inventory2.setTotalCopies(2);
        inventory2.setAvailableCopies(2);

        testLibrary.getInventories().add(testInventory);
        testLibrary.getInventories().add(inventory2);

        // When
        List<Book> allBooks = libraryService.getAvailableBooksInLibrary(testLibrary);
        List<Book> fictionBooks = libraryService.getBooksByGenreInLibrary(testLibrary, "Fiction");
        List<Book> scienceBooks = libraryService.getBooksByGenreInLibrary(testLibrary, "Science");

        // Then
        assertEquals(2, allBooks.size());
        assertEquals(1, fictionBooks.size());
        assertEquals(1, scienceBooks.size());
        assertEquals(testBook, fictionBooks.get(0));
        assertEquals(book2, scienceBooks.get(0));
    }

    @Test
    @DisplayName("Should handle multiple members in library")
    void testMultipleMembersInLibrary() {
        // Given
        Member member2 = new Member();
        member2.setId(2L);
        member2.setName("Another Member");
        member2.setEmail("another@member.com");
        member2.setMembershipNumber("MEM789012");

        testLibrary.getMembers().add(testMember);
        testLibrary.getMembers().add(member2);

        // When
        Optional<Member> foundByEmail1 = libraryService.findMemberInLibrary(testLibrary, testMember.getEmail());
        Optional<Member> foundByEmail2 = libraryService.findMemberInLibrary(testLibrary, member2.getEmail());
        Optional<Member> foundByNumber1 = libraryService.findMemberByNumberInLibrary(testLibrary, testMember.getMembershipNumber());
        Optional<Member> foundByNumber2 = libraryService.findMemberByNumberInLibrary(testLibrary, member2.getMembershipNumber());

        // Then
        assertTrue(foundByEmail1.isPresent());
        assertTrue(foundByEmail2.isPresent());
        assertTrue(foundByNumber1.isPresent());
        assertTrue(foundByNumber2.isPresent());
        assertEquals(testMember, foundByEmail1.get());
        assertEquals(member2, foundByEmail2.get());
        assertEquals(testMember, foundByNumber1.get());
        assertEquals(member2, foundByNumber2.get());
    }

    @Test
    @DisplayName("Should handle books with null or empty titles and authors")
    void testBooksWithNullData() {
        // Given
        Book bookWithNullTitle = new Book();
        bookWithNullTitle.setId(2L);
        bookWithNullTitle.setTitle(null);
        bookWithNullTitle.setAuthors(new ArrayList<>());

        Inventory inventoryWithNullTitle = new Inventory();
        inventoryWithNullTitle.setId(2L);
        inventoryWithNullTitle.setBook(bookWithNullTitle);
        inventoryWithNullTitle.setLibrary(testLibrary);
        inventoryWithNullTitle.setAvailableCopies(1);

        testLibrary.getInventories().add(testInventory);
        testLibrary.getInventories().add(inventoryWithNullTitle);

        // When
        List<Book> searchResult = libraryService.searchBooksInLibrary(testLibrary, "test");

        // Then
        assertEquals(1, searchResult.size()); // Only testBook should match
        assertEquals(testBook, searchResult.get(0));
    }

    @Test
    @DisplayName("Should handle members with null email or membership number")
    void testMembersWithNullData() {
        // Given
        Member memberWithNullEmail = new Member();
        memberWithNullEmail.setId(2L);
        memberWithNullEmail.setName("Null Email Member");
        memberWithNullEmail.setEmail(null);
        memberWithNullEmail.setMembershipNumber("MEM789012");

        Member memberWithNullNumber = new Member();
        memberWithNullNumber.setId(3L);
        memberWithNullNumber.setName("Null Number Member");
        memberWithNullNumber.setEmail("nullnumber@member.com");
        memberWithNullNumber.setMembershipNumber(null);

        testLibrary.getMembers().add(testMember);
        testLibrary.getMembers().add(memberWithNullEmail);
        testLibrary.getMembers().add(memberWithNullNumber);

        // When
        Optional<Member> foundByEmail = libraryService.findMemberInLibrary(testLibrary, testMember.getEmail());
        Optional<Member> foundByNumber = libraryService.findMemberByNumberInLibrary(testLibrary, testMember.getMembershipNumber());

        // Then
        assertTrue(foundByEmail.isPresent());
        assertTrue(foundByNumber.isPresent());
        assertEquals(testMember, foundByEmail.get());
        assertEquals(testMember, foundByNumber.get());
    }
} 