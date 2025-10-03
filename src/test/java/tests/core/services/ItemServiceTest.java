package tests.core.services;

import core.models.Item;
import core.repositories.ItemRepository;
import core.services.ItemService;
import core.services.ShelfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepo;

    @Mock
    private ShelfService shelfService;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemService(itemRepo, shelfService);  // Initialize with both dependencies
    }

    // 1. getItemByCode returns item successfully
    @Test
    void testGetItemByCodeSuccess() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemRepo.getItemByCode("A001")).thenReturn(item);

        Item result = itemService.getItemByCode("A001");

        assertNotNull(result);
        assertEquals("A001", result.getCode());
        assertEquals("Apple", result.getName());
        assertEquals(50.0, result.getPrice());
    }

    // 2. getItemByCode returns null if item not found
    @Test
    void testGetItemByCodeNotFound() throws SQLException {
        when(itemRepo.getItemByCode("Z999")).thenReturn(null);
        Item result = itemService.getItemByCode("Z999");
        assertNull(result);
    }

    // 3. addItem calls repo addItem successfully
    @Test
    void testAddItemSuccess() throws SQLException {
        Item item = new Item("C003", "Cherry", 40.0);
        int shelfDefault = 10;
        int shelfCurrent = 10;

        // Mocking successful item addition
        doNothing().when(itemRepo).addItem(item);

        itemService.addItem(item, shelfDefault, shelfCurrent);

        verify(itemRepo).addItem(item);  // Verifying the addItem method is called
    }

    // 4. addItem throws SQLException from repo
    @Test
    void testAddItemThrowsSQLException() throws SQLException {
        Item item = new Item("C003", "Cherry", 40.0);
        int shelfDefault = 10;
        int shelfCurrent = 10;

        // Mocking an exception thrown during item addition
        doThrow(new SQLException("DB error")).when(itemRepo).addItem(item);

        // Asserting that the SQLException is thrown when calling addItem
        assertThrows(SQLException.class, () -> itemService.addItem(item, shelfDefault, shelfCurrent));
    }

    // 5. updateItem calls repo updateItem successfully
    @Test
    void testUpdateItemSuccess() throws SQLException {
        Item item = new Item("D004", "Date", 60.0);
        doNothing().when(itemRepo).updateItem(item);

        itemService.updateItem(item);

        verify(itemRepo).updateItem(item);
    }

    // 6. updateItem throws SQLException from repo
    @Test
    void testUpdateItemThrowsSQLException() throws SQLException {
        Item item = new Item("D004", "Date", 60.0);
        doThrow(new SQLException("DB error")).when(itemRepo).updateItem(item);

        assertThrows(SQLException.class, () -> itemService.updateItem(item));
    }

    // 7. updateItemName updates name when item exists
    @Test
    void testUpdateItemNameSuccess() throws SQLException {
        Item oldItem = new Item("E005", "Eggplant", 70.0);
        when(itemRepo.getItemByCode("E005")).thenReturn(oldItem);
        doNothing().when(itemRepo).updateItem(any(Item.class));

        itemService.updateItemName("E005", "New Eggplant");

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepo).updateItem(captor.capture());
        assertEquals("New Eggplant", captor.getValue().getName());
        assertEquals("E005", captor.getValue().getCode());
        assertEquals(70.0, captor.getValue().getPrice());
    }

    // 8. updateItemName does nothing when item not found
    @Test
    void testUpdateItemNameNoUpdateIfNotFound() throws SQLException {
        when(itemRepo.getItemByCode("Z999")).thenReturn(null);

        itemService.updateItemName("Z999", "New Name");

        verify(itemRepo, never()).updateItem(any());
    }

    // 9. deleteItem calls repo deleteItem and shelfService deleteShelf successfully
    @Test
    void testDeleteItemSuccess() throws SQLException {
        String code = "G007";
        doNothing().when(itemRepo).deleteItem(code);
        doNothing().when(shelfService).deleteShelf(code);  // Mocking shelfService deleteShelf method

        itemService.deleteItem(code);

        verify(itemRepo).deleteItem(code);
        verify(shelfService).deleteShelf(code);  // Verifying the deleteShelf method is called
    }

    // 10. deleteItem throws SQLException from repo
    @Test
    void testDeleteItemThrowsSQLException() throws SQLException {
        String code = "G007";
        doThrow(new SQLException("DB error")).when(itemRepo).deleteItem(code);

        assertThrows(SQLException.class, () -> itemService.deleteItem(code));
    }

    // 11. deleteItem calls deleteShelf method even when exception occurs
//    @Test
//    void testDeleteItem_CallsDeleteShelfEvenIfExceptionThrown() throws SQLException {
//        String code = "G007";
//        doThrow(new SQLException("DB error")).when(itemRepo).deleteItem(code);
//
//        assertThrows(SQLException.class, () -> itemService.deleteItem(code));
//        verify(shelfService).deleteShelf(code);  // Verifying deleteShelf method is still called
//    }
}
