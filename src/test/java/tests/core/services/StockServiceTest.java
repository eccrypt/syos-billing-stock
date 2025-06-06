package tests.core.services;

import core.models.Item;
import core.models.StockEntry;
import core.repositories.StockEntryRepository;
import core.services.StockService;
import core.services.ItemService;
import core.services.ShelfService;
import core.observer.StockObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockServiceTest {

    @Mock private StockEntryRepository stockEntryRepository;
    @Mock private ItemService itemService;
    @Mock private ShelfService shelfService;  // Mock ShelfService for dependencies
    @Mock private StockObserver stockObserver;

    private StockService stockService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create StockService with mocked dependencies
        stockService = new StockService(null, itemService, shelfService);

        // Use reflection to inject the mock StockEntryRepository into private final field
        Field repoField = StockService.class.getDeclaredField("stockEntryRepository");
        repoField.setAccessible(true);
        repoField.set(stockService, stockEntryRepository);

        // Register mock observer
        stockService.registerObserver(stockObserver);
    }

    // 1. Register observer and verify update call
    @Test
    void testRegisterObserverReceivesUpdate() {
        StockObserver observer2 = mock(StockObserver.class);
        stockService.registerObserver(observer2);

        stockService.notifyObservers("CODE1", 5);

        verify(stockObserver).update("CODE1", 5);
        verify(observer2).update("CODE1", 5);
    }

    // 2. Remove observer and verify no update call
    @Test
    void testRemoveObserverStopsUpdates() {
        StockObserver observer2 = mock(StockObserver.class);
        stockService.registerObserver(observer2);
        stockService.removeObserver(observer2);

        stockService.notifyObservers("CODE1", 7);

        verify(stockObserver).update("CODE1", 7);
        verify(observer2, never()).update(anyString(), anyInt());
    }

    // 3. Register multiple observers and verify all notified
    @Test
    void testMultipleObserversReceiveUpdates() {
        StockObserver obs2 = mock(StockObserver.class);
        StockObserver obs3 = mock(StockObserver.class);
        stockService.registerObserver(obs2);
        stockService.registerObserver(obs3);

        stockService.notifyObservers("CODE2", 10);

        verify(stockObserver).update("CODE2", 10);
        verify(obs2).update("CODE2", 10);
        verify(obs3).update("CODE2", 10);
    }

    // 4. getAllStockEntries returns expected list
    @Test
    void testGetAllStockEntriesReturnsList() throws SQLException {
        List<StockEntry> entries = List.of(new StockEntry("ITEM1", 5, new Date(), new Date()));
        when(stockEntryRepository.findAll()).thenReturn(entries);

        List<StockEntry> result = stockService.getAllStockEntries();
        assertEquals(1, result.size());
        assertEquals("ITEM1", result.get(0).getItemCode());
    }

    // 5. getAllStockEntries throws SQLException
    @Test
    void testGetAllStockEntriesThrowsSQLException() throws SQLException {
        when(stockEntryRepository.findAll()).thenThrow(new SQLException("DB error"));
        assertThrows(SQLException.class, () -> stockService.getAllStockEntries());
    }

    // 6. addStockEntry success case with observer notified
    @Test
    void testAddStockEntry_success() throws Exception {
        Item item = new Item("ITEM1", "Name", 10.0);
        when(itemService.getItemByCode("ITEM1")).thenReturn(item);
        doNothing().when(stockEntryRepository).insert(any());
        when(stockEntryRepository.findAvailableByItemCode("ITEM1"))
                .thenReturn(List.of(new StockEntry("ITEM1", 10, new Date(), new Date())));

        stockService.addStockEntry("ITEM1", 10, "2025-01-01", "2025-12-31");

        verify(stockEntryRepository).insert(any());
        verify(stockObserver).update(eq("ITEM1"), anyInt());
    }

    // 7. addStockEntry throws IllegalArgumentException if item not found
    @Test
    void testAddStockEntryItemNotFoundThrows() throws SQLException {
        when(itemService.getItemByCode("UNKNOWN")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> stockService.addStockEntry("UNKNOWN", 10, "2025-01-01", "2025-12-31"));
    }

    // 8. addStockEntry throws ParseException on bad entryDate
    @Test
    void testAddStockEntryBadEntryDateThrows() throws SQLException {
        when(itemService.getItemByCode("ITEM1")).thenReturn(new Item("ITEM1", "Name", 10));
        assertThrows(ParseException.class, () -> stockService.addStockEntry("ITEM1", 10, "bad-date", "2025-12-31"));
    }

    // 9. addStockEntry throws ParseException on bad expiryDate
    @Test
    void testAddStockEntryBadExpiryDateThrows() throws SQLException {
        when(itemService.getItemByCode("ITEM1")).thenReturn(new Item("ITEM1", "Name", 10));
        assertThrows(ParseException.class, () -> stockService.addStockEntry("ITEM1", 10, "2025-01-01", "bad-date"));
    }

    // 10. addStockEntry updates stock level after insertion
    @Test
    void testAddStockEntryUpdatesStockLevel() throws Exception {
        Item item = new Item("ITEM1", "Name", 10);
        when(itemService.getItemByCode("ITEM1")).thenReturn(item);
        doNothing().when(stockEntryRepository).insert(any());
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(List.of(new StockEntry("ITEM1", 15, new Date(), new Date())));

        stockService.addStockEntry("ITEM1", 15, "2025-01-01", "2025-12-31");
        verify(stockObserver).update(eq("ITEM1"), eq(15));
    }

    // 11. allocateStock success partially allocates
    @Test
    void testAllocateStockPartialSuccess() throws SQLException {
        List<StockEntry> available = List.of(
                new StockEntry("ITEM1", 5, new Date(), new Date()),
                new StockEntry("ITEM1", 5, new Date(), new Date())
        );
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(available);
        doNothing().when(stockEntryRepository).reduceQuantity(any(), anyInt());
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(available);

        List<StockEntry> allocated = stockService.allocateStock("ITEM1", 8);

        assertNotNull(allocated);
        assertTrue(allocated.size() > 0);
        verify(stockEntryRepository, atLeastOnce()).reduceQuantity(any(), anyInt());
        verify(stockObserver).update(eq("ITEM1"), anyInt());
    }

    // 12. allocateStock throws SQLException when repository fails
    @Test
    void testAllocateStockThrowsSQLException() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> stockService.allocateStock("ITEM1", 5));
    }

    // 13. getTotalStockForItem returns sum correctly
    @Test
    void testGetTotalStockForItemReturnsSum() throws SQLException {
        List<StockEntry> entries = List.of(
                new StockEntry("ITEM1", 3, new Date(), new Date()),
                new StockEntry("ITEM1", 7, new Date(), new Date())
        );
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(entries);

        int total = stockService.getTotalStockForItem("ITEM1");
        assertEquals(10, total);
    }

    // 14. getTotalStockForItem returns 0 if no entries
    @Test
    void testGetTotalStockForItemReturnsZero() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(Collections.emptyList());
        assertEquals(0, stockService.getTotalStockForItem("ITEM1"));
    }

    // 15. itemExists returns true if itemService returns non-null
    @Test
    void testItemExistsTrue() throws SQLException {
        when(itemService.getItemByCode("ITEM1")).thenReturn(new Item("ITEM1", "Name", 10));
        assertTrue(stockService.itemExists("ITEM1"));
    }

    // 16. itemExists returns false if itemService returns null
    @Test
    void testItemExistsFalse() throws SQLException {
        when(itemService.getItemByCode("UNKNOWN")).thenReturn(null);
        assertFalse(stockService.itemExists("UNKNOWN"));
    }
}
