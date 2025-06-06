package tests.core.dao;

import core.dao.ShelfDAO;
import core.models.Shelf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ShelfDAOTest {

    @Mock
    private Connection conn;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    private ShelfDAO shelfDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        shelfDAO = new ShelfDAO(conn);
    }

    @Test
    void testAddShelf() throws SQLException {
        // Create a Shelf object to add
        Shelf shelf = new Shelf("ITEM-0001", 50, 30);

        // Mock PreparedStatement behavior
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1); // Simulating successful insert

        // Execute the addShelf method
        shelfDAO.addShelf(shelf);

        // Verify that executeUpdate was called once
        verify(stmt, times(1)).executeUpdate();
    }

    @Test
    void testGetShelfByProductCode_ShelfExists() throws SQLException {
        // Create a Shelf object to return
        Shelf expectedShelf = new Shelf(1, "ITEM-0001", 50, 30);

        // Mock the ResultSet to return a shelf
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("shelf_id")).thenReturn(1);
        when(rs.getString("product_code")).thenReturn("ITEM-0001");
        when(rs.getInt("shelf_default")).thenReturn(50);
        when(rs.getInt("shelf_current")).thenReturn(30);

        // Execute the getShelfByProductCode method
        Shelf shelf = shelfDAO.getShelfByProductCode("ITEM-0001");

        // Assert the returned shelf matches the expected values
        assertNotNull(shelf);
        assertEquals(expectedShelf.getProductCode(), shelf.getProductCode());
        assertEquals(expectedShelf.getShelfDefault(), shelf.getShelfDefault());
        assertEquals(expectedShelf.getShelfCurrent(), shelf.getShelfCurrent());
    }

    @Test
    void testGetShelfByProductCode_ShelfNotFound() throws SQLException {
        // Mock the ResultSet to return no results
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        // Execute the getShelfByProductCode method
        Shelf shelf = shelfDAO.getShelfByProductCode("ITEM-0001");

        // Assert that no shelf was found
        assertNull(shelf);
    }

    @Test
    void testUpdateShelf() throws SQLException {
        // Create a Shelf object to update
        Shelf shelf = new Shelf("ITEM-0001", 60, 40);

        // Mock PreparedStatement behavior
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        // Execute the updateShelf method
        shelfDAO.updateShelf(shelf);

        // Verify that executeUpdate was called once
        verify(stmt, times(1)).executeUpdate();
    }

    @Test
    void testDeleteShelf() throws SQLException {
        String productCode = "ITEM-0001";

        // Mock PreparedStatement behavior
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        // Execute the deleteShelf method
        shelfDAO.deleteShelf(productCode);

        // Verify that executeUpdate was called once
        verify(stmt, times(1)).executeUpdate();
    }
}
