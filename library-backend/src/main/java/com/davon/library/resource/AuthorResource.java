package com.davon.library.resource;

import com.davon.library.model.Author;
import com.davon.library.model.User;
import com.davon.library.repository.AuthorRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorResource {
    
    @Inject
    AuthorRepository authorRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    UserService userService;
    
    /**
     * Get all authors (public endpoint)
     */
    @GET
    public Response getAllAuthors() {
        try {
            List<Author> authors = authorRepository.listAll();
            return Response.ok(authors).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get author by ID (public endpoint)
     */
    @GET
    @Path("/{authorId}")
    public Response getAuthorById(@PathParam("authorId") Long authorId) {
        try {
            Optional<Author> author = authorRepository.findByIdOptional(authorId);
            if (author.isPresent()) {
                return Response.ok(author.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Author not found")
                              .build();
            }
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Search authors by name (public endpoint)
     */
    @GET
    @Path("/search")
    public Response searchAuthors(@QueryParam("name") String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Search name parameter is required")
                              .build();
            }
            
            List<Author> authors = authorRepository.findByNameContaining(name.trim());
            return Response.ok(authors).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Create new author (admin only)
     */
    @POST
    public Response createAuthor(AuthorCreationRequest request,
                                @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            if (request.name == null || request.name.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Author name is required")
                              .build();
            }
            
            Author author = new Author();
            author.setName(request.name.trim());
            author.setBio(request.bio != null ? request.bio.trim() : null);
            
            authorRepository.persist(author);
            return Response.status(Response.Status.CREATED).entity(author).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Update author (admin only)
     */
    @PUT
    @Path("/{authorId}")
    public Response updateAuthor(@PathParam("authorId") Long authorId,
                                AuthorUpdateRequest request,
                                @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
            if (authorOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Author not found")
                              .build();
            }
            
            Author author = authorOpt.get();
            
            // Update fields if provided
            if (request.name != null && !request.name.trim().isEmpty()) {
                author.setName(request.name.trim());
            }
            
            if (request.bio != null) {
                author.setBio(request.bio.trim().isEmpty() ? null : request.bio.trim());
            }
            
            authorRepository.persist(author);
            return Response.ok("Author updated successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Delete author (admin only)
     */
    @DELETE
    @Path("/{authorId}")
    public Response deleteAuthor(@PathParam("authorId") Long authorId,
                                @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
            if (authorOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Author not found")
                              .build();
            }
            
            Author author = authorOpt.get();
            
            // Check if author has books (business rule)
            if (author.getBooks() != null && !author.getBooks().isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                              .entity("Cannot delete author with associated books")
                              .build();
            }
            
            authorRepository.delete(author);
            return Response.ok("Author deleted successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get books by author (public endpoint)
     */
    @GET
    @Path("/{authorId}/books")
    public Response getBooksByAuthor(@PathParam("authorId") Long authorId) {
        try {
            Optional<Author> authorOpt = authorRepository.findByIdOptional(authorId);
            if (authorOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Author not found")
                              .build();
            }
            
            Author author = authorOpt.get();
            return Response.ok(author.getBooks()).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    // Helper methods
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
    
    // Request DTOs
    public static class AuthorCreationRequest {
        public String name;
        public String bio;
    }
    
    public static class AuthorUpdateRequest {
        public String name;
        public String bio;
    }
} 