package com.davon.library.resource;

import com.davon.library.model.Book;
import com.davon.library.model.Author;
import com.davon.library.model.User;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.AuthorRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.BookService;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {
    
    @Inject
    BookRepository bookRepository;
    
    @Inject
    AuthorRepository authorRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    BookService bookService;
    
    @Inject
    UserService userService;
    
    /**
     * Get all books (public endpoint)
     */
    @GET
    public List<Book> getAllBooks() {
        return bookRepository.listAll();
    }
    
    /**
     * Get book by ID (public endpoint)
     */
    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") Long id) {
        Optional<Book> book = bookRepository.findByIdOptional(id);
        if (book.isPresent()) {
            return Response.ok(book.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Book not found")
                          .build();
        }
    }
    
    /**
     * Search books by title (public endpoint)
     */
    @GET
    @Path("/search/title/{title}")
    public List<Book> searchBooksByTitle(@PathParam("title") String title) {
        return bookRepository.findByTitle(title);
    }
    
    /**
     * Get books by genre (public endpoint)
     */
    @GET
    @Path("/genre/{genre}")
    public List<Book> getBooksByGenre(@PathParam("genre") String genre) {
        return bookRepository.findByGenre(genre);
    }
    
    /**
     * Get available books (public endpoint)
     */
    @GET
    @Path("/available")
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
    
    /**
     * Search books (public endpoint)
     */
    @GET
    @Path("/search")
    public List<Book> searchBooks(@QueryParam("q") String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return bookRepository.listAll();
        }
        return bookRepository.searchBooks(searchTerm);
    }
    
    /**
     * Find book by ISBN (public endpoint)
     */
    @GET
    @Path("/isbn/{isbn}")
    public Response getBookByIsbn(@PathParam("isbn") String isbn) {
        Optional<Book> book = bookRepository.findByIsbn(isbn);
        if (book.isPresent()) {
            return Response.ok(book.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Book not found")
                          .build();
        }
    }
    
    /**
     * Add new book (admin only)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addBook(@HeaderParam("Authorization") String authHeader, 
                           BookCreateRequest request) {
        try {
            // Check admin authorization
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            // Create book
            Book book = new Book();
            book.setTitle(request.title);
            book.setIsbn(request.isbn);
            book.setPublicationYear(request.publicationYear);
            book.setGenre(request.genre);
            book.setStatus(Book.BookStatus.AVAILABLE);
            
            bookRepository.persist(book);
            
            // Add authors if provided
            if (request.authorIds != null) {
                for (Long authorId : request.authorIds) {
                    Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
                    if (authorOpt.isPresent()) {
                        Author author = authorOpt.get();
                        book.getAuthors().add(author);
                        author.getBooks().add(book);
                    }
                }
            }
            
            return Response.status(Response.Status.CREATED).entity(book).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Update book (admin only)
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateBook(@PathParam("id") Long id,
                              @HeaderParam("Authorization") String authHeader,
                              BookUpdateRequest request) {
        try {
            // Check admin authorization
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Book> bookOpt = bookRepository.findByIdOptional(id);
            if (bookOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Book not found")
                              .build();
            }
            
            Book book = bookOpt.get();
            
            // Update fields if provided
            if (request.title != null) book.setTitle(request.title);
            if (request.isbn != null) book.setIsbn(request.isbn);
            if (request.publicationYear != null) book.setPublicationYear(request.publicationYear);
            if (request.genre != null) book.setGenre(request.genre);
            if (request.status != null) book.setStatus(request.status);
            
            return Response.ok(book).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Delete book (admin only)
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteBook(@PathParam("id") Long id,
                              @HeaderParam("Authorization") String authHeader) {
        try {
            // Check admin authorization
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Book> bookOpt = bookRepository.findByIdOptional(id);
            if (bookOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Book not found")
                              .build();
            }
            
            Book book = bookOpt.get();
            
            // Check if book is currently borrowed
            if (book.getStatus() == Book.BookStatus.BORROWED) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Cannot delete book: Currently borrowed")
                              .build();
            }
            
            bookRepository.delete(book);
            return Response.ok("Book deleted successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Add author to book (admin only)
     */
    @POST
    @Path("/{bookId}/authors/{authorId}")
    @Transactional
    public Response addAuthorToBook(@PathParam("bookId") Long bookId,
                                   @PathParam("authorId") Long authorId,
                                   @HeaderParam("Authorization") String authHeader) {
        try {
            // Check admin authorization
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
            Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
            
            if (bookOpt.isEmpty() || authorOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Book or Author not found")
                              .build();
            }
            
            Book book = bookOpt.get();
            
            bookService.addAuthorToBook(book.getId(), authorId);
            
            return Response.ok("Author added to book successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Remove author from book (admin only)
     */
    @DELETE
    @Path("/{bookId}/authors/{authorId}")
    @Transactional
    public Response removeAuthorFromBook(@PathParam("bookId") Long bookId,
                                        @PathParam("authorId") Long authorId,
                                        @HeaderParam("Authorization") String authHeader) {
        try {
            // Check admin authorization
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Book> bookOpt = bookRepository.findByIdOptional(bookId);
            Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
            
            if (bookOpt.isEmpty() || authorOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Book or Author not found")
                              .build();
            }
            
            Book book = bookOpt.get();
            
            bookService.removeAuthorFromBook(book.getId(), authorId);
            
            return Response.ok("Author removed from book successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    // Helper method for authentication
    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            Long userId = Long.parseLong(token); // In real app, decode JWT
            return userRepository.findByIdOptional(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    // Request DTOs
    public static class BookCreateRequest {
        public String title;
        public String isbn;
        public Integer publicationYear;
        public String genre;
        public List<Long> authorIds;
    }
    
    public static class BookUpdateRequest {
        public String title;
        public String isbn;
        public Integer publicationYear;
        public String genre;
        public Book.BookStatus status;
    }
} 