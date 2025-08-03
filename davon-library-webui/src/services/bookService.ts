import { Book } from '../types/book';

class BookService {
    private readonly API_BASE_URL = 'http://localhost:8082/api';

    async getAllBooks(): Promise<Book[]> {
        const response = await fetch(`${this.API_BASE_URL}/books`);
        if (!response.ok) {
            throw new Error('Failed to fetch books');
        }
        return response.json();
    }

    async getBookById(id: string): Promise<Book | null> {
        const response = await fetch(`${this.API_BASE_URL}/books/${id}`);
        if (!response.ok) {
            return null;
        }
        return response.json();
    }

    async searchBooks(query: string): Promise<Book[]> {
        const response = await fetch(`${this.API_BASE_URL}/books/search?q=${encodeURIComponent(query)}`);
        if (!response.ok) {
            throw new Error('Failed to search books');
        }
        return response.json();
    }

    async getBooksByGenre(genre: string): Promise<Book[]> {
        const response = await fetch(`${this.API_BASE_URL}/books/genre/${genre}`);
        if (!response.ok) {
            throw new Error('Failed to fetch books by genre');
        }
        return response.json();
    }

    async addBook(book: Partial<Book>, adminId: string): Promise<Book> {
        const response = await fetch(`${this.API_BASE_URL}/books`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${adminId}`,
            },
            body: JSON.stringify(book),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to add book');
        }

        return response.json();
    }

    async updateBook(id: string, book: Partial<Book>, adminId: string): Promise<Book> {
        const response = await fetch(`${this.API_BASE_URL}/books/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${adminId}`,
            },
            body: JSON.stringify(book),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to update book');
        }

        return response.json();
    }

    async deleteBook(id: string, adminId: string): Promise<void> {
        const response = await fetch(`${this.API_BASE_URL}/books/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${adminId}`,
            },
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to delete book');
        }
    }
}

export const bookService = new BookService();
