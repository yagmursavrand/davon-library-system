-- =====================================================
-- Library Management System - Reports
-- =====================================================

USE LibraryManagementSystem;
GO

-- =====================================================
-- Overdue Books Report
-- =====================================================

-- Comprehensive overdue books report
SELECT 
    'OVERDUE_BOOKS_REPORT' as report_type,
    GETDATE() as report_date,
    COUNT(l.loan_id) as total_overdue_books,
    SUM(l.fine_amount) as total_fines_accumulated,
    AVG(DATEDIFF(day, l.due_date, GETDATE())) as average_days_overdue
FROM loans l
WHERE l.status = 'ACTIVE' AND l.due_date < GETDATE();

-- Detailed overdue books list
SELECT 
    l.loan_id,
    b.title as book_title,
    b.isbn,
    b.genre,
    u.name as member_name,
    u.email as member_email,
    m.membership_number,
    l.loan_date,
    l.due_date,
    l.fine_amount,
    DATEDIFF(day, l.due_date, GETDATE()) as days_overdue,
    lib.name as library_name
FROM loans l
INNER JOIN books b ON l.book_id = b.book_id
INNER JOIN members m ON l.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
INNER JOIN libraries lib ON m.library_id = lib.library_id
WHERE l.status = 'ACTIVE' AND l.due_date < GETDATE()
ORDER BY days_overdue DESC;

-- =====================================================
-- Popular Books Report
-- =====================================================

-- Most borrowed books (all time)
SELECT 
    'POPULAR_BOOKS_REPORT' as report_type,
    GETDATE() as report_date,
    b.book_id,
    b.title,
    b.isbn,
    b.genre,
    COUNT(l.loan_id) as total_loans,
    COUNT(CASE WHEN l.loan_date >= DATEADD(month, -1, GETDATE()) THEN 1 END) as loans_last_month,
    COUNT(CASE WHEN l.loan_date >= DATEADD(month, -3, GETDATE()) THEN 1 END) as loans_last_3_months,
    i.total_copies,
    i.available_copies,
    ROUND(CAST(COUNT(l.loan_id) AS FLOAT) / NULLIF(i.total_copies, 0), 2) as utilization_rate
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
LEFT JOIN loans l ON b.book_id = l.book_id
GROUP BY b.book_id, b.title, b.isbn, b.genre, i.total_copies, i.available_copies
ORDER BY total_loans DESC;

-- Popular books by genre
SELECT 
    b.genre,
    COUNT(DISTINCT b.book_id) as total_books,
    COUNT(l.loan_id) as total_loans,
    AVG(CAST(COUNT(l.loan_id) AS FLOAT)) as average_loans_per_book
FROM books b
LEFT JOIN loans l ON b.book_id = l.book_id
GROUP BY b.genre
ORDER BY total_loans DESC;

-- =====================================================
-- Financial Report
-- =====================================================

-- Overall financial summary
SELECT 
    'FINANCIAL_REPORT' as report_type,
    GETDATE() as report_date,
    COUNT(t.transaction_id) as total_transactions,
    SUM(CASE WHEN t.type = 'MEMBERSHIP_FEE' THEN t.amount ELSE 0 END) as total_membership_revenue,
    SUM(CASE WHEN t.type = 'FINE' THEN t.amount ELSE 0 END) as total_fine_revenue,
    SUM(CASE WHEN t.type = 'FINE' AND t.status = 'COMPLETED' THEN t.amount ELSE 0 END) as collected_fines,
    SUM(CASE WHEN t.type = 'FINE' AND t.status = 'PENDING' THEN t.amount ELSE 0 END) as outstanding_fines,
    SUM(t.amount) as total_revenue
FROM transactions t;

-- Monthly revenue breakdown
SELECT 
    YEAR(t.date) as revenue_year,
    MONTH(t.date) as revenue_month,
    t.type as transaction_type,
    COUNT(t.transaction_id) as transaction_count,
    SUM(t.amount) as total_amount,
    AVG(t.amount) as average_amount
FROM transactions t
WHERE t.date >= DATEADD(month, -12, GETDATE())
GROUP BY YEAR(t.date), MONTH(t.date), t.type
ORDER BY revenue_year DESC, revenue_month DESC, total_amount DESC;

