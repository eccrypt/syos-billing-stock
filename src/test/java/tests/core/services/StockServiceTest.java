package tests.core.services;

import core.models.Item;
import core.models.StockEntry;
import core.repositories.StockEntryRepository;
import core.services.StockService;
import core.services.ItemService;
import core.observer.StockObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockServiceTest {

    @Mock private StockEntryRepository stockEntryRepository;
    @Mock private ItemService itemService;
    @Mock private StockObserver stockObserver;

    private StockService stockService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create StockService with dummy connection and real itemService mock
        stockService = new StockService(null, itemService);

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

    // 4. Notify observers with correct params
    @Test
    void testNotifyObserversWithCorrectParams() {
        stockService.notifyObservers("ITEMX", 12);
        verify(stockObserver).update("ITEMX", 12);
    }

    // 5. getAllStockEntries returns expected list
    @Test
    void testGetAllStockEntriesReturnsList() throws SQLException {
        List<StockEntry> entries = List.of(new StockEntry("ITEM1", 5, new Date(), new Date()));
        when(stockEntryRepository.findAll()).thenReturn(entries);

        List<StockEntry> result = stockService.getAllStockEntries();
        assertEquals(1, result.size());
        assertEquals("ITEM1", result.get(0).getItemCode());
    }

    // 6. getAllStockEntries throws SQLException
    @Test
    void testGetAllStockEntriesThrowsSQLException() throws SQLException {
        when(stockEntryRepository.findAll()).thenThrow(new SQLException("DB error"));
        assertThrows(SQLException.class, () -> stockService.getAllStockEntries());
    }

    // 7. addStockEntry success case with observer notified
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

    // 8. addStockEntry throws IllegalArgumentException if item not found
    @Test
    void testAddStockEntryItemNotFoundThrows() throws SQLException {
        when(itemService.getItemByCode("UNKNOWN")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
                stockService.addStockEntry("UNKNOWN", 10, "2025-01-01", "2025-12-31")
        );
    }

    // 9. addStockEntry throws ParseException on bad entryDate
    @Test
    void testAddStockEntryBadEntryDateThrows() throws SQLException {
        when(itemService.getItemByCode("ITEM1")).thenReturn(new Item("ITEM1", "Name", 10));
        assertThrows(ParseException.class, () ->
                stockService.addStockEntry("ITEM1", 10, "bad-date", "2025-12-31")
        );
    }

    // 10. addStockEntry throws ParseException on bad expiryDate
    @Test
    void testAddStockEntryBadExpiryDateThrows() throws SQLException {
        when(itemService.getItemByCode("ITEM1")).thenReturn(new Item("ITEM1", "Name", 10));
        assertThrows(ParseException.class, () ->
                stockService.addStockEntry("ITEM1", 10, "2025-01-01", "bad-date")
        );
    }

    // 11. addStockEntry updates stock level after insertion
    @Test
    void testAddStockEntryUpdatesStockLevel() throws Exception {
        Item item = new Item("ITEM1", "Name", 10);
        when(itemService.getItemByCode("ITEM1")).thenReturn(item);
        doNothing().when(stockEntryRepository).insert(any());
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(List.of(new StockEntry("ITEM1", 15, new Date(), new Date())));

        stockService.addStockEntry("ITEM1", 15, "2025-01-01", "2025-12-31");
        verify(stockObserver).update(eq("ITEM1"), eq(15));
    }

    // 12. allocateStock success partially allocates
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

    // 13. allocateStock success full allocation
    @Test
    void testAllocateStockFullSuccess() throws SQLException {
        List<StockEntry> available = List.of(
                new StockEntry("ITEM1", 10, new Date(), new Date())
        );
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(available);
        doNothing().when(stockEntryRepository).reduceQuantity(any(), anyInt());

        List<StockEntry> allocated = stockService.allocateStock("ITEM1", 10);

        assertEquals(1, allocated.size());
        verify(stockEntryRepository).reduceQuantity(any(), eq(10));
        verify(stockObserver).update(eq("ITEM1"), anyInt());
    }

    // 14. allocateStock throws SQLException when repository fails
    @Test
    void testAllocateStockThrowsSQLException() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> stockService.allocateStock("ITEM1", 5));
    }

    // 15. allocateStock with zero quantity returns empty list
    @Test
    void testAllocateStockZeroQuantityReturnsEmpty() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(Collections.emptyList());
        List<StockEntry> allocated = stockService.allocateStock("ITEM1", 0);
        assertTrue(allocated.isEmpty());
        verify(stockObserver).update(eq("ITEM1"), eq(0));
    }

    // 16. allocateStock with quantity greater than available returns partial allocation
    @Test
    void testAllocateStockMoreThanAvailableReturnsPartial() throws SQLException {
        List<StockEntry> available = List.of(
                new StockEntry("ITEM1", 3, new Date(), new Date())
        );
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(available);
        doNothing().when(stockEntryRepository).reduceQuantity(any(), anyInt());

        List<StockEntry> allocated = stockService.allocateStock("ITEM1", 5);

        assertFalse(allocated.isEmpty());
        verify(stockObserver).update(eq("ITEM1"), anyInt());
    }

    // 17. updateStockEntry success and observer notified
    @Test
    void testUpdateStockEntrySuccess() throws SQLException {
        StockEntry entry = new StockEntry("ITEM1", 10, new Date(), new Date());
        doNothing().when(stockEntryRepository).update(entry);
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(List.of(entry));

        stockService.updateStockEntry(entry);

        verify(stockEntryRepository).update(entry);
        verify(stockObserver).update(eq("ITEM1"), eq(10));
    }

    // 18. updateStockEntry throws SQLException on failure
    @Test
    void testUpdateStockEntryThrowsSQLException() throws SQLException {
        StockEntry entry = new StockEntry("ITEM1", 10, new Date(), new Date());
        doThrow(new SQLException()).when(stockEntryRepository).update(entry);
        assertThrows(SQLException.class, () -> stockService.updateStockEntry(entry));
    }

    // 19. deleteStockEntry success and observer notified
    @Test
    void testDeleteStockEntrySuccess() throws SQLException {
        StockEntry entry = new StockEntry("ITEM1", 7, new Date(), new Date());
        doNothing().when(stockEntryRepository).delete(entry);
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(Collections.emptyList());

        stockService.deleteStockEntry(entry);

        verify(stockEntryRepository).delete(entry);
        verify(stockObserver).update(eq("ITEM1"), eq(0));
    }

    // 20. deleteStockEntry throws SQLException on failure
    @Test
    void testDeleteStockEntryThrowsSQLException() throws SQLException {
        StockEntry entry = new StockEntry("ITEM1", 7, new Date(), new Date());
        doThrow(new SQLException()).when(stockEntryRepository).delete(entry);
        assertThrows(SQLException.class, () -> stockService.deleteStockEntry(entry));
    }

    // 21. getTotalStockForItem returns sum correctly
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

    // 22. getTotalStockForItem returns 0 if no entries
    @Test
    void testGetTotalStockForItemReturnsZero() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(Collections.emptyList());
        assertEquals(0, stockService.getTotalStockForItem("ITEM1"));
    }

    // 23. getTotalStockForItem throws SQLException on repo failure
    @Test
    void testGetTotalStockForItemThrowsSQLException() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> stockService.getTotalStockForItem("ITEM1"));
    }

    // 24. parseDate parses valid date string correctly
    @Test
    void testParseDateValid() throws Exception {
        Method parseDate = StockService.class.getDeclaredMethod("parseDate", String.class);
        parseDate.setAccessible(true);
        Date date = (Date) parseDate.invoke(stockService, "2025-12-31");
        assertNotNull(date);
    }

    // 25. parseDate throws ParseException on invalid date string
    @Test
    void testParseDateInvalid() throws Exception {
        Method parseDate = StockService.class.getDeclaredMethod("parseDate", String.class);
        parseDate.setAccessible(true);
        assertThrows(Exception.class, () -> parseDate.invoke(stockService, "bad-date"));
    }

    // 26. itemExists returns true if itemService returns non-null
    @Test
    void testItemExistsTrue() throws SQLException {
        when(itemService.getItemByCode("ITEM1")).thenReturn(new Item("ITEM1", "Name", 10));
        assertTrue(stockService.itemExists("ITEM1"));
    }

    // 27. itemExists returns false if itemService returns null
    @Test
    void testItemExistsFalse() throws SQLException {
        when(itemService.getItemByCode("UNKNOWN")).thenReturn(null);
        assertFalse(stockService.itemExists("UNKNOWN"));
    }

    // 28. itemExists returns false if itemService throws SQLException
    @Test
    void testItemExistsSQLException() throws SQLException {
        when(itemService.getItemByCode("ERROR")).thenThrow(new SQLException());
        assertFalse(stockService.itemExists("ERROR"));
    }

    // 29. addStockEntry with zero quantity succeeds
    @Test
    void testAddStockEntryZeroQuantity() throws Exception {
        when(itemService.getItemByCode("ITEM1")).thenReturn(new Item("ITEM1", "Name", 10));
        doNothing().when(stockEntryRepository).insert(any());
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(List.of());

        stockService.addStockEntry("ITEM1", 0, "2025-01-01", "2025-12-31");
        verify(stockEntryRepository).insert(any());
    }

    // 30. addStockEntry with negative quantity (assumed allowed) succeeds
    @Test
    void testAddStockEntryNegativeQuantity() throws Exception {
        when(itemService.getItemByCode("ITEM1")).thenReturn(new Item("ITEM1", "Name", 10));
        doNothing().when(stockEntryRepository).insert(any());
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(List.of());

        stockService.addStockEntry("ITEM1", -5, "2025-01-01", "2025-12-31");
        verify(stockEntryRepository).insert(any());
    }

    // 31. allocateStock with zero quantity returns empty and notifies
    @Test
    void testAllocateStockZeroQuantity() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(Collections.emptyList());
        List<StockEntry> allocated = stockService.allocateStock("ITEM1", 0);
        assertTrue(allocated.isEmpty());
        verify(stockObserver).update("ITEM1", 0);
    }

    // 32. allocateStock with negative quantity returns empty or throws (assumed empty)
    @Test
    void testAllocateStockNegativeQuantity() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(Collections.emptyList());
        List<StockEntry> allocated = stockService.allocateStock("ITEM1", -3);
        assertTrue(allocated.isEmpty());
        verify(stockObserver).update("ITEM1", 0);
    }

    // 33. updateStockEntry with null entry throws NullPointerException
    @Test
    void testUpdateStockEntryNullThrows() {
        assertThrows(NullPointerException.class, () -> stockService.updateStockEntry(null));
    }

    // 34. deleteStockEntry with null entry throws NullPointerException
    @Test
    void testDeleteStockEntryNullThrows() {
        assertThrows(NullPointerException.class, () -> stockService.deleteStockEntry(null));
    }

    // 35. notifyObservers with empty observer list does not throw
    @Test
    void testNotifyObserversEmptyList() {
        stockService.removeObserver(stockObserver);
        assertDoesNotThrow(() -> stockService.notifyObservers("ITEM", 5));
    }

    // 36. allocateStock reduces quantity correctly on all allocated entries
    @Test
    void testAllocateStockReducesQuantity() throws SQLException {
        List<StockEntry> entries = List.of(new StockEntry("ITEM1", 5, new Date(), new Date()));
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(entries);
        doNothing().when(stockEntryRepository).reduceQuantity(any(), anyInt());

        stockService.allocateStock("ITEM1", 3);

        verify(stockEntryRepository).reduceQuantity(any(), anyInt());
    }

    // 37. updateStockEntry notifies correct new quantity
    @Test
    void testUpdateStockEntryNotifies() throws SQLException {
        StockEntry entry = new StockEntry("ITEM1", 4, new Date(), new Date());
        doNothing().when(stockEntryRepository).update(entry);
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(List.of(entry));

        stockService.updateStockEntry(entry);

        verify(stockObserver).update("ITEM1", 4);
    }

    // 38. deleteStockEntry notifies correct new quantity after deletion
    @Test
    void testDeleteStockEntryNotifies() throws SQLException {
        StockEntry entry = new StockEntry("ITEM1", 4, new Date(), new Date());
        doNothing().when(stockEntryRepository).delete(entry);
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(Collections.emptyList());

        stockService.deleteStockEntry(entry);

        verify(stockObserver).update("ITEM1", 0);
    }

    // 39. getTotalStockForItem sums zero quantity when entries empty
    @Test
    void testGetTotalStockForItemEmptyEntries() throws SQLException {
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(Collections.emptyList());
        int total = stockService.getTotalStockForItem("ITEM1");
        assertEquals(0, total);
    }

    // 40. getTotalStockForItem sums zero quantity when all entries zero
    @Test
    void testGetTotalStockForItemAllZero() throws SQLException {
        List<StockEntry> entries = List.of(
                new StockEntry("ITEM1", 0, new Date(), new Date()),
                new StockEntry("ITEM1", 0, new Date(), new Date())
        );
        when(stockEntryRepository.findAvailableByItemCode("ITEM1")).thenReturn(entries);

        int total = stockService.getTotalStockForItem("ITEM1");
        assertEquals(0, total);
    }
}
