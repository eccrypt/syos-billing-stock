package tests.core.services;

import core.dao.ShelfDAO;
import core.models.Shelf;
import core.services.ShelfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShelfServiceTest {

    @Mock private ShelfDAO shelfDAO;
    private ShelfService shelfService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        shelfService = new ShelfService(shelfDAO);
    }

    // 1. Add shelf entry successfully
    @Test
    void testAddShelfSuccess() throws SQLException {
        Shelf shelf = new Shelf("ITEM001", 10, 5);

        doNothing().when(shelfDAO).addShelf(shelf);

        shelfService.addShelf(shelf);

        verify(shelfDAO, times(1)).addShelf(shelf);
    }

    // 2. Add shelf throws SQLException
    @Test
    void testAddShelfThrowsSQLException() throws SQLException {
        Shelf shelf = new Shelf("ITEM001", 10, 5);

        doThrow(new SQLException("DB Error")).when(shelfDAO).addShelf(shelf);

        assertThrows(SQLException.class, () -> shelfService.addShelf(shelf));
    }

    // 3. Retrieve shelf by product code successfully
    @Test
    void testGetShelfByProductCodeSuccess() throws SQLException {
        String productCode = "ITEM001";
        Shelf mockShelf = new Shelf(productCode, 10, 5);

        when(shelfDAO.getShelfByProductCode(productCode)).thenReturn(mockShelf);

        Shelf result = shelfService.getShelfByProductCode(productCode);

        assertNotNull(result);
        assertEquals(productCode, result.getProductCode());
        assertEquals(10, result.getShelfDefault());
        assertEquals(5, result.getShelfCurrent());
    }

    // 4. Retrieve shelf throws SQLException
    @Test
    void testGetShelfByProductCodeThrowsSQLException() throws SQLException {
        String productCode = "ITEM001";

        when(shelfDAO.getShelfByProductCode(productCode)).thenThrow(new SQLException("DB Error"));

        assertThrows(SQLException.class, () -> shelfService.getShelfByProductCode(productCode));
    }

    // 5. Update shelf entry successfully
    @Test
    void testUpdateShelfSuccess() throws SQLException {
        Shelf shelf = new Shelf("ITEM001", 10, 5);
        doNothing().when(shelfDAO).updateShelf(shelf);

        shelfService.updateShelf(shelf);

        verify(shelfDAO, times(1)).updateShelf(shelf);
    }

    // 6. Update shelf throws SQLException
    @Test
    void testUpdateShelfThrowsSQLException() throws SQLException {
        Shelf shelf = new Shelf("ITEM001", 10, 5);

        doThrow(new SQLException("DB Error")).when(shelfDAO).updateShelf(shelf);

        assertThrows(SQLException.class, () -> shelfService.updateShelf(shelf));
    }

    // 7. Delete shelf successfully
    @Test
    void testDeleteShelfSuccess() throws SQLException {
        String productCode = "ITEM001";

        doNothing().when(shelfDAO).deleteShelf(productCode);

        shelfService.deleteShelf(productCode);

        verify(shelfDAO, times(1)).deleteShelf(productCode);
    }

    // 8. Delete shelf throws SQLException
    @Test
    void testDeleteShelfThrowsSQLException() throws SQLException {
        String productCode = "ITEM001";

        doThrow(new SQLException("DB Error")).when(shelfDAO).deleteShelf(productCode);

        assertThrows(SQLException.class, () -> shelfService.deleteShelf(productCode));
    }
}
