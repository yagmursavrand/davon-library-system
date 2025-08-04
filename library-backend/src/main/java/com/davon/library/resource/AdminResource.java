package com.davon.library.resource;

import com.davon.library.model.Admin;
import com.davon.library.model.User;
import com.davon.library.model.Fine;
import java.math.BigDecimal;
import com.davon.library.repository.AdminRepository;
import com.davon.library.repository.UserRepository;
import com.davon.library.service.AdminService;
import com.davon.library.service.UserService;
import java.util.Date;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/api/admins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {
    
    @Inject
    AdminRepository adminRepository;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    AdminService adminService;
    
    @Inject
    UserService userService;
    
    /**
     * Get all admins (super admin only)
     */
    @GET
    public Response getAllAdmins(@HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Admin> admins = adminRepository.listAll();
            return Response.ok(admins).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get admin by ID
     */
    @GET
    @Path("/{adminId}")
    public Response getAdminById(@PathParam("adminId") Long adminId,
                                @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Admin> admin = adminRepository.findByIdOptional(adminId);
            if (admin.isPresent()) {
                return Response.ok(admin.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Admin not found")
                              .build();
            }
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Create new admin (super admin only)
     */
    @POST
    public Response createAdmin(AdminCreationRequest request,
                               @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            // Check if user exists
            Optional<User> userOpt = userRepository.findByEmail(request.email);
            if (userOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("User not found with email: " + request.email)
                              .build();
            }
            
            User user = userOpt.get();
            
            // Create admin manually since promoteUserToAdmin doesn't exist
            Admin admin = new Admin();
            admin.setName(user.getName());
            admin.setEmail(user.getEmail());
            admin.setPassword(user.getPassword());
            admin.setRole("ADMIN");
            admin.setAdminLevel(request.adminLevel.toUpperCase());
            admin.setDepartment(request.department);
            admin.setCreatedAt(new Date());
            
            adminRepository.persist(admin);
            return Response.status(Response.Status.CREATED)
                          .entity(admin)
                          .build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Update admin details
     */
    @PUT
    @Path("/{adminId}")
    public Response updateAdmin(@PathParam("adminId") Long adminId,
                               AdminUpdateRequest request,
                               @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Admin> adminOpt = adminRepository.findByIdOptional(adminId);
            if (adminOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Admin not found")
                              .build();
            }
            
            Admin admin = adminOpt.get();
            
            // Update fields if provided
            if (request.adminLevel != null && !request.adminLevel.trim().isEmpty()) {
                admin.setAdminLevel(request.adminLevel.toUpperCase());
            }
            
            if (request.department != null && !request.department.trim().isEmpty()) {
                admin.setDepartment(request.department.trim());
            }
            
            adminRepository.persist(admin);
            return Response.ok("Admin updated successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Remove admin privileges (demote to user)
     */
    @DELETE
    @Path("/{adminId}")
    public Response removeAdmin(@PathParam("adminId") Long adminId,
                               @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            Optional<Admin> adminOpt = adminRepository.findByIdOptional(adminId);
            if (adminOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Admin not found")
                              .build();
            }
            
            Admin admin = adminOpt.get();
            adminRepository.delete(admin);
            return Response.ok("Admin privileges removed successfully").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get admins by department
     */
    @GET
    @Path("/department/{department}")
    public Response getAdminsByDepartment(@PathParam("department") String department,
                                         @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Admin> admins = adminRepository.findByDepartment(department);
            return Response.ok(admins).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    /**
     * Get admins by level
     */
    @GET
    @Path("/level/{level}")
    public Response getAdminsByLevel(@PathParam("level") String level,
                                    @HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Admin> admins = adminRepository.findByAdminLevel(level.toUpperCase());
            return Response.ok(admins).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Error: " + e.getMessage())
                          .build();
        }
    }
    
    public static class FineDTO {
        public Long id;
        public String userName;
        public String userEmail;
        public BigDecimal amount;
        public String reason;
        public Date issuedDate;

        public static FineDTO fromEntity(Fine fine) {
            FineDTO dto = new FineDTO();
            dto.id = fine.getId();
            dto.amount = fine.getAmount();
            dto.reason = fine.getReason();
            dto.issuedDate = fine.getIssuedDate();
            if (fine.getUser() != null) {
                dto.userName = fine.getUser().getName();
                dto.userEmail = fine.getUser().getEmail();
            }
            return dto;
        }
    }

    /**
     * Get all unpaid fines (for admin dashboard)
     */
    @GET
    @Path("/fines/unpaid")
    public Response getAllUnpaidFines(@HeaderParam("Authorization") String authHeader) {
        try {
            User authenticatedUser = getAuthenticatedUser(authHeader);
            if (authenticatedUser == null || !userService.isAdmin(authenticatedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                              .entity("Admin access required")
                              .build();
            }
            
            List<Fine> unpaidFines = adminService.getAllUnpaidFines();
            List<FineDTO> fineDTOs = unpaidFines.stream()
                                                .map(FineDTO::fromEntity)
                                                .collect(Collectors.toList());
            return Response.ok(fineDTOs).build();
            
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
    public static class AdminCreationRequest {
        public String email;
        public String adminLevel;
        public String department;
    }
    
    public static class AdminUpdateRequest {
        public String adminLevel;
        public String department;
    }
} 