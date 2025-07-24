package com.davon.library.resource;

import com.davon.library.model.Transaction;
import com.davon.library.model.User;
import com.davon.library.repository.TransactionRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.TransactionService;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {
    
    @Inject
    TransactionRepository transactionRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    TransactionService transactionService;
    
    @Inject
    UserService userService;
    
    /**
     * Get all transactions (admin only)
     */
    @GET
    public Response getAllTransactions(@HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Transaction> transactions = transactionRepository.listAll();
            return Response.ok(transactions).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get transaction by ID (admin or transaction owner)
     */
    @GET
    @Path("/{transactionId}")
    public Response getTransactionById(@PathParam("transactionId") Long transactionId,
                                      @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
            if (transactionOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Transaction not found")
                              .build();
            }
            
            Transaction transaction = transactionOpt.get();
            
            // Check if user can access this transaction
            if (!userService.isAdmin(authenticatedUser.getId()) && 
                !transaction.getUser().getId().equals(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Access denied")
                              .build();
            }
            
            return Response.ok(transaction).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get transactions by user (admin or user themselves)
     */
    @GET
    @Path("/user/{userId}")
    public Response getTransactionsByUser(@PathParam("userId") Long userId,
                                         @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            // Check if user can access these transactions
            if (!userService.isAdmin(authenticatedUser.getId()) && 
                !authenticatedUser.getId().equals(userId)) {
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
            
            List<Transaction> transactions = transactionRepository.findByUser(userOpt.get());
            return Response.ok(transactions).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get transactions by status (admin only)
     */
    @GET
    @Path("/status/{status}")
    public Response getTransactionsByStatus(@PathParam("status") String status,
                                           @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Transaction.TransactionStatus transactionStatus;
            try {
                transactionStatus = Transaction.TransactionStatus.valueOf(status.toUpperCase());
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Invalid transaction status")
                              .build();
            }
            
            List<Transaction> transactions = transactionRepository.findByStatus(transactionStatus);
            return Response.ok(transactions).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Update transaction status (admin only)
     */
    @PUT
    @Path("/{transactionId}/status")
    public Response updateTransactionStatus(@PathParam("transactionId") Long transactionId,
                                           StatusUpdateRequest request,
                                           @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Transaction> transactionOpt = transactionRepository.findByIdOptional(transactionId);
            if (transactionOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Transaction not found")
                              .build();
            }
            
            Transaction.TransactionStatus newStatus;
            try {
                newStatus = Transaction.TransactionStatus.valueOf(request.status.toUpperCase());
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Invalid transaction status")
                              .build();
            }
            
            // Update transaction status manually since updateTransactionStatus doesn't exist
            Transaction transaction = transactionOpt.get();
            transaction.setStatus(newStatus);
            transactionRepository.persist(transaction);
            return Response.ok("Transaction status updated successfully").build();
            
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
    public static class StatusUpdateRequest {
        public String status;
    }
} 