package com.davon.library.repository;

import com.davon.library.model.Inventory;
import com.davon.library.model.Book;
import com.davon.library.model.Library;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class InventoryRepository implements PanacheRepository<Inventory> {

    public Optional<Inventory> findByBookAndLibrary(Book book, Library library) {
        return find("book = ?1 and library = ?2", book, library).firstResultOptional();
    }

    public List<Inventory> findByBook(Book book) {
        return find("book", book).list();
    }

    public List<Inventory> findByLibrary(Library library) {
        return find("library", library).list();
    }

    public List<Inventory> findAvailableInventories() {
        return find("availableCopies > 0").list();
    }

    public List<Inventory> findLowStockInventories(int threshold) {
        return find("availableCopies < ?1", threshold).list();
    }

    public long countTotalCopies() {
        Long result = find("SELECT SUM(totalCopies) FROM Inventory").project(Long.class).firstResult();
        return result != null ? result : 0L;
    }

    public long countAvailableCopies() {
        Long result = find("SELECT SUM(availableCopies) FROM Inventory").project(Long.class).firstResult();
        return result != null ? result : 0L;
    }
} 