package tests.core.userfactory;

import core.models.*;
import core.userfactory.UserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserFactoryTest {

    @Test
    public void testCreateAdminUser() {
        User user = UserFactory.createUser(1, "adminUser", "admin");
        assertTrue(user instanceof Admin, "Expected instance of Admin");
        assertEquals("adminUser", user.getUsername());
        assertEquals("admin", user.getRole());
    }

    @Test
    public void testCreateEmployeeUser() {
        User user = UserFactory.createUser(2, "employeeUser", "employee");
        assertTrue(user instanceof Employee, "Expected instance of Employee");
        assertEquals("employeeUser", user.getUsername());
        assertEquals("employee", user.getRole());
    }

    @Test
    public void testCreateCustomerUser() {
        User user = UserFactory.createUser(3, "customerUser", "customer");
        assertTrue(user instanceof Customer, "Expected instance of Customer");
        assertEquals("customerUser", user.getUsername());
        assertEquals("customer", user.getRole());
    }

    @Test
    public void testCreateUnknownRoleUser() {
        User user = UserFactory.createUser(4, "guestUser", "guest");
        assertTrue(user instanceof User && !(user instanceof Admin || user instanceof Employee || user instanceof Customer),
                "Expected default User instance");
        assertEquals("guest", user.getRole());
    }

    @Test
    public void testCreateUserWithNullRole() {
        User user = UserFactory.createUser(5, "nullUser", null);
        assertTrue(user instanceof User && !(user instanceof Admin || user instanceof Employee || user instanceof Customer),
                "Expected default User instance");
        assertEquals("unknown", user.getRole());
    }
}
