-- =====================================================
-- Library Management System - Common Queries
-- =====================================================

USE LibraryManagementSystem;
GO

-- =====================================================
-- Book Search Queries
-- =====================================================

-- 1. Search books by title (partial match)
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    b.genre,
    b.publication_year,
    b.status,
    i.available_copies,
    i.total_copies,
    lib.name as library_name
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE b.title LIKE '%Harry Potter%'
ORDER BY b.title;

-- 2. Search books by author
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    b.genre,
    a.name as author_name,
    i.available_copies,
    lib.name as library_name
FROM books b
INNER JOIN book_authors ba ON b.book_id = ba.book_id
INNER JOIN authors a ON ba.author_id = a.author_id
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE a.name LIKE '%J.K. Rowling%'
ORDER BY b.title;

-- 3. Search books by ISBN
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    b.genre,
    b.publication_year,
    b.status,
    i.available_copies,
    i.total_copies,
    lib.name as library_name
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE b.isbn = '9780747532699';

-- 4. Search books by genre
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    b.genre,
    b.publication_year,
    i.available_copies,
    lib.name as library_name
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE b.genre = 'Fantasy'
ORDER BY b.title;

-- 5. Search available books
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    b.genre,
    i.available_copies,
    i.total_copies,
    lib.name as library_name
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE b.status = 'AVAILABLE' AND i.available_copies > 0
ORDER BY b.title;

-- =====================================================
-- User History Queries
-- =====================================================

-- 6. Get member's loan history
SELECT 
    l.loan_id,
    b.title as book_title,
    b.isbn,
    l.loan_date,
    l.due_date,
    l.return_date,
    l.status,
    l.fine_amount,
    DATEDIFF(day, l.loan_date, COALESCE(l.return_date, GETDATE())) as days_borrowed
FROM loans l
INNER JOIN books b ON l.book_id = b.book_id
INNER JOIN members m ON l.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE u.email = 'alice.johnson@email.com'
ORDER BY l.loan_date DESC;

-- 7. Get member's current loans
SELECT 
    l.loan_id,
    b.title as book_title,
    b.isbn,
    l.loan_date,
    l.due_date,
    l.fine_amount,
    DATEDIFF(day, GETDATE(), l.due_date) as days_remaining
FROM loans l
INNER JOIN books b ON l.book_id = b.book_id
INNER JOIN members m ON l.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE u.email = 'alice.johnson@email.com' 
AND l.status = 'ACTIVE'
ORDER BY l.due_date;

-- 8. Get member's overdue books
SELECT 
    l.loan_id,
    b.title as book_title,
    b.isbn,
    l.loan_date,
    l.due_date,
    l.fine_amount,
    DATEDIFF(day, l.due_date, GETDATE()) as days_overdue
FROM loans l
INNER JOIN books b ON l.book_id = b.book_id
INNER JOIN members m ON l.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE u.email = 'alice.johnson@email.com' 
AND l.status = 'ACTIVE' 
AND l.due_date < GETDATE()
ORDER BY l.due_date;

-- 9. Get member's fine history
SELECT 
    f.transaction_id,
    b.title as book_title,
    f.issued_date,
    f.paid_date,
    f.reason,
    f.fine_type,
    f.is_paid,
    t.amount
FROM fines f
INNER JOIN transactions t ON f.transaction_id = t.transaction_id
INNER JOIN loans l ON f.loan_id = l.loan_id
INNER JOIN books b ON l.book_id = b.book_id
INNER JOIN members m ON l.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE u.email = 'alice.johnson@email.com'
ORDER BY f.issued_date DESC;

-- 10. Get member's reservations
SELECT 
    r.reservation_id,
    b.title as book_title,
    b.isbn,
    r.reservation_date,
    r.expiry_date,
    r.status,
    r.notes,
    DATEDIFF(day, GETDATE(), r.expiry_date) as days_until_expiry
FROM reservations r
INNER JOIN books b ON r.book_id = b.book_id
INNER JOIN members m ON r.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE u.email = 'alice.johnson@email.com'
ORDER BY r.reservation_date DESC;

-- =====================================================
-- Library Staff Queries
-- =====================================================

-- 11. Get all overdue books
SELECT 
    l.loan_id,
    b.title as book_title,
    b.isbn,
    u.name as member_name,
    u.email as member_email,
    l.loan_date,
    l.due_date,
    l.fine_amount,
    DATEDIFF(day, l.due_date, GETDATE()) as days_overdue
FROM loans l
INNER JOIN books b ON l.book_id = b.book_id
INNER JOIN members m ON l.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE l.status = 'ACTIVE' 
AND l.due_date < GETDATE()
ORDER BY l.due_date;

-- 12. Get books with low inventory
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    i.total_copies,
    i.available_copies,
    i.reserved_copies,
    i.damaged_copies,
    lib.name as library_name
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE i.available_copies <= 1
ORDER BY i.available_copies, b.title;

-- 13. Get damaged books
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    i.damaged_copies,
    i.total_copies,
    lib.name as library_name
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE i.damaged_copies > 0
ORDER BY i.damaged_copies DESC;

-- 14. Get active reservations
SELECT 
    r.reservation_id,
    b.title as book_title,
    b.isbn,
    u.name as member_name,
    u.email as member_email,
    r.reservation_date,
    r.expiry_date,
    r.status,
    DATEDIFF(day, GETDATE(), r.expiry_date) as days_until_expiry
