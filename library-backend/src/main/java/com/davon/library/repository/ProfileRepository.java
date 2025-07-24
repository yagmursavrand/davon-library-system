package com.davon.library.repository;

import com.davon.library.model.Profile;
import com.davon.library.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Date;
import java.util.Optional;

@ApplicationScoped
public class ProfileRepository implements PanacheRepository<Profile> {

    public Optional<Profile> findByUser(User user) {
        return find("user", user).firstResultOptional();
    }

    public Optional<Profile> findByUserId(Long userId) {
        return find("user.id", userId).firstResultOptional();
    }

    public List<Profile> findByPhone(String phone) {
        return find("phone", phone).list();
    }

    public List<Profile> findProfilesWithAddress() {
        return find("address IS NOT NULL AND address != ''").list();
    }

    public List<Profile> findProfilesWithPhone() {
        return find("phone IS NOT NULL AND phone != ''").list();
    }

    public List<Profile> findCompleteProfiles() {
        return find("address IS NOT NULL AND phone IS NOT NULL AND birthDate IS NOT NULL").list();
    }

    public List<Profile> findIncompleteProfiles() {
        return find("address IS NULL OR phone IS NULL OR birthDate IS NULL").list();
    }

    public List<Profile> findRecentlyUpdated(int days) {
        Date cutoffDate = new Date(System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000));
        return find("lastUpdated >= ?1", cutoffDate).list();
    }

    public List<Profile> findByBirthDateRange(Date startDate, Date endDate) {
        return find("birthDate >= ?1 and birthDate <= ?2", startDate, endDate).list();
    }

    public long countCompleteProfiles() {
        return count("address IS NOT NULL AND phone IS NOT NULL AND birthDate IS NOT NULL");
    }

    public long countIncompleteProfiles() {
        return count("address IS NULL OR phone IS NULL OR birthDate IS NULL");
    }

    public boolean phoneExists(String phone) {
        return count("phone", phone) > 0;
    }
} 