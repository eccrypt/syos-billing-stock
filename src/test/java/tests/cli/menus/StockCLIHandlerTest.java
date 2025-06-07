package tests.cli.menus;

import cli.menus.StockCLIHandler;
import core.facade.StockFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

import static org.mockito.Mockito.*;

public class StockCLIHandlerTest {

    @Mock private StockFacade stockFacade;
    @Mock private Scanner mockScanner;

    private StockCLIHandler handler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Use the new constructor that accepts mock Scanner
        handler = new StockCLIHandler(stockFacade, mockScanner);
    }

    @Test
    public void testHandleAddStockEntry() {
        when(mockScanner.nextLine()).thenReturn("ITEM123", "10", "2025-12-31");
        handler.handleAddStockEntry();
        verify(stockFacade).stockItem(eq("ITEM123"), eq(10), anyString(), eq("2025-12-31"));
    }

    @Test
    public void testHandleAllocateStock() {
        when(mockScanner.nextLine()).thenReturn("ITEM456", "5");
        handler.handleAllocateStock();
        verify(stockFacade).allocateStock("ITEM456", 5);
    }

    @Test
    public void testHandleViewAllStockEntries() {
        handler.handleViewAllStockEntries();
        verify(stockFacade).printAllStockEntries();
    }

    @Test
    public void testHandleCheckReorderAlerts() {
        handler.handleCheckReorderAlerts();
        verify(stockFacade).checkAndPrintReorderAlerts();
    }

    @Test
    public void testHandleViewStockLevel() {
        when(mockScanner.nextLine()).thenReturn("ITEM789");
        handler.handleViewStockLevel();
        verify(stockFacade).printStockLevel("ITEM789");
    }

    @Test
    public void testHandleUpdateStockEntry() throws SQLException, ParseException {
        when(mockScanner.nextLine()).thenReturn("ITEM001", "15", "2025-10-01");
        handler.handleUpdateStockEntry();
        verify(stockFacade).updateStockEntry("ITEM001", 15, "2025-10-01");
    }

    @Test
    public void testHandleDeleteStockEntry() {
        when(mockScanner.nextLine()).thenReturn("101");
        handler.handleDeleteStockEntry();
        verify(stockFacade).deleteStockEntry(101);
    }
}
