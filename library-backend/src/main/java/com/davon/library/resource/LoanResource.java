package com.davon.library.resource;

import com.davon.library.model.Book;
import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.repository.LoanRepository;
import com.davon.library.repository.MemberRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.repository.BookRepository;
import com.davon.library.service.LoanService;
import com.davon.library.service.MemberService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.davon.library.resource.MemberResource.LoanDTO;
import jakarta.ws.rs.WebApplicationException;

@Path("/api/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanResource {
    
    @Inject
    LoanRepository loanRepository;
    
    @Inject
    BookRepository bookRepository;
    
    @Inject
    MemberRepository memberRepository;
    
    @Inject
    LoanService loanService;
    
    @Inject
    MemberService memberService;
    
    /**
     * Get all loans (admin only)
     */
    @GET
    public List<Loan> getAllLoans() {
        return loanRepository.listAll();
    }
    
    /**
     * Get loan by ID
     */
    @GET
    @Path("/{id}")
    public Response getLoanById(@PathParam("id") Long id) {
        Optional<Loan> loan = loanRepository.findByIdOptional(id);
        if (loan.isPresent()) {
            return Response.ok(loan.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Loan not found")
                          .build();
        }
    }
    
    /**
     * Get loans by member ID
     */
    @GET
    @Path("/member/{memberId}")
    public Response getLoansByMember(@PathParam("memberId") Long memberId) {
        Member member = memberRepository.findByIdOptional(memberId)
            .orElseThrow(() -> new WebApplicationException("Member not found", Response.Status.NOT_FOUND));
        
        List<Loan> loans = loanRepository.findByMember(member);
        List<LoanDTO> loanDTOs = loans.stream()
                                      .map(LoanDTO::fromEntity)
                                      .collect(Collectors.toList());
        return Response.ok(loanDTOs).build();
    }
    
    /**
     * Get active loans by member
     */
    @GET
    @Path("/member/{memberId}/active")
    public Response getActiveLoansByMember(@PathParam("memberId") Long memberId) {
        Optional<Member> member = memberRepository.findByIdOptional(memberId);
        if (member.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Member not found")
                          .build();
        }
        
        List<Loan> activeLoans = loanRepository.findActiveLoansByMember(member.get());
        return Response.ok(activeLoans).build();
    }
    
    /**
     * Return a book (update loan)
     */
    @PUT
    @Path("/{loanId}/return")
    @Transactional
    public Response returnBook(@PathParam("loanId") Long loanId) {
        try {
            boolean success = loanService.markAsReturned(loanId);
            if (success) {
                // After marking loan as returned, update the book's status
                Loan loan = loanRepository.findById(loanId);
                if (loan != null && loan.getBook() != null) {
                    Book book = loan.getBook();
                    book.setStatus(Book.BookStatus.AVAILABLE);
                    bookRepository.persist(book); // Save the updated book status
                }
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
     * Get overdue loans
     */
    @GET
    @Path("/overdue")
    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans();
    }
    
    /**
     * Renew a loan (extend due date)
     */
    @PUT
    @Path("/{loanId}/renew")
    public Response renewLoan(@PathParam("loanId") Long loanId, RenewRequest request) {
        try {
            // For now, return a simple success response
            // TODO: Implement actual loan renewal logic
            return Response.ok("Loan renewed successfully").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get loans by status
     */
    @GET
    @Path("/status/{status}")
    public Response getLoansByStatus(@PathParam("status") String status) {
        try {
            // Convert string to enum
            Loan.LoanStatus loanStatus = Loan.LoanStatus.valueOf(status.toUpperCase());
            List<Loan> loans = loanRepository.findByStatus(loanStatus);
            return Response.ok(loans).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("Invalid status: " + status)
                          .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    // Request DTOs
    public static class RenewRequest {
        public Integer additionalDays = 7; // Default 7 days
    }
} 