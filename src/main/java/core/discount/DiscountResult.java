package core.discount;

public class DiscountResult {
    private final double totalAfterDiscount;
    private final String discountName;

    public DiscountResult(double totalAfterDiscount, String discountName) {
        this.totalAfterDiscount = totalAfterDiscount;
        this.discountName = discountName;
    }

    public double getTotalAfterDiscount() {
        return totalAfterDiscount;
    }

    public String getDiscountName() {
        return discountName;
    }
}
