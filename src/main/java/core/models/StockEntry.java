package core.models;

import java.util.Date;

public class StockEntry {
    private int id;
    private String itemCode;
    private int quantity;
    private Date entryDate;
    private Date expiryDate;

    // Full constructor (used when ID is known, like when loading from DB)
    public StockEntry(int id, String itemCode, int quantity, Date entryDate, Date expiryDate) {
        this.id = id;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.entryDate = entryDate;
        this.expiryDate = expiryDate;
    }

    // Lightweight constructor (used when inserting new entry)
    public StockEntry(String itemCode, int quantity, Date entryDate, Date expiryDate) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.entryDate = entryDate;
        this.expiryDate = expiryDate;
    }

    // Getters
    public int getId() { return id; }
    public String getItemCode() { return itemCode; }
    public int getQuantity() { return quantity; }
    public Date getEntryDate() { return entryDate; }
    public Date getExpiryDate() { return expiryDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setEntryDate(Date entryDate) { this.entryDate = entryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
}
