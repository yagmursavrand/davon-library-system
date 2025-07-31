-- =====================================================
-- Library Management System - Sample Data Insertion
-- =====================================================

USE LibraryManagementSystem;
GO

-- =====================================================
-- Insert Roles
-- =====================================================
INSERT INTO roles (name) VALUES 
('ADMIN'),
('LIBRARIAN'),
('MEMBER'),
('GUEST');
GO

-- =====================================================
-- Insert Role Permissions
-- =====================================================
INSERT INTO role_permissions (role_id, permission) VALUES 
(1, 'MANAGE_USERS'),
(1, 'MANAGE_BOOKS'),
(1, 'MANAGE_LOANS'),
(1, 'MANAGE_FINES'),
(1, 'GENERATE_REPORTS'),
(1, 'SYSTEM_ADMIN'),
(2, 'MANAGE_BOOKS'),
(2, 'MANAGE_LOANS'),
(2, 'MANAGE_FINES'),
(2, 'GENERATE_REPORTS'),
(3, 'BORROW_BOOKS'),
(3, 'RESERVE_BOOKS'),
(3, 'VIEW_OWN_HISTORY'),
(4, 'VIEW_BOOKS'),
(4, 'SEARCH_BOOKS');
GO

-- =====================================================
-- Insert Libraries
-- =====================================================
INSERT INTO libraries (name, address, phone, email, opening_hours) VALUES 
('Central Library', '123 Main Street, Downtown', '+1-555-0101', 'central@library.com', 'Mon-Fri: 9AM-9PM, Sat-Sun: 10AM-6PM'),
('North Branch Library', '456 Oak Avenue, North District', '+1-555-0102', 'north@library.com', 'Mon-Fri: 8AM-8PM, Sat: 9AM-5PM'),
('South Community Library', '789 Pine Road, South District', '+1-555-0103', 'south@library.com', 'Mon-Sun: 10AM-8PM');
GO

-- =====================================================
-- Insert Users
-- =====================================================
INSERT INTO users (name, email, password, role, role_id, created_at) VALUES 
-- Admins
('John Admin', 'john.admin@library.com', 'hashed_password_123', 'ADMIN', 1, GETDATE()),
('Sarah Manager', 'sarah.manager@library.com', 'hashed_password_456', 'ADMIN', 1, GETDATE()),

-- Librarians
('Mike Librarian', 'mike.librarian@library.com', 'hashed_password_789', 'LIBRARIAN', 2, GETDATE()),
('Lisa Assistant', 'lisa.assistant@library.com', 'hashed_password_101', 'LIBRARIAN', 2, GETDATE()),

-- Members
('Alice Johnson', 'alice.johnson@email.com', 'hashed_password_111', 'MEMBER', 3, GETDATE()),
('Bob Smith', 'bob.smith@email.com', 'hashed_password_222', 'MEMBER', 3, GETDATE()),
('Carol Davis', 'carol.davis@email.com', 'hashed_password_333', 'MEMBER', 3, GETDATE()),
('David Wilson', 'david.wilson@email.com', 'hashed_password_444', 'MEMBER', 3, GETDATE()),
('Emma Brown', 'emma.brown@email.com', 'hashed_password_555', 'MEMBER', 3, GETDATE()),
('Frank Miller', 'frank.miller@email.com', 'hashed_password_666', 'MEMBER', 3, GETDATE()),

-- Guests
('Guest User', 'guest@email.com', 'hashed_password_999', 'GUEST', 4, GETDATE());
GO

