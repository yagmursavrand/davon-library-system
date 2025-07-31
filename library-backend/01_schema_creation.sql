-- =====================================================
-- Library Management System - MSSQL Database Schema
-- =====================================================

-- Create Database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'LibraryManagementSystem')
BEGIN
    CREATE DATABASE LibraryManagementSystem;
END
GO

USE LibraryManagementSystem;
GO

-- Drop existing views
IF OBJECT_ID('dbo.v_overdue_books', 'V') IS NOT NULL DROP VIEW dbo.v_overdue_books;
IF OBJECT_ID('dbo.v_available_books', 'V') IS NOT NULL DROP VIEW dbo.v_available_books;
IF OBJECT_ID('dbo.v_member_activity', 'V') IS NOT NULL DROP VIEW dbo.v_member_activity;
GO

-- Drop existing stored procedures
IF OBJECT_ID('dbo.sp_CalculateOverdueFine', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_CalculateOverdueFine;
IF OBJECT_ID('dbo.sp_GetPopularBooks', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_GetPopularBooks;
GO

-- Drop existing constraints
IF OBJECT_ID('dbo.FK__users__role_id__3D5E1FD2', 'F') IS NOT NULL ALTER TABLE dbo.users DROP CONSTRAINT FK__users__role_id__3D5E1FD2;
IF OBJECT_ID('dbo.FK__members__user_id__4AB81AF0', 'F') IS NOT NULL ALTER TABLE dbo.members DROP CONSTRAINT FK__members__user_id__4AB81AF0;
IF OBJECT_ID('dbo.FK__members__library__4BAC3F29', 'F') IS NOT NULL ALTER TABLE dbo.members DROP CONSTRAINT FK__members__library__4BAC3F29;
IF OBJECT_ID('dbo.FK__loans__book_id__68487DD7', 'F') IS NOT NULL ALTER TABLE dbo.loans DROP CONSTRAINT FK__loans__book_id__68487DD7;
IF OBJECT_ID('dbo.FK__loans__member_id__693CA210', 'F') IS NOT NULL ALTER TABLE dbo.loans DROP CONSTRAINT FK__loans__member_id__693CA210;
IF OBJECT_ID('dbo.FK__transacti__user___6E01572D', 'F') IS NOT NULL ALTER TABLE dbo.transactions DROP CONSTRAINT FK__transacti__user___6E01572D;
IF OBJECT_ID('dbo.FK__fines__transacti__73BA3083', 'F') IS NOT NULL ALTER TABLE dbo.fines DROP CONSTRAINT FK__fines__transacti__73BA3083;
IF OBJECT_ID('dbo.FK__fines__loan_id__74AE54BC', 'F') IS NOT NULL ALTER TABLE dbo.fines DROP CONSTRAINT FK__fines__loan_id__74AE54BC;
GO

-- Drop existing tables in reverse order of creation to avoid foreign key constraints
IF OBJECT_ID('dbo.member_fine_history', 'U') IS NOT NULL DROP TABLE dbo.member_fine_history;
IF OBJECT_ID('dbo.member_borrowed_books', 'U') IS NOT NULL DROP TABLE dbo.member_borrowed_books;
IF OBJECT_ID('dbo.notifications', 'U') IS NOT NULL DROP TABLE dbo.notifications;
IF OBJECT_ID('dbo.reservations', 'U') IS NOT NULL DROP TABLE dbo.reservations;
IF OBJECT_ID('dbo.fines', 'U') IS NOT NULL DROP TABLE dbo.fines;
IF OBJECT_ID('dbo.transactions', 'U') IS NOT NULL DROP TABLE dbo.transactions;
IF OBJECT_ID('dbo.loans', 'U') IS NOT NULL DROP TABLE dbo.loans;
IF OBJECT_ID('dbo.inventories', 'U') IS NOT NULL DROP TABLE dbo.inventories;
IF OBJECT_ID('dbo.book_authors', 'U') IS NOT NULL DROP TABLE dbo.book_authors;
IF OBJECT_ID('dbo.books', 'U') IS NOT NULL DROP TABLE dbo.books;
IF OBJECT_ID('dbo.authors', 'U') IS NOT NULL DROP TABLE dbo.authors;
IF OBJECT_ID('dbo.admins', 'U') IS NOT NULL DROP TABLE dbo.admins;
IF OBJECT_ID('dbo.members', 'U') IS NOT NULL DROP TABLE dbo.members;
IF OBJECT_ID('dbo.libraries', 'U') IS NOT NULL DROP TABLE dbo.libraries;
IF OBJECT_ID('dbo.profiles', 'U') IS NOT NULL DROP TABLE dbo.profiles;
IF OBJECT_ID('dbo.role_permissions', 'U') IS NOT NULL DROP TABLE dbo.role_permissions;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL DROP TABLE dbo.roles;
GO

-- =====================================================
-- Create Tables
-- =====================================================

-- Roles Table
CREATE TABLE roles (
    role_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
GO

-- Users Table
CREATE TABLE users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NULL,
    is_logged_in BIT DEFAULT 0,
    last_login_date DATETIME2 NULL,
    role_id BIGINT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);
GO

-- Role Permissions Table
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (role_id, permission),
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);
GO

-- Profiles Table
CREATE TABLE profiles (
    profile_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    address VARCHAR(500) NULL,
    phone VARCHAR(20) NULL,
    birth_date DATE NULL,
    last_updated DATETIME2 NULL,
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- Libraries Table
CREATE TABLE libraries (
    library_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    opening_hours VARCHAR(200) NULL
);
GO

-- Members Table
CREATE TABLE members (
    user_id BIGINT PRIMARY KEY,
    membership_number VARCHAR(50) UNIQUE,
    membership_start DATE NULL,
    membership_end DATE NULL,
    total_fines DECIMAL(10,2) DEFAULT 0.0,
    library_id BIGINT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (library_id) REFERENCES libraries(library_id)
);
GO

-- Admins Table
CREATE TABLE admins (
    user_id BIGINT PRIMARY KEY,
    admin_level VARCHAR(20) DEFAULT 'STANDARD',
    department VARCHAR(100) NULL,
    last_admin_action DATETIME2 NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- Authors Table
CREATE TABLE authors (
    author_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    bio VARCHAR(1000) NULL
);
GO

-- Books Table
CREATE TABLE books (
    book_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    publication_year INT DEFAULT 0,
    genre VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);
GO

-- Book Authors Junction Table (Many-to-Many)
CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (author_id) REFERENCES authors(author_id)
);
GO

-- Inventories Table
CREATE TABLE inventories (
    inventory_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    total_copies INT NOT NULL DEFAULT 0,
    available_copies INT NOT NULL DEFAULT 0,
    reserved_copies INT DEFAULT 0,
    damaged_copies INT DEFAULT 0,
    book_id BIGINT NOT NULL UNIQUE,
    library_id BIGINT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (library_id) REFERENCES libraries(library_id)
);
GO

-- Loans Table
CREATE TABLE loans (
    loan_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    due_date DATETIME2 NOT NULL,
    return_date DATETIME2 NULL,
    fine_amount DECIMAL(10,2) DEFAULT 0.0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (member_id) REFERENCES members(user_id)
);
GO

-- Transactions Table
CREATE TABLE transactions (
    transaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    transaction_type VARCHAR(31) NOT NULL,
    date DATETIME2 NOT NULL DEFAULT GETDATE(),
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    user_id BIGINT NULL,
    -- Columns for Fine
    issued_date DATETIME2 NULL,
    paid_date DATETIME2 NULL,
    reason VARCHAR(500) NULL,
    fine_type VARCHAR(20) NULL,
    is_paid BIT NULL,
    loan_id BIGINT NULL,
    -- Columns for Payment
    payment_date DATETIME2 NULL,
    payment_method VARCHAR(30) NULL,
    reference_number VARCHAR(100) NULL,
    confirmation_number VARCHAR(100) NULL,
    fine_id BIGINT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (loan_id) REFERENCES loans(loan_id),
    FOREIGN KEY (fine_id) REFERENCES transactions(transaction_id)
);
GO

-- Reservations Table
CREATE TABLE reservations (
    reservation_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    reservation_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    expiry_date DATETIME2 NOT NULL,
    notification_date DATETIME2 NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500) NULL,
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (member_id) REFERENCES members(user_id)
);
GO

-- Member Borrowed Books Collection Table
CREATE TABLE member_borrowed_books (
    member_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    PRIMARY KEY (member_id, book_id),
    FOREIGN KEY (member_id) REFERENCES members(user_id)
);
GO

-- Member Fine History Collection Table
CREATE TABLE member_fine_history (
    member_id BIGINT NOT NULL,
    fine_description VARCHAR(500) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(user_id)
);
GO

-- Notifications Table
CREATE TABLE notifications (
    notification_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    created_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    sent_date DATETIME2 NULL,
    read_date DATETIME2 NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_read BIT DEFAULT 0,
    related_entity_type VARCHAR(50) NULL,
    related_entity_id BIGINT NULL,
    retry_count INT DEFAULT 0,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- =====================================================
-- Create Indexes for Performance
-- =====================================================

-- Users Indexes
CREATE INDEX IX_users_email ON users(email);
CREATE INDEX IX_users_role ON users(role);
GO

-- Books Indexes
CREATE INDEX IX_books_isbn ON books(isbn);
CREATE INDEX IX_books_title ON books(title);
CREATE INDEX IX_books_genre ON books(genre);
CREATE INDEX IX_books_status ON books(status);
GO

-- Loans Indexes
CREATE INDEX IX_loans_member_id ON loans(member_id);
CREATE INDEX IX_loans_book_id ON loans(book_id);
CREATE INDEX IX_loans_status ON loans(status);
CREATE INDEX IX_loans_due_date ON loans(due_date);
GO

-- Reservations Indexes
CREATE INDEX IX_reservations_member_id ON reservations(member_id);
CREATE INDEX IX_reservations_book_id ON reservations(book_id);
CREATE INDEX IX_reservations_status ON reservations(status);
CREATE INDEX IX_reservations_expiry_date ON reservations(expiry_date);
GO

-- Transactions Indexes
CREATE INDEX IX_transactions_user_id ON transactions(user_id);
CREATE INDEX IX_transactions_type ON transactions(type);
CREATE INDEX IX_transactions_status ON transactions(status);
CREATE INDEX IX_transactions_date ON transactions(date);
GO

-- Members Indexes
CREATE INDEX IX_members_membership_number ON members(membership_number);
CREATE INDEX IX_members_library_id ON members(library_id);
GO

-- Notifications Indexes
CREATE INDEX IX_notifications_user_id ON notifications(user_id);
CREATE INDEX IX_notifications_type ON notifications(type);
CREATE INDEX IX_notifications_status ON notifications(status);
CREATE INDEX IX_notifications_created_date ON notifications(created_date);
CREATE INDEX IX_notifications_is_read ON notifications(is_read);
GO

-- =====================================================
-- Create Views for Common Queries
-- =====================================================

-- View for Overdue Books
CREATE VIEW v_overdue_books AS
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    l.loan_id,
    l.due_date,
    l.loan_date,
    m.user_id as member_id,
    u.name as member_name,
    DATEDIFF(day, l.due_date, GETDATE()) as days_overdue
FROM books b
INNER JOIN loans l ON b.book_id = l.book_id
INNER JOIN members m ON l.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE l.status = 'ACTIVE' 
AND l.due_date < GETDATE();
GO

-- View for Available Books
CREATE VIEW v_available_books AS
SELECT 
    b.book_id,
    b.title,
    b.isbn,
    b.genre,
    b.publication_year,
    i.total_copies,
    i.available_copies,
    lib.name as library_name
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE b.status = 'AVAILABLE' AND i.available_copies > 0;
GO

-- View for Member Activity
CREATE VIEW v_member_activity AS
SELECT 
    m.user_id,
    u.name,
    u.email,
    m.membership_number,
    COUNT(l.loan_id) as total_loans,
    COUNT(CASE WHEN l.status = 'ACTIVE' THEN 1 END) as active_loans,
    COUNT(CASE WHEN l.due_date < GETDATE() AND l.status = 'ACTIVE' THEN 1 END) as overdue_loans,
    m.total_fines
FROM members m
INNER JOIN users u ON m.user_id = u.user_id
LEFT JOIN loans l ON m.user_id = l.member_id
GROUP BY m.user_id, u.name, u.email, m.membership_number, m.total_fines;
GO

-- =====================================================
-- Create Stored Procedures
-- =====================================================

-- Stored Procedure to Calculate Fine for Overdue Loan
CREATE PROCEDURE sp_CalculateOverdueFine
    @LoanId BIGINT,
    @DailyRate DECIMAL(10,2) = 2.0,
    @MaxFine DECIMAL(10,2) = 50.0
AS
BEGIN
    DECLARE @DaysOverdue INT;
    DECLARE @FineAmount DECIMAL(10,2);
    DECLARE @LoanExists BIT;
    
    -- Check if loan exists and is overdue
    SELECT @LoanExists = 1, @DaysOverdue = DATEDIFF(day, due_date, GETDATE())
    FROM loans 
    WHERE loan_id = @LoanId AND status = 'ACTIVE' AND due_date < GETDATE();
    
    IF @LoanExists = 1 AND @DaysOverdue > 0
    BEGIN
        SET @FineAmount = @DaysOverdue * @DailyRate;
        
        IF @FineAmount > @MaxFine
            SET @FineAmount = @MaxFine;
            
        -- Update loan with fine amount
        UPDATE loans 
        SET fine_amount = @FineAmount 
        WHERE loan_id = @LoanId;
        
        SELECT @FineAmount as calculated_fine;
    END
    ELSE
    BEGIN
        SELECT 0 as calculated_fine;
    END
END;
GO

-- Stored Procedure to Get Popular Books
CREATE PROCEDURE sp_GetPopularBooks
    @TopCount INT = 10,
    @StartDate DATETIME2 = NULL,
    @EndDate DATETIME2 = NULL
AS
BEGIN
    IF @StartDate IS NULL
        SET @StartDate = DATEADD(month, -1, GETDATE());
    IF @EndDate IS NULL
        SET @EndDate = GETDATE();
        
    SELECT TOP(@TopCount)
        b.book_id,
        b.title,
        b.isbn,
        b.genre,
        COUNT(l.loan_id) as loan_count
    FROM books b
    INNER JOIN loans l ON b.book_id = l.book_id
    WHERE l.loan_date BETWEEN @StartDate AND @EndDate
    GROUP BY b.book_id, b.title, b.isbn, b.genre
    ORDER BY loan_count DESC;
END;
GO

-- =====================================================
-- Create Triggers
-- =====================================================

-- Trigger to update book status when loan is created
CREATE TRIGGER tr_LoanCreated_UpdateBookStatus
ON loans
AFTER INSERT
AS
BEGIN
    -- This trigger now correctly references the 'inserted' pseudo-table
    UPDATE books 
    SET status = 'BORROWED'
    FROM books b
    INNER JOIN inserted i ON b.book_id = i.book_id;
    
    UPDATE inventories 
    SET available_copies = available_copies - 1
    FROM inventories inv
    INNER JOIN inserted i ON inv.book_id = i.book_id;
END;
GO

-- Trigger to update book status when loan is returned
CREATE TRIGGER tr_LoanReturned_UpdateBookStatus
ON loans
AFTER UPDATE
AS
BEGIN
    IF UPDATE(status)
    BEGIN
        UPDATE books 
        SET status = 'AVAILABLE'
        FROM books b
        INNER JOIN inserted i ON b.book_id = i.book_id
        WHERE i.status = 'RETURNED';
        
        UPDATE inventories 
        SET available_copies = available_copies + 1
        FROM inventories inv
        INNER JOIN inserted i ON inv.book_id = i.book_id
        WHERE i.status = 'RETURNED';
    END
END;
GO

-- Trigger to update user's updated_at timestamp
CREATE TRIGGER tr_UserUpdated_UpdateTimestamp
ON users
AFTER UPDATE
AS
BEGIN
    UPDATE users 
    SET updated_at = GETDATE()
    FROM users u
    INNER JOIN inserted i ON u.user_id = i.user_id;
END;
GO

PRINT 'Library Management System Database Schema Created Successfully!';
GO
