package core.billing;

public abstract class BillDecorator implements Bill {
    protected Bill decoratedBill;

    public BillDecorator(Bill decoratedBill) {
        this.decoratedBill = decoratedBill;
    }

    public double getTotal() { return decoratedBill.getTotal(); }
    public double getDiscount() { return decoratedBill.getDiscount(); }
    public double getCashTendered() { return decoratedBill.getCashTendered(); }
    public double getChangeDue() { return decoratedBill.getChangeDue(); }
    public String print() { return decoratedBill.print(); }
}
