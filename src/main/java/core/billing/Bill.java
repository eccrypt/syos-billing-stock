package core.billing;

public interface Bill {
    double getTotal();
    double getDiscount();
    double getCashTendered();
    double getChangeDue();
    String print(); // for CLI/receipt output
}
