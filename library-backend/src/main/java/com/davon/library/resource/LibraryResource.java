package com.davon.library.resource;

import com.davon.library.model.Library;
import com.davon.library.model.User;
import com.davon.library.repository.LibraryRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/libraries")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LibraryResource {
    
    @Inject
    LibraryRepository libraryRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    UserService userService;
    
    /**
     * Get all libraries (public endpoint)
     */
    @GET
    public Response getAllLibraries() {
        try {
            List<Library> libraries = libraryRepository.listAll();
            return Response.ok(libraries).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get library by ID (public endpoint)
     */
    @GET
    @Path("/{libraryId}")
    public Response getLibraryById(@PathParam("libraryId") Long libraryId) {
        try {
            Optional<Library> library = libraryRepository.findByIdOptional(libraryId);
            if (library.isPresent()) {
                return Response.ok(library.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Library not found")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Create new library (admin only)
     */
    @POST
    public Response createLibrary(LibraryCreationRequest request,
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
                              .entity("Library name is required")
                              .build();
            }
            
            Library library = new Library();
            library.setName(request.name.trim());
            library.setAddress(request.address);
            library.setPhone(request.phone);
            library.setEmail(request.email);
            library.setOpeningHours(request.openingHours);
            
            libraryRepository.persist(library);
            return Response.status(Response.Status.CREATED).entity(library).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Update library (admin only)
     */
    @PUT
    @Path("/{libraryId}")
    public Response updateLibrary(@PathParam("libraryId") Long libraryId,
                                 LibraryUpdateRequest request,
                                 @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Library> libraryOpt = libraryRepository.findByIdOptional(libraryId);
            if (libraryOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Library not found")
                              .build();
            }
            
            Library library = libraryOpt.get();
            
            if (request.name != null && !request.name.trim().isEmpty()) {
                library.setName(request.name.trim());
            }
            if (request.address != null) {
                library.setAddress(request.address.trim());
            }
            if (request.phone != null) {
                library.setPhone(request.phone.trim());
            }
            if (request.email != null) {
                library.setEmail(request.email.trim());
            }
            if (request.openingHours != null) {
                library.setOpeningHours(request.openingHours.trim());
            }
            
            libraryRepository.persist(library);
            return Response.ok("Library updated successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Delete library (admin only)
     */
    @DELETE
    @Path("/{libraryId}")
    public Response deleteLibrary(@PathParam("libraryId") Long libraryId,
                                 @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Library> libraryOpt = libraryRepository.findByIdOptional(libraryId);
            if (libraryOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Library not found")
                              .build();
            }
            
            Library library = libraryOpt.get();
            
            // Check if library has members or inventory
            if (library.getMembers() != null && !library.getMembers().isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                              .entity("Cannot delete library with active members")
                              .build();
            }
            
            libraryRepository.delete(library);
            return Response.ok("Library deleted successfully").build();
            
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
    public static class LibraryCreationRequest {
        public String name;
        public String address;
        public String phone;
        public String email;
        public String openingHours;
    }
    
    public static class LibraryUpdateRequest {
        public String name;
        public String address;
        public String phone;
        public String email;
        public String openingHours;
    }
} 