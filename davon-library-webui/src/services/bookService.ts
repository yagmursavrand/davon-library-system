import { Book } from '../types/book';

class BookService {
    private readonly API_BASE_URL = 'http://localhost:8082/api';

    async getAllBooks(): Promise<Book[]> {
        const response = await fetch(`${this.API_BASE_URL}/books`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to fetch books');
        }
        return response.json();
    }

    async getBookById(id: string): Promise<Book | null> {
        const response = await fetch(`${this.API_BASE_URL}/books/${id}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            return null;
        }
        return response.json();
    }

    async searchBooks(query: string): Promise<Book[]> {
        const response = await fetch(`${this.API_BASE_URL}/books/search?q=${encodeURIComponent(query)}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to search books');
        }
        return response.json();
    }

    async getBooksByGenre(genre: string): Promise<Book[]> {
        const response = await fetch(`${this.API_BASE_URL}/books/genre/${genre}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to fetch books by genre');
        }
        return response.json();
    }

    async addBook(book: Partial<Book>): Promise<Book> {
        const response = await fetch(`${this.API_BASE_URL}/books`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
            body: JSON.stringify(book),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to add book');
        }

        return response.json();
    }

    async updateBook(id: string, book: Partial<Book>): Promise<Book> {
        const response = await fetch(`${this.API_BASE_URL}/books/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
            body: JSON.stringify(book),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to update book');
        }

        return response.json();
    }

    async deleteBook(id: string): Promise<void> {
        const response = await fetch(`${this.API_BASE_URL}/books/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to delete book');
        }
    }
}

export const bookService = new BookService(); 