FROM reservations r
INNER JOIN books b ON r.book_id = b.book_id
INNER JOIN members m ON r.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE r.status IN ('PENDING', 'ACTIVE')
ORDER BY r.reservation_date;

-- 15. Get members with outstanding fines
SELECT 
    m.user_id,
    u.name as member_name,
    u.email as member_email,
    m.membership_number,
    m.total_fines,
    COUNT(l.loan_id) as active_loans
FROM members m
INNER JOIN users u ON m.user_id = u.user_id
LEFT JOIN loans l ON m.user_id = l.member_id AND l.status = 'ACTIVE'
WHERE m.total_fines > 0
GROUP BY m.user_id, u.name, u.email, m.membership_number, m.total_fines
ORDER BY m.total_fines DESC;

-- =====================================================
-- Statistical Queries
-- =====================================================

-- 16. Get total books by library
SELECT 
    lib.library_id,
    lib.name as library_name,
    COUNT(b.book_id) as total_books,
    SUM(i.total_copies) as total_copies,
    SUM(i.available_copies) as available_copies,
    SUM(i.reserved_copies) as reserved_copies,
    SUM(i.damaged_copies) as damaged_copies
FROM libraries lib
LEFT JOIN inventories i ON lib.library_id = i.library_id
LEFT JOIN books b ON i.book_id = b.book_id
GROUP BY lib.library_id, lib.name
ORDER BY total_books DESC;

-- 17. Get total books by genre
SELECT 
    b.genre,
    COUNT(b.book_id) as total_books,
    SUM(i.total_copies) as total_copies,
    SUM(i.available_copies) as available_copies
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
GROUP BY b.genre
ORDER BY total_books DESC;

-- 18. Get total members by library
SELECT 
    lib.library_id,
    lib.name as library_name,
    COUNT(m.user_id) as total_members,
    COUNT(CASE WHEN m.membership_end >= GETDATE() THEN 1 END) as active_members,
    COUNT(CASE WHEN m.membership_end < GETDATE() THEN 1 END) as expired_members
FROM libraries lib
LEFT JOIN members m ON lib.library_id = m.library_id
GROUP BY lib.library_id, lib.name
ORDER BY total_members DESC;

-- 19. Get loan statistics by month
SELECT 
    YEAR(l.loan_date) as loan_year,
    MONTH(l.loan_date) as loan_month,
    COUNT(l.loan_id) as total_loans,
    COUNT(CASE WHEN l.status = 'RETURNED' THEN 1 END) as returned_loans,
    COUNT(CASE WHEN l.status = 'ACTIVE' THEN 1 END) as active_loans,
    COUNT(CASE WHEN l.due_date < GETDATE() AND l.status = 'ACTIVE' THEN 1 END) as overdue_loans
FROM loans l
WHERE l.loan_date >= DATEADD(month, -12, GETDATE())
GROUP BY YEAR(l.loan_date), MONTH(l.loan_date)
ORDER BY loan_year DESC, loan_month DESC;

-- 20. Get fine statistics
SELECT 
    f.fine_type,
    COUNT(f.transaction_id) as total_fines,
    SUM(t.amount) as total_amount,
    AVG(t.amount) as average_amount,
    COUNT(CASE WHEN f.is_paid = 1 THEN 1 END) as paid_fines,
    COUNT(CASE WHEN f.is_paid = 0 THEN 1 END) as unpaid_fines
FROM fines f
INNER JOIN transactions t ON f.transaction_id = t.transaction_id
GROUP BY f.fine_type
ORDER BY total_amount DESC;

-- =====================================================
-- Notification Queries
-- =====================================================

-- 21. Get user's notifications
SELECT 
    n.notification_id,
    n.title,
    n.message,
    n.type,
    n.priority,
    n.status,
    n.is_read,
    n.created_date,
    n.sent_date,
    n.read_date
FROM notifications n
INNER JOIN users u ON n.user_id = u.user_id
WHERE u.email = 'alice.johnson@email.com'
ORDER BY n.created_date DESC;

-- 22. Get unread notifications count
SELECT 
    u.email,
    COUNT(n.notification_id) as unread_count
FROM users u
LEFT JOIN notifications n ON u.user_id = n.user_id AND n.is_read = 0
GROUP BY u.email
ORDER BY unread_count DESC;

-- 23. Get notifications by type
SELECT 
    n.type,
    COUNT(n.notification_id) as total_notifications,
    COUNT(CASE WHEN n.is_read = 1 THEN 1 END) as read_notifications,
    COUNT(CASE WHEN n.is_read = 0 THEN 1 END) as unread_notifications
FROM notifications n
GROUP BY n.type
ORDER BY total_notifications DESC;

-- 24. Get urgent notifications
SELECT 
    n.notification_id,
    n.title,
    n.message,
    n.priority,
    u.name as user_name,
    u.email as user_email,
    n.created_date
FROM notifications n
INNER JOIN users u ON n.user_id = u.user_id
WHERE n.priority IN ('HIGH', 'URGENT')
ORDER BY n.created_date DESC;

-- 25. Get failed notifications for retry
SELECT 
    n.notification_id,
    n.title,
    n.message,
    n.retry_count,
    n.created_date,
    u.email as user_email
FROM notifications n
INNER JOIN users u ON n.user_id = u.user_id
WHERE n.status = 'FAILED' AND n.retry_count < 3
ORDER BY n.created_date;

PRINT 'Common queries executed successfully!';
GO 