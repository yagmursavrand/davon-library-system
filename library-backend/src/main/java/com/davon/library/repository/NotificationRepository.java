package com.davon.library.repository;

import com.davon.library.model.Notification;
import com.davon.library.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class NotificationRepository implements PanacheRepository<Notification> {

    @Inject
    EntityManager entityManager;

    /**
     * Find all notifications for a user
     */
    public List<Notification> findByUser(User user) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("user", user);
        return query.getResultList();
    }

    /**
     * Find notifications by status
     */
    public List<Notification> findByStatus(Notification.NotificationStatus status) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.status = :status ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * Find notifications by type
     */
    public List<Notification> findByType(Notification.NotificationType type) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.type = :type ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("type", type);
        return query.getResultList();
    }

    /**
     * Find unread notifications for user
     */
    public List<Notification> findUnreadByUser(User user) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("user", user);
        return query.getResultList();
    }

    /**
     * Find urgent notifications for user
     */
    public List<Notification> findUrgentByUser(User user) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.user = :user AND n.priority = :priority ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("user", user);
        query.setParameter("priority", Notification.NotificationPriority.URGENT);
        return query.getResultList();
    }

    /**
     * Find notifications by created date before
     */
    public List<Notification> findByCreatedDateBefore(Date date) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.createdDate < :date ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("date", date);
        return query.getResultList();
    }

    /**
     * Count notifications by user and status
     */
    public long countByUserAndStatus(User user, Notification.NotificationStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.status = :status",
            Long.class
        );
        query.setParameter("user", user);
        query.setParameter("status", status);
        return query.getSingleResult();
    }

    /**
     * Find notifications older than specified days
     */
    public List<Notification> findOlderThanDays(int days) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -days);
        Date cutoffDate = cal.getTime();
        
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.createdDate < :cutoffDate ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("cutoffDate", cutoffDate);
        return query.getResultList();
    }

    /**
     * Find failed notifications for retry
     */
    public List<Notification> findFailedForRetry() {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < :maxRetries ORDER BY n.createdDate ASC",
            Notification.class
        );
        query.setParameter("status", Notification.NotificationStatus.FAILED);
        query.setParameter("maxRetries", 3);
        return query.getResultList();
    }

    /**
     * Find notifications by priority level (greater than or equal to)
     */
    public List<Notification> findByPriorityLevel(Notification.NotificationPriority priority) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.priority >= :priority ORDER BY n.priority DESC, n.createdDate DESC",
            Notification.class
        );
        query.setParameter("priority", priority);
        return query.getResultList();
    }

    /**
     * Find notifications by user and type
     */
    public List<Notification> findByUserAndType(User user, Notification.NotificationType type) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.user = :user AND n.type = :type ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("user", user);
        query.setParameter("type", type);
        return query.getResultList();
    }

    /**
     * Find notifications by user and priority
     */
    public List<Notification> findByUserAndPriority(User user, Notification.NotificationPriority priority) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.user = :user AND n.priority = :priority ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("user", user);
        query.setParameter("priority", priority);
        return query.getResultList();
    }

    /**
     * Find pending notifications for user
     */
    public List<Notification> findPendingByUser(User user) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.user = :user AND n.status = :status ORDER BY n.createdDate ASC",
            Notification.class
        );
        query.setParameter("user", user);
        query.setParameter("status", Notification.NotificationStatus.PENDING);
        return query.getResultList();
    }

    /**
     * Find sent notifications for user
     */
    public List<Notification> findSentByUser(User user) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.user = :user AND n.status = :status ORDER BY n.sentDate DESC",
            Notification.class
        );
        query.setParameter("user", user);
        query.setParameter("status", Notification.NotificationStatus.SENT);
        return query.getResultList();
    }

    /**
     * Count unread notifications for user
     */
    public long countUnreadByUser(User user) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false",
            Long.class
        );
        query.setParameter("user", user);
        return query.getSingleResult();
    }

    /**
     * Find notifications created between two dates
     */
    public List<Notification> findByCreatedDateBetween(Date startDate, Date endDate) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.createdDate BETWEEN :startDate AND :endDate ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    /**
     * Find notifications by user created between two dates
     */
    public List<Notification> findByUserAndCreatedDateBetween(User user, Date startDate, Date endDate) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.user = :user AND n.createdDate BETWEEN :startDate AND :endDate ORDER BY n.createdDate DESC",
            Notification.class
        );
        query.setParameter("user", user);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
} 