-- =====================================================
-- Insert Profiles
-- =====================================================
INSERT INTO profiles (address, phone, birth_date, user_id) VALUES 
('123 Admin Street, Downtown', '+1-555-1001', '1980-05-15', 1),
('456 Manager Avenue, Uptown', '+1-555-1002', '1985-08-22', 2),
('789 Librarian Road, Midtown', '+1-555-1003', '1990-03-10', 3),
('321 Assistant Lane, Downtown', '+1-555-1004', '1988-12-05', 4),
('654 Johnson Street, North District', '+1-555-1005', '1992-07-18', 5),
('987 Smith Avenue, South District', '+1-555-1006', '1987-11-30', 6),
('147 Davis Road, East District', '+1-555-1007', '1995-04-12', 7),
('258 Wilson Lane, West District', '+1-555-1008', '1983-09-25', 8),
('369 Brown Street, Central District', '+1-555-1009', '1991-01-08', 9),
('741 Miller Avenue, Downtown', '+1-555-1010', '1989-06-14', 10);
GO

-- =====================================================
-- Insert Admins
-- =====================================================
INSERT INTO admins (user_id, admin_level, department, last_admin_action) VALUES 
(1, 'SUPER_ADMIN', 'System Administration', GETDATE()),
(2, 'SENIOR', 'Library Management', DATEADD(day, -1, GETDATE()));
GO

-- =====================================================
-- Insert Members
-- =====================================================
INSERT INTO members (user_id, membership_number, membership_start, membership_end, library_id) VALUES 
(5, 'MEM001', '2023-01-15', '2024-01-15', 1),
(6, 'MEM002', '2023-02-20', '2024-02-20', 1),
(7, 'MEM003', '2023-03-10', '2024-03-10', 2),
(8, 'MEM004', '2023-04-05', '2024-04-05', 2),
(9, 'MEM005', '2023-05-12', '2024-05-12', 3),
(10, 'MEM006', '2023-06-18', '2024-06-18', 3);
GO

-- =====================================================
-- Insert Authors
-- =====================================================
INSERT INTO authors (name, bio) VALUES 
('J.K. Rowling', 'British author best known for the Harry Potter series'),
('George R.R. Martin', 'American novelist and short story writer, creator of A Song of Ice and Fire'),
('Stephen King', 'American author of horror, supernatural fiction, suspense, and fantasy novels'),
('Agatha Christie', 'English writer known for her detective novels'),
('Ernest Hemingway', 'American novelist, short story writer, and journalist'),
('Jane Austen', 'English novelist known for her romantic fiction'),
('William Shakespeare', 'English playwright, poet, and actor'),
('Charles Dickens', 'English writer and social critic'),
('Mark Twain', 'American writer, humorist, entrepreneur, publisher, and lecturer'),
('F. Scott Fitzgerald', 'American novelist and short story writer'),
('Harper Lee', 'American novelist best known for To Kill a Mockingbird'),
('George Orwell', 'English novelist, essayist, journalist, and critic');
GO

-- =====================================================
-- Insert Books
-- =====================================================
INSERT INTO books (title, isbn, publication_year, genre, status) VALUES 
('Harry Potter and the Philosopher''s Stone', '9780747532699', 1997, 'Fantasy', 'AVAILABLE'),
('Harry Potter and the Chamber of Secrets', '9780747538493', 1998, 'Fantasy', 'AVAILABLE'),
('A Game of Thrones', '9780553103540', 1996, 'Fantasy', 'AVAILABLE'),
('A Clash of Kings', '9780553108033', 1998, 'Fantasy', 'AVAILABLE'),
('The Shining', '9780385121675', 1977, 'Horror', 'AVAILABLE'),
('It', '9780450411434', 1986, 'Horror', 'AVAILABLE'),
('Murder on the Orient Express', '9780007119318', 1934, 'Mystery', 'AVAILABLE'),
('The Old Man and the Sea', '9780684801223', 1952, 'Fiction', 'AVAILABLE'),
('Pride and Prejudice', '9780141439518', 1813, 'Romance', 'AVAILABLE'),
('Romeo and Juliet', '9780743477116', 1597, 'Drama', 'AVAILABLE'),
('Great Expectations', '9780141439563', 1861, 'Fiction', 'AVAILABLE'),
('The Adventures of Tom Sawyer', '9780143039562', 1876, 'Adventure', 'AVAILABLE'),
('The Great Gatsby', '9780743273565', 1925, 'Fiction', 'AVAILABLE'),
('To Kill a Mockingbird', '9780446310789', 1960, 'Fiction', 'AVAILABLE'),
('1984', '9780451524935', 1949, 'Dystopian', 'AVAILABLE');
GO

