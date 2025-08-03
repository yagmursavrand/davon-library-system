package com.davon.library.resource;

import com.davon.library.model.Fine;
import com.davon.library.model.Member;
import com.davon.library.repository.FineRepository;
import com.davon.library.repository.MemberRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/fines")
@Produces(MediaType.APPLICATION_JSON)
public class FineResource {

    @Inject
    FineRepository fineRepository;

    @Inject
    MemberRepository memberRepository;

    @GET
    @Path("/member/{memberId}")
    public Response getUnpaidFinesForMember(@PathParam("memberId") Long memberId, @HeaderParam("Authorization") String authHeader) {
        // Basic auth check, in a real app this would be more robust (e.g. JWT)
        if (authHeader == null || !authHeader.startsWith("Bearer ") || !authHeader.substring(7).equals(memberId.toString())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
        }

        Member member = memberRepository.findByIdOptional(memberId)
            .orElseThrow(() -> new WebApplicationException("Member not found", Response.Status.NOT_FOUND));

        List<Fine> unpaidFines = fineRepository.find("user = ?1 and paid = false", member).list();

        // We can create a DTO to send cleaner data to the frontend
        List<FineDTO> fineDTOs = unpaidFines.stream()
            .map(FineDTO::fromEntity)
            .collect(Collectors.toList());

        return Response.ok(fineDTOs).build();
    }

    @POST
    @Path("/member/{memberId}/pay")
    @Transactional
    public Response payAllFinesForMember(@PathParam("memberId") Long memberId, @HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ") || !authHeader.substring(7).equals(memberId.toString())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
        }

        Member member = memberRepository.findByIdOptional(memberId)
            .orElseThrow(() -> new WebApplicationException("Member not found", Response.Status.NOT_FOUND));

        List<Fine> unpaidFines = fineRepository.find("user = ?1 and paid = false", member).list();

        for (Fine fine : unpaidFines) {
            fine.setPaid(true);
            fine.setPaidDate(new Date());
            fine.setStatus(com.davon.library.model.Transaction.TransactionStatus.COMPLETED);
            // In a real app, you might set a payment method, e.g., "ONLINE"
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    // DTO to format the Fine data for the frontend
    public static class FineDTO {
        public Long id;
        public String bookTitle;
        public String amount;
        public String issuedDate;
        public String reason;

        public static FineDTO fromEntity(Fine fine) {
            FineDTO dto = new FineDTO();
            dto.id = fine.getId();
            dto.bookTitle = fine.getLoan() != null && fine.getLoan().getBook() != null ? fine.getLoan().getBook().getTitle() : "N/A";
            dto.amount = fine.getAmount().toPlainString();
            dto.issuedDate = fine.getIssuedDate().toString();
            dto.reason = fine.getReason();
            return dto;
        }
    }
}
