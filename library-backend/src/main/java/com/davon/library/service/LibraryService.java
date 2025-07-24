package com.davon.library.service;

import com.davon.library.model.Library;
import com.davon.library.model.Book;
import com.davon.library.model.Member;
import com.davon.library.model.Inventory;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.LibraryRepository;
import com.davon.library.repository.InventoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LibraryService {
    
    @Inject
    BookRepository bookRepository;
    
    @Inject
    MemberRepository memberRepository;
    
    @Inject
    LibraryRepository libraryRepository;
    
    @Inject
    InventoryRepository inventoryRepository;
    
    @Inject
    InventoryService inventoryService;
    
    @Inject
    MemberService memberService;
    
    /**
     * Add a book to library inventory (implements Library.addBook())
     */
    @Transactional
    public boolean addBookToLibrary(Library library, Book book, int copies) {
        if (library == null || book == null || copies <= 0) {
            System.out.println("Invalid parameters for adding book");
            return false;
        }
        
        // Ensure book is persisted first
        if (book.getId() == null) {
            bookRepository.persist(book);
        }
        
        // Check if inventory already exists for this book in this library
        Optional<Inventory> existingInventory = library.getInventories().stream()
            .filter(inv -> inv.getBook() != null && inv.getBook().getId().equals(book.getId()))
            .findFirst();
        
        if (existingInventory.isPresent()) {
            // Add copies to existing inventory
            Inventory inventory = existingInventory.get();
            inventoryService.addCopy(inventory.getId(), copies);
            System.out.println("Added " + copies + " copies of '" + book.getTitle() + "' to existing inventory in " + library.getName());
        } else {
            // Create new inventory for this book
            Inventory newInventory = new Inventory();
            newInventory.setBook(book);
            newInventory.setLibrary(library);
            newInventory.setTotalCopies(copies);
            newInventory.setAvailableCopies(copies);
            
            library.getInventories().add(newInventory);
            System.out.println("Added new book '" + book.getTitle() + "' with " + copies + " copies to " + library.getName());
        }
        
        return true;
    }
    
    /**
     * Remove book from library inventory (implements Library.removeBook())
     */
    @Transactional
    public boolean removeBookFromLibrary(Library library, Book book, int copies) {
        if (library == null || book == null || copies <= 0) {
            System.out.println("Invalid parameters for removing book");
            return false;
        }
        
        Optional<Inventory> inventoryOpt = library.getInventories().stream()
            .filter(inv -> inv.getBook() != null && inv.getBook().getId().equals(book.getId()))
            .findFirst();
        
        if (inventoryOpt.isEmpty()) {
            System.out.println("Book '" + book.getTitle() + "' not found in " + library.getName() + " inventory");
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        
        if (inventoryService.removeCopy(inventory.getId(), copies)) {
            // If all copies are removed, remove the inventory entry
            if (inventory.getTotalCopies() == 0) {
                library.getInventories().remove(inventory);
                System.out.println("All copies of '" + book.getTitle() + "' removed from " + library.getName());
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Register member to library (implements Library.registerMember())
     */
    @Transactional
    public boolean registerMemberToLibrary(Library library, Member member) {
        if (library == null || member == null) {
            System.out.println("Library and member cannot be null");
            return false;
        }
        
        // Check if member is already registered in this library
        Optional<Member> existingMember = memberRepository.findByEmail(member.getEmail());
        if (existingMember.isPresent() && existingMember.get().getLibrary() != null) {
            System.out.println("Member with email '" + member.getEmail() + "' is already registered");
            return false;
        }
        
        // Set library relationship
        member.setLibrary(library);
        
        // Persist member if not already persisted
        if (member.getId() == null) {
            memberRepository.persist(member);
        }
        
        // Add to library's member list
        library.getMembers().add(member);
        
        System.out.println("Member '" + member.getName() + "' registered to library '" + library.getName() + "'");
        return true;
    }
    
    /**
     * Search books in library
     */
    public List<Book> searchBooksInLibrary(Library library, String searchTerm) {
        if (library == null || searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        
        // Get all books from library inventory that match search term
        return library.getInventories().stream()
            .map(Inventory::getBook)
            .filter(book -> 
                (book.getTitle() != null && book.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) ||
                (book.getAuthors() != null && book.getAuthors().stream()
                    .anyMatch(author -> author.getName() != null && 
                             author.getName().toLowerCase().contains(searchTerm.toLowerCase())))
            )
            .toList();
    }
    
    /**
     * Get available books in library
     */
    public List<Book> getAvailableBooksInLibrary(Library library) {
        if (library == null) {
            return List.of();
        }
        
        return library.getInventories().stream()
            .filter(inventory -> inventory.getAvailableCopies() > 0)
            .map(Inventory::getBook)
            .toList();
    }
    
    /**
     * Get books by genre in library
     */
    public List<Book> getBooksByGenreInLibrary(Library library, String genre) {
        if (library == null || genre == null || genre.trim().isEmpty()) {
            return List.of();
        }
        
        return library.getInventories().stream()
            .map(Inventory::getBook)
            .filter(book -> book.getGenre() != null && book.getGenre().equalsIgnoreCase(genre.trim()))
            .toList();
    }
    
    /**
     * Find member by email in library
     */
    public Optional<Member> findMemberInLibrary(Library library, String email) {
        if (library == null || email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return library.getMembers().stream()
            .filter(m -> m.getEmail() != null && m.getEmail().equalsIgnoreCase(email.trim()))
            .findFirst();
    }
    
    /**
     * Find member by membership number in library
     */
    public Optional<Member> findMemberByNumberInLibrary(Library library, String membershipNumber) {
        if (library == null || membershipNumber == null || membershipNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return library.getMembers().stream()
            .filter(m -> m.getMembershipNumber() != null && m.getMembershipNumber().equals(membershipNumber))
            .findFirst();
    }
    
    /**
     * Get library statistics
     */
    public void getLibraryStatistics(Library library) {
        if (library == null) {
            System.out.println("Library cannot be null");
            return;
        }
        
        int totalBooks = library.getInventories().stream()
            .mapToInt(Inventory::getTotalCopies)
            .sum();
        
        int availableBooks = library.getInventories().stream()
            .mapToInt(Inventory::getAvailableCopies)
            .sum();
        
        int borrowedBooks = library.getInventories().stream()
            .mapToInt(inventory -> inventory.getTotalCopies() - inventory.getAvailableCopies())
            .sum();
        
        long activeMembers = library.getMembers().stream()
            .filter(member -> {
                // Check if membership is active (not expired)
                return member.getMembershipEnd() != null && 
                       member.getMembershipEnd().after(new java.util.Date());
            })
            .count();
        
        System.out.println("=== " + library.getName() + " Statistics ===");
        System.out.println("Total Book Copies: " + totalBooks);
        System.out.println("Available Copies: " + availableBooks);
        System.out.println("Borrowed Copies: " + borrowedBooks);
        System.out.println("Unique Book Titles: " + library.getInventories().size());
        System.out.println("Total Members: " + library.getMembers().size());
        System.out.println("Active Members: " + activeMembers);
        System.out.println("Library Address: " + (library.getAddress() != null ? library.getAddress() : "Not specified"));
        System.out.println("Contact: " + (library.getPhone() != null ? library.getPhone() : "Not specified"));
        System.out.println("===============================");
    }
    
    /**
     * Check if library is operational
     */
    public boolean isLibraryOperational(Library library) {
        return library != null &&
               library.getName() != null && !library.getName().trim().isEmpty() &&
               library.getAddress() != null && !library.getAddress().trim().isEmpty();
    }
} 