-- =====================================================
-- Insert Book Authors (Many-to-Many)
-- =====================================================
INSERT INTO book_authors (book_id, author_id) VALUES 
(1, 1), (2, 1),           -- Harry Potter books by J.K. Rowling
(3, 2), (4, 2),           -- Game of Thrones books by George R.R. Martin
(5, 3), (6, 3),           -- Stephen King books
(7, 4),                   -- Agatha Christie book
(8, 5),                   -- Ernest Hemingway book
(9, 6),                   -- Jane Austen book
(10, 7),                  -- William Shakespeare book
(11, 8),                  -- Charles Dickens book
(12, 9),                  -- Mark Twain book
(13, 10),                 -- F. Scott Fitzgerald book
(14, 11),                 -- Harper Lee book
(15, 12);                 -- George Orwell book
GO

-- =====================================================
-- Insert Inventories
-- =====================================================
INSERT INTO inventories (total_copies, available_copies, reserved_copies, damaged_copies, book_id, library_id) VALUES 
(5, 4, 1, 0, 1, 1),   -- Harry Potter 1: 5 total, 4 available, 1 reserved
(3, 2, 0, 1, 2, 1),   -- Harry Potter 2: 3 total, 2 available, 1 damaged
(4, 3, 1, 0, 3, 1),   -- Game of Thrones: 4 total, 3 available, 1 reserved
(2, 1, 0, 1, 4, 1),   -- Clash of Kings: 2 total, 1 available, 1 damaged
(3, 2, 0, 1, 5, 2),   -- The Shining: 3 total, 2 available, 1 damaged
(4, 3, 1, 0, 6, 2),   -- It: 4 total, 3 available, 1 reserved
(2, 1, 1, 0, 7, 2),   -- Murder on Orient Express: 2 total, 1 available, 1 reserved
(3, 2, 0, 1, 8, 3),   -- Old Man and Sea: 3 total, 2 available, 1 damaged
(5, 4, 1, 0, 9, 3),   -- Pride and Prejudice: 5 total, 4 available, 1 reserved
(2, 1, 0, 1, 10, 3),  -- Romeo and Juliet: 2 total, 1 available, 1 damaged
(4, 3, 1, 0, 11, 1),  -- Great Expectations: 4 total, 3 available, 1 reserved
(3, 2, 0, 1, 12, 2),  -- Tom Sawyer: 3 total, 2 available, 1 damaged
(2, 1, 1, 0, 13, 3),  -- Great Gatsby: 2 total, 1 available, 1 reserved
(3, 2, 0, 1, 14, 1),  -- To Kill a Mockingbird: 3 total, 2 available, 1 damaged
(4, 3, 1, 0, 15, 2);  -- 1984: 4 total, 3 available, 1 reserved
GO

-- =====================================================
-- Insert Loans
-- =====================================================
INSERT INTO loans (loan_date, due_date, return_date, fine_amount, status, book_id, member_id) VALUES 
-- Active loans
(DATEADD(day, -10, GETDATE()), DATEADD(day, 4, GETDATE()), NULL, 0.0, 'ACTIVE', 1, 5),
(DATEADD(day, -5, GETDATE()), DATEADD(day, 9, GETDATE()), NULL, 0.0, 'ACTIVE', 3, 6),
(DATEADD(day, -15, GETDATE()), DATEADD(day, -1, GETDATE()), NULL, 2.0, 'ACTIVE', 5, 7),  -- Overdue
(DATEADD(day, -20, GETDATE()), DATEADD(day, -6, GETDATE()), NULL, 12.0, 'ACTIVE', 7, 8), -- Overdue

