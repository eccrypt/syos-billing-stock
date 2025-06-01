package core.discount;

import core.models.Bill;

public class DiscountContext {
    private final DiscountHandler chain;

    public DiscountContext() {
        // Chain setup: BulkPurchase -> Seasonal -> NoDiscount
        DiscountHandler bulk = new BulkPurchaseDiscount();
        DiscountHandler seasonal = new SeasonalDiscount();
        DiscountHandler end = new NoDiscount();

        bulk.setNext(seasonal);
        seasonal.setNext(end);

        this.chain = bulk;
    }

    public DiscountResult applyDiscounts(Bill bill, double total) {
        return chain.applyDiscount(bill, total);
    }
}
