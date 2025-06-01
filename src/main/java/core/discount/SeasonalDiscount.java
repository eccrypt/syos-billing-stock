package core.discount;

import core.models.Bill;

import java.time.LocalDate;

public class SeasonalDiscount extends BaseDiscountHandler {
    @Override
    public DiscountResult applyDiscount(Bill bill, double total) {
        LocalDate now = LocalDate.now();
        if (now.getMonthValue() == 12) {
            System.out.println("🎄 Seasonal discount applied (15%)");
            total *= 0.85;
            // Return DiscountResult with updated total and message
            return new DiscountResult(total, "Seasonal discount applied (15%)");
        }
        // Pass on to the next handler in the chain
        return super.applyDiscount(bill, total);
    }
}
