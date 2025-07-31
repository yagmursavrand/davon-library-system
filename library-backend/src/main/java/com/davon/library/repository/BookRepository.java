package com.davon.library.repository;

import com.davon.library.model.Book;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookRepository implements PanacheRepository<Book> {
    
    /**
     * Find book by ISBN
     */
    public Optional<Book> findByIsbn(String isbn) {
        return find("isbn", isbn).firstResultOptional();
    }
    
    /**
     * Find books by title (case-insensitive)
     */
    public List<Book> findByTitle(String title) {
        return find("LOWER(title) LIKE LOWER(?1)", "%" + title + "%").list();
    }
    
    /**
     * Find books by genre
     */
    public List<Book> findByGenre(String genre) {
        return find("genre", genre).list();
    }
    
    /**
     * Find books by publication year
     */
    public List<Book> findByPublicationYear(int year) {
        return find("publicationYear", year).list();
    }
    
    /**
     * Find books by status
     */
    public List<Book> findByStatus(Book.BookStatus status) {
        return find("status", status).list();
    }
    
    /**
     * Find available books
     */
    public List<Book> findAvailableBooks() {
        return find("status", Book.BookStatus.AVAILABLE).list();
    }
    
    /**
     * Search books by title or author name
     */
    public List<Book> searchBooks(String searchTerm) {
        // Simplified query to avoid HQL complexity issues
        return find("LOWER(title) LIKE LOWER(?1)", "%" + searchTerm + "%").list();
    }
} 