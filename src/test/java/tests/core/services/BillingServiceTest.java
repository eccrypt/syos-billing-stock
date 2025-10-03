package tests.core.services;

import core.dao.BillDAO;
import core.dao.BillItemDAO;
import core.discount.DiscountContext;
import core.discount.DiscountResult;
import core.models.BillItem;
import core.models.Item;
import core.models.Shelf;
import core.models.StockEntry;
import core.services.BillingService;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BillingServiceTest {

    @Mock private ItemService itemService;
    @Mock private BillDAO billDAO;
    @Mock private BillItemDAO billItemDAO;
    @Mock private StockService stockService;
    @Mock private DiscountContext discountContext;
    @Mock private ShelfService shelfService;

    private BillingService billingService;
    private final Date dummyDate = new Date();

    @BeforeEach
    void setUp() throws SQLException {
        billingService = new BillingService(itemService, billDAO, billItemDAO, stockService, discountContext);
        when(stockService.getShelfService()).thenReturn(shelfService);
        when(shelfService.getShelfByProductCode("A001")).thenReturn(new Shelf(1, "A001", 10, 10));
        when(shelfService.getShelfByProductCode("B002")).thenReturn(new Shelf(2, "B002", 10, 10));
    }

    // 1. testCalculateTotal_singleItemQuantityOne
    @Test
    void testCalculateTotal_singleItemQuantityOne() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 1);
        assertEquals(50.0, billingService.calculateTotal(purchased));
    }

    // 60. testCreateBill_nullDiscountName (Fixed with Shelf setup using updated constructor)
    @Test
    void testCreateBill_nullDiscountName() throws SQLException {
        Item item = new Item("A001", "Apple", 10.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));
        when(shelfService.getShelfByProductCode("A001")).thenReturn(new Shelf(1, "A001", 10, 10)); // Using updated Shelf constructor

        DiscountResult discountResult = new DiscountResult(10.0, null);
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(discountResult);
        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 20.0);
        verify(billDAO).saveBill(any());
    }

}