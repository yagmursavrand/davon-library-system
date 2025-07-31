package com.davon.library.service;

import com.davon.library.model.Book;
import com.davon.library.model.Member;
import com.davon.library.model.Reservation;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.ReservationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
@ApplicationScoped
@Slf4j
public class ReservationService {

    @Inject
    private ReservationRepository reservationRepository;

    @Inject
    private BookRepository bookRepository;

    @Inject
    private MemberRepository memberRepository;

    // BUG 1: Wrong max reservations per member - should be 5, but set to 3
    private static final int MAX_RESERVATIONS_PER_MEMBER = 5; // BUG: Should be 5

    // BUG 2: Wrong reservation expiry days - should be 7, but set to 5
    private static final int RESERVATION_EXPIRY_DAYS = 7; // BUG: Should be 7

    /**
     * Create a new reservation
     * BUG 3: Missing validation for book availability
     */
    public Reservation createReservation(Long bookId, Long memberId, String notes) {
        Book book = bookRepository.findById(bookId);
        Member member = memberRepository.findById(memberId);

        if (book == null || member == null) {
            log.error("Book or member not found");
            return null;
        }

        // BUG 3: Missing validation for book availability - FIXED
        // Check if book is available for reservation
        if (!isBookAvailableForReservation(bookId)) {
            log.warn("Book {} is not available for reservation", bookId);
            return null;
        }

        // Check if member has too many reservations
        List<Reservation> memberReservations = reservationRepository.findByMember(member);
        if (memberReservations.size() >= MAX_RESERVATIONS_PER_MEMBER) {
            log.warn("Member {} has reached maximum reservations", memberId);
            return null;
        }

        // Check if member already has a reservation for this book
        List<Reservation> existingReservations = reservationRepository.findByBook(book);
        boolean alreadyReserved = existingReservations.stream()
                .anyMatch(reservation -> reservation.getMember().getId().equals(memberId) 
                        && reservation.getStatus() == Reservation.ReservationStatus.PENDING);

        if (alreadyReserved) {
            log.warn("Member {} already has a reservation for book {}", memberId, bookId);
            return null;
        }

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setMember(member);
        reservation.setNotes(notes);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);
        reservation.setReservationDate(new Date());

        // BUG 4: Wrong expiry date calculation - should add RESERVATION_EXPIRY_DAYS, but using 3
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(java.util.Calendar.DAY_OF_MONTH, RESERVATION_EXPIRY_DAYS); // BUG: Should use RESERVATION_EXPIRY_DAYS
        reservation.setExpiryDate(cal.getTime());



        reservationRepository.persist(reservation);
        log.info("Reservation created for book {} by member {}", bookId, memberId);
        return reservation;
    }

    /**
     * Cancel a reservation
     * BUG 6: Missing validation for reservation ownership
     */
    public boolean cancelReservation(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId);
        
        if (reservation == null) {
            log.error("Reservation not found: {}", reservationId);
            return false;
        }

        // BUG 6: Missing validation for reservation ownership - FIXED
        // Check if the member owns this reservation
        if (!reservation.getMember().getId().equals(memberId)) {
            log.error("Member {} does not own reservation {}", memberId, reservationId);
            return false;
        }

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            log.warn("Cannot cancel reservation in status: {}", reservation.getStatus());
            return false;
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.persist(reservation);
        log.info("Reservation {} cancelled by member {}", reservationId, memberId);
        return true;
    }

    /**
     * Fulfill a reservation when book becomes available
     * BUG 7: Wrong status update - should be FULFILLED, but using ACTIVE
     */
    public boolean fulfillReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId);
        
        if (reservation == null) {
            log.error("Reservation not found: {}", reservationId);
            return false;
        }

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            log.warn("Cannot fulfill reservation in status: {}", reservation.getStatus());
            return false;
        }

        // BUG 7: Wrong status update - FIXED
        // Update status to fulfilled
        reservation.setStatus(Reservation.ReservationStatus.FULFILLED);
        reservationRepository.persist(reservation);
        log.info("Reservation {} fulfilled", reservationId);
        return true;
    }

    /**
     * Get reservations for a member
     * BUG 8: Wrong status filter - should include all statuses, but filtering by PENDING only
     */
    public List<Reservation> getMemberReservations(Long memberId) {
        Member member = memberRepository.findById(memberId);
        if (member == null) {
            log.error("Member not found: {}", memberId);
            return List.of();
        }

        // BUG 8: Wrong status filter - FIXED
        // Return all reservations for member
        return reservationRepository.findByMember(member);
    }

    /**
     * Get active reservations for a book
     * BUG 9: Wrong method call - should use findActiveReservationsByBook, but using findByBook
     */
    public List<Reservation> getBookReservations(Long bookId) {
        Book book = bookRepository.findById(bookId);
        if (book == null) {
            log.error("Book not found: {}", bookId);
            return List.of();
        }

        // BUG 9: Wrong method call - FIXED
        // Return active reservations for book
        return reservationRepository.findActiveReservationsByBook(book);
    }

    /**
     * Check if book is available for reservation
     * BUG 10: Wrong availability logic - should check active reservations, but checking all
     */
    public boolean isBookAvailableForReservation(Long bookId) {
        Book book = bookRepository.findById(bookId);
        if (book == null) {
            return false;
        }

        // BUG 10: Wrong availability logic - FIXED
        // Check if book has active reservations
        List<Reservation> activeReservations = reservationRepository.findActiveReservationsByBook(book);
        return activeReservations.isEmpty();
    }

    /**
     * Process expired reservations
     * BUG 11: Wrong expiry check - should check if current date is after expiry, but using before
     */
    public void processExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations();
        
        for (Reservation reservation : expiredReservations) {
            // BUG 11: Wrong expiry check - FIXED
            // Check if reservation is expired
            if (new Date().after(reservation.getExpiryDate())) {
                reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
                reservationRepository.persist(reservation);
                log.info("Reservation {} expired", reservation.getId());
            }
        }
    }

    /**
     * Get reservation queue position
     * BUG 12: Wrong position calculation - should count active reservations, but counting all
     */
    public int getReservationQueuePosition(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId);
        if (reservation == null) {
            return -1;
        }

        // BUG 12: Wrong position calculation - FIXED
        // Count pending reservations for position calculation
        List<Reservation> pendingReservations = reservationRepository.findPendingReservationsByBook(reservation.getBook());
        
        int position = 1;
        for (Reservation r : pendingReservations) {
            if (r.getId().equals(reservationId)) {
                return position;
            }
            position++;
        }
        
        return -1;
    }
} 