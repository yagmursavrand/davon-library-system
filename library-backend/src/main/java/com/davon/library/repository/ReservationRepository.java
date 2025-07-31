package com.davon.library.repository;

import com.davon.library.model.Reservation;
import com.davon.library.model.Book;
import com.davon.library.model.Member;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class ReservationRepository implements PanacheRepository<Reservation> {

    @Inject
    EntityManager entityManager;

    /**
     * Find all reservations for a book
     */
    public List<Reservation> findByBook(Book book) {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.book = :book ORDER BY r.reservationDate ASC",
            Reservation.class
        );
        query.setParameter("book", book);
        return query.getResultList();
    }

    /**
     * Find all reservations for a member
     */
    public List<Reservation> findByMember(Member member) {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.member = :member ORDER BY r.reservationDate DESC",
            Reservation.class
        );
        query.setParameter("member", member);
        return query.getResultList();
    }

    /**
     * Find pending reservations for a book (waiting in queue)
     */
    public List<Reservation> findPendingReservationsByBook(Book book) {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.book = :book AND r.status = :status ORDER BY r.reservationDate ASC",
            Reservation.class
        );
        query.setParameter("book", book);
        query.setParameter("status", Reservation.ReservationStatus.PENDING);
        return query.getResultList();
    }

    /**
     * Find active reservations for a book (ready to be picked up)
     */
    public List<Reservation> findActiveReservationsByBook(Book book) {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.book = :book AND r.status = :status ORDER BY r.reservationDate ASC",
            Reservation.class
        );
        query.setParameter("book", book);
        query.setParameter("status", Reservation.ReservationStatus.ACTIVE);
        return query.getResultList();
    }

    /**
     * Count pending reservations for a book (how many people are waiting)
     */
    public long countPendingReservationsByBook(Book book) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(r) FROM Reservation r WHERE r.book = :book AND r.status = :status",
            Long.class
        );
        query.setParameter("book", book);
        query.setParameter("status", Reservation.ReservationStatus.PENDING);
        return query.getSingleResult();
    }

    /**
     * Count active reservations for a book (how many are ready to pick up)
     */
    public long countActiveReservationsByBook(Book book) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(r) FROM Reservation r WHERE r.book = :book AND r.status = :status",
            Long.class
        );
        query.setParameter("book", book);
        query.setParameter("status", Reservation.ReservationStatus.ACTIVE);
        return query.getSingleResult();
    }

    /**
     * Find expired reservations (past expiry date and still pending)
     */
    public List<Reservation> findExpiredReservations() {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.expiryDate < :currentDate AND r.status = :status",
            Reservation.class
        );
        query.setParameter("currentDate", new Date());
        query.setParameter("status", Reservation.ReservationStatus.PENDING);
        return query.getResultList();
    }

    /**
     * Find reservations that need notification (active but not notified yet)
     */
    public List<Reservation> findReservationsNeedingNotification() {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.notificationDate IS NULL AND r.status = :status",
            Reservation.class
        );
        query.setParameter("status", Reservation.ReservationStatus.ACTIVE);
        return query.getResultList();
    }

    /**
     * Find reservations expiring soon (within next 24 hours)
     */
    public List<Reservation> findReservationsExpiringSoon() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.HOUR_OF_DAY, 24);
        Date futureDate = cal.getTime();
        
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.expiryDate <= :futureDate AND r.status = :status",
            Reservation.class
        );
        query.setParameter("futureDate", futureDate);
        query.setParameter("status", Reservation.ReservationStatus.PENDING);
        return query.getResultList();
    }

    /**
     * Find reservations by status
     */
    public List<Reservation> findByStatus(Reservation.ReservationStatus status) {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.status = :status ORDER BY r.reservationDate DESC",
            Reservation.class
        );
        query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * Find reservations expiring between two dates
     */
    public List<Reservation> findReservationsExpiringBetween(Date startDate, Date endDate) {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.expiryDate BETWEEN :startDate AND :endDate AND r.status = :status",
            Reservation.class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("status", Reservation.ReservationStatus.PENDING);
        return query.getResultList();
    }

    /**
     * Find fulfilled reservations for a book
     */
    public List<Reservation> findFulfilledReservationsByBook(Book book) {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.book = :book AND r.status = :status ORDER BY r.reservationDate DESC",
            Reservation.class
        );
        query.setParameter("book", book);
        query.setParameter("status", Reservation.ReservationStatus.FULFILLED);
        return query.getResultList();
    }

    /**
     * Find cancelled reservations for a member
     */
    public List<Reservation> findCancelledReservationsByMember(Member member) {
        TypedQuery<Reservation> query = entityManager.createQuery(
            "SELECT r FROM Reservation r WHERE r.member = :member AND r.status = :status ORDER BY r.reservationDate DESC",
            Reservation.class
        );
        query.setParameter("member", member);
        query.setParameter("status", Reservation.ReservationStatus.CANCELLED);
        return query.getResultList();
    }
} 