package core.userfactory;

import core.models.Admin;
import core.models.Customer;
import core.models.Employee;
import core.models.User;

public class UserFactory {

    public static User createUser(int id, String username, String role) {
        if (role == null) return new User(id, username, "unknown");
        switch (role.toLowerCase()) {
            case "admin":
                return new Admin(id, username);
            case "employee":
                return new Employee(id, username);
            case "customer":
                return new Customer(id, username);
            default:
                return new User(id, username, role);
        }
    }
}
