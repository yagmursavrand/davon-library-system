package com.davon.library.service;

import com.davon.library.model.Inventory;
import com.davon.library.model.Book;
import com.davon.library.model.Library;
import com.davon.library.repository.InventoryRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.LibraryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;

/**
 * InventoryService - handles all inventory-related business operations
 */
@ApplicationScoped
public class InventoryService {
    
    @Inject
    InventoryRepository inventoryRepository;
    
    @Inject
    BookRepository bookRepository;
    
    @Inject
    LibraryRepository libraryRepository;
    
    /**
     * Add copies to inventory
     */
    @Transactional
    public boolean addCopy(Long inventoryId, int copies) {
        if (inventoryId == null || copies <= 0) {
            System.out.println("Invalid inventory ID or copy count");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            System.out.println("Inventory not found");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        inventory.setTotalCopies(inventory.getTotalCopies() + copies);
        inventory.setAvailableCopies(inventory.getAvailableCopies() + copies);
        
        System.out.println("Added " + copies + " copies. Total: " + inventory.getTotalCopies() + ", Available: " + inventory.getAvailableCopies());
        return true;
    }
    
    /**
     * Remove copies from inventory
     */
    @Transactional
    public boolean removeCopy(Long inventoryId, int copies) {
        if (inventoryId == null || copies <= 0) {
            System.out.println("Invalid inventory ID or copy count");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            System.out.println("Inventory not found");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        if (copies > inventory.getTotalCopies()) {
            System.out.println("Cannot remove more copies than total available");
            return false;
        }
        
        inventory.setTotalCopies(inventory.getTotalCopies() - copies);
        
        // Adjust available copies if necessary
        if (inventory.getAvailableCopies() > copies) {
            inventory.setAvailableCopies(inventory.getAvailableCopies() - copies);
        } else {
            inventory.setAvailableCopies(0);
        }
        
        System.out.println("Removed " + copies + " copies. Total: " + inventory.getTotalCopies() + ", Available: " + inventory.getAvailableCopies());
        return true;
    }
    
    /**
     * Borrow a copy
     */
    @Transactional
    public boolean borrowCopy(Long inventoryId) {
        if (inventoryId == null) {
            System.out.println("Inventory ID cannot be null");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            System.out.println("Inventory not found");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        if (inventory.getAvailableCopies() <= 0) {
            System.out.println("No copies available for borrowing");
            return false;
        }
        
        inventory.setAvailableCopies(inventory.getAvailableCopies() - 1);
        System.out.println("Copy borrowed. Available copies: " + inventory.getAvailableCopies());
        return true;
    }
    
    /**
     * Return a copy
     */
    @Transactional
    public boolean returnCopy(Long inventoryId) {
        if (inventoryId == null) {
            System.out.println("Inventory ID cannot be null");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            System.out.println("Inventory not found");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        if (inventory.getAvailableCopies() >= inventory.getTotalCopies()) {
            System.out.println("All copies are already available");
            return false;
        }
        
        inventory.setAvailableCopies(inventory.getAvailableCopies() + 1);
        System.out.println("Copy returned. Available copies: " + inventory.getAvailableCopies());
        return true;
    }
    
    /**
     * Reserve a copy
     */
    @Transactional
    public boolean reserveCopy(Long inventoryId) {
        if (inventoryId == null) {
            System.out.println("Inventory ID cannot be null");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            System.out.println("Inventory not found");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        if (inventory.getAvailableCopies() <= 0) {
            System.out.println("No copies available for reservation");
            return false;
        }
        
        inventory.setAvailableCopies(inventory.getAvailableCopies() - 1);
        inventory.setReservedCopies(inventory.getReservedCopies() + 1);
        
        System.out.println("Copy reserved. Available: " + inventory.getAvailableCopies() + ", Reserved: " + inventory.getReservedCopies());
        return true;
    }
    
    /**
     * Cancel reservation
     */
    @Transactional
    public boolean cancelReservation(Long inventoryId) {
        if (inventoryId == null) {
            System.out.println("Inventory ID cannot be null");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            System.out.println("Inventory not found");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        if (inventory.getReservedCopies() <= 0) {
            System.out.println("No reserved copies to cancel");
            return false;
        }
        
        inventory.setReservedCopies(inventory.getReservedCopies() - 1);
        inventory.setAvailableCopies(inventory.getAvailableCopies() + 1);
        
        System.out.println("Reservation cancelled. Available: " + inventory.getAvailableCopies() + ", Reserved: " + inventory.getReservedCopies());
        return true;
    }
    
    /**
     * Mark copies as damaged
     */
    @Transactional
    public boolean markAsDamaged(Long inventoryId, int copies) {
        if (inventoryId == null || copies <= 0) {
            System.out.println("Invalid inventory ID or copy count");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            System.out.println("Inventory not found");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        if (copies > inventory.getAvailableCopies()) {
            System.out.println("Cannot mark more copies as damaged than available");
            return false;
        }
        
        inventory.setAvailableCopies(inventory.getAvailableCopies() - copies);
        inventory.setDamagedCopies(inventory.getDamagedCopies() + copies);
        
        System.out.println("Marked " + copies + " copies as damaged. Available: " + inventory.getAvailableCopies() + ", Damaged: " + inventory.getDamagedCopies());
        return true;
    }
    
    /**
     * Repair damaged copies
     */
    @Transactional
    public boolean repairDamagedCopy(Long inventoryId, int copies) {
        if (inventoryId == null || copies <= 0) {
            System.out.println("Invalid inventory ID or copy count");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            System.out.println("Inventory not found");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        if (copies > inventory.getDamagedCopies()) {
            System.out.println("Cannot repair more copies than damaged");
            return false;
        }
        
        inventory.setDamagedCopies(inventory.getDamagedCopies() - copies);
        inventory.setAvailableCopies(inventory.getAvailableCopies() + copies);
        
        System.out.println("Repaired " + copies + " copies. Available: " + inventory.getAvailableCopies() + ", Damaged: " + inventory.getDamagedCopies());
        return true;
    }
    
    /**
     * Create inventory for book in library
     */
    @Transactional
    public Inventory createInventory(Long bookId, Long libraryId, int initialCopies) {
        if (bookId == null || libraryId == null || initialCopies < 0) {
            System.out.println("Invalid parameters for inventory creation");
            return null;
        }
        
        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found");
            return null;
        }
        
        Optional<Library> libraryOpt = libraryRepository.findByIdOptional(libraryId);
        if (libraryOpt.isEmpty()) {
            System.out.println("Library not found");
            return null;
        }
        
        Inventory inventory = new Inventory();
        inventory.setBook(bookOpt.get());
        inventory.setLibrary(libraryOpt.get());
        inventory.setTotalCopies(initialCopies);
        inventory.setAvailableCopies(initialCopies);
        inventory.setReservedCopies(0);
        inventory.setDamagedCopies(0);
        
        inventoryRepository.persist(inventory);
        
        System.out.println("Inventory created for book: " + bookOpt.get().getTitle() + " with " + initialCopies + " copies");
        return inventory;
    }
    
    // Utility methods
    public int getBorrowedCopies(Long inventoryId) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            return 0;
        }
        
        Inventory inventory = inventoryOpt.get();
        return inventory.getTotalCopies() - inventory.getAvailableCopies() - inventory.getReservedCopies() - inventory.getDamagedCopies();
    }
    
    public boolean isAvailable(Long inventoryId) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            return false;
        }
        
        return inventoryOpt.get().getAvailableCopies() > 0;
    }
    
    public double getAvailabilityRate(Long inventoryId) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
        if (inventoryOpt.isEmpty()) {
            return 0.0;
        }
        
        Inventory inventory = inventoryOpt.get();
        if (inventory.getTotalCopies() == 0) {
            return 0.0;
        }
        
        return (double) inventory.getAvailableCopies() / inventory.getTotalCopies() * 100;
    }
} 
 