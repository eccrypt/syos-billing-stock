package tests.core.report;

import core.models.StockEntry;
import core.services.StockService;
import core.report.StockReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

public class StockReportTest {

    private StockReport stockReport;
    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = mock(StockService.class);
        stockReport = new StockReport(stockService);
    }

    @Test
    void testGenerate_StockReport() throws SQLException {
        // Convert LocalDate to Date
        Date entryDate1 = Date.from(LocalDate.of(2025, 5, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expiryDate1 = Date.from(LocalDate.of(2025, 6, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Date entryDate2 = Date.from(LocalDate.of(2025, 5, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expiryDate2 = Date.from(LocalDate.of(2025, 7, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mocking the StockService to return a list of StockEntries
        StockEntry entry1 = new StockEntry("ITEM1", 100, entryDate1, expiryDate1);
        StockEntry entry2 = new StockEntry("ITEM2", 50, entryDate2, expiryDate2);
        List<StockEntry> stockEntries = Arrays.asList(entry1, entry2);

        when(stockService.getAllStockEntries()).thenReturn(stockEntries);

        // Running the report generation
        stockReport.generate();

        // Verify that the stock entries were fetched correctly
        verify(stockService, times(1)).getAllStockEntries();
    }

    @Test
    void testGenerate_NoStockEntries() throws SQLException {
        // Mocking the StockService to return no entries
        when(stockService.getAllStockEntries()).thenReturn(Arrays.asList());

        // Running the report generation
        stockReport.generate();

        // Verify method call and handling
        verify(stockService, times(1)).getAllStockEntries();
    }

    @Test
    void testGenerate_ErrorFetchingStock() throws SQLException {
        // Simulate error in StockService
        when(stockService.getAllStockEntries()).thenThrow(new RuntimeException("Database error"));

        // Running the report generation
        stockReport.generate();

        // Verify that error is handled and printed
        verify(stockService, times(1)).getAllStockEntries();
    }
}
