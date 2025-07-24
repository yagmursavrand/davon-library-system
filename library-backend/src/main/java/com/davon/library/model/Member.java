package com.davon.library.model;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * Member entity - represents a library member extending User.
 * 
 * This class is a pure data entity with no business logic.
 * All business operations are handled in MemberService.
 * 
 * @see com.davon.library.service.MemberService for business operations
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"library", "loans"}) // Exclude to prevent circular references
@ToString(exclude = {"library", "loans"}, callSuper = true) // Exclude to prevent circular references
public class Member extends User {
    
    @Column(name = "membership_number", unique = true, length = 50)
    private String membershipNumber;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "membership_start")
    private Date membershipStart;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "membership_end")
    private Date membershipEnd;
    
    // One-to-Many relationship with Loan
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_borrowed_books", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "book_id")
    private List<Long> borrowedBookIds = new ArrayList<>();
    
    @Column(name = "total_fines")
    private double totalFines = 0.0;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_fine_history", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "fine_description")
    private List<String> fineHistory = new ArrayList<>();
    
    // Many-to-One relationship with Library
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id")
    private Library library;
} 