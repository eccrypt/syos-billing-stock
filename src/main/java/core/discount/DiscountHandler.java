package core.discount;

import core.models.Bill;

public interface DiscountHandler {
    void setNext(DiscountHandler next);
    double applyDiscount(Bill bill, double total);
}
