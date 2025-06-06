package tests.core.dao;

import core.dao.StockEntryDAO;
import core.models.StockEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class StockEntryDAOTest {

    @Mock
    private Connection conn;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    private StockEntryDAO stockEntryDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        stockEntryDAO = new StockEntryDAO(conn);
    }

    @Test
    void testInsert() throws SQLException {
        // Convert LocalDate to java.sql.Date
        LocalDate localDate = LocalDate.of(2025, 5, 10);
        Date entryDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expiryDate = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mock the PreparedStatement behavior
        StockEntry stockEntry = new StockEntry("ITEM-001", 100, entryDate, expiryDate);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);  // Simulating successful insertion
        when(stmt.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);  // Simulate the generated ID

        stockEntryDAO.insert(stockEntry);

        // Verify that executeUpdate was called once
        verify(stmt, times(1)).executeUpdate();
    }

    @Test
    void testUpdate() throws SQLException {
        // Convert LocalDate to java.sql.Date
        LocalDate localDate = LocalDate.of(2025, 5, 10);
        Date entryDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expiryDate = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mock the PreparedStatement behavior
        StockEntry stockEntry = new StockEntry(1, "ITEM-001", 150, entryDate, expiryDate);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        stockEntryDAO.update(stockEntry);

        // Verify that executeUpdate was called once
        verify(stmt, times(1)).executeUpdate();
    }

    @Test
    void testDelete() throws SQLException {
        // Convert LocalDate to java.sql.Date
        LocalDate localDate = LocalDate.of(2025, 5, 10);
        Date entryDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expiryDate = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mock the PreparedStatement behavior
        StockEntry stockEntry = new StockEntry(1, "ITEM-001", 100, entryDate, expiryDate);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        stockEntryDAO.delete(stockEntry);

        // Verify that executeUpdate was called once
        verify(stmt, times(1)).executeUpdate();
    }

    @Test
    void testFindAll() throws SQLException {
        // Convert LocalDate to java.sql.Date
        LocalDate localDate = LocalDate.of(2025, 5, 10);
        Date entryDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expiryDate = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mock the ResultSet to return multiple StockEntries
        StockEntry stockEntry1 = new StockEntry(1, "ITEM-001", 100, entryDate, expiryDate);
        StockEntry stockEntry2 = new StockEntry(2, "ITEM-002", 200, entryDate, expiryDate);
        List<StockEntry> expectedEntries = Arrays.asList(stockEntry1, stockEntry2);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("stock_entry_id")).thenReturn(1, 2);
        when(rs.getString("item_code")).thenReturn("ITEM-001", "ITEM-002");
        when(rs.getInt("quantity")).thenReturn(100, 200);
        when(rs.getDate("entry_date")).thenReturn((java.sql.Date) entryDate);
        when(rs.getDate("expiry_date")).thenReturn((java.sql.Date) expiryDate);

        List<StockEntry> stockEntries = stockEntryDAO.findAll();

        // Assert that the retrieved stock entries match the expected ones
        assertEquals(expectedEntries.size(), stockEntries.size());
        assertEquals(expectedEntries.get(0).getItemCode(), stockEntries.get(0).getItemCode());
    }

    @Test
    void testReduceQuantity() throws SQLException {
        // Convert LocalDate to java.sql.Date
        LocalDate localDate = LocalDate.of(2025, 5, 10);
        Date entryDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expiryDate = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mock the PreparedStatement behavior
        StockEntry stockEntry = new StockEntry(1, "ITEM-001", 100, entryDate, expiryDate);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        stockEntryDAO.reduceQuantity(stockEntry, 10);

        // Verify that executeUpdate was called once
        verify(stmt, times(1)).executeUpdate();
    }

    @Test
    void testGetStockEntryByItemCode() throws SQLException {
        // Convert LocalDate to java.sql.Date
        LocalDate localDate = LocalDate.of(2025, 5, 10);
        Date entryDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expiryDate = Date.from(localDate.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mock the ResultSet to return a single StockEntry
        StockEntry expectedStockEntry = new StockEntry(1, "ITEM-001", 100, entryDate, expiryDate);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("stock_entry_id")).thenReturn(1);
        when(rs.getString("item_code")).thenReturn("ITEM-001");
        when(rs.getInt("quantity")).thenReturn(100);
        when(rs.getDate("entry_date")).thenReturn((java.sql.Date) entryDate);
        when(rs.getDate("expiry_date")).thenReturn((java.sql.Date) expiryDate);

        StockEntry stockEntry = stockEntryDAO.getStockEntryByItemCode("ITEM-001");

        // Assert that the retrieved stock entry matches the expected one
        assertNotNull(stockEntry);
        assertEquals(expectedStockEntry.getItemCode(), stockEntry.getItemCode());
    }

    @Test
    void testGetStockEntryByItemCode_StockEntryNotFound() throws SQLException {
        // Mock the ResultSet to return no results
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        StockEntry stockEntry = stockEntryDAO.getStockEntryByItemCode("ITEM-001");

        // Assert that no stock entry is returned
        assertNull(stockEntry);
    }
}
