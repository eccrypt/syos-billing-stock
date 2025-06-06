package tests.core.billing;

import core.billing.BasicBill;
import core.billing.BillDecorator;
import core.billing.DiscountedBill;
import core.billing.FooterBill;
import core.models.BillItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BillDecoratorTest {

    private BillDecorator decoratedBill;

    @BeforeEach
    void setUp() {
        BillItem item1 = new BillItem("ITEM001", "Item 1", 2, 20.0);
        BillItem item2 = new BillItem("ITEM002", "Item 2", 1, 50.0);

        decoratedBill = new FooterBill(new DiscountedBill(new BasicBill(70.0, 10.0, 60.0, 10.0, Arrays.asList(item1, item2), 12345)));
    }

    @Test
    void testPrint() {
        String result = decoratedBill.print();

        assertTrue(result.contains("Discount"));
        assertTrue(result.contains("Net Total"));
        assertTrue(result.contains("Thank you for shopping with us!"));
    }
}
