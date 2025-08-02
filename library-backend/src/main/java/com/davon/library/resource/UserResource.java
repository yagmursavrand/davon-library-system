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
                          .entity("{\"error\": \"User not found\"}")
                          .build();
        }
    }
    
    /**
     * Register new user (public endpoint)
     */
    @POST
    public Response registerUser(User user) {
        try {
            User registeredUser = userService.registerUser(
                user.getName(),
                user.getEmail(),
                user.getPassword()
            );

            if (registeredUser != null) {
                return Response.status(Response.Status.CREATED)
                              .entity(registeredUser)
                              .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("{\"error\": \"Registration failed. Email might already exist or data is invalid.\"}")
                              .build();
            }
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"An unexpected error occurred during registration.\"}")
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
            boolean loggedIn = userService.login(loginRequest.getEmail(), loginRequest.getPassword());

            if (loggedIn) {
                // Return the full user object on successful login
                Optional<User> userOpt = userService.getUserByEmail(loginRequest.getEmail());
                return userOpt.map(user -> Response.ok(user).build())
                              .orElse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                              .entity("{\"error\": \"Could not retrieve user details after login.\"}")
                                              .build());
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("{\"error\": \"Invalid email or password\"}")
                              .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"An unexpected error occurred during login.\"}")
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
            boolean success = userService.updateProfile(id, updatedUser.getName(), updatedUser.getEmail(), updatedUser.getPassword());
            
            if (success) {
                Optional<User> userOpt = userRepository.findByIdOptional(id);
                return userOpt.map(user -> Response.ok(user).build())
                              .orElse(Response.status(Response.Status.NOT_FOUND)
                                              .entity("{\"error\": \"User not found after update.\"}")
                                              .build());
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("{\"error\": \"Failed to update user profile. Data may be invalid or unchanged.\"}")
                              .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"An unexpected error occurred while updating profile.\"}")
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
     * Create user (using service layer with business logic) - Admin only
     */
    @POST
    @Path("/admin/{adminId}/add-user")
    public Response addUser(@PathParam("adminId") Long adminId, User user) {
        try {
            if (!userService.isAdmin(adminId)) {
                 return Response.status(Response.Status.FORBIDDEN)
                               .entity("{\"error\": \"Admin permissions required.\"}")
                               .build();
            }
            
            boolean success = adminService.addUser((Admin)userRepository.findByIdOptional(adminId).get(), user);
            
            if (success) {
                return Response.status(Response.Status.CREATED)
                              .entity(user)
                              .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("{\"error\": \"Failed to add user.\"}")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"" + e.getMessage() + "\"}")
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
             if (!userService.isAdmin(adminId)) {
                 return Response.status(Response.Status.FORBIDDEN)
                               .entity("{\"error\": \"Admin permissions required.\"}")
                               .build();
            }
            
            boolean success = adminService.updateUser((Admin)userRepository.findByIdOptional(adminId).get(), userId, updatedUser);
            
            if (success) {
                return Response.ok("{\"message\": \"User updated successfully\"}").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("{\"error\": \"Failed to update user\"}")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"" + e.getMessage() + "\"}")
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
            if (!userService.isAdmin(adminId)) {
                 return Response.status(Response.Status.FORBIDDEN)
                               .entity("{\"error\": \"Admin permissions required.\"}")
                               .build();
            }
            
            boolean success = adminService.removeUser((Admin)userRepository.findByIdOptional(adminId).get(), userId);
            
            if (success) {
                return Response.ok("{\"message\": \"User deleted successfully\"}").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("{\"error\": \"Failed to delete user\"}")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"" + e.getMessage() + "\"}")
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
            if (!userService.isAdmin(adminId)) {
                 return Response.status(Response.Status.FORBIDDEN)
                               .entity("{\"error\": \"Admin permissions required.\"}")
                               .build();
            }
            
            adminService.getUserStatistics((Admin)userRepository.findByIdOptional(adminId).get());
            
            long totalUsers = userRepository.count();
            return Response.ok("{\"message\": \"Statistics generated. Total users: " + totalUsers + "\"}").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("{\"error\": \"" + e.getMessage() + "\"}")
                          .build();
        }
    }
}