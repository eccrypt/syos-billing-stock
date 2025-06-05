package core.discount;

import core.models.Bill;

public class NoDiscount extends BaseDiscountHandler {
    @Override
    public DiscountResult applyDiscount(Bill bill, double total) {
        if (bill == null) {
            throw new NullPointerException("Bill cannot be null");
        }
        // no discount applied
        return new DiscountResult(total, "No discount applied.");
    }
}
