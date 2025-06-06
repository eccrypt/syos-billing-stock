package tests.cli;

import cli.ReportCLI;
import core.command.GenerateReportCommand;
import core.command.ReportInvoker;
import core.dao.BillDAO;
import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.User;
import core.observer.ReorderNotifier;
import core.report.*;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

import static org.mockito.Mockito.*;

public class ReportCLITest {

    @Mock
    private Connection connection;

    @Mock
    private ShelfService shelfService;

    @Mock
    private ItemService itemService;

    @Mock
    private StockService stockService;

    @Mock
    private BillDAO billDAO;

    @Mock
    private ReorderNotifier reorderNotifier;

    @Mock
    private Scanner scanner;

    private ReportCLI reportCLI;

    @Captor
    private ArgumentCaptor<ReportTemplate> reportTemplateCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        reportCLI = new ReportCLI(scanner, connection);
    }

    @Test
    public void testGenerateReorderReport() throws SQLException {
        // Arrange
        when(scanner.nextLine()).thenReturn("1"); // User selects "1" for Reorder Report
        when(scanner.nextLine()).thenReturn("0"); // User selects "0" to exit
        when(itemService.getAllItems()).thenReturn(null); // Mocking service methods

        // Act
        reportCLI.showMenu(mock(User.class)); // Simulate user interaction

        // Verify
        verify(stockService, times(1)).registerObserver(reorderNotifier);
        verify(reorderNotifier, times(1)).update(anyString(), anyInt());  // ReorderNotifier's update method should be called
        verify(reorderNotifier, times(1)).getReorderItems();
    }

    @Test
    public void testGenerateDailySalesReport() throws SQLException {
        // Arrange
        when(scanner.nextLine()).thenReturn("2"); // User selects "2" for Daily Sales Report
        when(scanner.nextLine()).thenReturn("2025-05-10"); // Date input for report
        when(billDAO.getBillsByDate(LocalDate.of(2025, 5, 10))).thenReturn(null); // Mock empty bills list

        // Act
        reportCLI.showMenu(mock(User.class)); // Simulate user interaction

        // Verify
        verify(billDAO, times(1)).getBillsByDate(LocalDate.of(2025, 5, 10));
        verifyNoMoreInteractions(billDAO);
    }

    @Test
    public void testGenerateStockReport() throws SQLException {
        // Arrange
        when(scanner.nextLine()).thenReturn("3"); // User selects "3" for Stock Report
        when(scanner.nextLine()).thenReturn("0"); // User selects "0" to exit

        // Act
        reportCLI.showMenu(mock(User.class)); // Simulate user interaction

        // Verify
        verify(stockService, times(1)).registerObserver(any());  // Verify that StockService registers observers
        verify(stockService, times(1)).getAllStockEntries();
    }

    @Test
    public void testGenerateBillReport() throws SQLException {
        // Arrange
        when(scanner.nextLine()).thenReturn("4") // User selects "4" for Bill Report
                .thenReturn("2025-06-06"); // Date input for report
        when(billDAO.getBillsByDate(LocalDate.of(2025, 6, 6))).thenReturn(null); // Mock empty bills list

        // Act
        // Simulate user interaction for selecting the menu option and entering the date.
        reportCLI.showMenu(mock(User.class));

        // Verify
        verify(billDAO, times(1)).getBillsByDate(LocalDate.of(2025, 6, 6));  // Verify the DAO method is called
        verifyNoMoreInteractions(billDAO);  // Ensure no more interactions with billDAO
    }


    @Test
    public void testGenerateAllReports() throws SQLException {
        // Arrange
        when(scanner.nextLine()).thenReturn("5"); // User selects "5" for Generate All Reports
        when(scanner.nextLine()).thenReturn("2025-05-10"); // Date input for all reports
        when(itemService.getAllItems()).thenReturn(null); // Mock item service
        when(billDAO.getBillsByDate(LocalDate.of(2025, 5, 10))).thenReturn(null); // Mock bill DAO

        // Act
        reportCLI.showMenu(mock(User.class)); // Simulate user interaction

        // Verify that each individual report generation method is called
        verify(reorderNotifier, times(1)).getReorderItems();
        verify(billDAO, times(1)).getBillsByDate(LocalDate.of(2025, 5, 10));
        verify(stockService, times(1)).getAllStockEntries();
    }

    @Test
    public void testBackOption() {
        // Arrange
        when(scanner.nextLine()).thenReturn("0"); // User selects "0" to go back

        // Act
        reportCLI.showMenu(mock(User.class)); // Simulate user interaction

        // Verify that no report generation method is called
        verifyNoMoreInteractions(stockService, billDAO, reorderNotifier);
    }
}
