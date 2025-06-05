package tests.core.services;

import core.models.Item;
import core.repositories.ItemRepository;
import core.services.ItemService;
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

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemService(itemRepo);
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

    // 3. getItemByCode throws SQLException from repo
    @Test
    void testGetItemByCodeThrowsSQLException() throws SQLException {
        when(itemRepo.getItemByCode("A001")).thenThrow(new SQLException("DB error"));
        assertThrows(SQLException.class, () -> itemService.getItemByCode("A001"));
    }

    // 4. getAllItems returns list successfully
    @Test
    void testGetAllItemsSuccess() throws SQLException {
        List<Item> items = List.of(
                new Item("A001", "Apple", 50.0),
                new Item("B002", "Banana", 30.0)
        );
        when(itemRepo.getAllItems()).thenReturn(items);

        List<Item> result = itemService.getAllItems();

        assertEquals(2, result.size());
    }

    // 5. getAllItems throws SQLException from repo
    @Test
    void testGetAllItemsThrowsSQLException() throws SQLException {
        when(itemRepo.getAllItems()).thenThrow(new SQLException("DB error"));
        assertThrows(SQLException.class, () -> itemService.getAllItems());
    }

    // 6. addItem calls repo addItem successfully
    @Test
    void testAddItemSuccess() throws SQLException {
        Item item = new Item("C003", "Cherry", 40.0);
        doNothing().when(itemRepo).addItem(item);

        itemService.addItem(item);

        verify(itemRepo).addItem(item);
    }

    // 7. addItem throws SQLException from repo
    @Test
    void testAddItemThrowsSQLException() throws SQLException {
        Item item = new Item("C003", "Cherry", 40.0);
        doThrow(new SQLException("DB error")).when(itemRepo).addItem(item);

        assertThrows(SQLException.class, () -> itemService.addItem(item));
    }

    // 8. updateItem calls repo updateItem successfully
    @Test
    void testUpdateItemSuccess() throws SQLException {
        Item item = new Item("D004", "Date", 60.0);
        doNothing().when(itemRepo).updateItem(item);

        itemService.updateItem(item);

        verify(itemRepo).updateItem(item);
    }

    // 9. updateItem throws SQLException from repo
    @Test
    void testUpdateItemThrowsSQLException() throws SQLException {
        Item item = new Item("D004", "Date", 60.0);
        doThrow(new SQLException("DB error")).when(itemRepo).updateItem(item);

        assertThrows(SQLException.class, () -> itemService.updateItem(item));
    }

    // 10. updateItemName updates name when item exists
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

    // 11. updateItemName does nothing when item not found
    @Test
    void testUpdateItemNameNoUpdateIfNotFound() throws SQLException {
        when(itemRepo.getItemByCode("Z999")).thenReturn(null);

        itemService.updateItemName("Z999", "New Name");

        verify(itemRepo, never()).updateItem(any());
    }

    // 12. updateItemName throws SQLException from getItemByCode
    @Test
    void testUpdateItemNameThrowsSQLExceptionOnGet() throws SQLException {
        when(itemRepo.getItemByCode("E005")).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> itemService.updateItemName("E005", "New Name"));
    }

    // 13. updateItemName throws SQLException from updateItem
    @Test
    void testUpdateItemNameThrowsSQLExceptionOnUpdate() throws SQLException {
        Item oldItem = new Item("E005", "Eggplant", 70.0);
        when(itemRepo.getItemByCode("E005")).thenReturn(oldItem);
        doThrow(new SQLException()).when(itemRepo).updateItem(any());

        assertThrows(SQLException.class, () -> itemService.updateItemName("E005", "New Eggplant"));
    }

    // 14. updateItemPrice updates price when item exists
    @Test
    void testUpdateItemPriceSuccess() throws SQLException {
        Item oldItem = new Item("F006", "Fig", 80.0);
        when(itemRepo.getItemByCode("F006")).thenReturn(oldItem);
        doNothing().when(itemRepo).updateItem(any(Item.class));

        itemService.updateItemPrice("F006", 90.0);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepo).updateItem(captor.capture());
        assertEquals(90.0, captor.getValue().getPrice());
        assertEquals("F006", captor.getValue().getCode());
        assertEquals("Fig", captor.getValue().getName());
    }

    // 15. updateItemPrice does nothing when item not found
    @Test
    void testUpdateItemPriceNoUpdateIfNotFound() throws SQLException {
        when(itemRepo.getItemByCode("Z999")).thenReturn(null);

        itemService.updateItemPrice("Z999", 50.0);

        verify(itemRepo, never()).updateItem(any());
    }

    // 16. updateItemPrice throws SQLException from getItemByCode
    @Test
    void testUpdateItemPriceThrowsSQLExceptionOnGet() throws SQLException {
        when(itemRepo.getItemByCode("F006")).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> itemService.updateItemPrice("F006", 90.0));
    }

    // 17. updateItemPrice throws SQLException from updateItem
    @Test
    void testUpdateItemPriceThrowsSQLExceptionOnUpdate() throws SQLException {
        Item oldItem = new Item("F006", "Fig", 80.0);
        when(itemRepo.getItemByCode("F006")).thenReturn(oldItem);
        doThrow(new SQLException()).when(itemRepo).updateItem(any());

        assertThrows(SQLException.class, () -> itemService.updateItemPrice("F006", 90.0));
    }

    // 18. deleteItem calls repo deleteItem successfully
    @Test
    void testDeleteItemSuccess() throws SQLException {
        doNothing().when(itemRepo).deleteItem("G007");

        itemService.deleteItem("G007");

        verify(itemRepo).deleteItem("G007");
    }

    // 19. deleteItem throws SQLException from repo
    @Test
    void testDeleteItemThrowsSQLException() throws SQLException {
        doThrow(new SQLException("DB error")).when(itemRepo).deleteItem("G007");

        assertThrows(SQLException.class, () -> itemService.deleteItem("G007"));
    }

    // 20. getAllItems returns empty list if no items found
    @Test
    void testGetAllItemsEmptyList() throws SQLException {
        when(itemRepo.getAllItems()).thenReturn(List.of());

        List<Item> items = itemService.getAllItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }
}
