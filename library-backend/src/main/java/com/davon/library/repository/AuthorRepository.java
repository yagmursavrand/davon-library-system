package com.davon.library.repository;

import com.davon.library.model.Author;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AuthorRepository implements PanacheRepository<Author> {
    
    /**
     * Find author by name
     */
    public Optional<Author> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
    
    /**
     * Find authors by name pattern (case-insensitive)
     */
    public List<Author> findByNamePattern(String namePattern) {
        return find("LOWER(name) LIKE LOWER(?1)", "%" + namePattern + "%").list();
    }

    /**
     * Find authors by name containing (alias for findByNamePattern)
     */
    public List<Author> findByNameContaining(String namePattern) {
        return findByNamePattern(namePattern);
    }
    
    /**
     * Find authors who have written books
     */
    public List<Author> findAuthorsWithBooks() {
        return find("SIZE(books) > 0").list();
    }
    
    /**
     * Find authors without books
     */
    public List<Author> findAuthorsWithoutBooks() {
        return find("SIZE(books) = 0").list();
    }
    
    /**
     * Search authors by name or bio
     */
    public List<Author> searchAuthors(String searchTerm) {
        return find("LOWER(name) LIKE LOWER(?1) OR LOWER(bio) LIKE LOWER(?1)", 
                   "%" + searchTerm + "%").list();
    }
    
    /**
     * Find prolific authors (with more than specified number of books)
     */
    public List<Author> findProlificAuthors(int minBooks) {
        return find("SIZE(books) >= ?1", minBooks).list();
    }
    
    /**
     * Count total authors
     */
    public long countAllAuthors() {
        return count();
    }
} 