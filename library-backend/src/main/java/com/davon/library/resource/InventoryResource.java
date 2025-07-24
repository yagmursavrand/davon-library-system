package com.davon.library.resource;

import com.davon.library.model.Inventory;
import com.davon.library.model.User;
import com.davon.library.repository.InventoryRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.InventoryService;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {
    
    @Inject
    InventoryRepository inventoryRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    InventoryService inventoryService;
    
    @Inject
    UserService userService;
    
    /**
     * Get all inventory items (admin only)
     */
    @GET
    public Response getAllInventory(@HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Inventory> inventory = inventoryRepository.listAll();
            return Response.ok(inventory).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get inventory by ID (admin only)
     */
    @GET
    @Path("/{inventoryId}")
    public Response getInventoryById(@PathParam("inventoryId") Long inventoryId,
                                   @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Inventory> inventory = inventoryRepository.findByIdOptional(inventoryId);
            if (inventory.isPresent()) {
                return Response.ok(inventory.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Inventory not found")
                              .build();
            }
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Update inventory (admin only)
     */
    @PUT
    @Path("/{inventoryId}")
    public Response updateInventory(@PathParam("inventoryId") Long inventoryId,
                                   InventoryUpdateRequest request,
                                   @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Inventory> inventoryOpt = inventoryRepository.findByIdOptional(inventoryId);
            if (inventoryOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Inventory not found")
                              .build();
            }
            
            Inventory inventory = inventoryOpt.get();
            
            if (request.totalCopies != null) {
                inventory.setTotalCopies(request.totalCopies);
            }
            if (request.availableCopies != null) {
                inventory.setAvailableCopies(request.availableCopies);
            }
            if (request.reservedCopies != null) {
                inventory.setReservedCopies(request.reservedCopies);
            }
            if (request.damagedCopies != null) {
                inventory.setDamagedCopies(request.damagedCopies);
            }
            
            inventoryRepository.persist(inventory);
            return Response.ok("Inventory updated successfully").build();
            
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
    public static class InventoryUpdateRequest {
        public Integer totalCopies;
        public Integer availableCopies;
        public Integer reservedCopies;
        public Integer damagedCopies;
    }
} 