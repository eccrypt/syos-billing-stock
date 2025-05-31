package core.billing;

import java.util.List;
import core.models.BillItem;

public class BasicBill implements Bill {
    private double total, discount, cashTendered, changeDue;
    private List<BillItem> items;
    private int serialNumber;

    public BasicBill(double total, double discount, double cashTendered, double changeDue, List<BillItem> items, int serialNumber) {
        this.total = total;
        this.discount = discount;
        this.cashTendered = cashTendered;
        this.changeDue = changeDue;
        this.items = items;
        this.serialNumber = serialNumber;
    }

    public double getTotal() { return total; }
    public double getDiscount() { return discount; }
    public double getCashTendered() { return cashTendered; }
    public double getChangeDue() { return changeDue; }
    public List<BillItem> getItems() { return items; }
    public int getSerialNumber() { return serialNumber; }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        for (BillItem item : items) {
            sb.append(item.getItemName())
                    .append(" x").append(item.getQuantity())
                    .append(" = ").append(item.getTotalPrice()).append("\n");
        }
        sb.append("Total: ").append(total).append("\n");
        return sb.toString();
    }

}

