package tests.core.report;

import core.models.Item;
import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.report.ReorderReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ReorderReportTest {

    private ReorderReport reorderReport;
    private ReorderNotifier reorderNotifier;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = mock(ItemService.class);
        reorderNotifier = mock(ReorderNotifier.class);
        reorderReport = new ReorderReport(reorderNotifier, itemService);
    }

    @Test
    void testGenerate_ReorderReport() throws Exception {
        // Mock low stock items
        Map<String, Integer> lowStockItems = new HashMap<>();
        lowStockItems.put("ITEM1", 20);
        lowStockItems.put("ITEM2", 30);

        // Mock item details
        Item item1 = new Item("ITEM1", "Test Item 1", 100);
        Item item2 = new Item("ITEM2", "Test Item 2", 200);
        when(itemService.getItemByCode("ITEM1")).thenReturn(item1);
        when(itemService.getItemByCode("ITEM2")).thenReturn(item2);

        // Mock the reorderNotifier to return low stock items
        when(reorderNotifier.getReorderItems()).thenReturn(lowStockItems);

        // Run the report
        reorderReport.generate();

        // Verify interaction with the reorder notifier and item service
        verify(reorderNotifier, times(1)).getReorderItems();
        verify(itemService, times(2)).getItemByCode(anyString());
    }

    @Test
    void testGenerate_NoLowStockItems() {
        // Mock the reorderNotifier to return no low stock items
        when(reorderNotifier.getReorderItems()).thenReturn(new HashMap<>());

        // Run the report
        reorderReport.generate();

        // Verify interaction with the reorder notifier
        verify(reorderNotifier, times(1)).getReorderItems();
    }

    @Test
    void testGenerate_ItemNotFound() throws Exception {
        // Mock low stock items
        Map<String, Integer> lowStockItems = new HashMap<>();
        lowStockItems.put("ITEM1", 20);

        // Mock the reorderNotifier to return low stock items
        when(reorderNotifier.getReorderItems()).thenReturn(lowStockItems);

        // Simulate item not found for ITEM1
        when(itemService.getItemByCode("ITEM1")).thenThrow(new Exception("Item not found"));

        // Run the report
        reorderReport.generate();

        // Verify interaction with the reorder notifier and item service
        verify(reorderNotifier, times(1)).getReorderItems();
        verify(itemService, times(1)).getItemByCode("ITEM1");
    }
}
