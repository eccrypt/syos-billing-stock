package tests.core.strategy.stock;

import core.models.StockEntry;
import core.strategy.stock.StockAllocator;
import core.strategy.stock.StockSelectionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockAllocatorTest {

    private StockAllocator stockAllocator;
    private StockSelectionStrategy mockStrategy;

    @BeforeEach
    void setUp() {
        // Mocking the StockSelectionStrategy
        mockStrategy = mock(StockSelectionStrategy.class);
        stockAllocator = new StockAllocator(mockStrategy);
    }

    @Test
    void testAllocate_ValidStockAllocation() {
        // Create mock StockEntries
        StockEntry entry1 = new StockEntry("ITEM1", 10, null, null);
        StockEntry entry2 = new StockEntry("ITEM2", 20, null, null);
        List<StockEntry> availableEntries = Arrays.asList(entry1, entry2);

        // Mock the behavior of selectStock
        when(mockStrategy.selectStock(availableEntries, 25)).thenReturn(Arrays.asList(entry1, entry2));

        // Call the allocate method
        List<StockEntry> allocatedStock = stockAllocator.allocate(availableEntries, 25);

        // Validate the result
        assertNotNull(allocatedStock);
        assertEquals(2, allocatedStock.size());  // 2 entries should be selected
        assertEquals(10, allocatedStock.get(0).getQuantity());  // First entry should have 10
        assertEquals(20, allocatedStock.get(1).getQuantity());  // Second entry should have 20

        // Verify if selectStock method of mockStrategy was called once
        verify(mockStrategy, times(1)).selectStock(availableEntries, 25);
    }

    @Test
    void testAllocate_ZeroQuantity() {
        // Create mock StockEntries
        StockEntry entry1 = new StockEntry("ITEM1", 10, null, null);
        StockEntry entry2 = new StockEntry("ITEM2", 20, null, null);
        List<StockEntry> availableEntries = Arrays.asList(entry1, entry2);

        // Mock the behavior of selectStock
        when(mockStrategy.selectStock(availableEntries, 0)).thenReturn(Arrays.asList());

        // Call the allocate method with 0 quantity needed
        List<StockEntry> allocatedStock = stockAllocator.allocate(availableEntries, 0);

        // Validate the result (should return an empty list)
        assertTrue(allocatedStock.isEmpty());

        // Verify if selectStock method of mockStrategy was called once
        verify(mockStrategy, times(1)).selectStock(availableEntries, 0);
    }

    @Test
    void testAllocate_InsufficientStock() {
        // Create mock StockEntries
        StockEntry entry1 = new StockEntry("ITEM1", 10, null, null);
        StockEntry entry2 = new StockEntry("ITEM2", 10, null, null);
        List<StockEntry> availableEntries = Arrays.asList(entry1, entry2);

        // Mock the behavior of selectStock
        when(mockStrategy.selectStock(availableEntries, 25)).thenReturn(Arrays.asList(entry1, entry2));

        // Call the allocate method with more quantity than available stock
        List<StockEntry> allocatedStock = stockAllocator.allocate(availableEntries, 25);

        // Validate the result (should return both entries, but not enough stock)
        assertNotNull(allocatedStock);
        assertEquals(2, allocatedStock.size());
        assertEquals(10, allocatedStock.get(0).getQuantity());
        assertEquals(10, allocatedStock.get(1).getQuantity());

        // Verify if selectStock method of mockStrategy was called once
        verify(mockStrategy, times(1)).selectStock(availableEntries, 25);
    }

    @Test
    void testAllocate_NoStockAvailable() {
        // Create an empty list of available entries
        List<StockEntry> availableEntries = Arrays.asList();

        // Mock the behavior of selectStock
        when(mockStrategy.selectStock(availableEntries, 10)).thenReturn(Arrays.asList());

        // Call the allocate method with the required quantity
        List<StockEntry> allocatedStock = stockAllocator.allocate(availableEntries, 10);

        // Validate the result (should return an empty list since no stock is available)
        assertTrue(allocatedStock.isEmpty());

        // Verify if selectStock method of mockStrategy was called once
        verify(mockStrategy, times(1)).selectStock(availableEntries, 10);
    }

    @Test
    void testAllocate_MockNotCalledForWrongQuantity() {
        // Create mock StockEntries
        StockEntry entry1 = new StockEntry("ITEM1", 10, null, null);
        List<StockEntry> availableEntries = Arrays.asList(entry1);

        // Call the allocate method with wrong quantity, which is 0
        stockAllocator.allocate(availableEntries, 0);

        // Verify selectStock was called with 0 quantity
        verify(mockStrategy, times(1)).selectStock(availableEntries, 0);
    }
}
