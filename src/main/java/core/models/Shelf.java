package core.models;

public class Shelf {
    private int shelfId;
    private String productCode;
    private int shelfDefault;
    private int shelfCurrent;

    // Constructor for creating a new Shelf
    public Shelf(String productCode, int shelfDefault, int shelfCurrent) {
        this.productCode = productCode;
        this.shelfDefault = shelfDefault;
        this.shelfCurrent = shelfCurrent;
    }

    // Constructor for retrieving Shelf from DB (with shelfId)
    public Shelf(int shelfId, String productCode, int shelfDefault, int shelfCurrent) {
        this.shelfId = shelfId;
        this.productCode = productCode;
        this.shelfDefault = shelfDefault;
        this.shelfCurrent = shelfCurrent;
    }

    // Getters and Setters
    public int getShelfId() {
        return shelfId;
    }

    public void setShelfId(int shelfId) {
        this.shelfId = shelfId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getShelfDefault() {
        return shelfDefault;
    }

    public void setShelfDefault(int shelfDefault) {
        this.shelfDefault = shelfDefault;
    }

    public int getShelfCurrent() {
        return shelfCurrent;
    }

    public void setShelfCurrent(int shelfCurrent) {
        this.shelfCurrent = shelfCurrent;
    }
}
