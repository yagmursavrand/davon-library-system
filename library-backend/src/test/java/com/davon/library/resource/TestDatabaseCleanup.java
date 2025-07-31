package com.davon.library.resource;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Utility class for cleaning up the test database in the correct order
 * to respect foreign key constraints, especially for MSSQL.
 */
public class TestDatabaseCleanup {

    /**
     * Cleans up the database by deleting all data in the correct order
     * to respect foreign key constraints. It also disables and re-enables
     * triggers to prevent interference during cleanup.
     *
     * @param entityManager The EntityManager to use for database operations
     */
    @Transactional
    public static void cleanupDatabase(EntityManager entityManager) {
        // The order of deletion is critical to avoid foreign key constraint violations.
        // We delete from the "many" side of relationships before the "one" side.
        
        // Junction tables and entities with multiple foreign keys first
        entityManager.createNativeQuery("DELETE FROM notifications").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM reservations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM role_permissions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM book_authors").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM member_borrowed_books").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM member_fine_history").executeUpdate();

        // Entities that are highly dependent on others
        entityManager.createNativeQuery("DELETE FROM transactions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM loans").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM inventories").executeUpdate();
        
        // Core entities that are referenced by the above tables
        entityManager.createNativeQuery("DELETE FROM admins").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM members").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM profiles").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM books").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM authors").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM libraries").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM roles").executeUpdate();
    }
}
