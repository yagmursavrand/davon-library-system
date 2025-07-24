package com.davon.library.service;

import com.davon.library.model.Inventory;
import com.davon.library.model.Book;
import com.davon.library.model.Library;
import com.davon.library.repository.InventoryRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.LibraryRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("InventoryService Unit Tests")
public class InventoryServiceTest {

    @Inject
    InventoryService inventoryService;

    @InjectMock
    InventoryRepository inventoryRepository;

    @InjectMock
    BookRepository bookRepository;

    @InjectMock
    LibraryRepository libraryRepository;

    private Inventory testInventory;
    private Book testBook;
    private Library testLibrary;

    @BeforeEach
    void setUp() {
        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");

        // Setup test library
        testLibrary = new Library();
        testLibrary.setId(1L);
        testLibrary.setName("Test Library");

        // Setup test inventory
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setBook(testBook);
        testInventory.setLibrary(testLibrary);
        testInventory.setTotalCopies(10);
        testInventory.setAvailableCopies(8);
        testInventory.setReservedCopies(1);
        testInventory.setDamagedCopies(1);
    }

    // ===== ADD COPY TESTS =====

    @Test
    @DisplayName("Should add copies to inventory successfully")
    void testAddCopy_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.addCopy(1L, 5);

        // Then
        assertTrue(result);
        assertEquals(15, testInventory.getTotalCopies());
        assertEquals(13, testInventory.getAvailableCopies());
    }

    @Test
    @DisplayName("Should fail to add copies when inventory ID is null")
    void testAddCopy_NullInventoryId() {
        // When
        boolean result = inventoryService.addCopy(null, 5);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to add copies when copy count is zero")
    void testAddCopy_ZeroCopies() {
        // When
        boolean result = inventoryService.addCopy(1L, 0);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to add copies when copy count is negative")
    void testAddCopy_NegativeCopies() {
        // When
        boolean result = inventoryService.addCopy(1L, -5);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to add copies when inventory not found")
    void testAddCopy_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.addCopy(1L, 5);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByIdOptional(1L);
    }

    // ===== REMOVE COPY TESTS =====

    @Test
    @DisplayName("Should remove copies from inventory successfully")
    void testRemoveCopy_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.removeCopy(1L, 3);

        // Then
        assertTrue(result);
        assertEquals(7, testInventory.getTotalCopies());
        assertEquals(5, testInventory.getAvailableCopies());
    }

    @Test
    @DisplayName("Should remove all available copies when removing more than available")
    void testRemoveCopy_RemoveMoreThanAvailable() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.removeCopy(1L, 9);

        // Then
        assertTrue(result);
        assertEquals(1, testInventory.getTotalCopies());
        assertEquals(0, testInventory.getAvailableCopies());
    }

    @Test
    @DisplayName("Should fail to remove copies when count exceeds total")
    void testRemoveCopy_ExceedsTotalCopies() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.removeCopy(1L, 15);

        // Then
        assertFalse(result);
        assertEquals(10, testInventory.getTotalCopies()); // Unchanged
        assertEquals(8, testInventory.getAvailableCopies()); // Unchanged
    }

    @Test
    @DisplayName("Should fail to remove copies when inventory ID is null")
    void testRemoveCopy_NullInventoryId() {
        // When
        boolean result = inventoryService.removeCopy(null, 3);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to remove copies when inventory not found")
    void testRemoveCopy_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.removeCopy(1L, 3);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByIdOptional(1L);
    }

    // ===== BORROW COPY TESTS =====

    @Test
    @DisplayName("Should borrow copy successfully")
    void testBorrowCopy_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.borrowCopy(1L);

        // Then
        assertTrue(result);
        assertEquals(7, testInventory.getAvailableCopies());
    }

    @Test
    @DisplayName("Should fail to borrow when no copies available")
    void testBorrowCopy_NoCopiesAvailable() {
        // Given
        testInventory.setAvailableCopies(0);
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.borrowCopy(1L);

        // Then
        assertFalse(result);
        assertEquals(0, testInventory.getAvailableCopies()); // Unchanged
    }

    @Test
    @DisplayName("Should fail to borrow when inventory ID is null")
    void testBorrowCopy_NullInventoryId() {
        // When
        boolean result = inventoryService.borrowCopy(null);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to borrow when inventory not found")
    void testBorrowCopy_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.borrowCopy(1L);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByIdOptional(1L);
    }

    // ===== RETURN COPY TESTS =====

    @Test
    @DisplayName("Should return copy successfully")
    void testReturnCopy_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.returnCopy(1L);

        // Then
        assertTrue(result);
        assertEquals(9, testInventory.getAvailableCopies());
    }

    @Test
    @DisplayName("Should fail to return when all copies are already available")
    void testReturnCopy_AllCopiesAvailable() {
        // Given
        testInventory.setAvailableCopies(10); // All copies available
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.returnCopy(1L);

        // Then
        assertFalse(result);
        assertEquals(10, testInventory.getAvailableCopies()); // Unchanged
    }

    @Test
    @DisplayName("Should fail to return when inventory ID is null")
    void testReturnCopy_NullInventoryId() {
        // When
        boolean result = inventoryService.returnCopy(null);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to return when inventory not found")
    void testReturnCopy_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.returnCopy(1L);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByIdOptional(1L);
    }

    // ===== RESERVE COPY TESTS =====

    @Test
    @DisplayName("Should reserve copy successfully")
    void testReserveCopy_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.reserveCopy(1L);

        // Then
        assertTrue(result);
        assertEquals(7, testInventory.getAvailableCopies());
        assertEquals(2, testInventory.getReservedCopies());
    }

    @Test
    @DisplayName("Should fail to reserve when no copies available")
    void testReserveCopy_NoCopiesAvailable() {
        // Given
        testInventory.setAvailableCopies(0);
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.reserveCopy(1L);

        // Then
        assertFalse(result);
        assertEquals(0, testInventory.getAvailableCopies()); // Unchanged
        assertEquals(1, testInventory.getReservedCopies()); // Unchanged
    }

    @Test
    @DisplayName("Should fail to reserve when inventory ID is null")
    void testReserveCopy_NullInventoryId() {
        // When
        boolean result = inventoryService.reserveCopy(null);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to reserve when inventory not found")
    void testReserveCopy_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.reserveCopy(1L);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByIdOptional(1L);
    }

    // ===== CANCEL RESERVATION TESTS =====

    @Test
    @DisplayName("Should cancel reservation successfully")
    void testCancelReservation_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.cancelReservation(1L);

        // Then
        assertTrue(result);
        assertEquals(9, testInventory.getAvailableCopies());
        assertEquals(0, testInventory.getReservedCopies());
    }

    @Test
    @DisplayName("Should fail to cancel reservation when no reserved copies")
    void testCancelReservation_NoReservedCopies() {
        // Given
        testInventory.setReservedCopies(0);
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.cancelReservation(1L);

        // Then
        assertFalse(result);
        assertEquals(8, testInventory.getAvailableCopies()); // Unchanged
        assertEquals(0, testInventory.getReservedCopies()); // Unchanged
    }

    @Test
    @DisplayName("Should fail to cancel reservation when inventory ID is null")
    void testCancelReservation_NullInventoryId() {
        // When
        boolean result = inventoryService.cancelReservation(null);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to cancel reservation when inventory not found")
    void testCancelReservation_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.cancelReservation(1L);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByIdOptional(1L);
    }

    // ===== MARK AS DAMAGED TESTS =====

    @Test
    @DisplayName("Should mark copies as damaged successfully")
    void testMarkAsDamaged_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.markAsDamaged(1L, 3);

        // Then
        assertTrue(result);
        assertEquals(5, testInventory.getAvailableCopies());
        assertEquals(4, testInventory.getDamagedCopies());
    }

    @Test
    @DisplayName("Should fail to mark as damaged when exceeding available copies")
    void testMarkAsDamaged_ExceedsAvailableCopies() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.markAsDamaged(1L, 10);

        // Then
        assertFalse(result);
        assertEquals(8, testInventory.getAvailableCopies()); // Unchanged
        assertEquals(1, testInventory.getDamagedCopies()); // Unchanged
    }

    @Test
    @DisplayName("Should fail to mark as damaged when inventory ID is null")
    void testMarkAsDamaged_NullInventoryId() {
        // When
        boolean result = inventoryService.markAsDamaged(null, 3);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to mark as damaged when copy count is zero")
    void testMarkAsDamaged_ZeroCopies() {
        // When
        boolean result = inventoryService.markAsDamaged(1L, 0);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to mark as damaged when inventory not found")
    void testMarkAsDamaged_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.markAsDamaged(1L, 3);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByIdOptional(1L);
    }

    // ===== REPAIR DAMAGED COPY TESTS =====

    @Test
    @DisplayName("Should repair damaged copies successfully")
    void testRepairDamagedCopy_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.repairDamagedCopy(1L, 1);

        // Then
        assertTrue(result);
        assertEquals(9, testInventory.getAvailableCopies());
        assertEquals(0, testInventory.getDamagedCopies());
    }

    @Test
    @DisplayName("Should fail to repair when exceeding damaged copies")
    void testRepairDamagedCopy_ExceedsDamagedCopies() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.repairDamagedCopy(1L, 5);

        // Then
        assertFalse(result);
        assertEquals(8, testInventory.getAvailableCopies()); // Unchanged
        assertEquals(1, testInventory.getDamagedCopies()); // Unchanged
    }

    @Test
    @DisplayName("Should fail to repair when inventory ID is null")
    void testRepairDamagedCopy_NullInventoryId() {
        // When
        boolean result = inventoryService.repairDamagedCopy(null, 1);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to repair when copy count is zero")
    void testRepairDamagedCopy_ZeroCopies() {
        // When
        boolean result = inventoryService.repairDamagedCopy(1L, 0);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).findByIdOptional(any());
    }

    @Test
    @DisplayName("Should fail to repair when inventory not found")
    void testRepairDamagedCopy_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.repairDamagedCopy(1L, 1);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByIdOptional(1L);
    }

    // ===== CREATE INVENTORY TESTS =====

    @Test
    @DisplayName("Should create inventory successfully")
    void testCreateInventory_Success() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(libraryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLibrary));

        // When
        Inventory result = inventoryService.createInventory(1L, 1L, 5);

        // Then
        assertNotNull(result);
        assertEquals(testBook, result.getBook());
        assertEquals(testLibrary, result.getLibrary());
        assertEquals(5, result.getTotalCopies());
        assertEquals(5, result.getAvailableCopies());
        assertEquals(0, result.getReservedCopies());
        assertEquals(0, result.getDamagedCopies());
        verify(inventoryRepository).persist(result);
    }

    @Test
    @DisplayName("Should fail to create inventory when book ID is null")
    void testCreateInventory_NullBookId() {
        // When
        Inventory result = inventoryService.createInventory(null, 1L, 5);

        // Then
        assertNull(result);
        verify(bookRepository, never()).findByIdOptional(any());
        verifyNoMoreInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Should fail to create inventory when library ID is null")
    void testCreateInventory_NullLibraryId() {
        // When
        Inventory result = inventoryService.createInventory(1L, null, 5);

        // Then
        assertNull(result);
        verify(bookRepository, never()).findByIdOptional(any());
        verifyNoMoreInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Should fail to create inventory when initial copies is negative")
    void testCreateInventory_NegativeInitialCopies() {
        // When
        Inventory result = inventoryService.createInventory(1L, 1L, -1);

        // Then
        assertNull(result);
        verify(bookRepository, never()).findByIdOptional(any());
        verifyNoMoreInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Should create inventory with zero initial copies")
    void testCreateInventory_ZeroInitialCopies() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(libraryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testLibrary));

        // When
        Inventory result = inventoryService.createInventory(1L, 1L, 0);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalCopies());
        assertEquals(0, result.getAvailableCopies());
        verify(inventoryRepository).persist(result);
    }

    @Test
    @DisplayName("Should fail to create inventory when book not found")
    void testCreateInventory_BookNotFound() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        Inventory result = inventoryService.createInventory(1L, 1L, 5);

        // Then
        assertNull(result);
        verify(bookRepository).findByIdOptional(1L);
        verify(libraryRepository, never()).findByIdOptional(any());
        verifyNoMoreInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Should fail to create inventory when library not found")
    void testCreateInventory_LibraryNotFound() {
        // Given
        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(testBook));
        when(libraryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        Inventory result = inventoryService.createInventory(1L, 1L, 5);

        // Then
        assertNull(result);
        verify(bookRepository).findByIdOptional(1L);
        verify(libraryRepository).findByIdOptional(1L);
        verifyNoMoreInteractions(inventoryRepository);
    }

    // ===== UTILITY METHOD TESTS =====

    @Test
    @DisplayName("Should calculate borrowed copies correctly")
    void testGetBorrowedCopies_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        int borrowedCopies = inventoryService.getBorrowedCopies(1L);

        // Then
        assertEquals(0, borrowedCopies); // 10 total - 8 available - 1 reserved - 1 damaged = 0
    }

    @Test
    @DisplayName("Should return zero borrowed copies when inventory not found")
    void testGetBorrowedCopies_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        int borrowedCopies = inventoryService.getBorrowedCopies(1L);

        // Then
        assertEquals(0, borrowedCopies);
    }

    @Test
    @DisplayName("Should check availability correctly when copies available")
    void testIsAvailable_True() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean isAvailable = inventoryService.isAvailable(1L);

        // Then
        assertTrue(isAvailable);
    }

    @Test
    @DisplayName("Should check availability correctly when no copies available")
    void testIsAvailable_False() {
        // Given
        testInventory.setAvailableCopies(0);
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean isAvailable = inventoryService.isAvailable(1L);

        // Then
        assertFalse(isAvailable);
    }

    @Test
    @DisplayName("Should return false for availability when inventory not found")
    void testIsAvailable_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        boolean isAvailable = inventoryService.isAvailable(1L);

        // Then
        assertFalse(isAvailable);
    }

    @Test
    @DisplayName("Should calculate availability rate correctly")
    void testGetAvailabilityRate_Success() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        double availabilityRate = inventoryService.getAvailabilityRate(1L);

        // Then
        assertEquals(80.0, availabilityRate, 0.01); // 8/10 * 100 = 80%
    }

    @Test
    @DisplayName("Should return zero availability rate when no total copies")
    void testGetAvailabilityRate_NoTotalCopies() {
        // Given
        testInventory.setTotalCopies(0);
        testInventory.setAvailableCopies(0);
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(testInventory));

        // When
        double availabilityRate = inventoryService.getAvailabilityRate(1L);

        // Then
        assertEquals(0.0, availabilityRate, 0.01);
    }

    @Test
    @DisplayName("Should return zero availability rate when inventory not found")
    void testGetAvailabilityRate_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        // When
        double availabilityRate = inventoryService.getAvailabilityRate(1L);

        // Then
        assertEquals(0.0, availabilityRate, 0.01);
    }
} 