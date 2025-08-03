-- SQL Script to Restore Essential Library Data (v2)
-- Force the required setting for this session.
SET QUOTED_IDENTIFIER ON;
GO

-- Step 1: Add a central library
SET IDENTITY_INSERT libraries ON;
INSERT INTO libraries (library_id, name, address)
VALUES (1, 'Davon Central Library', '123 Library St, Booksville');
SET IDENTITY_INSERT libraries OFF;
GO

-- Step 2: Add some authors
SET IDENTITY_INSERT authors ON;
INSERT INTO authors (author_id, name) VALUES
(1, 'George Orwell'),
(2, 'J.R.R. Tolkien'),
(3, 'J.K. Rowling'),
(4, 'Frank Herbert'),
(5, 'Jane Austen');
SET IDENTITY_INSERT authors OFF;
GO

-- Step 3: Add some books
SET IDENTITY_INSERT books ON;
INSERT INTO books (book_id, title, isbn, publication_year, genre, status) VALUES
(1, '1984', '978-0451524935', 1949, 'Dystopian', 'AVAILABLE'),
(2, 'The Hobbit', '978-0345339683', 1937, 'Fantasy', 'AVAILABLE'),
(3, 'Dune', '978-0441013593', 1965, 'Science Fiction', 'AVAILABLE'),
(4, 'Pride and Prejudice', '978-0141439518', 1813, 'Romance', 'AVAILABLE'),
(5, 'Harry Potter and the Sorcerer''s Stone', '978-0590353427', 1997, 'Fantasy', 'AVAILABLE');
SET IDENTITY_INSERT books OFF;
GO

-- Step 4: Link books to authors
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1),
(2, 2),
(3, 4),
(4, 5),
(5, 3);
GO

-- Step 5: Add books to the library inventory
SET IDENTITY_INSERT inventories ON;
INSERT INTO inventories (inventory_id, book_id, library_id, total_copies, available_copies) VALUES
(1, 1, 1, 10, 10),
(2, 2, 1, 15, 15),
(3, 3, 1, 8, 8),
(4, 4, 1, 12, 12),
(5, 5, 1, 20, 20);
SET IDENTITY_INSERT inventories OFF;
GO

PRINT 'Sample library, author, book, and inventory data has been restored.';
