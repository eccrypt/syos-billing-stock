package core.services;

import core.models.Item;
import core.models.StockEntry;
import core.repositories.StockEntryRepository;
import core.dao.StockEntryDAO;
import core.strategy.stock.StockAllocator;
import core.strategy.stock.strategy.ExpiryAwareStockSelectionStrategy;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StockService {
    private final StockEntryRepository stockEntryRepository;
    private final ItemService itemService;

    public StockService(Connection conn, ItemService itemService) {
        this.stockEntryRepository = new StockEntryDAO(conn); // DAO implements the repository interface
        this.itemService = itemService;
    }

    public List<StockEntry> getAllStockEntries() throws SQLException {
        return stockEntryRepository.findAll();
    }

    public void addStockEntry(String itemCode, int quantity, String entryDateStr, String expiryDateStr)
            throws SQLException, ParseException {

        Item item = itemService.getItemByCode(itemCode);
        if (item == null) {
            throw new IllegalArgumentException("Item with code '" + itemCode + "' does not exist.");
        }

        Date entryDate = parseDate(entryDateStr);
        Date expiryDate = parseDate(expiryDateStr);
        StockEntry entry = new StockEntry(itemCode, quantity, entryDate, expiryDate);
        stockEntryRepository.insert(entry);
    }

    public List<StockEntry> allocateStock(String itemCode, int quantity) throws SQLException {
        List<StockEntry> available = stockEntryRepository.findAvailableByItemCode(itemCode);
        StockAllocator allocator = new StockAllocator(new ExpiryAwareStockSelectionStrategy());
        List<StockEntry> allocated = allocator.allocate(available, quantity);

        for (StockEntry entry : allocated) {
            stockEntryRepository.reduceQuantity(entry, entry.getQuantity());
        }

        return allocated;
    }

    public void updateStockEntry(StockEntry entry) throws SQLException {
        stockEntryRepository.update(entry);
    }

    public void deleteStockEntry(StockEntry entry) throws SQLException {
        stockEntryRepository.delete(entry);
    }

    private Date parseDate(String input) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(input);
    }

    public boolean itemExists(String itemCode) {
        try {
            return itemService.getItemByCode(itemCode) != null;
        } catch (SQLException e) {
            return false;
        }
    }
}
