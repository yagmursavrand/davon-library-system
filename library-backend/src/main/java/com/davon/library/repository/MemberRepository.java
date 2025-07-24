package com.davon.library.repository;

import com.davon.library.model.Member;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.Date;

@ApplicationScoped
public class MemberRepository implements PanacheRepository<Member> {
    
    /**
     * Find member by membership number
     */
    public Optional<Member> findByMembershipNumber(String membershipNumber) {
        return find("membershipNumber", membershipNumber).firstResultOptional();
    }
    
    /**
     * Find member by email
     */
    public Optional<Member> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
    
    /**
     * Find active members (membership not expired)
     */
    public List<Member> findActiveMembers() {
        return find("membershipEnd > ?1", new Date()).list();
    }
    
    /**
     * Find members with expired membership
     */
    public List<Member> findExpiredMembers() {
        return find("membershipEnd <= ?1", new Date()).list();
    }
    
    /**
     * Find members with outstanding fines
     */
    public List<Member> findMembersWithFines() {
        return find("totalFines > 0").list();
    }
    
    /**
     * Find members with fines above threshold
     */
    public List<Member> findMembersWithFinesAbove(double threshold) {
        return find("totalFines > ?1", threshold).list();
    }
    
    /**
     * Find members with membership expiring soon (within days)
     */
    public List<Member> findMembersExpiringSoon(int days) {
        Date futureDate = new Date(System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000));
        return find("membershipEnd BETWEEN ?1 AND ?2", new Date(), futureDate).list();
    }
    
    /**
     * Find members who borrowed books
     */
    public List<Member> findMembersWithBorrowedBooks() {
        return find("SIZE(borrowedBookIds) > 0").list();
    }
    
    /**
     * Count total active members
     */
    public long countActiveMembers() {
        return count("membershipEnd > ?1", new Date());
    }
} 