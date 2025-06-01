package core.models;

import java.util.Date;
import java.util.List;

public abstract class Bill {
    private int id;
    private int serialNumber;
    private Date billDate;
    private double total;
    private double discount;
    private double cashTendered;
    private double changeDue;
    private List<BillItem> items;

    public Bill(int id, int serialNumber, Date billDate, double total, double discount,
                double cashTendered, double changeDue, List<BillItem> items) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.billDate = billDate;
        this.total = total;
        this.discount = discount;
        this.cashTendered = cashTendered;
        this.changeDue = changeDue;
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Date getBillDate() {
        return billDate;
    }

    public double getTotal() {
        return total;
    }

    public double getDiscount() {
        return discount;
    }

    public double getCashTendered() {
        return cashTendered;
    }

    public double getChangeDue() {
        return changeDue;
    }

    public List<BillItem> getItems() {
        return items;
    }

    /**
     * ✅ New method to support bulk discount logic
     */
    public int getTotalQuantity() {
        return items.stream().mapToInt(BillItem::getQuantity).sum();
    }

    public abstract String print();
}
