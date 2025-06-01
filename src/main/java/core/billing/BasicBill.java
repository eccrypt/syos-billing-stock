package core.billing;

import core.models.Bill;
import core.models.BillItem;

import java.util.Date;
import java.util.List;

public class BasicBill extends Bill {

    public BasicBill(double total, double discount, double cashTendered, double changeDue, List<BillItem> items, int serialNumber) {
        super(0, serialNumber, new Date(), total, discount, cashTendered, changeDue, items);
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        for (BillItem item : getItems()) {
            sb.append(item.getItemName())
                    .append(" x").append(item.getQuantity())
                    .append(" = ").append(item.getTotalPrice()).append("\n");
        }
        sb.append("Total: ").append(getTotal()).append("\n");
        return sb.toString();
    }
}
