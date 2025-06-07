package tests.cli;

import cli.ReportCLI;
import core.dao.BillDAO;
import core.models.User;
import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

import static org.mockito.Mockito.*;

public class ReportCLITest {

    @Mock private Connection connection;
    @Mock private Scanner scanner;
    @Mock private BillDAO billDAO;
    @Mock private StockService stockService;
    @Mock private ItemService itemService;
    @Mock private ShelfService shelfService;
    @Mock private ReorderNotifier reorderNotifier;

    private ReportCLI reportCLI;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reportCLI = new ReportCLI(scanner, connection,
                billDAO, stockService, itemService, shelfService, reorderNotifier);
    }

    @Test
    public void testGenerateReorderReport() {
        when(scanner.nextLine()).thenReturn("1");

        reportCLI.showMenu(mock(User.class));

        verify(reorderNotifier, times(1)).getReorderItems();
    }

    @Test
    public void testGenerateDailySalesReport() throws SQLException {
        when(scanner.nextLine()).thenReturn("2", "2025-06-06");

        reportCLI.showMenu(mock(User.class));

        verify(billDAO, times(1)).getBillsByDate(LocalDate.of(2025, 6, 6));
    }

    @Test
    public void testGenerateStockReport() throws SQLException {
        when(scanner.nextLine()).thenReturn("3");

        reportCLI.showMenu(mock(User.class));

        verify(stockService, times(1)).getAllStockEntries();
    }

    @Test
    public void testGenerateBillReport() throws SQLException {
        when(scanner.nextLine()).thenReturn("4", "2025-06-06");

        reportCLI.showMenu(mock(User.class));

        verify(billDAO, times(1)).getBillsByDate(LocalDate.of(2025, 6, 6));
    }

    @Test
    public void testGenerateAllReports() throws SQLException {
        when(scanner.nextLine()).thenReturn("5", "2025-06-06");

        reportCLI.showMenu(mock(User.class));

        verify(reorderNotifier, times(1)).getReorderItems();
        verify(billDAO, times(2)).getBillsByDate(LocalDate.of(2025, 6, 6)); // <-- fixed
        verify(stockService, times(1)).getAllStockEntries();
    }


    @Test
    public void testBackOption() {
        when(scanner.nextLine()).thenReturn("0");

        reportCLI.showMenu(mock(User.class));

        verifyNoInteractions(billDAO, stockService, itemService, reorderNotifier);
    }
}
