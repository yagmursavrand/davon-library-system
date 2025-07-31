'use client';

import React, { useState, useEffect } from 'react';
import { Book, BookStatus } from '../types/book';
import { bookService } from '../services/bookService';
import styles from './BookCatalog.module.css';

interface BookCatalogProps {
    showAdminControls?: boolean;
}

const BookCatalog: React.FC<BookCatalogProps> = ({ showAdminControls = false }) => {
    const [books, setBooks] = useState<Book[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedGenre, setSelectedGenre] = useState<string>('all');
    const [selectedStatus, setSelectedStatus] = useState<BookStatus | 'all'>('all');

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
                                    <button className={`${styles.adminButton} ${styles.editButton}`}>
                                        Edit
                                    </button>
                                    <button className={`${styles.adminButton} ${styles.deleteButton}`}>
                                        Delete
                                    </button>
                                </div>
                            )}
                        </div>

                        {book.status === BookStatus.AVAILABLE && !showAdminControls && (
                            <button className={styles.searchButton} style={{ width: '100%', marginTop: '1rem' }}>
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