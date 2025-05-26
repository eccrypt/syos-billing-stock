package core.models;

public class Customer extends User {
    public Customer(int id, String username) {
        super(id, username, "customer");
    }
    // Add customer-specific methods or fields if needed
}