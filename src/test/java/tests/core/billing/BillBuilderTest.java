package tests.core.billing;

import core.billing.BillBuilder;
import core.billing.BasicBill;
import core.models.BillItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BillBuilderTest {

    private BillBuilder billBuilder;

    @BeforeEach
    void setUp() {
        billBuilder = new BillBuilder();
    }

    @Test
    void testBuild() {
        BillItem item1 = new BillItem("ITEM001", "Item 1", 2, 20.0);
        BillItem item2 = new BillItem("ITEM002", "Item 2", 1, 50.0);

        var bill = billBuilder.build(70.0, 10.0, 60.0, 10.0, Arrays.asList(item1, item2), 12345);

        assertTrue(bill instanceof BasicBill);
        assertTrue(bill.print().contains("Discount"));
        assertTrue(bill.print().contains("Net Total"));
    }
}
