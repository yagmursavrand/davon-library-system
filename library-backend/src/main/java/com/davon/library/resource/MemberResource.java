package com.davon.library.resource;

import com.davon.library.model.Member;
import com.davon.library.model.User;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.MemberService;
import com.davon.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Path("/api/members")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemberResource {
    
    @Inject
    MemberRepository memberRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    MemberService memberService;
    
    @Inject
    UserService userService;
    
    /**
     * Register a new member (public endpoint)
     */
    @POST
    @Path("/register")
    public Response registerMember(MemberRegistrationRequest request) {
        try {
            Member member = memberService.registerMember(
                request.name, request.email, request.password);
            return Response.status(Response.Status.CREATED).entity(member).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("Registration failed: " + e.getMessage())
                          .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get member profile (requires authentication)
     */
    @GET
    @Path("/{memberId}/profile")
    public Response getMemberProfile(@PathParam("memberId") Long memberId,
                                   @HeaderParam("Authorization") String authHeader) {
        try {
            // Simple authentication check
            if (!isAuthenticated(authHeader)) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            Optional<Member> member = memberRepository.findByIdOptional(memberId);
            if (member.isPresent()) {
                return Response.ok(member.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Member not found")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Borrow a book (requires member authentication)
     */
    @POST
    @Path("/{memberId}/borrow/{bookId}")
    public Response borrowBook(@PathParam("memberId") Long memberId,
                              @PathParam("bookId") Long bookId,
                              @HeaderParam("Authorization") String authHeader) {
        try {
            // Authentication check
            Member authenticatedMember = getAuthenticatedMember(authHeader, memberId);
            if (authenticatedMember == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Unauthorized access")
                              .build();
            }
            
            boolean success = memberService.borrowBook(authenticatedMember, bookId);
            if (success) {
                return Response.ok("Book borrowed successfully").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Failed to borrow book")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Return a book (requires member authentication)
     */
    @POST
    @Path("/{memberId}/return/{bookId}")
    public Response returnBook(@PathParam("memberId") Long memberId,
                              @PathParam("bookId") Long bookId,
                              @HeaderParam("Authorization") String authHeader) {
        try {
            // Authentication check
            Member authenticatedMember = getAuthenticatedMember(authHeader, memberId);
            if (authenticatedMember == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Unauthorized access")
                              .build();
            }
            
            boolean success = memberService.returnBook(authenticatedMember, bookId);
            if (success) {
                return Response.ok("Book returned successfully").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Failed to return book")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Pay fine (requires member authentication)
     */
    @POST
    @Path("/{memberId}/pay-fine/{fineId}")
    public Response payFine(@PathParam("memberId") Long memberId,
                           @PathParam("fineId") Long fineId,
                           @HeaderParam("Authorization") String authHeader,
                           PaymentRequest request) {
        try {
            // Authentication check
            Member authenticatedMember = getAuthenticatedMember(authHeader, memberId);
            if (authenticatedMember == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Unauthorized access")
                              .build();
            }
            
            boolean success = memberService.payFine(authenticatedMember, fineId, 
                                                   request.amount, request.description);
            if (success) {
                return Response.ok("Fine payment processed successfully").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Failed to process payment")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Renew membership (requires member authentication)
     */
    @POST
    @Path("/{memberId}/renew-membership")
    public Response renewMembership(@PathParam("memberId") Long memberId,
                                   @HeaderParam("Authorization") String authHeader,
                                   RenewalRequest request) {
        try {
            // Authentication check
            Member authenticatedMember = getAuthenticatedMember(authHeader, memberId);
            if (authenticatedMember == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Unauthorized access")
                              .build();
            }
            
            boolean success = memberService.renewMembership(authenticatedMember, request.years);
            if (success) {
                return Response.ok("Membership renewed successfully").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity("Failed to renew membership")
                              .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get member statistics (requires member or admin authentication)
     */
    @GET
    @Path("/{memberId}/statistics")
    public Response getMemberStatistics(@PathParam("memberId") Long memberId,
                                       @HeaderParam("Authorization") String authHeader) {
        try {
            // Check if user is member themselves or admin
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                              .entity("Authentication required")
                              .build();
            }
            
            // Allow access if user is admin or accessing their own data
            if (!userService.isAdmin(authenticatedUser.getId()) && !authenticatedUser.getId().equals(memberId)) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Access denied")
                              .build();
            }
            
            Optional<Member> memberOpt = memberRepository.findByIdOptional(memberId);
            if (memberOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Member not found")
                              .build();
            }
            
            memberService.getMemberStatistics(memberOpt.get());
            return Response.ok("Statistics generated (check console)").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get all members (admin only)
     */
    @GET
    public Response getAllMembers(@HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Member> members = memberRepository.listAll();
            return Response.ok(members).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    // Helper methods for authentication
    private boolean isAuthenticated(String authHeader) {
        // Simple authentication check - in real app, use JWT or session
        return authHeader != null && authHeader.startsWith("Bearer ");
    }
    
    private User getAuthenticatedUser(String authHeader) {
        if (!isAuthenticated(authHeader)) {
            return null;
        }
        
        // Extract user ID from token (simplified for demo)
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            Long userId = Long.parseLong(token); // In real app, decode JWT
            return userRepository.findByIdOptional(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Member getAuthenticatedMember(String authHeader, Long expectedMemberId) {
        User user = getAuthenticatedUser(authHeader);
        if (user == null || !(user instanceof Member)) {
            return null;
        }
        
        // Ensure user is accessing their own data
        if (!user.getId().equals(expectedMemberId)) {
            return null;
        }
        
        return (Member) user;
    }
    
    // Request DTOs
    public static class MemberRegistrationRequest {
        public String name;
        public String email;
        public String password;
    }
    
    public static class PaymentRequest {
        public BigDecimal amount;
        public String description;
    }
    
    public static class RenewalRequest {
        public int years;
    }
} 