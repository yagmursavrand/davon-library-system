package com.davon.library.repository;

import com.davon.library.model.Admin;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AdminRepository implements PanacheRepository<Admin> {

    public Optional<Admin> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public List<Admin> findByAdminLevel(String adminLevel) {
        return find("adminLevel", adminLevel).list();
    }

    public List<Admin> findByDepartment(String department) {
        return find("department", department).list();
    }

    public List<Admin> findLoggedInAdmins() {
        return find("loggedIn", true).list();
    }

    public List<Admin> findByRole(String role) {
        return find("role", role).list();
    }

    public boolean emailExists(String email) {
        return count("email", email) > 0;
    }

    public List<Admin> findSuperAdmins() {
        return find("adminLevel", "SUPER_ADMIN").list();
    }

    public List<Admin> findActiveAdmins() {
        return find("loggedIn = true AND adminLevel IS NOT NULL").list();
    }
} 
 