package com.davon.library.repository;

import com.davon.library.model.Library;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LibraryRepository implements PanacheRepository<Library> {
    
    /**
     * Find library by name
     */
    public Optional<Library> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
    
    /**
     * Find libraries by city/address
     */
    public List<Library> findByAddress(String addressKeyword) {
        return find("LOWER(address) LIKE LOWER(?1)", "%" + addressKeyword + "%").list();
    }
    
    /**
     * Find operational libraries
     */
    public List<Library> findOperationalLibraries() {
        return find("name IS NOT NULL AND address IS NOT NULL").list();
    }
    
    /**
     * Find libraries with phone number
     */
    public List<Library> findLibrariesWithContact() {
        return find("phone IS NOT NULL OR email IS NOT NULL").list();
    }
    
    /**
     * Search libraries by name or address
     */
    public List<Library> searchLibraries(String searchTerm) {
        return find("LOWER(name) LIKE LOWER(?1) OR LOWER(address) LIKE LOWER(?1)", 
                   "%" + searchTerm + "%").list();
    }
} 