package tests.cli.menus;

import cli.menus.ItemCLIHandler;
import core.facade.StockFacade;
import core.models.Item;
import core.services.ItemService;
import core.services.ShelfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Scanner;

import static org.mockito.Mockito.*;

public class ItemCLIHandlerTest {

    @Mock private ItemService itemService;
    @Mock private ShelfService shelfService;
    @Mock private StockFacade stockFacade;
    @Mock private Scanner mockScanner;

    private ItemCLIHandler handler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stockFacade.getShelfService()).thenReturn(shelfService);
        handler = new ItemCLIHandler(itemService, shelfService, stockFacade, mockScanner);
    }

    @Test
    public void testHandleViewAllItems() throws SQLException {
        handler.handleViewAllItems();
        verify(itemService).getAllItems();
    }

    @Test
    public void testHandleSearchItem() throws SQLException {
        when(mockScanner.nextLine()).thenReturn("ITEM123");
        Item mockItem = new Item("ITEM123", "Test Item", 100.0);
        when(itemService.getItemByCode("ITEM123")).thenReturn(mockItem);
        handler.handleSearchItem();
        verify(itemService).getItemByCode("ITEM123");
    }

    @Test
    public void testHandleUpdateItem() throws SQLException {
        when(mockScanner.nextLine()).thenReturn("ITEM123", "Updated Item", "150.0");
        handler.handleUpdateItem();
        verify(itemService).updateItem(any(Item.class));
    }

    @Test
    public void testHandleUpdateItemName() throws SQLException {
        when(mockScanner.nextLine()).thenReturn("ITEM123", "New Name");
        handler.handleUpdateItemName();
        verify(itemService).updateItemName("ITEM123", "New Name");
    }

    @Test
    public void testHandleUpdateItemPrice() throws SQLException {
        when(mockScanner.nextLine()).thenReturn("ITEM123", "200.0");
        handler.handleUpdateItemPrice();
        verify(itemService).updateItemPrice("ITEM123", 200.0);
    }

    @Test
    public void testHandleDeleteItem() throws SQLException {
        when(mockScanner.nextLine()).thenReturn("ITEM123");
        handler.handleDeleteItem();
        verify(itemService).deleteItem("ITEM123");
    }

    @Test
    public void testHandleAllocateStock() throws SQLException {
        when(mockScanner.nextLine()).thenReturn("ITEM123", "5");
        handler.handleAllocateStock();
        verify(stockFacade).allocateStock("ITEM123", 5);
    }

    @Test
    public void testHandleViewStockLevel() throws SQLException {
        when(mockScanner.nextLine()).thenReturn("ITEM123");
        handler.handleViewStockLevel();
        verify(stockFacade).printStockLevel("ITEM123");
    }

    @Test
    public void testHandleCheckReorderAlerts() throws SQLException {
        handler.handleCheckReorderAlerts();
        verify(stockFacade).checkAndPrintReorderAlerts();
    }
}
