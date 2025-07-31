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
 * Library entity - represents a library in the system.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in LibraryService.
 * 
 * @see com.davon.library.service.LibraryService for business operations
 */
@Entity
@Table(name = "libraries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_id")
    private Long id;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "opening_hours", length = 200)
    private String openingHours;
    
    // One-to-Many relationship with Inventory (Library manages inventories)
    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Inventory> inventories = new ArrayList<>();
    
    // One-to-Many relationship with Member (Library registers members)
    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Member> members = new ArrayList<>();
} 