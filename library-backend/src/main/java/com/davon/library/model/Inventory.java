package com.davon.library.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

/**
 * Inventory entity - represents book inventory in a library.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in InventoryService.
 * 
 * @see com.davon.library.service.InventoryService for business operations
 */
@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;
    
    @Column(name = "total_copies", nullable = false)
    private int totalCopies = 0;
    
    @Column(name = "available_copies", nullable = false)
    private int availableCopies = 0;
    
    @Column(name = "reserved_copies")
    private int reservedCopies = 0;
    
    @Column(name = "damaged_copies")
    private int damagedCopies = 0;
    
    // One-to-One relationship with Book
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Book book;
    
    // Many-to-One relationship with Library
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    @JsonIgnore
    private Library library;
} 