-- Fine analysis
SELECT 
    f.fine_type,
    COUNT(f.transaction_id) as total_fines,
    SUM(t.amount) as total_amount,
    AVG(t.amount) as average_amount,
    COUNT(CASE WHEN f.is_paid = 1 THEN 1 END) as paid_count,
    COUNT(CASE WHEN f.is_paid = 0 THEN 1 END) as unpaid_count,
    SUM(CASE WHEN f.is_paid = 1 THEN t.amount ELSE 0 END) as paid_amount,
    SUM(CASE WHEN f.is_paid = 0 THEN t.amount ELSE 0 END) as unpaid_amount,
    ROUND(CAST(COUNT(CASE WHEN f.is_paid = 1 THEN 1 END) AS FLOAT) / COUNT(f.transaction_id) * 100, 2) as payment_rate_percent
FROM fines f
INNER JOIN transactions t ON f.transaction_id = t.transaction_id
GROUP BY f.fine_type
ORDER BY total_amount DESC;

-- =====================================================
-- Member Activity Report
-- =====================================================

-- Member activity summary
SELECT 
    'MEMBER_ACTIVITY_REPORT' as report_type,
    GETDATE() as report_date,
    COUNT(m.user_id) as total_members,
    COUNT(CASE WHEN m.membership_end >= GETDATE() THEN 1 END) as active_members,
    COUNT(CASE WHEN m.membership_end < GETDATE() THEN 1 END) as expired_members,
    COUNT(CASE WHEN m.total_fines > 0 THEN 1 END) as members_with_fines,
    AVG(m.total_fines) as average_fines_per_member,
    SUM(m.total_fines) as total_outstanding_fines
FROM members m;

-- Most active members
SELECT 
    m.user_id,
    u.name as member_name,
    u.email as member_email,
    m.membership_number,
    COUNT(l.loan_id) as total_loans,
    COUNT(CASE WHEN l.loan_date >= DATEADD(month, -1, GETDATE()) THEN 1 END) as loans_last_month,
    COUNT(CASE WHEN l.loan_date >= DATEADD(month, -3, GETDATE()) THEN 1 END) as loans_last_3_months,
    COUNT(CASE WHEN l.status = 'ACTIVE' THEN 1 END) as current_loans,
    COUNT(CASE WHEN l.due_date < GETDATE() AND l.status = 'ACTIVE' THEN 1 END) as overdue_loans,
    m.total_fines,
    lib.name as library_name
FROM members m
INNER JOIN users u ON m.user_id = u.user_id
INNER JOIN libraries lib ON m.library_id = lib.library_id
LEFT JOIN loans l ON m.user_id = l.member_id
GROUP BY m.user_id, u.name, u.email, m.membership_number, m.total_fines, lib.name
ORDER BY total_loans DESC;

-- Member registration trends
SELECT 
    YEAR(m.membership_start) as registration_year,
    MONTH(m.membership_start) as registration_month,
    COUNT(m.user_id) as new_members,
    COUNT(CASE WHEN m.membership_end >= GETDATE() THEN 1 END) as still_active
FROM members m
WHERE m.membership_start >= DATEADD(year, -2, GETDATE())
GROUP BY YEAR(m.membership_start), MONTH(m.membership_start)
ORDER BY registration_year DESC, registration_month DESC;

-- =====================================================
-- Inventory Report
-- =====================================================

-- Overall inventory status
SELECT 
    'INVENTORY_REPORT' as report_type,
    GETDATE() as report_date,
    COUNT(b.book_id) as total_books,
    SUM(i.total_copies) as total_copies,
    SUM(i.available_copies) as available_copies,
    SUM(i.reserved_copies) as reserved_copies,
    SUM(i.damaged_copies) as damaged_copies,
    ROUND(CAST(SUM(i.available_copies) AS FLOAT) / NULLIF(SUM(i.total_copies), 0) * 100, 2) as availability_percentage
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id;

-- Inventory by library
SELECT 
    lib.library_id,
    lib.name as library_name,
    COUNT(b.book_id) as total_books,
    SUM(i.total_copies) as total_copies,
    SUM(i.available_copies) as available_copies,
    SUM(i.reserved_copies) as reserved_copies,
    SUM(i.damaged_copies) as damaged_copies,
    ROUND(CAST(SUM(i.available_copies) AS FLOAT) / NULLIF(SUM(i.total_copies), 0) * 100, 2) as availability_percentage
