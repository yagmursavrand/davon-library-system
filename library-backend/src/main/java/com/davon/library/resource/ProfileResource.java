package com.davon.library.resource;

import com.davon.library.model.Profile;
import com.davon.library.model.User;
import com.davon.library.repository.ProfileRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.Optional;

@Path("/api/profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfileResource {
    
    @Inject
    ProfileRepository profileRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    UserService userService;
    
    /**
     * Get profile by user ID (user themselves or admin)
     */
    @GET
    @Path("/user/{userId}")
    public Response getProfileByUserId(@PathParam("userId") Long userId,
                                      @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            // Check access permissions
            if (!userService.isAdmin(authenticatedUser.getId()) && 
                !authenticatedUser.getId().equals(userId)) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Access denied")
                              .build();
            }
            
            Optional<Profile> profile = profileRepository.findByUserId(userId);
            if (profile.isPresent()) {
                return Response.ok(profile.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Profile not found")
                              .build();
            }
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Create or update profile (user themselves)
     */
    @PUT
    @Path("/user/{userId}")
    public Response updateProfile(@PathParam("userId") Long userId,
                                 ProfileUpdateRequest request,
                                 @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            // Users can only update their own profile
            if (!authenticatedUser.getId().equals(userId)) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Access denied")
                              .build();
            }
            
            Optional<User> userOpt = userRepository.findByIdOptional(userId);
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("User not found")
                              .build();
            }
            
            User user = userOpt.get();
            Optional<Profile> profileOpt = profileRepository.findByUserId(userId);
            Profile profile;
            
            if (profileOpt.isPresent()) {
                // Update existing profile
                profile = profileOpt.get();
            } else {
                // Create new profile
                profile = new Profile();
                profile.setUser(user);
            }
            
            // Update fields if provided
            if (request.phone != null) {
                profile.setPhone(request.phone.trim().isEmpty() ? null : request.phone.trim());
            }
            if (request.address != null) {
                profile.setAddress(request.address.trim().isEmpty() ? null : request.address.trim());
            }
            if (request.birthDate != null) {
                profile.setBirthDate(request.birthDate);
            }
            
            profile.setLastUpdated(new Date());
            profileRepository.persist(profile);
            
            return Response.ok("Profile updated successfully").build();
            
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
    public static class ProfileUpdateRequest {
        public String phone;
        public String address;
        public Date birthDate;
    }
} 