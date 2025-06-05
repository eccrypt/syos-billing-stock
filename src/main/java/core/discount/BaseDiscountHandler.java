package core.discount;

import core.models.Bill;

public abstract class BaseDiscountHandler implements DiscountHandler {
    protected DiscountHandler next;

    @Override
    public void setNext(DiscountHandler next) {
        this.next = next;
    }

    @Override
    public DiscountResult applyDiscount(Bill bill, double total) {
        if (bill == null) {
            throw new NullPointerException("Bill cannot be null");
        }
        return next != null ? next.applyDiscount(bill, total) : new DiscountResult(total, "No discount applied.");
    }


}
