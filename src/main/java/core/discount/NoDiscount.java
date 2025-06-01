package core.discount;

import core.models.Bill;

public class NoDiscount extends BaseDiscountHandler {
    @Override
    public DiscountResult applyDiscount(Bill bill, double total) {
        // No discount applied, return the original total and a message
        return new DiscountResult(total, "No discount applied.");
    }
}
