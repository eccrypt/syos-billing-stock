package cli;

import core.dao.ItemDAO;
import core.models.User;
import core.services.ItemService;

import java.sql.Connection;
import java.util.Scanner;

public class EmployeeCLI {
    private final Scanner sc;
    private final Connection connection;

    public EmployeeCLI(Scanner sc, Connection connection) {
        this.sc = sc;
        this.connection = connection;
    }

    public void menu(User user) {
        while (true) {
            System.out.println("\n=== Employee Menu ===");
            System.out.println("1. Billing");
            System.out.println("2. Stock Management");
            System.out.println("3. Item Management");
            System.out.println("0. Logout");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> new BillingCLI(user, connection).startBilling();
                case "2" -> new StockCLI(connection).showMenu();
                case "3" -> {
                    ItemService itemService = new ItemService(new ItemDAO(connection));
                    new ItemCLI(itemService).start();
                }
                case "0" -> {
                    System.out.println("🔒 Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
