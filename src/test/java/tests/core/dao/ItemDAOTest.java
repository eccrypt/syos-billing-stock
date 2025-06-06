package tests.core.dao;

import core.dao.ItemDAO;
import core.models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ItemDAOTest {

    @Mock
    private Connection conn;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    private ItemDAO itemDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        itemDAO = new ItemDAO(conn);
    }

    @Test
    void testGetItemByCode_ItemExists() throws SQLException {
        // Mock the ResultSet to return a specific item
        Item expectedItem = new Item("ITEM-0001", "Item 1", 100.0);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("code")).thenReturn("ITEM-0001");
        when(rs.getString("name")).thenReturn("Item 1");
        when(rs.getDouble("price")).thenReturn(100.0);

        Item item = itemDAO.getItemByCode("ITEM-0001");

        assertNotNull(item);
        assertEquals(expectedItem.getCode(), item.getCode());
        assertEquals(expectedItem.getName(), item.getName());
        assertEquals(expectedItem.getPrice(), item.getPrice());
    }

    @Test
    void testGetItemByCode_ItemNotFound() throws SQLException {
        // Mock ResultSet to return nothing
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Item item = itemDAO.getItemByCode("ITEM-0001");

        assertNull(item);
    }

   // @Test
//    void testGetAllItems() throws SQLException {
//        // Mock ResultSet to return multiple items
//        Item item1 = new Item("ITEM-0001", "Item 1", 100.0);
//        Item item2 = new Item("ITEM-0002", "Item 2", 200.0);
//        List<Item> expectedItems = Arrays.asList(item1, item2);
//
//        when(conn.prepareStatement(anyString())).thenReturn(stmt);
//        when(stmt.executeQuery()).thenReturn(rs);
//        when(rs.next()).thenReturn(true, true, false);
//        when(rs.getString("code")).thenReturn("ITEM-0001", "ITEM-0002");
//        when(rs.getString("name")).thenReturn("Item 1", "Item 2");
//        when(rs.getDouble("price")).thenReturn(100.0, 200.0);
//
//        List<Item> items = itemDAO.getAllItems();
//
//        assertNotNull(items);
//        assertEquals(2, items.size());
//        assertTrue(items.containsAll(expectedItems));
//    }

//    @Test
//    void testAddItem() throws SQLException {
//        Item item = new Item(null, "New Item", 300.0);
//
//        // Mock the behavior of the prepared statement
//        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
//        when(stmt.executeUpdate()).thenReturn(1);
//        when(stmt.getGeneratedKeys()).thenReturn(rs);
//        when(rs.next()).thenReturn(true);
//        when(rs.getInt(1)).thenReturn(1);
//
//        itemDAO.addItem(item);
//
//        // Verify that the generated code is set on the item
//        assertEquals("ITEM-0001", item.getCode());  // The mock generateProductCode() logic gives "ITEM-0001"
//        verify(stmt, times(1)).executeUpdate();
//    }

    @Test
    void testUpdateItem() throws SQLException {
        Item item = new Item("ITEM-0001", "Updated Item", 400.0);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        itemDAO.updateItem(item);

        verify(stmt, times(1)).setString(1, item.getName());
        verify(stmt, times(1)).setDouble(2, item.getPrice());
        verify(stmt, times(1)).setString(3, item.getCode());
        verify(stmt, times(1)).executeUpdate();
    }

    @Test
    void testDeleteItem() throws SQLException {
        String itemCode = "ITEM-0001";

        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        itemDAO.deleteItem(itemCode);

        verify(stmt, times(1)).setString(1, itemCode);
        verify(stmt, times(1)).executeUpdate();
    }

    @Test
    void testGenerateProductCode() throws SQLException {
        // Mock the ResultSet to return a valid last product code
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("code")).thenReturn("ITEM-0005");

        String newCode = itemDAO.generateProductCode();

        assertEquals("ITEM-0006", newCode);  // It should increment the last code
    }
}
