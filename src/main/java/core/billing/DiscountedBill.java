package core.billing;

public class DiscountedBill extends BillDecorator {
    public DiscountedBill(Bill decoratedBill) {
        super(decoratedBill);
    }

    @Override
    public String print() {
        double netTotal = getTotal() - getDiscount();
        return super.print() + "Discount: " + getDiscount() + "\n" + "Net Total: " + netTotal + "\n";
    }
}
