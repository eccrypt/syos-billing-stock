package tests.core.dao;

import core.models.BillItem;
import core.dao.BillItemDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class BillItemDAOTest {

    @Mock
    private Connection conn;

    @Mock
    private PreparedStatement stmt;

    private BillItemDAO billItemDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        billItemDAO = new BillItemDAO(conn);
    }

    @Test
    void testSaveBillItems() throws SQLException {
        int billId = 1;
        BillItem item1 = new BillItem("ITEM1", "Item 1", 2, 20.0);
        BillItem item2 = new BillItem("ITEM2", "Item 2", 3, 30.0);
        List<BillItem> items = Arrays.asList(item1, item2);

        // Mocking the PreparedStatement behavior
        when(conn.prepareStatement(any(String.class))).thenReturn(stmt);

        // Running the saveBillItems method
        billItemDAO.saveBillItems(billId, items);

        // Verify that the set methods were called for each item in the list
        // Verify stmt.setInt(1, billId) called for both items (2 times)
        verify(stmt, times(2)).setInt(1, billId);

        // Verify stmt.setString(2, ...) called for both items (2 times)
        verify(stmt, times(2)).setString(2, "ITEM1");
        verify(stmt, times(2)).setString(3, "Item 1");
        verify(stmt, times(2)).setInt(4, 2);
        verify(stmt, times(2)).setDouble(5, 20.0);

        verify(stmt, times(2)).setString(2, "ITEM2");
        verify(stmt, times(2)).setString(3, "Item 2");
        verify(stmt, times(2)).setInt(4, 3);
        verify(stmt, times(2)).setDouble(5, 30.0);

        // Verify addBatch() was called once per item (2 times)
        verify(stmt, times(2)).addBatch();

        // Verify executeBatch() was called once
        verify(stmt, times(1)).executeBatch();
    }

    @Test
    void testSaveBillItems_ExceptionHandling() throws SQLException {
        int billId = 1;
        BillItem item1 = new BillItem("ITEM1", "Item 1", 2, 20.0);
        BillItem item2 = new BillItem("ITEM2", "Item 2", 3, 30.0);
        List<BillItem> items = Arrays.asList(item1, item2);

        // Mocking the PreparedStatement behavior
        when(conn.prepareStatement(any(String.class))).thenReturn(stmt);
        doThrow(new SQLException("Database error")).when(stmt).executeBatch();  // Simulate an exception during batch execution

        // Running the saveBillItems method to test exception handling
        assertThrows(SQLException.class, () -> billItemDAO.saveBillItems(billId, items));

        // Verify that executeBatch was called once and threw the exception
        verify(stmt, times(1)).executeBatch();
    }
}
