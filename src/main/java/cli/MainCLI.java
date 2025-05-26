package cli;

import core.dao.UserDAO;
import core.models.User;
import core.utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class MainCLI {
    private final Scanner sc = new Scanner(System.in);
    private final UserDAO userDAO;
    private final Connection connection;

    public MainCLI(Connection connection) {
        this.connection = connection;
        this.userDAO = new UserDAO(connection);
    }

    public void start() {
        while (true) {
            System.out.println("\n=== SYOS CLI ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.print("Enter Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Role (admin/customer/employee): ");
        String role = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        boolean success = userDAO.registerUser(username, role, password);
        if (success) {
            System.out.println("✅ User registered successfully!");
        } else {
            System.out.println("❌ Registration failed.");
        }
    }

    private void login() {
        System.out.print("Enter Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        User user = userDAO.authenticateUser(username, password);

        if (user != null) {
            System.out.println("✅ Welcome, " + user.getUsername() + " (" + user.getRole() + ")");

            // Type-checking to verify the factory pattern works
            if (user instanceof core.models.Employee) {
                System.out.println("[DEBUG] User is an instance of Employee class.");
            } else if (user instanceof core.models.Admin) {
                System.out.println("[DEBUG] User is an instance of Admin class.");
            } else if (user instanceof core.models.Customer) {
                System.out.println("[DEBUG] User is an instance of Customer class.");
            } else {
                System.out.println("[DEBUG] User is a generic User instance.");
            }

            if (user.getRole().equalsIgnoreCase("employee")) {
                BillingCLI billingCLI = new BillingCLI(user, connection);
                billingCLI.startBilling();
            } else {
                System.out.println("🔒 Access to billing is only for employees.");
            }
        } else {
            System.out.println("❌ Invalid credentials.");
        }

    }

    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            new MainCLI(conn).start();
        } catch (SQLException e) {
            System.err.println("❌ Database connection error: " + e.getMessage());
        }
    }
}
