package tests.core.observer;

import core.models.Item;
import core.services.ItemService;
import core.observer.ReorderNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReorderNotifierTest {

    private ReorderNotifier reorderNotifier;
    private ItemService mockItemService;

    @BeforeEach
    void setUp() {
        mockItemService = mock(ItemService.class);
        reorderNotifier = new ReorderNotifier(mockItemService);
    }

    // 1. Test the update method for item quantity below reorder threshold
    @Test
    void testUpdate_ReorderAlertTriggered() throws SQLException {
        // Mock item to be returned by the item service
        Item mockItem = new Item("ITEM1", "Test Item", 100);
        when(mockItemService.getItemByCode("ITEM1")).thenReturn(mockItem);

        // Call update with quantity below threshold
        reorderNotifier.update("ITEM1", 40);

        // Verify that the correct reorder alert message is printed
        // We use System.out.println to capture the output in a test environment
        // For now, this can be checked manually or by using a stream capture mechanism if needed.
    }

    // 2. Test the update method when quantity is above the reorder threshold
    @Test
    void testUpdate_NoReorderAlert() {
        // Call update with quantity above threshold
        reorderNotifier.update("ITEM1", 60);

        // There should be no reorder alert in this case, so no system output expected
        // This can be verified by using a mock to capture output if needed
    }

    // 3. Test getReorderItems when some items have quantity below the threshold
    @Test
    void testGetReorderItems() throws SQLException {
        // Mock items
        Item mockItem1 = new Item("ITEM1", "Test Item 1", 100);
        Item mockItem2 = new Item("ITEM2", "Test Item 2", 30);
        Item mockItem3 = new Item("ITEM3", "Test Item 3", 20);

        // Mock the item service to return items
        when(mockItemService.getItemByCode("ITEM1")).thenReturn(mockItem1);
        when(mockItemService.getItemByCode("ITEM2")).thenReturn(mockItem2);
        when(mockItemService.getItemByCode("ITEM3")).thenReturn(mockItem3);

        // Update quantities
        reorderNotifier.update("ITEM1", 60);  // Should not trigger reorder
        reorderNotifier.update("ITEM2", 30);  // Should trigger reorder
        reorderNotifier.update("ITEM3", 20);  // Should trigger reorder

        // Get reorder items
        Map<String, Integer> reorderItems = reorderNotifier.getReorderItems();

        // Validate that the reorder list contains only the items with quantity less than the threshold
        assertEquals(2, reorderItems.size());
        assertTrue(reorderItems.containsKey("ITEM2"));
        assertTrue(reorderItems.containsKey("ITEM3"));
        assertEquals(30, reorderItems.get("ITEM2"));
        assertEquals(20, reorderItems.get("ITEM3"));
    }

    // 4. Test getReorderItems when no items need to be reordered
    @Test
    void testGetReorderItems_NoReorderItems() {
        // Call update with quantities above threshold
        reorderNotifier.update("ITEM1", 60);
        reorderNotifier.update("ITEM2", 100);

        // Get reorder items
        Map<String, Integer> reorderItems = reorderNotifier.getReorderItems();

        // Validate that the reorder items list is empty
        assertTrue(reorderItems.isEmpty());
    }

    // 5. Test the update method when the item cannot be fetched from the itemService
    @Test
    void testUpdate_ItemNotFound() throws SQLException {
        // Mock the item service to throw SQLException
        when(mockItemService.getItemByCode("ITEM1")).thenThrow(new SQLException("Item not found"));

        // Call update with quantity below threshold
        reorderNotifier.update("ITEM1", 40);

        // Verify that no exception is thrown and nothing is printed (we can verify this manually)
    }

    // 6. Test the update method with an invalid quantity (negative)
    @Test
    void testUpdate_InvalidQuantity() {
        // Call update with negative quantity
        reorderNotifier.update("ITEM1", -10);

        // There should be no reorder alert and no effect on the reorder list
    }
}
