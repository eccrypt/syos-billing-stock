package cli;

import core.models.User;
import core.services.AuthenticationService;

import java.util.Scanner;

public class AuthenticationCLI {
    private final Scanner sc;
    private final AuthenticationService authService;

    public AuthenticationCLI(Scanner sc, AuthenticationService authService) {
        this.sc = sc;
        this.authService = authService;
    }

    public void register() {
        System.out.print("Enter Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Role (admin/customer/employee): ");
        String role = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        boolean success = authService.registerUser(username, role, password);
        if (success) {
            System.out.println("✅ User registered successfully!");
        } else {
            System.out.println("❌ Registration failed.");
        }
    }

    public User login() {
        System.out.print("Enter Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        return authService.login(username, password);
    }
}
