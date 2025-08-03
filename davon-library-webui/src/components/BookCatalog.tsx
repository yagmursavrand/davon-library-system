'use client';

import React, { useState, useEffect } from 'react';
import { Book, BookStatus } from '../types/book';
import { bookService } from '../services/bookService';
import { useAuth } from '../contexts/AuthContext';
import loanService from '../services/loanService';
import styles from './BookCatalog.module.css';

interface BookCatalogProps {
    showAdminControls?: boolean;
}

const BookCatalog: React.FC<BookCatalogProps> = ({ showAdminControls = false }) => {
    const { user } = useAuth();
    const [books, setBooks] = useState<Book[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedGenre, setSelectedGenre] = useState<string>('all');
        const [selectedStatus, setSelectedStatus] = useState<BookStatus | 'all'>('all');
    const [editingBook, setEditingBook] = useState<Book | null>(null);
        const [isDeleting, setIsDeleting] = useState<number | null>(null);
    const [isAddingBook, setIsAddingBook] = useState<boolean>(false);
        const [newBook, setNewBook] = useState<Partial<Book> & { authorName?: string }>({
        title: '',
        isbn: '',
        genre: '',
        publicationYear: new Date().getFullYear(),
        status: BookStatus.AVAILABLE,
        authorName: '',
    });

    useEffect(() => {
        loadBooks();
    }, []);

    const loadBooks = async () => {
        try {
            setLoading(true);
            const fetchedBooks = await bookService.getAllBooks();
            setBooks(fetchedBooks);
            setError(null);
        } catch (err) {
            setError('Failed to load books');
            console.error('Error loading books:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleBorrow = async (bookId: number) => {
        if (!user) {
            alert('You must be logged in to borrow a book.');
            return;
        }

        try {
            await loanService.borrowBook(user.id, bookId);
            alert('Book borrowed successfully!');
            loadBooks();
        } catch (err: any) {
            alert(`Failed to borrow book. Reason: ${err.message}`);
            console.error(err);
        }
    };

        const handleSearch = async () => {
        if (!searchQuery.trim()) {
            loadBooks();
            return;
        }

        try {
            setLoading(true);
            const searchResults = await bookService.searchBooks(searchQuery);
            setBooks(searchResults);
            setError(null);
        } catch (err) {
            setError('Failed to search books');
            console.error('Error searching books:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleEditClick = (book: Book) => {
        setEditingBook({ ...book });
    };

    const handleDeleteClick = (bookId: number) => {
        setIsDeleting(bookId);
    };

    const handleFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        if (!editingBook) return;
        setEditingBook({
            ...editingBook,
            [e.target.name]: e.target.value,
        });
    };

    const handleUpdateBook = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!editingBook || !user) {
            alert('You must be logged in as an admin to perform this action.');
            return;
        }

        try {
            const bookToUpdate = {
                ...editingBook,
                publicationYear: Number(editingBook.publicationYear)
            };
            await bookService.updateBook(bookToUpdate.id.toString(), bookToUpdate, user.id);
            setEditingBook(null);
            loadBooks();
        } catch (err) {
            console.error('Failed to update book:', err);
            alert('Failed to update book. Please try again.');
        }
    };

        const handleConfirmDelete = async () => {
        if (!isDeleting || !user) {
            alert('You must be logged in as an admin to perform this action.');
            return;
        }

        try {
            await bookService.deleteBook(isDeleting.toString(), user.id);
            setIsDeleting(null);
            loadBooks();
        } catch (err) {
            console.error('Failed to delete book:', err);
            alert('Failed to delete book. Please try again.');
        }
    };

    const handleAddBookClick = () => {
        setIsAddingBook(true);
    };

    const handleNewBookFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setNewBook({
            ...newBook,
            [e.target.name]: e.target.value,
        });
    };

    const handleAddNewBook = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!user) {
            alert('You must be logged in as an admin to perform this action.');
            return;
        }
        try {
            const bookToAdd = {
                ...newBook,
                publicationYear: Number(newBook.publicationYear)
            };
            await bookService.addBook(bookToAdd, user.id);
            setIsAddingBook(false);
            setNewBook({
                title: '',
                isbn: '',
                genre: '',
                publicationYear: new Date().getFullYear(),
                status: BookStatus.AVAILABLE,
                authorName: '',
            });
            loadBooks();
        } catch (err) {
            console.error('Failed to add book:', err);
            alert('Failed to add book. Please try again.');
        }
    };

    const filterBooks = () => {
        return books.filter(book => {
            const genreMatch = selectedGenre === 'all' || book.genre === selectedGenre;
            const statusMatch = selectedStatus === 'all' || book.status === selectedStatus;
            return genreMatch && statusMatch;
        });
    };

    const getStatusColor = (status: BookStatus) => {
        switch (status) {
            case BookStatus.AVAILABLE:
                return styles.statusAvailable;
            case BookStatus.BORROWED:
                return styles.statusBorrowed;
            case BookStatus.RESERVED:
                return styles.statusReserved;
            case BookStatus.MAINTENANCE:
                return styles.statusMaintenance;
            default:
                return styles.statusDefault;
        }
    };

    const genres = Array.from(new Set(books.map(book => book.genre)));

    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <div className={styles.spinner}></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.errorContainer}>
                <div className={styles.errorMessage}>{error}</div>
                <button
                    onClick={loadBooks}
                    className={styles.retryButton}
                >
                    Retry
                </button>
            </div>
        );
    }

    const filteredBooks = filterBooks();

    return (
        <div className={styles.catalogContainer}>
            {/* Edit Book Modal */}
            {editingBook && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modalContent}>
                        <h2>Edit Book</h2>
                        <form onSubmit={handleUpdateBook}>
                            <div className={styles.formGroup}>
                                <label>Title</label>
                                <input
                                    type="text"
                                    name="title"
                                    value={editingBook.title}
                                    onChange={handleFormChange}
                                    required
                                />
                            </div>
                            <div className={styles.formGroup}>
                                <label>ISBN</label>
                                <input
                                    type="text"
                                    name="isbn"
                                    value={editingBook.isbn}
                                    onChange={handleFormChange}
                                    required
                                />
                            </div>
                            <div className={styles.formGroup}>
                                <label>Genre</label>
                                <input
                                    type="text"
                                    name="genre"
                                    value={editingBook.genre}
                                    onChange={handleFormChange}
                                    required
                                />
                            </div>
                            <div className={styles.formGroup}>
                                <label>Publication Year</label>
                                <input
                                    type="number"
                                    name="publicationYear"
                                    value={editingBook.publicationYear}
                                    onChange={handleFormChange}
                                    required
                                />
                            </div>
                            <div className={styles.formGroup}>
                                <label>Status</label>
                                <select name="status" value={editingBook.status} onChange={handleFormChange}>
                                    {Object.values(BookStatus).map(s => <option key={s} value={s}>{s}</option>)}
                                </select>
                            </div>
                            <div className={styles.modalActions}>
                                <button type="submit" className={styles.saveButton}>Save Changes</button>
                                <button type="button" onClick={() => setEditingBook(null)} className={styles.cancelButton}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Delete Confirmation Modal */}
            {isDeleting !== null && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modalContent}>
                        <h2>Confirm Deletion</h2>
                        <p>Are you sure you want to delete this book? This action cannot be undone.</p>
                        <div className={styles.modalActions}>
                            <button onClick={handleConfirmDelete} className={styles.deleteConfirmButton}>Yes, Delete</button>
                            <button onClick={() => setIsDeleting(null)} className={styles.cancelButton}>Cancel</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Add Book Modal */}
            {isAddingBook && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modalContent}>
                        <h2>Add New Book</h2>
                        <form onSubmit={handleAddNewBook}>
                            <div className={styles.formGroup}>
                                <label>Title</label>
                                <input type="text" name="title" value={newBook.title} onChange={handleNewBookFormChange} required />
                            </div>
                            <div className={styles.formGroup}>
                                <label>ISBN</label>
                                <input type="text" name="isbn" value={newBook.isbn} onChange={handleNewBookFormChange} required />
                            </div>
                            <div className={styles.formGroup}>
                                <label>Genre</label>
                                <input type="text" name="genre" value={newBook.genre} onChange={handleNewBookFormChange} required />
                            </div>
                            <div className={styles.formGroup}>
                                <label>Author Name</label>
                                <input type="text" name="authorName" value={newBook.authorName} onChange={handleNewBookFormChange} required />
                            </div>
                            <div className={styles.formGroup}>
                                <label>Publication Year</label>
                                <input type="number" name="publicationYear" value={newBook.publicationYear} onChange={handleNewBookFormChange} required />
                            </div>
                            <div className={styles.formGroup}>
                                <label>Status</label>
                                <select name="status" value={newBook.status} onChange={handleNewBookFormChange}>
                                    {Object.values(BookStatus).map(s => <option key={s} value={s}>{s}</option>)}
                                </select>
                            </div>
                            <div className={styles.modalActions}>
                                <button type="submit" className={styles.saveButton}>Add Book</button>
                                <button type="button" onClick={() => setIsAddingBook(false)} className={styles.cancelButton}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

                        {/* Search and Filter Section */}
            <div className={styles.searchSection}>
                <div className={styles.searchRow}>
                    <input
                        type="text"
                        placeholder="Search books by title, author, or ISBN..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className={styles.searchInput}
                    />
                    <button
                        onClick={handleSearch}
                        className={styles.searchButton}
                    >
                        Search
                    </button>
                    {showAdminControls && (
                         <button onClick={handleAddBookClick} className={styles.addButton}>
                            Add New Book
                         </button>
                    )}
                </div>

                <div className={styles.filterRow}>
                    <select
                        value={selectedGenre}
                        onChange={(e) => setSelectedGenre(e.target.value)}
                        className={styles.filterSelect}
                    >
                        <option value="all">All Genres</option>
                        {genres.map(genre => (
                            <option key={genre} value={genre}>{genre}</option>
                        ))}
                    </select>

                    <select
                        value={selectedStatus}
                        onChange={(e) => setSelectedStatus(e.target.value as BookStatus | 'all')}
                        className={styles.filterSelect}
                    >
                        <option value="all">All Status</option>
                        <option value={BookStatus.AVAILABLE}>Available</option>
                        <option value={BookStatus.BORROWED}>Borrowed</option>
                        <option value={BookStatus.RESERVED}>Reserved</option>
                        <option value={BookStatus.MAINTENANCE}>Maintenance</option>
                    </select>
                </div>
            </div>

            {/* Books Grid */}
            <div className={styles.booksGrid}>
                {filteredBooks.map((book) => (
                    <div
                        key={book.id}
                        className={styles.bookCard}
                    >
                        <h3 className={styles.bookTitle}>
                            {book.title}
                        </h3>
                        
                        <div className={styles.bookInfo}>
                            <div className={styles.bookInfoRow}>
                                <span className={styles.bookLabel}>ISBN:</span>
                                <span className={styles.bookValue}>{book.isbn}</span>
                            </div>
                            <div className={styles.bookInfoRow}>
                                <span className={styles.bookLabel}>Genre:</span>
                                <span className={styles.bookValue}>{book.genre}</span>
                            </div>
                            <div className={styles.bookInfoRow}>
                                <span className={styles.bookLabel}>Year:</span>
                                <span className={styles.bookValue}>{book.publicationYear}</span>
                            </div>
                            {book.authors && book.authors.length > 0 && (
                                <div className={styles.bookInfoRow}>
                                    <span className={styles.bookLabel}>Author:</span>
                                    <span className={styles.bookValue}>{book.authors[0].name}</span>
                                </div>
                            )}
                        </div>

                        <div className={styles.bookInfoRow}>
                            <span className={`${styles.statusBadge} ${getStatusColor(book.status)}`}>
                                {book.status}
                            </span>

                            {showAdminControls && (
                                                                <div className={styles.adminControls}>
                                    <button onClick={() => handleEditClick(book)} className={`${styles.adminButton} ${styles.editButton}`}>
                                        Edit
                                    </button>
                                    <button onClick={() => handleDeleteClick(book.id)} className={`${styles.adminButton} ${styles.deleteButton}`}>
                                        Delete
                                    </button>
                                </div>
                            )}
                        </div>

                        {book.status === BookStatus.AVAILABLE && !showAdminControls && (
                            <button 
                                onClick={() => handleBorrow(book.id)}
                                className={styles.searchButton} 
                                style={{ width: '100%', marginTop: '1rem' }}
                            >
                                Borrow Book
                            </button>
                        )}
                    </div>
                ))}
            </div>

            {filteredBooks.length === 0 && (
                <div className={styles.errorContainer}>
                    <p className={styles.errorMessage}>No books found matching your criteria.</p>
                </div>
            )}
        </div>
    );
};

export default BookCatalog; 