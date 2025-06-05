package tests.core.services;

import core.dao.BillDAO;
import core.dao.BillItemDAO;
import core.discount.DiscountContext;
import core.discount.DiscountResult;
import core.models.BillItem;
import core.models.Item;
import core.models.StockEntry;
import core.services.BillingService;
import core.services.ItemService;
import core.services.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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

    private BillingService billingService;

    // Dummy date for StockEntry constructor
    private final Date dummyDate = new Date();

    @BeforeEach
    void setUp() {
        billingService = new BillingService(itemService, billDAO, billItemDAO, stockService, discountContext);
    }

    //
    // calculateTotal() tests (20)
    //

    @Test
    void testCalculateTotal_singleItemQuantityOne() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 1);
        assertEquals(50.0, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_singleItemMultipleQuantity() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 3);
        assertEquals(150.0, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_multipleItemsEachQuantityOne() throws SQLException {
        Item item1 = new Item("A001", "Apple", 50.0);
        Item item2 = new Item("B002", "Banana", 30.0);
        when(itemService.getItemByCode("A001")).thenReturn(item1);
        when(itemService.getItemByCode("B002")).thenReturn(item2);
        Map<String, Integer> purchased = Map.of("A001", 1, "B002", 1);
        assertEquals(80.0, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_multipleItemsVaryingQuantities() throws SQLException {
        Item item1 = new Item("A001", "Apple", 50.0);
        Item item2 = new Item("B002", "Banana", 30.0);
        when(itemService.getItemByCode("A001")).thenReturn(item1);
        when(itemService.getItemByCode("B002")).thenReturn(item2);
        Map<String, Integer> purchased = Map.of("A001", 2, "B002", 3);
        assertEquals(190.0, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_itemPriceZero() throws SQLException {
        Item item = new Item("A001", "FreeItem", 0.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 5);
        assertEquals(0.0, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_quantityZero() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 0);
        assertEquals(0.0, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_emptyMap() throws SQLException {
        Map<String, Integer> purchased = Map.of();
        assertEquals(0.0, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_largeQuantity() throws SQLException {
        Item item = new Item("A001", "Apple", 10.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", Integer.MAX_VALUE);
        double expected = 10.0 * (double) Integer.MAX_VALUE;
        assertEquals(expected, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_largePrice() throws SQLException {
        Item item = new Item("A001", "ExpensiveItem", Double.MAX_VALUE / 2);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 2);
        double expected = Double.MAX_VALUE;
        assertEquals(expected, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_fractionalPrice() throws SQLException {
        Item item = new Item("A001", "FractionalPrice", 19.99);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 3);
        assertEquals(59.97, billingService.calculateTotal(purchased), 0.001);
    }

    @Test
    void testCalculateTotal_itemNotFound_throwsSQLException() throws SQLException {
        when(itemService.getItemByCode("Z999")).thenReturn(null);
        Map<String, Integer> purchased = Map.of("Z999", 1);
        SQLException thrown = assertThrows(SQLException.class, () -> billingService.calculateTotal(purchased));
        assertEquals("Item not found: Z999", thrown.getMessage());
    }

    @Test
    void testCalculateTotal_nullKeyInMap() throws SQLException {
        Map<String, Integer> purchased = new HashMap<>();
        purchased.put(null, 1);
        when(itemService.getItemByCode(null)).thenReturn(null);
        SQLException thrown = assertThrows(SQLException.class, () -> billingService.calculateTotal(purchased));
        assertTrue(thrown.getMessage().contains("Item not found"));
    }

    @Test
    void testCalculateTotal_nullValueInMap() throws SQLException {
        Map<String, Integer> purchased = new HashMap<>();
        purchased.put("A001", null);
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        assertThrows(NullPointerException.class, () -> billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_negativeQuantity() throws SQLException {
        Map<String, Integer> purchased = Map.of("A001", -1);
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        double total = billingService.calculateTotal(purchased);
        assertEquals(-50.0, total);
    }

    @Test
    void testCalculateTotal_mixedValidInvalidItem() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(itemService.getItemByCode("X999")).thenReturn(null);
        Map<String, Integer> purchased = new HashMap<>();
        purchased.put("A001", 1);
        purchased.put("X999", 2);
        SQLException thrown = assertThrows(SQLException.class, () -> billingService.calculateTotal(purchased));
        assertEquals("Item not found: X999", thrown.getMessage());
    }

    @Test
    void testCalculateTotal_nullMap() {
        assertThrows(NullPointerException.class, () -> billingService.calculateTotal(null));
    }

    @Test
    void testCalculateTotal_itemNegativePrice() throws SQLException {
        Item item = new Item("A001", "Apple", -50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 1);
        double total = billingService.calculateTotal(purchased);
        assertEquals(-50.0, total);
    }

    @Test
    void testCalculateTotal_duplicateKeysNotPossible() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 1);
        assertEquals(50.0, billingService.calculateTotal(purchased));
        assertEquals(50.0, billingService.calculateTotal(purchased));
    }

    @Test
    void testCalculateTotal_multipleCalls_consistency() throws SQLException {
        Item item = new Item("A001", "Apple", 20.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 2);
        double first = billingService.calculateTotal(purchased);
        double second = billingService.calculateTotal(purchased);
        assertEquals(first, second);
    }

    @Test
    void testCalculateTotal_quantityAsIntegerMaxValue() throws SQLException {
        Item item = new Item("A001", "Apple", 1.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", Integer.MAX_VALUE);
        double total = billingService.calculateTotal(purchased);
        assertEquals((double) Integer.MAX_VALUE, total);
    }

    //
    // createBill() tests (40)
    //

    @Test
    void testCreateBill_singleItemSufficientStock() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 2))
                .thenReturn(List.of(new StockEntry("A001", 2, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(100.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 2), 200.0);

        verify(billDAO).saveBill(any());
        verify(billItemDAO).saveBillItems(eq(1), anyList());
    }

    @Test
    void testCreateBill_multipleItemsSufficientStock() throws SQLException {
        Item item1 = new Item("A001", "Apple", 50.0);
        Item item2 = new Item("B002", "Banana", 30.0);
        when(itemService.getItemByCode("A001")).thenReturn(item1);
        when(itemService.getItemByCode("B002")).thenReturn(item2);

        when(stockService.allocateStock("A001", 2))
                .thenReturn(List.of(new StockEntry("A001", 2, dummyDate, dummyDate)));

        when(stockService.allocateStock("B002", 3))
                .thenReturn(List.of(new StockEntry("B002", 3, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(190.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 2, "B002", 3), 200.0);

        verify(billDAO).saveBill(any());
        verify(billItemDAO).saveBillItems(eq(1), anyList());
    }

    @Test
    void testCreateBill_stockExactlyEqualsQuantity() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 5))
                .thenReturn(List.of(new StockEntry("A001", 5, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(250.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 5), 300.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_itemPriceZero() throws SQLException {
        Item item = new Item("A001", "FreeItem", 0.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 10))
                .thenReturn(List.of(new StockEntry("A001", 10, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(0.0, "Freebie"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 10), 0.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_cashTenderedEqualsTotal() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(50.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 50.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_cashTenderedGreaterThanTotal() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(50.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 100.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_cashTenderedLessThanTotal_changeNegative() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(50.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 40.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_discountReducesTotalToZero() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(0.0, "Full Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 0.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_discountAppliesPartially() throws SQLException {
        Item item = new Item("A001", "Apple", 100.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(70.0, "30% Off"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 100.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_noDiscountApplied() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 2))
                .thenReturn(List.of(new StockEntry("A001", 2, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(100.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 2), 120.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_discountContextReturnsNull() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        lenient().when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(null);

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        assertThrows(NullPointerException.class, () ->
                billingService.createBill(Map.of("A001", 1), 100.0));
    }


    @Test
    void testCreateBill_discountContextThrows() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenThrow(new RuntimeException("Discount error"));

        assertThrows(RuntimeException.class, () -> billingService.createBill(Map.of("A001", 1), 100.0));
    }

    @Test
    void testCreateBill_itemNotFoundThrows() throws SQLException {
        when(itemService.getItemByCode("X999")).thenReturn(null);
        SQLException thrown = assertThrows(SQLException.class, () -> billingService.createBill(Map.of("X999", 1), 100.0));
        assertTrue(thrown.getMessage().contains("Item not found"));
    }

    @Test
    void testCreateBill_insufficientStockThrows() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 3))
                .thenReturn(List.of(new StockEntry("A001", 2, dummyDate, dummyDate)));

        SQLException thrown = assertThrows(SQLException.class, () -> billingService.createBill(Map.of("A001", 3), 200.0));
        assertTrue(thrown.getMessage().contains("Insufficient stock"));
    }

    @Test
    void testCreateBill_stockServiceReturnsEmptyList() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(Collections.emptyList());

        SQLException thrown = assertThrows(SQLException.class, () -> billingService.createBill(Map.of("A001", 1), 100.0));
        assertTrue(thrown.getMessage().contains("Insufficient stock"));
    }

    @Test
    void testCreateBill_billDAOSaveThrows() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(50.0, "No Discount"));

        // Stub billDAO.saveBill to throw
        when(billDAO.saveBill(any())).thenThrow(new SQLException("DB error"));

        // We do not expect billItemDAO.saveBillItems to be called because saveBill throws
        // So no need to stub it here; or stub it leniently if you want

        SQLException thrown = assertThrows(SQLException.class, () ->
                billingService.createBill(Map.of("A001", 1), 100.0)
        );
        assertEquals("DB error", thrown.getMessage());
    }


    @Test
    void testCreateBill_billItemDAOSaveThrows() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(50.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);

        doThrow(new SQLException("DB error on items")).when(billItemDAO).saveBillItems(anyInt(), anyList());

        SQLException thrown = assertThrows(SQLException.class, () -> billingService.createBill(Map.of("A001", 1), 100.0));
        assertEquals("DB error on items", thrown.getMessage());
    }

    @Test
    void testCreateBill_nullPurchasedItems() {
        assertThrows(NullPointerException.class, () -> billingService.createBill(null, 100.0));
    }

    @Test
    void testCreateBill_negativeCashTendered() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 1))
                .thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(50.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), -10.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_billItemWithZeroQuantity() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        when(stockService.allocateStock("A001", 0))
                .thenReturn(Collections.emptyList());

        when(discountContext.applyDiscounts(any(), anyDouble()))
                .thenReturn(new DiscountResult(0.0, "No Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 0), 0.0);

        verify(billDAO).saveBill(any());
    }

    @Test
    void testCreateBill_billItemWithNegativeQuantity() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);

        Map<String, Integer> purchased = new HashMap<>();
        purchased.put("A001", -1);

        when(stockService.allocateStock("A001", -1))
                .thenThrow(new SQLException("Invalid quantity"));

        SQLException thrown = assertThrows(SQLException.class, () -> billingService.createBill(purchased, 100.0));
        assertTrue(thrown.getMessage().contains("Invalid quantity"));
    }


    // 42. Parameterized test for calculateTotal with multiple variations
    @ParameterizedTest
    @CsvSource({
            "A001,1,50.0",
            "A001,2,100.0",
            "B002,3,90.0"
    })
    void testCalculateTotal_parameterized(String code, int qty, double expectedTotal) throws SQLException {
        Item item = new Item(code, "ItemName", expectedTotal / qty);
        when(itemService.getItemByCode(code)).thenReturn(item);
        Map<String, Integer> purchased = Map.of(code, qty);
        double total = billingService.calculateTotal(purchased);
        assertEquals(expectedTotal, total);
    }

    // 43. Verify discountContext.applyDiscounts is called exactly once during createBill
    @Test
    void testCreateBill_discountContextCalledOnce() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(50.0, "No Discount"));
        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 100.0);

        verify(discountContext, times(1)).applyDiscounts(any(), anyDouble());
    }

    // 44. Verify billDAO.saveBill called exactly once
    @Test
    void testCreateBill_billDAOSaveCalledOnce() throws SQLException {
        Item item = new Item("A001", "Apple", 25.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 2)).thenReturn(List.of(new StockEntry("A001", 2, dummyDate, dummyDate)));
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(50.0, "No Discount"));
        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 2), 100.0);

        verify(billDAO, times(1)).saveBill(any());
    }

    // 45. Test createBill with very large quantities allocated successfully
    @Test
    void testCreateBill_largeQuantity() throws SQLException {
        Item item = new Item("A001", "Apple", 1.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        List<StockEntry> largeStock = Collections.nCopies(1000, new StockEntry("A001", 1, dummyDate, dummyDate));
        when(stockService.allocateStock("A001", 1000)).thenReturn(largeStock);
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(1000.0, "No Discount"));
        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1000), 1500.0);

        verify(billDAO).saveBill(any());
    }

    // 46. createBill throws SQLException if ItemService throws SQLException
    @Test
    void testCreateBill_itemServiceThrows() throws SQLException {
        when(itemService.getItemByCode("A001")).thenThrow(new SQLException("DB error on item service"));
        SQLException thrown = assertThrows(SQLException.class,
                () -> billingService.createBill(Map.of("A001", 1), 100.0));
        assertEquals("DB error on item service", thrown.getMessage());
    }

    // 47. createBill throws SQLException if StockService throws SQLException
    @Test
    void testCreateBill_stockServiceThrows() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenThrow(new SQLException("DB error on stock service"));
        SQLException thrown = assertThrows(SQLException.class,
                () -> billingService.createBill(Map.of("A001", 1), 100.0));
        assertEquals("DB error on stock service", thrown.getMessage());
    }

    // 48. calculateTotal returns zero when quantity is zero for multiple items
    @Test
    void testCalculateTotal_multipleItemsZeroQuantity() throws SQLException {
        Item item1 = new Item("A001", "Apple", 50.0);
        Item item2 = new Item("B002", "Banana", 30.0);
        when(itemService.getItemByCode("A001")).thenReturn(item1);
        when(itemService.getItemByCode("B002")).thenReturn(item2);
        Map<String, Integer> purchased = Map.of("A001", 0, "B002", 0);
        double total = billingService.calculateTotal(purchased);
        assertEquals(0.0, total);
    }

    // 49. createBill handles empty purchasedItems gracefully (no exception)
    @Test
    void testCreateBill_emptyPurchasedItems() throws SQLException {
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(0.0, "No Discount"));
        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Collections.emptyMap(), 0.0);

        verify(billDAO).saveBill(any());
    }

    // 50. calculateTotal with very large number of items (performance/consistency)
    @Test
    void testCalculateTotal_largeNumberOfItems() throws SQLException {
        Map<String, Integer> purchased = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String code = "I" + i;
            purchased.put(code, 1);
            when(itemService.getItemByCode(code)).thenReturn(new Item(code, "Item" + i, 1.0));
        }
        double total = billingService.calculateTotal(purchased);
        assertEquals(1000.0, total);
    }

    // 51. createBill verifies billItemDAO.saveBillItems called with correct billId and list size
    @Test
    void testCreateBill_billItemDAOSaveCalledWithCorrectArguments() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(50.0, "No Discount"));
        when(billDAO.saveBill(any())).thenReturn(123);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 100.0);

        ArgumentCaptor<Integer> billIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<List<BillItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(billItemDAO).saveBillItems(billIdCaptor.capture(), itemsCaptor.capture());

        assertEquals(123, billIdCaptor.getValue());
        assertEquals(1, itemsCaptor.getValue().size());
    }

    // 52. calculateTotal throws SQLException if itemService returns null for any item in large map
    @Test
    void testCalculateTotal_largeMapWithInvalidItem() throws SQLException {
        Map<String, Integer> purchased = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            String code = "I" + i;
            purchased.put(code, 1);
            when(itemService.getItemByCode(code)).thenReturn(new Item(code, "Item" + i, 1.0));
        }
        purchased.put("INVALID", 1);
        when(itemService.getItemByCode("INVALID")).thenReturn(null);

        SQLException thrown = assertThrows(SQLException.class, () -> billingService.calculateTotal(purchased));
        assertEquals("Item not found: INVALID", thrown.getMessage());
    }

    // 53. createBill passes negative quantity throws SQLException from stockService
    @Test
    void testCreateBill_negativeQuantity() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", -5)).thenThrow(new SQLException("Invalid quantity"));

        SQLException thrown = assertThrows(SQLException.class, () -> billingService.createBill(Map.of("A001", -5), 100.0));
        assertEquals("Invalid quantity", thrown.getMessage());
    }

    // 54. createBill with null billItems list throws NullPointerException (simulate by forcing)
    @Test
    void testCreateBill_nullBillItemsThrows() throws SQLException {
        // We can't directly pass null billItems, but simulate by mocking billDAO.saveBill to throw NPE
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(50.0, "No Discount"));
        when(billDAO.saveBill(any())).thenThrow(new NullPointerException("Bill items null"));

        assertThrows(NullPointerException.class, () -> billingService.createBill(Map.of("A001", 1), 100.0));
    }

    // 55. calculateTotal zero price and zero quantity returns zero
    @Test
    void testCalculateTotal_zeroPriceZeroQuantity() throws SQLException {
        Item item = new Item("A001", "FreeItem", 0.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", 0);
        assertEquals(0.0, billingService.calculateTotal(purchased));
    }

    // 56. createBill large discount greater than total (discountResult totalAfterDiscount negative or zero)
    @Test
    void testCreateBill_discountGreaterThanTotal() throws SQLException {
        Item item = new Item("A001", "Apple", 50.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        // Discount greater than total -> totalAfterDiscount = -10 (invalid, but test resilience)
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(-10.0, "Over Discount"));

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 50.0);

        verify(billDAO).saveBill(any());
    }

    // 57. calculateTotal quantity as Integer.MIN_VALUE
    @Test
    void testCalculateTotal_quantityIntegerMinValue() throws SQLException {
        Item item = new Item("A001", "Apple", 1.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        Map<String, Integer> purchased = Map.of("A001", Integer.MIN_VALUE);
        double total = billingService.calculateTotal(purchased);
        assertEquals((double) Integer.MIN_VALUE, total);
    }

    // 58. createBill called twice with same parameters returns consistent serial numbers (mock)
    @Test
    void testCreateBill_multipleCallsConsistentBehavior() throws SQLException {
        Item item = new Item("A001", "Apple", 10.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(10.0, "No Discount"));
        when(billDAO.saveBill(any())).thenReturn(1);

        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 20.0);
        billingService.createBill(Map.of("A001", 1), 20.0);

        verify(billDAO, times(2)).saveBill(any());
    }

    // 59. createBill cashTendered exactly zero with nonzero total (change negative)
    @Test
    void testCreateBill_zeroCashTenderedNonZeroTotal() throws SQLException {
        Item item = new Item("A001", "Apple", 20.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(new DiscountResult(20.0, "No Discount"));
        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        billingService.createBill(Map.of("A001", 1), 0.0);

        verify(billDAO).saveBill(any());
    }

    // 60. createBill with null discount name (verify no NPE)
    @Test
    void testCreateBill_nullDiscountName() throws SQLException {
        Item item = new Item("A001", "Apple", 10.0);
        when(itemService.getItemByCode("A001")).thenReturn(item);
        when(stockService.allocateStock("A001", 1)).thenReturn(List.of(new StockEntry("A001", 1, dummyDate, dummyDate)));

        DiscountResult discountResult = new DiscountResult(10.0, null);
        when(discountContext.applyDiscounts(any(), anyDouble())).thenReturn(discountResult);

        when(billDAO.saveBill(any())).thenReturn(1);
        doNothing().when(billItemDAO).saveBillItems(anyInt(), anyList());

        // Should not throw NullPointerException even if discountName is null
        billingService.createBill(Map.of("A001", 1), 20.0);

        verify(billDAO).saveBill(any());
    }

}
