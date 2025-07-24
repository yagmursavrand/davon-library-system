package com.davon.library.repository;

import com.davon.library.model.Role;
import com.davon.library.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

    public Optional<Role> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public List<Role> findByUser(User user) {
        return find("users", user).list();
    }

    public boolean roleNameExists(String name) {
        return count("name", name) > 0;
    }

    public List<Role> findRolesWithPermissions() {
        return find("SIZE(permissions) > 0").list();
    }

    public long countRolesByUser(User user) {
        return count("users", user);
    }

    public List<Role> findRolesWithUsers() {
        return find("SIZE(users) > 0").list();
    }

    public List<Role> findEmptyRoles() {
        return find("SIZE(users) = 0").list();
    }
} 