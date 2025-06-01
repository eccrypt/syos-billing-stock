package core.billing;

import core.models.Bill;

import java.text.SimpleDateFormat;

public class FooterBill extends BillDecorator {
    public FooterBill(Bill decoratedBill) {
        super(decoratedBill);
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();

        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getBillDate());

        sb.append("=== BILL ===\n");
        sb.append("Bill No: ").append(getSerialNumber()).append("\n");
        sb.append("Date: ").append(dateStr).append("\n\n");
        sb.append(super.print());
        sb.append(String.format("Cash Tendered: %.2f\n", getCashTendered()));
        sb.append(String.format("Change: %.2f\n", getChangeDue()));
        sb.append("Thank you for shopping with us!");

        return sb.toString();
    }
}
