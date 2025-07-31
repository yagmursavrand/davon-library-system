# Library Management System - MSSQL Database Documentation

## Table of Contents
1. [Overview](#overview)
2. [Database Schema](#database-schema)
3. [Entity Relationship Diagram](#entity-relationship-diagram)
4. [Tables Description](#tables-description)
5. [Relationships](#relationships)
6. [Indexes and Performance](#indexes-and-performance)
7. [Views](#views)
8. [Stored Procedures](#stored-procedures)
9. [Triggers](#triggers)
10. [Common Queries](#common-queries)
11. [Reports](#reports)
12. [Sample Data](#sample-data)
13. [Integration with Java Backend](#integration-with-java-backend)

## Overview

The Library Management System database is designed to handle all aspects of library operations including:
- Book and author management
- Member registration and management
- Loan and reservation tracking
- Fine calculation and management
- Staff and permission management
- Financial transactions
- Inventory management

## Database Schema

### Core Tables
- **users** - Base user information
- **members** - Library member details
- **admins** - Administrative staff
- **libraries** - Library locations
- **books** - Book information
- **authors** - Author information
- **loans** - Book loan records
- **reservations** - Book reservation records
- **transactions** - Financial transactions
- **fines** - Fine records
- **inventories** - Book inventory management

### Junction Tables
- **book_authors** - Many-to-many relationship between books and authors
- **role_permissions** - Role-based permissions
- **member_borrowed_books** - Member's borrowed book history
- **member_fine_history** - Member's fine history

## Entity Relationship Diagram

The ERD shows the complete database structure with all relationships:
- **One-to-Many**: Library → Members, Library → Inventories, Book → Loans
- **Many-to-Many**: Books ↔ Authors
- **One-to-One**: User → Profile, Book → Inventory
- **Inheritance**: User → Member, User → Admin, Transaction → Fine

## Tables Description

### 1. users
Base table for all users in the system.
```sql
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
    role_id BIGINT NULL
);
```

### 2. members
Library members extending the users table.
```sql
CREATE TABLE members (
    user_id BIGINT PRIMARY KEY,
    membership_number VARCHAR(50) UNIQUE,
    membership_start DATE NULL,
    membership_end DATE NULL,
    total_fines DECIMAL(10,2) DEFAULT 0.0,
    library_id BIGINT NULL
);
```

### 3. books
Book information and metadata.
```sql
CREATE TABLE books (
    book_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    publication_year INT DEFAULT 0,
    genre VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);
```

### 4. loans
Book loan records with due dates and fine tracking.
```sql
CREATE TABLE loans (
    loan_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    due_date DATETIME2 NOT NULL,
    return_date DATETIME2 NULL,
    fine_amount DECIMAL(10,2) DEFAULT 0.0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL
);
```

## Relationships

### Primary Relationships
1. **Library → Members**: One library can have many members
2. **Library → Inventories**: One library manages many book inventories
3. **Book → Loans**: One book can have many loan records
4. **Member → Loans**: One member can have many loans
5. **Book ↔ Authors**: Many-to-many relationship through book_authors table

### Foreign Key Constraints
- All foreign keys are properly defined with referential integrity
- Cascade operations are configured where appropriate
- Nullable foreign keys for optional relationships

## Indexes and Performance

### Performance Indexes
- **Email indexes**: For user authentication
- **ISBN indexes**: For book searches
- **Status indexes**: For filtering active/inactive records
- **Date indexes**: For temporal queries
- **Composite indexes**: For complex queries

### Query Optimization
- Indexes on frequently queried columns
- Covering indexes for common SELECT operations
- Partitioning strategy for large tables (future consideration)

## Views

### 1. v_overdue_books
Shows all currently overdue books with member information.
```sql
CREATE VIEW v_overdue_books AS
SELECT 
    b.book_id, b.title, b.isbn, l.loan_id, l.due_date,
    m.user_id as member_id, u.name as member_name,
    DATEDIFF(day, l.due_date, GETDATE()) as days_overdue
FROM books b
INNER JOIN loans l ON b.book_id = l.book_id
INNER JOIN members m ON l.member_id = m.user_id
INNER JOIN users u ON m.user_id = u.user_id
WHERE l.status = 'ACTIVE' AND l.due_date < GETDATE();
```

### 2. v_available_books
Shows all available books with inventory information.
```sql
CREATE VIEW v_available_books AS
SELECT 
    b.book_id, b.title, b.isbn, b.genre, b.publication_year,
    i.total_copies, i.available_copies, lib.name as library_name
FROM books b
INNER JOIN inventories i ON b.book_id = i.book_id
INNER JOIN libraries lib ON i.library_id = lib.library_id
WHERE b.status = 'AVAILABLE' AND i.available_copies > 0;
```

### 3. v_member_activity
Shows member activity statistics.
```sql
CREATE VIEW v_member_activity AS
SELECT 
    m.user_id, u.name, u.email, m.membership_number,
    COUNT(l.loan_id) as total_loans,
    COUNT(CASE WHEN l.status = 'ACTIVE' THEN 1 END) as active_loans,
    COUNT(CASE WHEN l.due_date < GETDATE() AND l.status = 'ACTIVE' THEN 1 END) as overdue_loans,
    m.total_fines
FROM members m
INNER JOIN users u ON m.user_id = u.user_id
LEFT JOIN loans l ON m.user_id = l.member_id
GROUP BY m.user_id, u.name, u.email, m.membership_number, m.total_fines;
```

## Stored Procedures

### 1. sp_CalculateOverdueFine
Calculates fine amount for overdue loans.
```sql
CREATE PROCEDURE sp_CalculateOverdueFine
    @LoanId BIGINT,
    @DailyRate DECIMAL(10,2) = 2.0,
    @MaxFine DECIMAL(10,2) = 50.0
AS
BEGIN
    -- Fine calculation logic
END;
```

### 2. sp_GetPopularBooks
Retrieves most popular books based on loan count.
```sql
CREATE PROCEDURE sp_GetPopularBooks
    @TopCount INT = 10,
    @StartDate DATETIME2 = NULL,
    @EndDate DATETIME2 = NULL
AS
BEGIN
    -- Popular books logic
END;
```

## Triggers

### 1. tr_LoanCreated_UpdateBookStatus
Automatically updates book status when loan is created.
```sql
CREATE TRIGGER tr_LoanCreated_UpdateBookStatus
ON loans
AFTER INSERT
AS
BEGIN
    -- Update book status to BORROWED
    -- Decrease available copies
END;
```

### 2. tr_LoanReturned_UpdateBookStatus
Updates book status when loan is returned.
```sql
CREATE TRIGGER tr_LoanReturned_UpdateBookStatus
ON loans
AFTER UPDATE
AS
BEGIN
    -- Update book status to AVAILABLE
    -- Increase available copies
END;
```

## Common Queries

### Book Search Queries
1. **Search by title**: Partial match search using LIKE
2. **Search by author**: Join with authors table
3. **Search by ISBN**: Exact match search
4. **Search by genre**: Filter by book genre
5. **Available books**: Show only available books

### User History Queries
6. **Loan history**: Complete loan history for a member
7. **Current loans**: Active loans for a member
8. **Overdue books**: Overdue books for a member
9. **Fine history**: Fine records for a member
10. **Reservations**: Reservation history for a member

### Library Staff Queries
11. **All overdue books**: System-wide overdue books
12. **Low inventory**: Books with low stock
13. **Damaged books**: Books with damaged copies
14. **Active reservations**: Current reservations
15. **Outstanding fines**: Members with unpaid fines

## Reports

### 1. Overdue Books Report
- Total overdue books count
- Total fines accumulated
- Average days overdue
- Detailed overdue books list

### 2. Popular Books Report
- Most borrowed books (all time)
- Popular books by genre
- Utilization rates
- Recent popularity trends

### 3. Financial Report
- Overall financial summary
- Monthly revenue breakdown
- Fine analysis
- Payment rates

### 4. Member Activity Report
- Member activity summary
- Most active members
- Registration trends
- Fine statistics

### 5. Inventory Report
- Overall inventory status
- Inventory by library
- Books requiring attention
- Stock levels

### 6. Reservation Report
- Reservation summary
- Most requested books
- Expired reservations
- Fulfillment rates

### 7. Staff Activity Report
- Staff activity summary
- Admin activity tracking
- Login statistics
- Performance metrics

### 8. Comprehensive Library Report
- Complete library overview
- Performance metrics
- System health indicators
- Operational statistics

## Sample Data

The database includes comprehensive sample data:
- **3 Libraries**: Central, North Branch, South Community
- **15 Books**: Popular titles across different genres
- **12 Authors**: Well-known authors
- **10 Users**: Mix of admins, librarians, and members
- **8 Loans**: Active and returned loans
- **8 Reservations**: Various reservation statuses
- **Financial Data**: Membership fees and fines

## Integration with Java Backend

### Connection Configuration
```properties
# application.properties
quarkus.datasource.db-kind=mssql
quarkus.datasource.username=library_user
quarkus.datasource.password=secure_password
quarkus.datasource.jdbc.url=jdbc:sqlserver://localhost:1433;databaseName=LibraryManagementSystem
```

### Repository Updates
- Update repository methods to use native SQL where needed
- Implement custom queries for complex operations
- Use stored procedures for business logic
- Leverage database views for common queries

### Performance Considerations
- Use connection pooling
- Implement query optimization
- Consider read replicas for reporting
- Monitor query performance

### Security
- Use parameterized queries
- Implement proper authentication
- Role-based access control
- Audit logging

## Usage Instructions

### 1. Database Setup
```bash
# Run schema creation
sqlcmd -S localhost -i 01_schema_creation.sql

# Insert sample data
sqlcmd -S localhost -i 02_sample_data.sql

# Test common queries
sqlcmd -S localhost -i 03_common_queries.sql

# Generate reports
sqlcmd -S localhost -i 04_reports.sql
```

### 2. Common Operations
- **Add new book**: Insert into books and inventories tables
- **Register member**: Insert into users and members tables
- **Create loan**: Insert into loans table (triggers handle status updates)
- **Calculate fines**: Use sp_CalculateOverdueFine procedure
- **Generate reports**: Use the comprehensive report queries

### 3. Maintenance
- Regular backup procedures
- Index maintenance
- Statistics updates
- Performance monitoring

## Conclusion

This MSSQL database provides a robust foundation for the Library Management System with:
- Comprehensive data model
- Optimized performance
- Rich reporting capabilities
- Scalable architecture
- Integration-ready design

The database is production-ready and can handle real-world library operations efficiently. 