FROM libraries lib
LEFT JOIN inventories i ON lib.library_id = i.library_id
LEFT JOIN books b ON i.book_id = b.book_id
GROUP BY lib.library_id, lib.name
ORDER BY total_copies DESC;

-- Books requiring attention
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    i.total_copies,
    i.available_copies,
    i.reserved_copies,
    i.damaged_copies,
    lib.name as library_name,
    CASE 
        WHEN i.available_copies = 0 THEN 'OUT_OF_STOCK'
        WHEN i.available_copies <= 1 THEN 'LOW_STOCK'
        WHEN i.damaged_copies > 0 THEN 'DAMAGED_COPIES'
        ELSE 'NORMAL'
    END as status
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE i.available_copies <= 1 OR i.damaged_copies > 0
ORDER BY i.available_copies, i.damaged_copies DESC;

-- =====================================================
-- Reservation Report
-- =====================================================

-- Reservation summary
SELECT 
    'RESERVATION_REPORT' as report_type,
    GETDATE() as report_date,
    COUNT(r.reservation_id) as total_reservations,
    COUNT(CASE WHEN r.status = 'PENDING' THEN 1 END) as pending_reservations,
    COUNT(CASE WHEN r.status = 'ACTIVE' THEN 1 END) as active_reservations,
    COUNT(CASE WHEN r.status = 'FULFILLED' THEN 1 END) as fulfilled_reservations,
    COUNT(CASE WHEN r.status = 'EXPIRED' THEN 1 END) as expired_reservations,
    COUNT(CASE WHEN r.expiry_date < GETDATE() THEN 1 END) as expired_reservations
FROM reservations r;

-- Most requested books
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    b.genre,
    COUNT(r.reservation_id) as total_reservations,
    COUNT(CASE WHEN r.status = 'PENDING' THEN 1 END) as pending_reservations,
    COUNT(CASE WHEN r.status = 'ACTIVE' THEN 1 END) as active_reservations,
    COUNT(CASE WHEN r.status = 'FULFILLED' THEN 1 END) as fulfilled_reservations,
    i.available_copies,
    i.total_copies
FROM books b
INNER JOIN reservations r ON b.book_id = r.book_id
INNER JOIN inventories i ON b.book_id = i.book_id
GROUP BY b.book_id, b.title, b.isbn, b.genre, i.available_copies, i.total_copies
ORDER BY total_reservations DESC;

-- =====================================================
-- Staff Activity Report
-- =====================================================

-- Staff activity summary
SELECT 
    'STAFF_ACTIVITY_REPORT' as report_type,
    GETDATE() as report_date,
    COUNT(u.user_id) as total_staff,
    COUNT(CASE WHEN u.role = 'ADMIN' THEN 1 END) as admin_count,
    COUNT(CASE WHEN u.role = 'LIBRARIAN' THEN 1 END) as librarian_count,
    COUNT(CASE WHEN u.is_logged_in = 1 THEN 1 END) as currently_logged_in
FROM users u
WHERE u.role IN ('ADMIN', 'LIBRARIAN');

-- Admin activity
SELECT 
    a.user_id,
    u.name as admin_name,
    u.email as admin_email,
    a.admin_level,
    a.department,
    a.last_admin_action,
    DATEDIFF(day, a.last_admin_action, GETDATE()) as days_since_last_action
FROM admins a
INNER JOIN users u ON a.user_id = u.user_id
ORDER BY a.last_admin_action DESC;

-- =====================================================
-- Comprehensive Library Report
-- =====================================================

