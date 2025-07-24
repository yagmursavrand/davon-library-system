package com.davon.library.service;

import com.davon.library.model.Book;
import com.davon.library.model.Author;
import com.davon.library.model.Loan;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.AuthorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;

/**
 * BookService - handles all book-related business operations
 */
@ApplicationScoped
public class BookService {
    
    @Inject
    BookRepository bookRepository;
    
    @Inject
    AuthorRepository authorRepository;
    
    /**
     * Add author to book
     */
    @Transactional
    public boolean addAuthorToBook(Long bookId, Long authorId) {
        if (bookId == null || authorId == null) {
            System.out.println("Book ID and Author ID cannot be null");
            return false;
        }
        
        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found with ID: " + bookId);
            return false;
        }
        
        Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
        if (authorOpt.isEmpty()) {
            System.out.println("Author not found with ID: " + authorId);
            return false;
        }
        
        Book book = bookOpt.get();
        Author author = authorOpt.get();
        
        if (book.getAuthors().contains(author)) {
            System.out.println("Author is already associated with this book");
            return false;
        }
        
        book.getAuthors().add(author);
        author.getBooks().add(book);
        
        System.out.println("Author '" + author.getName() + "' added to book '" + book.getTitle() + "'");
        return true;
    }
    
    /**
     * Remove author from book
     */
    @Transactional
    public boolean removeAuthorFromBook(Long bookId, Long authorId) {
        if (bookId == null || authorId == null) {
            System.out.println("Book ID and Author ID cannot be null");
            return false;
        }
        
        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found with ID: " + bookId);
            return false;
        }
        
        Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
        if (authorOpt.isEmpty()) {
            System.out.println("Author not found with ID: " + authorId);
            return false;
        }
        
        Book book = bookOpt.get();
        Author author = authorOpt.get();
        
        if (!book.getAuthors().contains(author)) {
            System.out.println("Author is not associated with this book");
            return false;
        }
        
        book.getAuthors().remove(author);
        author.getBooks().remove(book);
        
        System.out.println("Author '" + author.getName() + "' removed from book '" + book.getTitle() + "'");
        return true;
    }
    
    /**
     * Create new book
     */
    @Transactional
    public Book createBook(String title, String isbn, int publicationYear, String genre) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Book title is required");
            return null;
        }
        
        if (isbn != null && bookRepository.findByIsbn(isbn).isPresent()) {
            System.out.println("Book with ISBN " + isbn + " already exists");
            return null;
        }
        
        Book book = new Book();
        book.setTitle(title.trim());
        book.setIsbn(isbn);
        book.setPublicationYear(publicationYear);
        book.setGenre(genre);
        book.setStatus(Book.BookStatus.AVAILABLE);
        
        bookRepository.persist(book);
        
        System.out.println("Book created successfully: " + book.getTitle());
        return book;
    }
    
    /**
     * Update book information
     */
    @Transactional
    public boolean updateBook(Long bookId, String title, String isbn, int publicationYear, String genre) {
        if (bookId == null) {
            System.out.println("Book ID cannot be null");
            return false;
        }
        
        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found with ID: " + bookId);
            return false;
        }
        
        Book book = bookOpt.get();
        
        if (title != null && !title.trim().isEmpty()) {
            book.setTitle(title.trim());
        }
        
        if (isbn != null) {
            // Check if new ISBN conflicts with another book
            Optional<Book> isbnConflict = bookRepository.findByIsbn(isbn);
            if (isbnConflict.isPresent() && !isbnConflict.get().getId().equals(bookId)) {
                System.out.println("ISBN already exists for another book");
                return false;
            }
            book.setIsbn(isbn);
        }
        
        if (publicationYear > 0) {
            book.setPublicationYear(publicationYear);
        }
        
        if (genre != null) {
            book.setGenre(genre);
        }
        
        System.out.println("Book updated successfully: " + book.getTitle());
        return true;
    }
    
    /**
     * Delete book
     */
    @Transactional
    public boolean deleteBook(Long bookId) {
        if (bookId == null) {
            System.out.println("Book ID cannot be null");
            return false;
        }
        
        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found with ID: " + bookId);
            return false;
        }
        
        Book book = bookOpt.get();
        
        // Check if book has active loans
        if (book.getLoans() != null && !book.getLoans().isEmpty()) {
            boolean hasActiveLoans = book.getLoans().stream()
                .anyMatch(loan -> loan.getStatus() == Loan.LoanStatus.ACTIVE);
            
            if (hasActiveLoans) {
                System.out.println("Cannot delete book: Has active loans");
                return false;
            }
        }
        
        bookRepository.delete(book);
        
        System.out.println("Book deleted successfully: " + book.getTitle());
        return true;
    }
} 
 