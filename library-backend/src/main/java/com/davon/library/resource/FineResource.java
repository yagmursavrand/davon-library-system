package com.davon.library.resource;

import com.davon.library.model.Fine;
import com.davon.library.model.Member;
import com.davon.library.model.Payment;
import com.davon.library.repository.FineRepository;
import com.davon.library.repository.MemberRepository;
import com.davon.library.service.PaymentService;
import com.davon.library.service.TransactionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Path("/api/fines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FineResource {
    
    @Inject
    FineRepository fineRepository;
    
    @Inject
    MemberRepository memberRepository;
    
    @Inject
    PaymentService paymentService;
    
    @Inject
    TransactionService transactionService;
    
    /**
     * Get all fines (admin only)
     */
    @GET
    public List<Fine> getAllFines() {
        return fineRepository.listAll();
    }
    
    /**
     * Get fine by ID
     */
    @GET
    @Path("/{id}")
    public Response getFineById(@PathParam("id") Long id) {
        Optional<Fine> fine = fineRepository.findByIdOptional(id);
        if (fine.isPresent()) {
            return Response.ok(fine.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Fine not found")
                          .build();
        }
    }
    
    /**
     * Get fines by member ID
     */
    @GET
    @Path("/member/{memberId}")
    public Response getFinesByMember(@PathParam("memberId") Long memberId) {
        Optional<Member> member = memberRepository.findByIdOptional(memberId);
        if (member.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Member not found")
                          .build();
        }
        
        List<Fine> fines = fineRepository.findByUser(member.get());
        return Response.ok(fines).build();
    }
    
    /**
     * Get unpaid fines by member
     */
    @GET
    @Path("/member/{memberId}/unpaid")
    public Response getUnpaidFinesByMember(@PathParam("memberId") Long memberId) {
        Optional<Member> member = memberRepository.findByIdOptional(memberId);
        if (member.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Member not found")
                          .build();
        }
        
        List<Fine> unpaidFines = fineRepository.findUnpaidFines(member.get());
        return Response.ok(unpaidFines).build();
    }
    
    /**
     * Pay a fine
     */
    @POST
    @Path("/{fineId}/pay")
    public Response payFine(@PathParam("fineId") Long fineId, PaymentRequest request) {
        try {
            Optional<Fine> fine = fineRepository.findByIdOptional(fineId);
            if (fine.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Fine not found")
                              .build();
            }
            
            // Create payment
            Payment payment = paymentService.createPayment(
                fine.get().getUser(), 
                request.amount, 
                Payment.PaymentMethod.valueOf(request.paymentMethod),
                fine.get()
            );
            
            if (payment != null) {
                // Process payment
                boolean success = paymentService.processPayment(payment.getId());
                if (success) {
                    return Response.status(Response.Status.CREATED)
                                  .entity("Fine payment processed successfully")
                                  .build();
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                                  .entity("Payment processing failed")
                                  .build();
                }
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
    
    /**
     * Get member's total unpaid fines amount
     */
    @GET
    @Path("/member/{memberId}/total")
    public Response getTotalUnpaidFines(@PathParam("memberId") Long memberId) {
        Optional<Member> member = memberRepository.findByIdOptional(memberId);
        if (member.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Member not found")
                          .build();
        }
        
        BigDecimal totalFines = fineRepository.calculateTotalUnpaidFines(member.get());
        return Response.ok(new TotalFinesResponse(totalFines)).build();
    }
    
    /**
     * Get all overdue unpaid fines
     */
    @GET
    @Path("/overdue")
    public List<Fine> getOverdueFines() {
        return fineRepository.findOverdueFines();
    }
    
    // Request/Response DTOs
    public static class PaymentRequest {
        public BigDecimal amount;
        public String paymentMethod; // CASH, CREDIT_CARD, etc.
    }
    
    public static class TotalFinesResponse {
        public BigDecimal totalAmount;
        
        public TotalFinesResponse(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
} 