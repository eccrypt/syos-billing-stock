package core.services;

import core.models.Item;
import core.models.StockEntry;
import core.models.Shelf;
import core.repositories.StockEntryRepository;
import core.dao.StockEntryDAO;
import core.strategy.stock.StockAllocator;
import core.strategy.stock.ExpiryAwareStockSelectionStrategy;
import core.observer.StockObserver;
import core.observer.StockSubject;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StockService implements StockSubject {

    private final StockEntryRepository stockEntryRepository;
    private final ItemService itemService;
    private final ShelfService shelfService;  // Add ShelfService

    private final List<StockObserver> observers = new ArrayList<>();
    private final Map<String, Integer> stockLevels = new HashMap<>();

    public StockService(Connection conn, ItemService itemService, ShelfService shelfService) {
        this.stockEntryRepository = new StockEntryDAO(conn);
        this.itemService = itemService;
        this.shelfService = shelfService;  // Initialize ShelfService
    }

    // Add the method to expose ShelfService
    public ShelfService getShelfService() {
        return shelfService;
    }

    @Override
    public void registerObserver(StockObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String itemCode, int newQuantity) {
        for (StockObserver observer : observers) {
            observer.update(itemCode, newQuantity);
        }
    }

    public List<StockEntry> getAllStockEntries() throws SQLException {
        return stockEntryRepository.findAll();
    }

    // Fetch the stock entry by itemCode
    public StockEntry getStockEntryByItemCode(String itemCode) throws SQLException {
        return ((StockEntryDAO) stockEntryRepository).getStockEntryByItemCode(itemCode);
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

        // Update and notify observers
        int newQuantity = getTotalStockForItem(itemCode);
        notifyObservers(itemCode, newQuantity);
    }

    public List<StockEntry> allocateStock(String itemCode, int quantity) throws SQLException {
        List<StockEntry> available = stockEntryRepository.findAvailableByItemCode(itemCode);
        StockAllocator allocator = new StockAllocator(new ExpiryAwareStockSelectionStrategy());
        List<StockEntry> allocated = allocator.allocate(available, quantity);

        for (StockEntry entry : allocated) {
            stockEntryRepository.reduceQuantity(entry, entry.getQuantity());
        }

        // Update and notify observers
        int newQuantity = getTotalStockForItem(itemCode);
        notifyObservers(itemCode, newQuantity);

        return allocated;
    }

    public void updateStockEntry(StockEntry entry) throws SQLException {
        stockEntryRepository.update(entry);

        // Update and notify observers
        int newQuantity = getTotalStockForItem(entry.getItemCode());
        notifyObservers(entry.getItemCode(), newQuantity);
    }

    public void deleteStockEntry(StockEntry entry) throws SQLException {
        stockEntryRepository.delete(entry);

        // Update and notify observers
        int newQuantity = getTotalStockForItem(entry.getItemCode());
        notifyObservers(entry.getItemCode(), newQuantity);
    }

    public int getTotalStockForItem(String itemCode) throws SQLException {
        List<StockEntry> entries = stockEntryRepository.findAvailableByItemCode(itemCode);
        return entries.stream().mapToInt(StockEntry::getQuantity).sum();
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

