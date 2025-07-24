package com.davon.library.resource;

import com.davon.library.model.Payment;
import com.davon.library.model.User;
import com.davon.library.model.Fine;
import com.davon.library.repository.PaymentRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.repository.FineRepository;
import com.davon.library.service.PaymentService;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {
    
    @Inject
    PaymentRepository paymentRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    FineRepository fineRepository;
    
    @Inject
    PaymentService paymentService;
    
    @Inject
    UserService userService;
    
    /**
     * Get all payments (admin only)
     */
    @GET
    public Response getAllPayments(@HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Payment> payments = paymentRepository.listAll();
            return Response.ok(payments).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get payment by ID (admin or payment owner)
     */
    @GET
    @Path("/{paymentId}")
    public Response getPaymentById(@PathParam("paymentId") Long paymentId,
                                  @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            Optional<Payment> paymentOpt = paymentRepository.findByIdOptional(paymentId);
            if (paymentOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Payment not found")
                              .build();
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if user can access this payment
            if (!userService.isAdmin(authenticatedUser.getId()) && 
                !payment.getUser().getId().equals(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Access denied")
                              .build();
            }
            
            return Response.ok(payment).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get payments by user (admin or user themselves)
     */
    @GET
    @Path("/user/{userId}")
    public Response getPaymentsByUser(@PathParam("userId") Long userId,
                                     @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            // Check if user can access these payments
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
            
            List<Payment> payments = paymentRepository.findByUser(userOpt.get());
            return Response.ok(payments).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Create payment (authenticated users)
     */
    @POST
    public Response createPayment(PaymentCreationRequest request,
                                 @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            if (request.amount <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Payment amount must be greater than 0")
                              .build();
            }
            
            Payment.PaymentMethod method;
            try {
                method = Payment.PaymentMethod.valueOf(request.paymentMethod.toUpperCase());
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Invalid payment method")
                              .build();
            }
            
            Fine fine = null;
            if (request.fineId != null) {
                Optional<Fine> fineOpt = fineRepository.findByIdOptional(request.fineId);
                fine = fineOpt.orElse(null);
            }
            
            Payment payment = paymentService.createPayment(
                authenticatedUser, 
                request.amount, 
                method, 
                fine
            );
            
            if (payment != null) {
                return Response.status(Response.Status.CREATED).entity(payment).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Failed to create payment")
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
    public static class PaymentCreationRequest {
        public double amount;
        public String paymentMethod;
        public Long fineId;
    }
} 