package tests.core.billing;

import core.billing.FooterBill;
import core.billing.DiscountedBill;
import core.billing.BasicBill;
import core.models.BillItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FooterBillTest {

    private FooterBill footerBill;

    @BeforeEach
    void setUp() {
        BillItem item1 = new BillItem("ITEM001", "Item 1", 2, 20.0);
        BillItem item2 = new BillItem("ITEM002", "Item 2", 1, 50.0);

        footerBill = new FooterBill(new DiscountedBill(new BasicBill(70.0, 10.0, 60.0, 10.0, Arrays.asList(item1, item2), 12345)));
    }

    @Test
    void testPrint() {
        String result = footerBill.print();

        // Check that certain parts of the result are present
        assertTrue(result.contains("Serial Number"));
        assertTrue(result.contains("Total: 70.0"));
        assertTrue(result.contains("Discount: 10.0"));
        assertTrue(result.contains("Net Total: 60.0"));
        assertTrue(result.contains("Cash Tendered: 60.0"));
        assertTrue(result.contains("Change Due: 10.0"));
        assertTrue(result.contains("Thank you for shopping with us!"));
    }
}
