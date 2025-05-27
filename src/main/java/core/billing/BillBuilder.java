package core.billing;

import java.util.List;
import core.models.BillItem;

public class BillBuilder {
    public static Bill build(double total, double discount, double cash, double change, List<BillItem> items) {
        Bill bill = new BasicBill(total, discount, cash, change, items);
        bill = new DiscountedBill(bill);
        bill = new FooterBill(bill);
        return bill;
    }
}
