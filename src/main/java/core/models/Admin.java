package core.models;


public class Admin extends User {
    public Admin(int id, String username) {
        super(id, username, "admin");
    }
    // Add admin-specific methods or fields if needed
}