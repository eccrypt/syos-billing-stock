package core.models;

public class BillItem {
    private String itemCode;
    private String itemName;
    private int quantity;
    private double totalPrice;

    public BillItem(String itemCode, String itemName, int quantity, double totalPrice) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
}