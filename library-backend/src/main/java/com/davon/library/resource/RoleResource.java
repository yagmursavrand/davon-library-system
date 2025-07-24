package com.davon.library.resource;

import com.davon.library.model.Role;
import com.davon.library.model.User;
import com.davon.library.repository.RoleRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.UserService;
import com.davon.library.service.RoleService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {
    
    @Inject
    RoleRepository roleRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    UserService userService;
    
    @Inject
    RoleService roleService;
    
    /**
     * Get all roles (admin only)
     */
    @GET
    public Response getAllRoles(@HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Role> roles = roleRepository.listAll();
            return Response.ok(roles).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get role by ID (admin only)
     */
    @GET
    @Path("/{roleId}")
    public Response getRoleById(@PathParam("roleId") Long roleId,
                               @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Role> role = roleRepository.findByIdOptional(roleId);
            if (role.isPresent()) {
                return Response.ok(role.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Role not found")
                              .build();
            }
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Create new role (admin only)
     */
    @POST
    public Response createRole(RoleCreationRequest request,
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
                              .entity("Role name is required")
                              .build();
            }
            
            // Use service layer for business logic
            Role role = roleService.createRole(request.name, null);
            if (role == null) {
                return Response.status(Response.Status.CONFLICT)
                              .entity("Role already exists")
                              .build();
            }
            
            return Response.status(Response.Status.CREATED).entity(role).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Update role (admin only)
     */
    @PUT
    @Path("/{roleId}")
    public Response updateRole(@PathParam("roleId") Long roleId,
                              RoleUpdateRequest request,
                              @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Role> roleOpt = roleRepository.findByIdOptional(roleId);
            if (roleOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Role not found")
                              .build();
            }
            
            // Use service layer for business logic
            boolean updated = roleService.updateRole(roleId, request.name, null);
            if (updated) {
                return Response.ok("Role updated successfully").build();
            } else {
                return Response.status(Response.Status.CONFLICT)
                              .entity("Role name already exists or no changes made")
                              .build();
            }
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Delete role (admin only)
     */
    @DELETE
    @Path("/{roleId}")
    public Response deleteRole(@PathParam("roleId") Long roleId,
                              @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            // Use service layer for business logic
            boolean deleted = roleService.deleteRole(roleId);
            if (deleted) {
                return Response.ok("Role deleted successfully").build();
            } else {
                return Response.status(Response.Status.CONFLICT)
                              .entity("Cannot delete role - it may be assigned to users or not found")
                              .build();
            }
            
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
    public static class RoleCreationRequest {
        public String name;
        public String description;
    }
    
    public static class RoleUpdateRequest {
        public String name;
        public String description;
    }
} 