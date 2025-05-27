package core.billing;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FooterBill extends BillDecorator {
    public FooterBill(Bill decoratedBill) {
        super(decoratedBill);
    }

    @Override
    public String print() {
        String base = super.print();
        String footer = "\nThank you for shopping with us!\nDate: " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        return base + footer;
    }
}
