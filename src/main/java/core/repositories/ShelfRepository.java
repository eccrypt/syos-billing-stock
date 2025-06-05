package core.repositories;

import core.models.Shelf;
import java.sql.SQLException;

public interface ShelfRepository {
    void addShelf(Shelf shelf) throws SQLException;   // Add new shelf when item is created
    Shelf getShelfByProductCode(String productCode) throws SQLException; // Get shelf by product code
    void updateShelf(Shelf shelf) throws SQLException; // Update shelf (e.g., current quantity)
    void deleteShelf(String productCode) throws SQLException; // Delete shelf by product code
}
