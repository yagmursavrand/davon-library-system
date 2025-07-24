package com.davon.library.service;

import com.davon.library.model.Author;
import com.davon.library.model.Book;
import com.davon.library.repository.AuthorRepository;
import com.davon.library.repository.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * AuthorService - handles all author-related business operations
 */
@ApplicationScoped
public class AuthorService {

    @Inject
    AuthorRepository authorRepository;
    
    @Inject
    BookRepository bookRepository;

    /**
     * Get all authors
     */
    public List<Author> getAllAuthors() {
        return authorRepository.listAll();
    }

    /**
     * Get author by ID
     */
    public Optional<Author> getAuthorById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return authorRepository.findByIdOptional(id);
    }

    /**
     * Create new author
     */
    @Transactional
    public Author createAuthor(String name, String bio) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Author name cannot be empty");
            return null;
        }

        String trimmedName = name.trim();
        
        // Check if author with same name already exists
        Optional<Author> existingAuthor = authorRepository.findByName(trimmedName);
        if (existingAuthor.isPresent()) {
            System.out.println("Author with name '" + trimmedName + "' already exists");
            return existingAuthor.get();
        }

        Author author = new Author();
        author.setName(trimmedName);
        author.setBio(bio);
        
        authorRepository.persist(author);
        
        System.out.println("Author created successfully: " + author.getName());
        return author;
    }

    /**
     * Update author information
     */
    @Transactional
    public boolean updateAuthor(Long authorId, String name, String bio) {
        if (authorId == null) {
            System.out.println("Author ID cannot be null");
            return false;
        }

        Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
        if (authorOpt.isEmpty()) {
            System.out.println("Author not found with ID: " + authorId);
            return false;
        }

        Author author = authorOpt.get();
        boolean updated = false;

        if (name != null && !name.trim().isEmpty() && !name.equals(author.getName())) {
            author.setName(name.trim());
            updated = true;
        }

        if (bio != null && !bio.equals(author.getBio())) {
            author.setBio(bio);
            updated = true;
        }

        if (updated) {
            System.out.println("Author updated successfully: " + author.getName());
        } else {
            System.out.println("No changes made to author");
        }

        return updated;
    }

    /**
     * Delete author
     */
    @Transactional
    public boolean deleteAuthor(Long authorId) {
        if (authorId == null) {
            System.out.println("Author ID cannot be null");
            return false;
        }

        Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
        if (authorOpt.isEmpty()) {
            System.out.println("Author not found with ID: " + authorId);
            return false;
        }

        Author author = authorOpt.get();

        // Check if author has books
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            System.out.println("Cannot delete author: Author has " + author.getBooks().size() + " books");
            return false;
        }

        authorRepository.delete(author);
        System.out.println("Author deleted successfully: " + author.getName());
        return true;
    }

    /**
     * Add book to author (implements Author.addBook())
     */
    @Transactional
    public boolean addBookToAuthor(Long authorId, Long bookId) {
        if (authorId == null || bookId == null) {
            System.out.println("Author ID and Book ID cannot be null");
            return false;
        }

        Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
        if (authorOpt.isEmpty()) {
            System.out.println("Author not found with ID: " + authorId);
            return false;
        }

        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found with ID: " + bookId);
            return false;
        }

        Author author = authorOpt.get();
        Book book = bookOpt.get();

        if (author.getBooks().contains(book)) {
            System.out.println("Book is already associated with this author");
            return false;
        }

        author.getBooks().add(book);
        book.getAuthors().add(author);

        System.out.println("Book '" + book.getTitle() + "' added to author '" + author.getName() + "'");
        return true;
    }

    /**
     * Remove book from author (implements Author.removeBook())
     */
    @Transactional
    public boolean removeBookFromAuthor(Long authorId, Long bookId) {
        if (authorId == null || bookId == null) {
            System.out.println("Author ID and Book ID cannot be null");
            return false;
        }

        Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
        if (authorOpt.isEmpty()) {
            System.out.println("Author not found with ID: " + authorId);
            return false;
        }

        Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
        if (bookOpt.isEmpty()) {
            System.out.println("Book not found with ID: " + bookId);
            return false;
        }

        Author author = authorOpt.get();
        Book book = bookOpt.get();

        if (!author.getBooks().contains(book)) {
            System.out.println("Book is not associated with this author");
            return false;
        }

        author.getBooks().remove(book);
        book.getAuthors().remove(author);

        System.out.println("Book '" + book.getTitle() + "' removed from author '" + author.getName() + "'");
        return true;
    }

    /**
     * Get books by author
     */
    public List<Book> getBooksByAuthor(Long authorId) {
        if (authorId == null) {
            return List.of();
        }

        Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
        if (authorOpt.isEmpty()) {
            return List.of();
        }

        return authorOpt.get().getBooks();
    }

    /**
     * Search authors by name
     */
    public List<Author> searchAuthorsByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }

        return authorRepository.find("name LIKE ?1", "%" + searchTerm.trim() + "%").list();
    }

    /**
     * Get author statistics
     */
    public void getAuthorStatistics(Long authorId) {
        if (authorId == null) {
            System.out.println("Author ID cannot be null");
            return;
        }

        Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
        if (authorOpt.isEmpty()) {
            System.out.println("Author not found");
            return;
        }

        Author author = authorOpt.get();
        
        System.out.println("=== Author Statistics ===");
        System.out.println("Name: " + author.getName());
        System.out.println("Bio: " + (author.getBio() != null ? author.getBio() : "No bio available"));
        System.out.println("Number of Books: " + (author.getBooks() != null ? author.getBooks().size() : 0));
        
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            System.out.println("Books:");
            author.getBooks().forEach(book -> 
                System.out.println("  - " + book.getTitle() + " (" + book.getPublicationYear() + ")")
            );
        }
        System.out.println("========================");
    }
} 