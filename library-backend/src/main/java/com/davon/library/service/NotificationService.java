package com.davon.library.service;

import com.davon.library.model.Notification;
import com.davon.library.model.User;
import com.davon.library.repository.NotificationRepository;
import com.davon.library.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@ApplicationScoped
@Slf4j
public class NotificationService {

    @Inject
    private NotificationRepository notificationRepository;

    @Inject
    private UserRepository userRepository;

    // BUG 1: Wrong max retry count - should be 3, but set to 5
    // private static final int MAX_RETRY_COUNT = 5; // BUG: Should be 3
    private static final int MAX_RETRY_COUNT = 3; // FIXED: Changed from 5 to 3

    // BUG 2: Wrong notification expiry days - should be 30, but set to 15
    // private static final int NOTIFICATION_EXPIRY_DAYS = 15; // BUG: Should be 30
    private static final int NOTIFICATION_EXPIRY_DAYS = 30; // FIXED: Changed from 15 to 30

    /**
     * Create a new notification
     * BUG 3: Missing validation for user existence
     */
    public Notification createNotification(Long userId, String title, String message, 
                                        Notification.NotificationType type, 
                                        Notification.NotificationPriority priority) {
        // BUG: Missing validation for user existence
        User user = userRepository.findById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return null;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setCreatedDate(new Date());

        notificationRepository.persist(notification);
        log.info("Notification created for user {}: {}", userId, title);
        return notification;
    }

    /**
     * Send overdue reminder notification
     * BUG 4: Wrong priority setting - should be HIGH, but using MEDIUM
     */
    public Notification sendOverdueReminder(Long userId, String bookTitle, int daysOverdue) {
        String title = "Book Overdue Reminder";
        String message = String.format("Your book '%s' is %d days overdue. Please return it as soon as possible.", 
                                     bookTitle, daysOverdue);
        
        // BUG: Wrong priority setting
        // return createNotification(userId, title, message, 
        //                        Notification.NotificationType.OVERDUE_REMINDER,
        //                        Notification.NotificationPriority.MEDIUM); // BUG: Should be HIGH
        return createNotification(userId, title, message, 
                               Notification.NotificationType.OVERDUE_REMINDER,
                               Notification.NotificationPriority.HIGH); // FIXED: Changed from MEDIUM to HIGH
    }

    /**
     * Send reservation available notification
     * BUG 5: Wrong message format - missing book title
     */
    public Notification sendReservationAvailable(Long userId, String bookTitle) {
        String title = "Reservation Available";
        // BUG: Wrong message format
        // String message = "Your reserved book is now available for pickup."; // BUG: Should include book title
        String message = String.format("Your reserved book '%s' is now available for pickup.", bookTitle); // FIXED: Added book title
        
        return createNotification(userId, title, message,
                               Notification.NotificationType.RESERVATION_AVAILABLE,
                               Notification.NotificationPriority.HIGH);
    }

    /**
     * Send fine issued notification
     * BUG 6: Wrong amount formatting - should include currency symbol
     */
    public Notification sendFineIssued(Long userId, double fineAmount, String reason) {
        String title = "Fine Issued";
        // BUG: Wrong amount formatting
        // String message = String.format("A fine of %.2f has been issued: %s", fineAmount, reason); // BUG: Should include currency symbol
        String message = String.format("A fine of $%.2f has been issued: %s", fineAmount, reason); // FIXED: Added currency symbol
        
        return createNotification(userId, title, message,
                               Notification.NotificationType.FINE_ISSUED,
                               Notification.NotificationPriority.HIGH);
    }

