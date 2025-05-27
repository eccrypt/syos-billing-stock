package core.billing;

import java.util.List;
import core.models.BillItem;

public class BasicBill implements Bill {
    private double total;
    private double discount;
    private double cashTendered;
    private double changeDue;
    private List<BillItem> items;

    public BasicBill(double total, double discount, double cashTendered, double changeDue, List<BillItem> items) {
        this.total = total;
        this.discount = discount;
        this.cashTendered = cashTendered;
        this.changeDue = changeDue;
        this.items = items;
    }

    public double getTotal() { return total; }
    public double getDiscount() { return discount; }
    public double getCashTendered() { return cashTendered; }
    public double getChangeDue() { return changeDue; }
    public List<BillItem> getItems() { return items; }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder("=== BILL ===\n");
        for (BillItem item : items) {
            sb.append(item.getItemName())
                    .append(" x").append(item.getQuantity())
                    .append(" = ").append(item.getTotalPrice()).append("\n");
        }
        sb.append("Total: ").append(total).append("\n");
        return sb.toString();
    }
}
