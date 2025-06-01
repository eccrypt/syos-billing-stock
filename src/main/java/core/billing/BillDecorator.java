package core.billing;

import core.models.Bill;

public abstract class BillDecorator extends Bill {
    protected Bill decoratedBill;

    public BillDecorator(Bill decoratedBill) {
        super(decoratedBill.getId(),
                decoratedBill.getSerialNumber(),
                decoratedBill.getBillDate(),
                decoratedBill.getTotal(),
                decoratedBill.getDiscount(),
                decoratedBill.getCashTendered(),
                decoratedBill.getChangeDue(),
                decoratedBill.getItems());
        this.decoratedBill = decoratedBill;
    }

    @Override
    public double getTotal() { return decoratedBill.getTotal(); }
    @Override
    public double getDiscount() { return decoratedBill.getDiscount(); }
    @Override
    public double getCashTendered() { return decoratedBill.getCashTendered(); }
    @Override
    public double getChangeDue() { return decoratedBill.getChangeDue(); }
    @Override
    public int getSerialNumber() { return decoratedBill.getSerialNumber(); }
    @Override
    public String print() { return decoratedBill.print(); }
}