-- Returned loans
(DATEADD(day, -30, GETDATE()), DATEADD(day, -16, GETDATE()), DATEADD(day, -15, GETDATE()), 0.0, 'RETURNED', 2, 5),
(DATEADD(day, -25, GETDATE()), DATEADD(day, -11, GETDATE()), DATEADD(day, -10, GETDATE()), 0.0, 'RETURNED', 4, 6),
(DATEADD(day, -40, GETDATE()), DATEADD(day, -26, GETDATE()), DATEADD(day, -25, GETDATE()), 0.0, 'RETURNED', 6, 7),
(DATEADD(day, -35, GETDATE()), DATEADD(day, -21, GETDATE()), DATEADD(day, -20, GETDATE()), 0.0, 'RETURNED', 8, 8);
GO

-- =====================================================
-- Insert Transactions
-- =====================================================
INSERT INTO transactions (transaction_type, date, type, amount, description, status, user_id, issued_date, paid_date, reason, fine_type, is_paid, loan_id) VALUES 
-- Membership fees
('TRANSACTION', GETDATE(), 'MEMBERSHIP_FEE', 25.00, 'Annual membership fee', 'COMPLETED', 5, NULL, NULL, NULL, NULL, NULL, NULL),
('TRANSACTION', GETDATE(), 'MEMBERSHIP_FEE', 25.00, 'Annual membership fee', 'COMPLETED', 6, NULL, NULL, NULL, NULL, NULL, NULL),
('TRANSACTION', GETDATE(), 'MEMBERSHIP_FEE', 25.00, 'Annual membership fee', 'COMPLETED', 7, NULL, NULL, NULL, NULL, NULL, NULL),
('TRANSACTION', GETDATE(), 'MEMBERSHIP_FEE', 25.00, 'Annual membership fee', 'COMPLETED', 8, NULL, NULL, NULL, NULL, NULL, NULL),
('TRANSACTION', GETDATE(), 'MEMBERSHIP_FEE', 25.00, 'Annual membership fee', 'COMPLETED', 9, NULL, NULL, NULL, NULL, NULL, NULL),
('TRANSACTION', GETDATE(), 'MEMBERSHIP_FEE', 25.00, 'Annual membership fee', 'COMPLETED', 10, NULL, NULL, NULL, NULL, NULL, NULL),

-- Fine transactions
('FINE', DATEADD(day, -1, GETDATE()), 'FINE', 2.00, 'Overdue fine for The Shining', 'COMPLETED', 7, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()), 'Book returned 1 day late', 'OVERDUE', 1, 3),
('FINE', DATEADD(day, -6, GETDATE()), 'FINE', 12.00, 'Overdue fine for Murder on Orient Express', 'PENDING', 8, DATEADD(day, -6, GETDATE()), NULL, 'Book returned 6 days late', 'OVERDUE', 0, 4);
GO

-- =====================================================
-- Insert Reservations
-- =====================================================
INSERT INTO reservations (reservation_date, expiry_date, status, book_id, member_id) VALUES 
(DATEADD(day, -2, GETDATE()), DATEADD(day, 5, GETDATE()), 'PENDING', 1, 9),
(DATEADD(day, -1, GETDATE()), DATEADD(day, 6, GETDATE()), 'ACTIVE', 3, 10),
(DATEADD(day, -3, GETDATE()), DATEADD(day, 4, GETDATE()), 'PENDING', 6, 5),
(DATEADD(day, -5, GETDATE()), DATEADD(day, 2, GETDATE()), 'ACTIVE', 7, 6),
(DATEADD(day, -1, GETDATE()), DATEADD(day, 6, GETDATE()), 'PENDING', 9, 7),
(DATEADD(day, -2, GETDATE()), DATEADD(day, 5, GETDATE()), 'ACTIVE', 11, 8),
(DATEADD(day, -1, GETDATE()), DATEADD(day, 6, GETDATE()), 'PENDING', 13, 9),
(DATEADD(day, -3, GETDATE()), DATEADD(day, 4, GETDATE()), 'ACTIVE', 15, 10);
GO

