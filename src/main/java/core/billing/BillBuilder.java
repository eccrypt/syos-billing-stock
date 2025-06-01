package core.billing;

import core.models.Bill;
import core.models.BillItem;

import java.util.List;

public class BillBuilder {
    public static Bill build(double total, double discount, double cashTendered, double changeDue, List<BillItem> items, int serialNumber) {
        Bill bill = new BasicBill(total, discount, cashTendered, changeDue, items, serialNumber);
        bill = new DiscountedBill(bill);  // applies discount formatting logic
        bill = new FooterBill(bill);      // appends footer or summary
        return bill;
    }
}
