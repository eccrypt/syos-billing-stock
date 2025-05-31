package cli;

import core.models.User;
import core.services.AuthenticationService;

import java.sql.Connection;
import java.util.Scanner;

public class ApplicationCLI {
    private final Scanner sc = new Scanner(System.in);
    private final AuthenticationCLI authCLI;
    private final EmployeeCLI employeeCLI;

    public ApplicationCLI(AuthenticationService authService, Connection connection) {
        this.authCLI = new AuthenticationCLI(sc, authService);
        this.employeeCLI = new EmployeeCLI(sc, connection);
    }

    public void start() {
        while (true) {
            System.out.println("\n=== SYOS CLI ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");

            String choice = sc.nextLine();

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

    private void routeToCLI(User user) {
        switch (user.getRole().toLowerCase()) {
            case "employee" -> employeeCLI.menu(user);
            case "admin" -> System.out.println("Admin CLI not implemented yet.");
            case "customer" -> System.out.println("Customer CLI not implemented yet.");
            default -> System.out.println("⚠ Unknown role: " + user.getRole());
        }
    }

}
