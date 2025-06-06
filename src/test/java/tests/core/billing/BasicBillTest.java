package tests.core.billing;

import core.billing.BasicBill;
import core.models.BillItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicBillTest {

    private BasicBill basicBill;

    @BeforeEach
    void setUp() {
        BillItem item1 = new BillItem("ITEM001", "Item 1", 2, 20.0);
        BillItem item2 = new BillItem("ITEM002", "Item 2", 1, 50.0);
        basicBill = new BasicBill(70.0, 10.0, 60.0, 10.0, Arrays.asList(item1, item2), 12345);
    }

    @Test
    void testPrint() {
        String expected = "Item 1 x2 = 40.0\nItem 2 x1 = 50.0\nTotal: 70.0\n";
        String actual = basicBill.print();

        assertEquals(expected, actual);
    }
}
