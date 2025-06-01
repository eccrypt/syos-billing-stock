package core.facade;

import core.models.Item;
import core.models.StockEntry;
import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.services.StockService;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class StockFacade {

    private final ItemService itemService;
    private final StockService stockService;
    private final ReorderNotifier reorderNotifier;

    public StockFacade(ItemService itemService, StockService stockService, ReorderNotifier reorderNotifier) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.reorderNotifier = reorderNotifier;

        // Register the notifier as an observer
        this.stockService.registerObserver(reorderNotifier);
    }

    /**
     * Stock an item with a new stock entry.
     */
    public void stockItem(String itemCode, int quantity, String entryDate, String expiryDate) {
        try {
            if (!stockService.itemExists(itemCode)) {
                System.out.println("❌ Item with code '" + itemCode + "' does not exist.");
                return;
            }

            stockService.addStockEntry(itemCode, quantity, entryDate, expiryDate);
            System.out.println("✅ Stock entry added successfully.");
        } catch (SQLException | ParseException e) {
            System.out.println("❌ Failed to stock item: " + e.getMessage());
        }
    }

    /**
     * Allocate stock based on expiry date priority (e.g. FIFO by expiry).
     */
    public void allocateStock(String itemCode, int quantity) {
        try {
            List<StockEntry> allocated = stockService.allocateStock(itemCode, quantity);

            if (allocated.isEmpty()) {
                System.out.println("⚠️ No stock allocated (insufficient or expired).");
            } else {
                System.out.printf("✅ Allocated %d units from %d entries.\n", quantity, allocated.size());
            }

        } catch (SQLException e) {
            System.out.println("❌ Error allocating stock: " + e.getMessage());
        }
    }

    /**
     * Print the current total quantity available for an item.
     */
    public void printStockLevel(String itemCode) {
        try {
            int quantity = stockService.getTotalStockForItem(itemCode);
            System.out.printf("📦 Available stock for [%s]: %d units\n", itemCode, quantity);
        } catch (SQLException e) {
            System.out.println("❌ Failed to retrieve stock level: " + e.getMessage());
        }
    }

    /**
     * Print all stock entries (for report/debugging).
     */
    public void printAllStockEntries() {
        try {
            List<StockEntry> entries = stockService.getAllStockEntries();
            System.out.println("\n📋 All Stock Entries:");
            for (StockEntry entry : entries) {
                System.out.printf("Item: %s | Qty: %d | Entry: %s | Expiry: %s\n",
                        entry.getItemCode(), entry.getQuantity(), entry.getEntryDate(), entry.getExpiryDate());
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to list stock entries: " + e.getMessage());
        }
    }

    /**
     * Print reorder alerts for all items that fall below the threshold.
     */
    public void checkAndPrintReorderAlerts() {
        try {
            for (Item item : itemService.getAllItems()) {
                int stockQty = stockService.getTotalStockForItem(item.getCode());
                reorderNotifier.update(item.getCode(), stockQty);
            }

            Map<String, Integer> reorderItems = reorderNotifier.getReorderItems();
            if (reorderItems.isEmpty()) {
                System.out.println("✅ No items need reordering.");
            } else {
                System.out.println("\n🔔 Items to reorder:");
                for (Map.Entry<String, Integer> entry : reorderItems.entrySet()) {
                    Item item = itemService.getItemByCode(entry.getKey());
                    System.out.printf("⚠️ [%s - %s]: %d units left\n",
                            item.getCode(), item.getName(), entry.getValue());
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Failed to check reorder items: " + e.getMessage());
        }
    }

    public void updateStockEntry(int entryId, int newQuantity, String newExpiryDate) {
        try {
            StockEntry existing = stockService.getAllStockEntries().stream()
                    .filter(e -> e.getId() == entryId)
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                System.out.println("⚠️ No stock entry found with ID " + entryId);
                return;
            }

            existing.setQuantity(newQuantity);
            existing.setExpiryDate(new SimpleDateFormat("yyyy-MM-dd").parse(newExpiryDate));

            stockService.updateStockEntry(existing);
            System.out.println("✅ Stock entry updated successfully.");

        } catch (SQLException | ParseException e) {
            System.out.println("❌ Failed to update stock entry: " + e.getMessage());
        }
    }


    public void deleteStockEntry(int entryId) {
        try {
            StockEntry existing = stockService.getAllStockEntries().stream()
                    .filter(e -> e.getId() == entryId)
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                System.out.println("⚠️ No stock entry found with ID " + entryId);
                return;
            }

            stockService.deleteStockEntry(existing);
            System.out.println("✅ Stock entry deleted successfully.");

        } catch (SQLException e) {
            System.out.println("❌ Failed to delete stock entry: " + e.getMessage());
        }
    }

}
