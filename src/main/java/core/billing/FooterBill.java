package core.billing;

import java.text.SimpleDateFormat;

public class FooterBill extends BillDecorator {
    public FooterBill(Bill decoratedBill) {
        super(decoratedBill);
    }

    @Override
    public String print() {
        String base = super.print();

        String cashLine = String.format("Cash Tendered: %.2f\n", getCashTendered());
        String changeLine = String.format("Change: %.2f\n", getChangeDue());

        String footer = "\n" + cashLine + changeLine +
                "Thank you for shopping with us!\nDate: " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        return base + footer;
    }
}
