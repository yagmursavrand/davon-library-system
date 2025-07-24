package com.davon.library.service;

import com.davon.library.model.Profile;
import com.davon.library.model.User;
import com.davon.library.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.Calendar;
import java.util.Optional;

/**
 * ProfileService - handles all profile-related business operations
 */
@ApplicationScoped
public class ProfileService {

    @Inject
    UserRepository userRepository;

    /**
     * Update profile information with validation
     */
    @Transactional
    public boolean updateProfile(Long profileId, String newAddress, String newPhone, Date newBirthDate) {
        if (profileId == null) {
            System.out.println("Profile ID cannot be null");
            return false;
        }

        Optional<User> userOpt = userRepository.find("profile.id", profileId).firstResultOptional();
        if (userOpt.isEmpty()) {
            System.out.println("Profile not found");
            return false;
        }

        Profile profile = userOpt.get().getProfile();
        if (profile == null) {
            System.out.println("User has no profile");
            return false;
        }

        boolean updated = false;
        
        // Update address
        if (newAddress != null && !newAddress.trim().isEmpty() && !newAddress.equals(profile.getAddress())) {
            if (isValidAddress(newAddress)) {
                profile.setAddress(newAddress.trim());
                updated = true;
                System.out.println("Address updated successfully");
            } else {
                System.out.println("Invalid address format");
                return false;
            }
        }
        
        // Update phone
        if (newPhone != null && !newPhone.trim().isEmpty() && !newPhone.equals(profile.getPhone())) {
            if (isValidPhoneNumber(newPhone)) {
                profile.setPhone(newPhone.trim());
                updated = true;
                System.out.println("Phone number updated successfully");
            } else {
                System.out.println("Invalid phone number format");
                return false;
            }
        }
        
        // Update birth date
        if (newBirthDate != null && !newBirthDate.equals(profile.getBirthDate())) {
            if (isValidBirthDate(newBirthDate)) {
                profile.setBirthDate(newBirthDate);
                updated = true;
                System.out.println("Birth date updated successfully");
            } else {
                System.out.println("Invalid birth date");
                return false;
            }
        }
        
        if (updated) {
            profile.setLastUpdated(new Date());
            System.out.println("Profile updated successfully at: " + profile.getLastUpdated());
        } else {
            System.out.println("No changes made to profile");
        }
        
        return updated;
    }

    /**
     * Update individual field
     */
    @Transactional
    public boolean updateProfile(Long profileId, String fieldName, String newValue) {
        if (profileId == null || fieldName == null) {
            System.out.println("Profile ID and field name cannot be null");
            return false;
        }

        Optional<User> userOpt = userRepository.find("profile.id", profileId).firstResultOptional();
        if (userOpt.isEmpty()) {
            System.out.println("Profile not found");
            return false;
        }

        Profile profile = userOpt.get().getProfile();
        if (profile == null) {
            System.out.println("User has no profile");
            return false;
        }

        switch (fieldName.toLowerCase()) {
            case "address":
                return updateProfile(profileId, newValue, profile.getPhone(), profile.getBirthDate());
            case "phone":
                return updateProfile(profileId, profile.getAddress(), newValue, profile.getBirthDate());
            default:
                System.out.println("Invalid field name: " + fieldName);
                return false;
        }
    }

    /**
     * Update birth date only
     */
    @Transactional
    public boolean updateProfileBirthDate(Long profileId, Date newBirthDate) {
        if (profileId == null) {
            System.out.println("Profile ID cannot be null");
            return false;
        }

        Optional<User> userOpt = userRepository.find("profile.id", profileId).firstResultOptional();
        if (userOpt.isEmpty()) {
            System.out.println("Profile not found");
            return false;
        }

        Profile profile = userOpt.get().getProfile();
        if (profile == null) {
            System.out.println("User has no profile");
            return false;
        }

        return updateProfile(profileId, profile.getAddress(), profile.getPhone(), newBirthDate);
    }

    /**
     * Get age based on birth date
     */
    public int getAge(Profile profile) {
        if (profile == null || profile.getBirthDate() == null) {
            return -1;
        }
        
        Calendar now = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        birth.setTime(profile.getBirthDate());
        
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        
        // Check if birthday has occurred this year
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        
        return age;
    }

    /**
     * Check if profile is complete
     */
    public boolean isProfileComplete(Profile profile) {
        if (profile == null) {
            return false;
        }
        
        return profile.getAddress() != null && !profile.getAddress().trim().isEmpty() &&
               profile.getPhone() != null && !profile.getPhone().trim().isEmpty() &&
               profile.getBirthDate() != null;
    }

    /**
     * Get formatted phone number
     */
    public String getFormattedPhone(Profile profile) {
        if (profile == null || profile.getPhone() == null) {
            return null;
        }
        
        String cleanPhone = profile.getPhone().replaceAll("[^0-9]", "");
        if (cleanPhone.length() == 10) {
            return String.format("(%s) %s-%s", 
                cleanPhone.substring(0, 3),
                cleanPhone.substring(3, 6),
                cleanPhone.substring(6));
        }
        return profile.getPhone(); // Return as-is if not 10 digits
    }

    /**
     * Display profile summary
     */
    public void displayProfile(Profile profile) {
        if (profile == null) {
            System.out.println("Profile is null");
            return;
        }

        System.out.println("=== Profile Information ===");
        System.out.println("Address: " + (profile.getAddress() != null ? profile.getAddress() : "Not provided"));
        System.out.println("Phone: " + (profile.getPhone() != null ? getFormattedPhone(profile) : "Not provided"));
        System.out.println("Birth Date: " + (profile.getBirthDate() != null ? profile.getBirthDate() : "Not provided"));
        System.out.println("Age: " + (getAge(profile) >= 0 ? getAge(profile) + " years old" : "Unknown"));
        System.out.println("Profile Complete: " + (isProfileComplete(profile) ? "Yes" : "No"));
        System.out.println("Last Updated: " + (profile.getLastUpdated() != null ? profile.getLastUpdated() : "Never"));
        System.out.println("===========================");
    }

    /**
     * Create new profile for user
     */
    @Transactional
    public Profile createProfile(User user, String address, String phone, Date birthDate) {
        if (user == null) {
            System.out.println("User cannot be null");
            return null;
        }

        if (user.getProfile() != null) {
            System.out.println("User already has a profile");
            return user.getProfile();
        }

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setAddress(address);
        profile.setPhone(phone);
        profile.setBirthDate(birthDate);
        profile.setLastUpdated(new Date());

        user.setProfile(profile);

        System.out.println("Profile created successfully for user: " + user.getName());
        return profile;
    }

    // Validation helper methods
    private boolean isValidAddress(String address) {
        if (address == null || address.trim().length() < 10) {
            return false;
        }
        // Basic validation - should contain letters and numbers
        return address.matches(".*[a-zA-Z].*") && address.length() <= 200;
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        
        // Remove all non-digits
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        
        // Should be 10-15 digits
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 15;
    }

    private boolean isValidBirthDate(Date birthDate) {
        if (birthDate == null) return false;
        
        Calendar now = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        
        // Birth date should not be in the future
        if (birthDate.after(now.getTime())) {
            return false;
        }
        
        // Person should not be older than 150 years
        Calendar maxAge = Calendar.getInstance();
        maxAge.add(Calendar.YEAR, -150);
        
        return birthDate.after(maxAge.getTime());
    }
} 
 