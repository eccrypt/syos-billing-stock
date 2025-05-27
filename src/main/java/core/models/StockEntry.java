package core.models;

import java.util.Date;

public class StockEntry {
    private String itemCode;
    private int quantity;
    private Date entryDate;
    private Date expiryDate;

    public StockEntry(String itemCode, int quantity, Date entryDate, Date expiryDate) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.entryDate = entryDate;
        this.expiryDate = expiryDate;
    }

    public String getItemCode() { return itemCode; }
    public int getQuantity() { return quantity; }
    public Date getEntryDate() { return entryDate; }
    public Date getExpiryDate() { return expiryDate; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
