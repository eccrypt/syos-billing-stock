package core.repositories;

import core.models.StockEntry;

import java.sql.SQLException;
import java.util.List;

public interface StockEntryRepository {
    void insert(StockEntry entry) throws SQLException;
    void update(StockEntry entry) throws SQLException;
    void delete(StockEntry entry) throws SQLException;
    List<StockEntry> findAll() throws SQLException;
    List<StockEntry> findAvailableByItemCode(String itemCode) throws SQLException;
    void reduceQuantity(StockEntry entry, int reduceQty) throws SQLException;
}
