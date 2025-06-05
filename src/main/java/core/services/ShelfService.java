package core.services;

import core.dao.ShelfDAO;
import core.models.Shelf;

import java.sql.SQLException;

public class ShelfService {
    private final ShelfDAO shelfDAO;

    // Constructor accepting ShelfDAO instance (Dependency Injection)
    public ShelfService(ShelfDAO shelfDAO) {
        this.shelfDAO = shelfDAO;
    }

    // Add a new shelf entry for the item
    public void addShelf(Shelf shelf) throws SQLException {
        shelfDAO.addShelf(shelf);
    }

    // Retrieve a shelf by its product code
    public Shelf getShelfByProductCode(String productCode) throws SQLException {
        return shelfDAO.getShelfByProductCode(productCode);
    }

    // Update an existing shelf entry
    public void updateShelf(Shelf shelf) throws SQLException {
        shelfDAO.updateShelf(shelf);
    }

    // Delete shelf entry by product code
    public void deleteShelf(String productCode) throws SQLException {
        shelfDAO.deleteShelf(productCode);
    }
}