-- =====================================================
-- Insert Member Borrowed Books
-- =====================================================
INSERT INTO member_borrowed_books (member_id, book_id) VALUES 
(5, 1), (5, 2),  -- Alice Johnson borrowed books 1 and 2
(6, 3), (6, 4),  -- Bob Smith borrowed books 3 and 4
(7, 5), (7, 6),  -- Carol Davis borrowed books 5 and 6
(8, 7), (8, 8);  -- David Wilson borrowed books 7 and 8
GO

-- =====================================================
-- Insert Member Fine History
-- =====================================================
INSERT INTO member_fine_history (member_id, fine_description) VALUES 
(7, 'Fine added: $2.00 - Overdue return on 2024-01-14'),
(8, 'Fine added: $12.00 - Overdue return on 2024-01-09');
GO

-- Update member total fines
UPDATE members SET total_fines = 2.00 WHERE user_id = 7;
GO
UPDATE members SET total_fines = 12.00 WHERE user_id = 8;
GO

-- =====================================================
-- Insert Notifications
-- =====================================================
INSERT INTO notifications (created_date, sent_date, title, message, type, priority, status, is_read, user_id) VALUES 
-- Overdue reminders
(DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()), 'Overdue Book Reminder', 'Your book "The Shining" is overdue. Please return it as soon as possible.', 'OVERDUE_REMINDER', 'HIGH', 'SENT', 0, 7),
(DATEADD(day, -6, GETDATE()), DATEADD(day, -6, GETDATE()), 'Overdue Book Reminder', 'Your book "Murder on the Orient Express" is overdue. Please return it as soon as possible.', 'OVERDUE_REMINDER', 'URGENT', 'SENT', 0, 8),

-- Reservation notifications
(DATEADD(day, -2, GETDATE()), DATEADD(day, -2, GETDATE()), 'Book Available', 'The book "Harry Potter and the Philosopher''s Stone" is now available for pickup.', 'RESERVATION_AVAILABLE', 'MEDIUM', 'SENT', 1, 9),
(DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()), 'Book Available', 'The book "A Game of Thrones" is now available for pickup.', 'RESERVATION_AVAILABLE', 'MEDIUM', 'SENT', 0, 10),

-- Fine notifications
(DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()), 'Fine Issued', 'A fine of $2.00 has been issued for overdue book "The Shining".', 'FINE_ISSUED', 'HIGH', 'SENT', 1, 7),
(DATEADD(day, -6, GETDATE()), DATEADD(day, -6, GETDATE()), 'Fine Issued', 'A fine of $12.00 has been issued for overdue book "Murder on the Orient Express".', 'FINE_ISSUED', 'URGENT', 'SENT', 0, 8),

-- Membership notifications
(DATEADD(day, -5, GETDATE()), DATEADD(day, -5, GETDATE()), 'Membership Expiry', 'Your library membership will expire in 10 days. Please renew to continue borrowing books.', 'MEMBERSHIP_EXPIRY', 'MEDIUM', 'SENT', 0, 5),
(DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()), 'Membership Expiry', 'Your library membership will expire in 5 days. Please renew to continue borrowing books.', 'MEMBERSHIP_EXPIRY', 'HIGH', 'SENT', 0, 6),

-- System notifications
(GETDATE(), NULL, 'System Maintenance', 'The library system will be under maintenance tonight from 2 AM to 4 AM.', 'SYSTEM_MAINTENANCE', 'LOW', 'PENDING', 0, 5),
(GETDATE(), NULL, 'Welcome Message', 'Welcome to our library! We hope you enjoy your reading experience.', 'GENERAL', 'LOW', 'PENDING', 0, 9);
GO

PRINT 'Sample data inserted successfully!';
GO
