package com.davon.library.resource;

import com.davon.library.model.Book;
import com.davon.library.model.Author;
import com.davon.library.model.Inventory;
import com.davon.library.model.User;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.AuthorRepository;
import com.davon.library.repository.InventoryRepository;
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

import java.util.stream.Collectors;

@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {

    @Inject
    BookRepository bookRepository;
    
    @Inject
    AuthorRepository authorRepository;
    
    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    UserRepository userRepository;
    
    @Inject
    BookService bookService;
    
    @Inject
    UserService userService;
    
    // New endpoint to get detailed book info
    @GET
    @Path("/details")
    public List<BookDetailDTO> getAllBookDetails() {
        List<Book> books = bookRepository.listAll();
        return books.stream()
                    .map(BookDetailDTO::new)
                    .collect(Collectors.toList());
    }

    // ... (other GET methods remain the same) ...

    @GET
    public List<Book> getAllBooks() {
        return bookRepository.listAll();
    }
    
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

    @GET
    @Path("/search/title/{title}")
    public List<Book> searchBooksByTitle(@PathParam("title") String title) {
        return bookRepository.findByTitle(title);
    }

    @GET
    @Path("/genre/{genre}")
    public List<Book> getBooksByGenre(@PathParam("genre") String genre) {
        return bookRepository.findByGenre(genre);
    }

    @GET
    @Path("/available")
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    @GET
    @Path("/search")
    public List<Book> searchBooks(@QueryParam("q") String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return bookRepository.listAll();
        }
        return bookRepository.searchBooks(searchTerm);
    }

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
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addBook(@HeaderParam("Authorization") String authHeader, 
                           BookCreateRequest request) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Book book = new Book();
            book.setTitle(request.title);
            book.setIsbn(request.isbn);
            book.setPublicationYear(request.publicationYear);
            book.setGenre(request.genre);
            book.setStatus(Book.BookStatus.AVAILABLE);
            
            // Handle Author
            if (request.authorName != null && !request.authorName.trim().isEmpty()) {
                // Check if author exists
                Optional<Author> existingAuthor = authorRepository.find("name", request.authorName).firstResultOptional();
                
                Author author;
                if (existingAuthor.isPresent()) {
                    author = existingAuthor.get();
                } else {
                    // Create new author if not found
                    author = new Author();
                    author.setName(request.authorName);
                    authorRepository.persist(author);
                }
                
                // Link author to book
                book.getAuthors().add(author);
                author.getBooks().add(book);
            }
            
            bookRepository.persist(book);
            
            return Response.status(Response.Status.CREATED).entity(book).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    // ... (PUT, DELETE, and other methods remain the same) ...

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateBook(@PathParam("id") Long id,
                              @HeaderParam("Authorization") String authHeader,
                              BookUpdateRequest request) {
        try {
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
    
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteBook(@PathParam("id") Long id,
                              @HeaderParam("Authorization") String authHeader) {
        try {
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
            
            if (book.getStatus() == Book.BookStatus.BORROWED) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Cannot delete book: Currently borrowed")
                              .build();
            }
            
            Inventory inventory = book.getInventory();
            if (inventory != null) {
                inventoryRepository.delete(inventory);
            }
            
            bookRepository.delete(book);
            return Response.ok("Book deleted successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    @POST
    @Path("/{bookId}/authors/{authorId}")
    @Transactional
    public Response addAuthorToBook(@PathParam("bookId") Long bookId,
                                   @PathParam("authorId") Long authorId,
                                   @HeaderParam("Authorization") String authHeader) {
        try {
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
    
    @DELETE
    @Path("/{bookId}/authors/{authorId}")
    @Transactional
    public Response removeAuthorFromBook(@PathParam("bookId") Long bookId,
                                        @PathParam("authorId") Long authorId,
                                        @HeaderParam("Authorization") String authHeader) {
        try {
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
    
    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7);
            Long userId = Long.parseLong(token);
            return userRepository.findByIdOptional(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static class BookCreateRequest {
        public String title;
        public String isbn;
        public Integer publicationYear;
        public String genre;
        public String authorName; // Added authorName
    }
    
    public static class BookUpdateRequest {
        public String title;
        public String isbn;
        public Integer publicationYear;
        public String genre;
        public Book.BookStatus status;
    }

    // DTO for returning detailed book info, including borrower
    public static class BookDetailDTO {
        public Long id;
        public String title;
        public String isbn;
        public int publicationYear;
        public String genre;
        public Book.BookStatus status;
        public String borrowerName; // Can be null

        public BookDetailDTO(Book book) {
            this.id = book.getId();
            this.title = book.getTitle();
            this.isbn = book.getIsbn();
            this.publicationYear = book.getPublicationYear();
            this.genre = book.getGenre();
            this.status = book.getStatus();
            
            if (book.getStatus() == Book.BookStatus.BORROWED && !book.getLoans().isEmpty()) {
                // Find the active loan to get the borrower's name
                book.getLoans().stream()
                    .filter(loan -> loan.getStatus() == com.davon.library.model.Loan.LoanStatus.ACTIVE || loan.getStatus() == com.davon.library.model.Loan.LoanStatus.RENEWED)
                    .findFirst()
                    .ifPresent(activeLoan -> this.borrowerName = activeLoan.getMember().getName());
            }
        }
    }
}
