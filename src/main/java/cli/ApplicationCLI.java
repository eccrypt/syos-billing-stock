package cli;

import core.models.User;
import core.services.AuthenticationService;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

public class ApplicationCLI {
    private final AuthenticationCLI authCLI;
    private final EmployeeCLI employeeCLI;
    private final Scanner sc;

    public ApplicationCLI(AuthenticationService authService, Connection connection, Scanner scanner) {
        this.sc = scanner;
        this.authCLI = new AuthenticationCLI(getScanner(), authService);
        this.employeeCLI = new EmployeeCLI(getScanner(), connection);
    }

    // Default constructor using System.in
    public ApplicationCLI(AuthenticationService authService, Connection connection) {
        this(authService, connection, new Scanner(System.in));
    }

    // Allow test override
    protected Scanner getScanner() {
        return sc;
    }

    public void start() throws SQLException, ParseException {
        while (true) {
            System.out.println("\n=== SYOS CLI ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");

            String choice = getScanner().nextLine();

            switch (choice) {
                case "1" -> authCLI.register();
                case "2" -> {
                    User user = authCLI.login();
                    if (user != null) {
                        System.out.printf("✅ Logged in as %s (%s)%n", user.getUsername(), user.getRole());
                        routeToCLI(user);
                    } else {
                        System.out.println("❌ Login failed.");
                    }
                }
                case "3" -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // Allow test override
    protected void routeToCLI(User user) throws SQLException, ParseException {
        switch (user.getRole().toLowerCase()) {
            case "employee" -> employeeCLI.menu(user);
            case "admin" -> System.out.println("Admin CLI not implemented yet.");
            case "customer" -> System.out.println("Customer CLI not implemented yet.");
            default -> System.out.println("⚠ Unknown role: " + user.getRole());
        }
    }
}
