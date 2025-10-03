package tests.core.strategy.stock;

import core.models.StockEntry;
import core.strategy.stock.ExpiryAwareStockSelectionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExpiryAwareStockSelectionStrategyTest {

    private ExpiryAwareStockSelectionStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ExpiryAwareStockSelectionStrategy();
    }

    // 1. Test sorting by expiry date, then by entry date
//    @Test
//    void testSelectStock_SortsByExpiryAndEntry() {
//        StockEntry entry1 = new StockEntry("ITEM1", 5, Date.from(LocalDate.of(2025, 5, 20).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(LocalDate.of(2025, 6, 10).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));  // Expiry 2025-06-10
//        StockEntry entry2 = new StockEntry("ITEM1", 10, Date.from(LocalDate.of(2025, 5, 10).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(LocalDate.of(2025, 6, 12).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())); // Expiry 2025-06-12
//        StockEntry entry3 = new StockEntry("ITEM1", 3, Date.from(LocalDate.of(2025, 5, 5).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(LocalDate.of(2025, 6, 5).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));   // Expiry 2025-06-05
//
//        List<StockEntry> availableEntries = Arrays.asList(entry1, entry2, entry3);
//
//        // Call method to be tested
//        List<StockEntry> result = strategy.selectStock(availableEntries, 10);
//
//        // Debugging: Print selected stock
//        System.out.println("Selected Stock Entries:");
//        for (StockEntry entry : result) {
//            System.out.println("ItemCode: " + entry.getItemCode() + ", Quantity: " + entry.getQuantity());
//        }
//
//        // Validate results
//        assertEquals(2, result.size());  // We expect two entries: entry3 (3) and entry2 (7)
//        assertStockEntryEquals(entry3, result.get(0));  // entry3 should be selected first (lowest expiry)
//        assertStockEntryEquals(entry2, result.get(1));  // entry2 should be selected next (after entry3)
//    }


    // 2. Test selecting stock for a requested quantity
    @Test
    void testSelectStock_SelectsCorrectQuantity() {
        LocalDate entryDate1 = LocalDate.of(2025, 5, 5);
        LocalDate expiryDate1 = LocalDate.of(2025, 6, 5);

        LocalDate entryDate2 = LocalDate.of(2025, 5, 15);
        LocalDate expiryDate2 = LocalDate.of(2025, 6, 10);

        StockEntry entry1 = new StockEntry("ITEM1", 10, Date.from(entryDate1.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()), Date.from(expiryDate1.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        StockEntry entry2 = new StockEntry("ITEM1", 15, Date.from(entryDate2.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()), Date.from(expiryDate2.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        List<StockEntry> availableEntries = Arrays.asList(entry1, entry2);

        // Call method to be tested
        List<StockEntry> result = strategy.selectStock(availableEntries, 20);

        // Validate results
        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getQuantity());
        assertEquals(10, result.get(1).getQuantity());
    }

    // 3. Test selecting stock with insufficient quantity
    @Test
    void testSelectStock_InsufficientStock() {
        LocalDate entryDate = LocalDate.of(2025, 5, 5);
        LocalDate expiryDate = LocalDate.of(2025, 6, 5);

        StockEntry entry1 = new StockEntry("ITEM1", 5, Date.from(entryDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()), Date.from(expiryDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        List<StockEntry> availableEntries = Arrays.asList(entry1);

        // Call method to be tested
        List<StockEntry> result = strategy.selectStock(availableEntries, 10);

        // Validate results
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getQuantity());  // Only 5 units should be selected
    }

    // 4. Test selecting stock with exact quantity
    @Test
    void testSelectStock_ExactQuantity() {
        LocalDate entryDate = LocalDate.of(2025, 5, 5);
        LocalDate expiryDate = LocalDate.of(2025, 6, 5);

        StockEntry entry1 = new StockEntry("ITEM1", 10, Date.from(entryDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()), Date.from(expiryDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        List<StockEntry> availableEntries = Arrays.asList(entry1);

        // Call method to be tested
        List<StockEntry> result = strategy.selectStock(availableEntries, 10);

        // Validate results
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getQuantity());
    }

    // 5. Test selecting stock with zero quantity requested
    @Test
    void testSelectStock_ZeroQuantity() {
        LocalDate entryDate = LocalDate.of(2025, 5, 5);
        LocalDate expiryDate = LocalDate.of(2025, 6, 5);

        StockEntry entry1 = new StockEntry("ITEM1", 10, Date.from(entryDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()), Date.from(expiryDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        List<StockEntry> availableEntries = Arrays.asList(entry1);

        // Call method to be tested
        List<StockEntry> result = strategy.selectStock(availableEntries, 0);

        // Validate results
        assertTrue(result.isEmpty());  // Ensure that an empty list is returned for zero quantity request
    }

    // 6. Test selecting stock when no available entries
    @Test
    void testSelectStock_NoEntries() {
        List<StockEntry> availableEntries = Collections.emptyList();

        // Call method to be tested
        List<StockEntry> result = strategy.selectStock(availableEntries, 10);

        // Validate results
        assertTrue(result.isEmpty());
    }

    // Helper method to compare two StockEntry objects based on their fields
    private void assertStockEntryEquals(StockEntry expected, StockEntry actual) {
        assertEquals(expected.getItemCode(), actual.getItemCode());
        assertEquals(expected.getQuantity(), actual.getQuantity());
        assertEquals(expected.getEntryDate(), actual.getEntryDate());
        assertEquals(expected.getExpiryDate(), actual.getExpiryDate());
    }
}
