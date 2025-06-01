package core.discount;

import core.models.Bill;

public class BulkPurchaseDiscount extends BaseDiscountHandler {
    @Override
    public DiscountResult applyDiscount(Bill bill, double total) {
        if (bill.getTotalQuantity() >= 10) {
            double discounted = total * 0.90;
            return new DiscountResult(discounted, "📦 Bulk Purchase Discount Applied (10%)");
        }
        return super.applyDiscount(bill, total);
    }
}