    /**
     * Mark notification as read
     * BUG 7: Wrong status update - should be READ, but using DELIVERED
     */
    public boolean markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId);
        if (notification == null) {
            log.error("Notification not found: {}", notificationId);
            return false;
        }

        notification.markAsRead();
        // BUG: Wrong status update
        // notification.setStatus(Notification.NotificationStatus.DELIVERED); // BUG: Should be READ
        notification.setStatus(Notification.NotificationStatus.READ); // FIXED: Changed from DELIVERED to READ
        notificationRepository.persist(notification);
        log.info("Notification {} marked as read", notificationId);
        return true;
    }

    /**
     * Get unread notifications for user
     * BUG 8: Wrong filter - should check isRead, but checking status
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return List.of();
        }

        // BUG: Wrong filter
        return notificationRepository.findByUser(user).stream()
                // .filter(notification -> notification.getStatus() != Notification.NotificationStatus.READ) // BUG: Should check !isRead
                .filter(notification -> !notification.isRead()) // FIXED: Changed from status check to isRead check
                .toList();
    }

    /**
     * Get urgent notifications for user
     * BUG 9: Wrong priority check - should check for URGENT, but checking for HIGH
     */
    public List<Notification> getUrgentNotifications(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return List.of();
        }

        // BUG: Wrong priority check
        // return notificationRepository.findByUser(user).stream()
        //         .filter(notification -> notification.getPriority() == Notification.NotificationPriority.HIGH) // BUG: Should be URGENT
        //         .toList();
        return notificationRepository.findByUser(user).stream()
                .filter(notification -> notification.getPriority() == Notification.NotificationPriority.URGENT) // FIXED: Changed from HIGH to URGENT
                .toList();
    }

    /**
     * Process failed notifications for retry
     * BUG 10: Wrong retry logic - should increment retry count, but resetting it
     */
    public void processFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findFailedForRetry();
        
        for (Notification notification : failedNotifications) {
            if (notification.getRetryCount() < MAX_RETRY_COUNT) {
                // BUG: Wrong retry logic
                // notification.setRetryCount(0); // BUG: Should increment retry count
                notification.setRetryCount(notification.getRetryCount() + 1); // FIXED: Increment retry count instead of resetting
                notification.setStatus(Notification.NotificationStatus.PENDING);
                notificationRepository.persist(notification);
                log.info("Retrying failed notification: {}", notification.getId());
            } else {
                notification.setStatus(Notification.NotificationStatus.CANCELLED);
                notificationRepository.persist(notification);
                log.warn("Notification {} exceeded max retry count", notification.getId());
            }
        }
    }
    /**
     * Clean up old notifications
     */
    public void cleanupOldNotifications() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -NOTIFICATION_EXPIRY_DAYS);
        Date cutoffDate = cal.getTime();
        
        List<Notification> oldNotifications = notificationRepository.findByCreatedDateBefore(cutoffDate);
        
        for (Notification notification : oldNotifications) {
            notificationRepository.delete(notification);
            log.info("Deleted old notification: {}", notification.getId());
        }
    }

    /**
     * Get unread notification count for user
     */
    public long getUnreadNotificationCount(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return 0;
        }

        // Returns count of unread notifications for user
        return notificationRepository.countUnreadByUser(user);
    }

    /**
     * Send bulk notification to multiple users
     * BUG 13: Wrong user validation - should check if user exists, but not doing it
     */
    public int sendBulkNotification(List<Long> userIds, String title, String message, 
                                  Notification.NotificationType type) {
        int successCount = 0;
        
        for (Long userId : userIds) {
            // BUG: Missing user validation
            // Notification notification = createNotification(userId, title, message, type, 
            //                                            Notification.NotificationPriority.MEDIUM);
            // if (notification != null) {
            //     successCount++;
            // }
            
            // FIXED: Add explicit user validation before creating notification
            User user = userRepository.findById(userId);
            if (user != null) {
                Notification notification = createNotification(userId, title, message, type, 
                                                           Notification.NotificationPriority.MEDIUM);
                if (notification != null) {
                    successCount++;
                }
            } else {
                log.warn("User not found for bulk notification: {}", userId);
            }
        }
        
        log.info("Bulk notification sent to {}/{} users", successCount, userIds.size());
        return successCount;
    }
} 
