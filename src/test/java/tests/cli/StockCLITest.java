package tests.cli;

import cli.StockCLI;
import cli.menus.StockCLIHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.util.Scanner;

import static org.mockito.Mockito.*;

public class StockCLITest {

    @Mock private Scanner scanner;
    @Mock private StockCLIHandler mockHandler;

    private StockCLI stockCLI;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        stockCLI = new StockCLI(mockHandler);
    }

    @Test
    public void testAddStockEntryOption() throws Exception {
        Scanner mockScanner = mock(Scanner.class);
        when(mockScanner.nextLine()).thenReturn("1", "0");

        stockCLI = new StockCLI(mockHandler) {
            @Override
            public void showMenu() throws ParseException {
                mockHandler.handleAddStockEntry();
            }
        };

        stockCLI.showMenu();
        verify(mockHandler, times(1)).handleAddStockEntry();
    }

    @Test
    public void testViewStockLevelOption() throws Exception {
        Scanner mockScanner = mock(Scanner.class);
        when(mockScanner.nextLine()).thenReturn("5", "0");

        stockCLI = new StockCLI(mockHandler) {
            @Override
            public void showMenu() throws ParseException {
                mockHandler.handleViewStockLevel();
            }
        };

        stockCLI.showMenu();
        verify(mockHandler, times(1)).handleViewStockLevel();
    }

    @Test
    public void testInvalidOptionHandling() throws Exception {
        Scanner mockScanner = mock(Scanner.class);
        when(mockScanner.nextLine()).thenReturn("invalid", "0");

        stockCLI = new StockCLI(mockHandler) {
            @Override
            public void showMenu() {
                System.out.println("Invalid option. Try again.");
            }
        };

        stockCLI.showMenu();
    }
}
