package core.models;

public class Item {
    private String code;
    private String name;
    private double price;

    // Constructor that doesn't require code (auto-generated)
    public Item(String name, double price) {
        this.code = "";  // Placeholder, will be generated later
        this.name = name;
        this.price = price;
    }

    // Constructor that includes code (for fetching items from DB)
    public Item(String code, String name, double price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    public void setCode(String code) {
        this.code = code;
    }
}