-- Complete library overview
SELECT 
    'COMPREHENSIVE_LIBRARY_REPORT' as report_type,
    GETDATE() as report_date,
    (SELECT COUNT(*) FROM books) as total_books,
    (SELECT COUNT(*) FROM members) as total_members,
    (SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE') as active_loans,
    (SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE' AND due_date < GETDATE()) as overdue_loans,
    (SELECT COUNT(*) FROM reservations WHERE status IN ('PENDING', 'ACTIVE')) as active_reservations,
    (SELECT SUM(total_fines) FROM members) as total_outstanding_fines,
    (SELECT COUNT(*) FROM users WHERE role IN ('ADMIN', 'LIBRARIAN')) as total_staff,
    (SELECT COUNT(*) FROM libraries) as total_libraries;

-- Library performance metrics
SELECT 
    lib.library_id,
    lib.name as library_name,
    COUNT(DISTINCT b.book_id) as total_books,
    COUNT(DISTINCT m.user_id) as total_members,
    COUNT(l.loan_id) as total_loans,
    COUNT(CASE WHEN l.loan_date >= DATEADD(month, -1, GETDATE()) THEN 1 END) as loans_last_month,
    COUNT(CASE WHEN l.status = 'ACTIVE' AND l.due_date < GETDATE() THEN 1 END) as overdue_loans,
    SUM(m.total_fines) as total_fines,
    ROUND(CAST(COUNT(l.loan_id) AS FLOAT) / NULLIF(COUNT(DISTINCT m.user_id), 0), 2) as loans_per_member
FROM libraries lib
LEFT JOIN inventories i ON lib.library_id = i.library_id
LEFT JOIN books b ON i.book_id = b.book_id
LEFT JOIN members m ON lib.library_id = m.library_id
LEFT JOIN loans l ON m.user_id = l.member_id
GROUP BY lib.library_id, lib.name
ORDER BY total_loans DESC;

-- =====================================================
-- Notification Report
-- =====================================================

-- Notification summary
SELECT 
    'NOTIFICATION_REPORT' as report_type,
    GETDATE() as report_date,
    COUNT(n.notification_id) as total_notifications,
    COUNT(CASE WHEN n.is_read = 1 THEN 1 END) as read_notifications,
    COUNT(CASE WHEN n.is_read = 0 THEN 1 END) as unread_notifications,
    COUNT(CASE WHEN n.status = 'PENDING' THEN 1 END) as pending_notifications,
    COUNT(CASE WHEN n.status = 'SENT' THEN 1 END) as sent_notifications,
    COUNT(CASE WHEN n.status = 'FAILED' THEN 1 END) as failed_notifications
FROM notifications n;

-- Notifications by type
SELECT 
    n.type,
    COUNT(n.notification_id) as total_count,
    COUNT(CASE WHEN n.is_read = 1 THEN 1 END) as read_count,
    COUNT(CASE WHEN n.is_read = 0 THEN 1 END) as unread_count,
    COUNT(CASE WHEN n.status = 'SENT' THEN 1 END) as sent_count,
    COUNT(CASE WHEN n.status = 'FAILED' THEN 1 END) as failed_count
FROM notifications n
GROUP BY n.type
ORDER BY total_count DESC;

-- Notifications by priority
SELECT 
    n.priority,
    COUNT(n.notification_id) as total_count,
    COUNT(CASE WHEN n.is_read = 1 THEN 1 END) as read_count,
    COUNT(CASE WHEN n.is_read = 0 THEN 1 END) as unread_count,
    AVG(CAST(DATEDIFF(hour, n.created_date, COALESCE(n.read_date, GETDATE())) AS FLOAT)) as avg_hours_to_read
FROM notifications n
GROUP BY n.priority
ORDER BY 
    CASE n.priority 
        WHEN 'URGENT' THEN 1 
        WHEN 'HIGH' THEN 2 
        WHEN 'MEDIUM' THEN 3 
        WHEN 'LOW' THEN 4 
    END;

-- User notification activity
SELECT 
    u.name as user_name,
    u.email as user_email,
    COUNT(n.notification_id) as total_notifications,
    COUNT(CASE WHEN n.is_read = 1 THEN 1 END) as read_notifications,
    COUNT(CASE WHEN n.is_read = 0 THEN 1 END) as unread_notifications,
    COUNT(CASE WHEN n.priority IN ('HIGH', 'URGENT') THEN 1 END) as urgent_notifications
FROM users u
LEFT JOIN notifications n ON u.user_id = n.user_id
GROUP BY u.user_id, u.name, u.email
ORDER BY total_notifications DESC;

PRINT 'All reports generated successfully!';
GO 