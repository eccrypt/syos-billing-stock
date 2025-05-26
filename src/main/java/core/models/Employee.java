package core.models;

public class Employee extends User {
    public Employee(int id, String username) {
        super(id, username, "employee");
    }
    // Add employee-specific methods or fields if needed
}