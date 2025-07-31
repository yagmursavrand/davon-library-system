package com.davon.library.resource;

import com.davon.library.model.User;
import com.davon.library.model.Admin;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.AdminService;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    AdminService adminService;
    
    @Inject
    UserService userService;
    
    /**
     * Get all users (using repository directly)
     */
    @GET
    public List<User> getAllUsers() {
        return userRepository.listAll();
    }
    
    /**
     * Get user by ID (using repository)
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        Optional<User> user = userRepository.findByIdOptional(id);
        if (user.isPresent()) {
            return Response.ok(user.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("User not found")
                          .build();
        }
    }
    
    /**
     * Find user by email (using repository)
     */
    @GET
    @Path("/email/{email}")
    public Response getUserByEmail(@PathParam("email") String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return Response.ok(user.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("User not found")
                          .build();
        }
    }
    
    /**
     * Get users by role (using repository)
     */
    @GET
    @Path("/role/{role}")
    public List<User> getUsersByRole(@PathParam("role") String role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Get active users (using repository)
     */
    @GET
    @Path("/active")
    public List<User> getActiveUsers() {
        return userRepository.findActiveUsers();
    }
    
    /**
     * Register new user (public endpoint)
     */
    @POST
    public Response registerUser(User user) {
        try {
            // Check if user with same email already exists
            Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                return Response.status(Response.Status.CONFLICT)
                              .entity("{\"error\": \"User with this email already exists\"}")
                              .build();
            }
            
            // Set default role if not specified
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("member");
            }
            
            // Use service layer for business logic
            User createdUser = userService.createUser(user.getName(), user.getEmail(), user.getPassword(), user.getRole());
            
            if (createdUser != null) {
                return Response.status(Response.Status.CREATED)
                              .entity(createdUser)
                              .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("{\"error\": \"Failed to create user\"}")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"" + e.getMessage() + "\"}")
                          .build();
        }
    }

        /**
     * Login user (public endpoint)
     */
    @POST
    @Path("/login")
    public Response loginUser(User loginRequest) {
        try {
            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("{\"error\": \"Invalid email or password\"}")
                              .build();
            }

            User user = userOpt.get();

            // Check password (in real app, use proper password hashing)
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("{\"error\": \"Invalid email or password\"}")
                              .build();
            }

            // Update login status
            user.setLoggedIn(true);
            userRepository.persist(user);

            return Response.ok(user).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"" + e.getMessage() + "\"}")
                          .build();
        }
    }

    /**
     * Update user profile (public endpoint)
     */
    @PUT
    @Path("/{id}")
    public Response updateUserProfile(@PathParam("id") Long id, User updatedUser) {
        try {
            Optional<User> userOpt = userRepository.findByIdOptional(id);
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("{\"error\": \"User not found\"}")
                              .build();
            }

            User user = userOpt.get();
            
            // Update only allowed fields
            if (updatedUser.getName() != null) {
                user.setName(updatedUser.getName());
            }
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
            }
            
            userRepository.persist(user);
            
            return Response.ok(user).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"" + e.getMessage() + "\"}")
                          .build();
        }
    }

    /**
     * Create user (using service layer with business logic) - Admin only
     */
    @POST
    @Path("/admin/{adminId}/add-user")
    public Response addUser(@PathParam("adminId") Long adminId, User user) {
        try {
            // Get admin
            Optional<User> adminOpt = userRepository.findByIdOptional(adminId);
            if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Admin)) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin not found or insufficient permissions")
                              .build();
            }
            
            Admin admin = (Admin) adminOpt.get();
            
            // Use service layer for business logic
            boolean success = adminService.addUser(admin, user);
            
            if (success) {
                return Response.status(Response.Status.CREATED)
                              .entity(user)
                              .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Failed to add user")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Update user (using service layer)
     */
    @PUT
    @Path("/admin/{adminId}/update-user/{userId}")
    public Response updateUser(@PathParam("adminId") Long adminId, 
                              @PathParam("userId") Long userId, 
                              User updatedUser) {
        try {
            // Get admin
            Optional<User> adminOpt = userRepository.findByIdOptional(adminId);
            if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Admin)) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin not found or insufficient permissions")
                              .build();
            }
            
            Admin admin = (Admin) adminOpt.get();
            
            // Use service layer for business logic
            boolean success = adminService.updateUser(admin, userId, updatedUser);
            
            if (success) {
                return Response.ok("User updated successfully").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Failed to update user")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Delete user (using service layer)
     */
    @DELETE
    @Path("/admin/{adminId}/delete-user/{userId}")
    public Response deleteUser(@PathParam("adminId") Long adminId, 
                              @PathParam("userId") Long userId) {
        try {
            // Get admin
            Optional<User> adminOpt = userRepository.findByIdOptional(adminId);
            if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Admin)) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin not found or insufficient permissions")
                              .build();
            }
            
            Admin admin = (Admin) adminOpt.get();
            
            // Use service layer for business logic
            boolean success = adminService.removeUser(admin, userId);
            
            if (success) {
                return Response.ok("User deleted successfully").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Failed to delete user")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get user statistics (using service layer)
     */
    @GET
    @Path("/admin/{adminId}/statistics")
    public Response getUserStatistics(@PathParam("adminId") Long adminId) {
        try {
            // Get admin
            Optional<User> adminOpt = userRepository.findByIdOptional(adminId);
            if (adminOpt.isEmpty() || !(adminOpt.get() instanceof Admin)) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin not found or insufficient permissions")
                              .build();
            }
            
            Admin admin = (Admin) adminOpt.get();
            
            // Use service layer - this prints to console for now
            adminService.getUserStatistics(admin);
            
            // Return summary
            long totalUsers = userRepository.count();
            return Response.ok("Statistics generated. Total users: " + totalUsers).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
} 