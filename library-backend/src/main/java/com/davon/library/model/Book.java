package com.davon.library.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.*;

/**
 * Book entity - represents a book in the library system.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in BookService.
 * 
 * @see com.davon.library.service.BookService for business operations
 */
@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;
    
    @Column(name = "publication_year")
    private int publicationYear = 0;
    
    @Column(name = "genre", length = 100)
    private String genre;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookStatus status = BookStatus.AVAILABLE;
    
    // Many-to-Many relationship with Author
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @JsonIgnore
    private List<Author> authors = new ArrayList<>();
    
    // One-to-One relationship with Inventory
    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Inventory inventory;
    
    // One-to-Many relationship with Loan
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Loan> loans = new ArrayList<>();
    
    // Book status enum
    public enum BookStatus {
        AVAILABLE, BORROWED, RESERVED, MAINTENANCE, LOST
    }
} 