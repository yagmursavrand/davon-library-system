package com.davon.library.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.*;

/**
 * Author entity - represents an author in the library system.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in AuthorService.
 * 
 * @see com.davon.library.service.AuthorService for business operations
 */
@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"books"}) // Exclude to prevent circular references
@EqualsAndHashCode(exclude = {"books"}) // Exclude to prevent circular references
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "bio", length = 1000)
    private String bio;
    
    // Many-to-Many relationship with Book
    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();
} 