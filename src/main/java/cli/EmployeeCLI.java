package cli;

import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.User;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;
import core.facade.StockFacade;
import core.observer.ReorderNotifier;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

public class EmployeeCLI {
    private final Scanner sc;
    private final Connection connection;
    private final ItemCLI injectedItemCLI; // Optional injected CLI (for testing)

    // Production constructor
    public EmployeeCLI(Scanner sc, Connection connection) {
        this.sc = sc;
        this.connection = connection;
        this.injectedItemCLI = null;
    }

    // Test constructor (optional ItemCLI injection)
    public EmployeeCLI(Scanner sc, Connection connection, ItemCLI injectedItemCLI) {
        this.sc = sc;
        this.connection = connection;
        this.injectedItemCLI = injectedItemCLI;
    }

    public void menu(User user) throws SQLException, ParseException {
        while (true) {
            System.out.println("\n=== Employee Menu ===");
            System.out.println("1. Billing");
            System.out.println("2. Stock Management");
            System.out.println("3. Item Management");
            System.out.println("4. Generate Report");
            System.out.println("0. Logout");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> new BillingCLI(user, connection).startBilling();
                case "2" -> new StockCLI(connection).showMenu();
                case "3" -> {
                    if (injectedItemCLI != null) {
                        injectedItemCLI.start(); // Use mock in tests
                    } else {
                        // Normal execution
                        ShelfService shelfService = new ShelfService(new ShelfDAO(connection));
                        ItemService itemService = new ItemService(new ItemDAO(connection), shelfService);
                        StockService stockService = new StockService(connection, itemService, shelfService);
                        ReorderNotifier reorderNotifier = new ReorderNotifier(itemService);
                        StockFacade stockFacade = new StockFacade(itemService, stockService, shelfService, reorderNotifier);

                        new ItemCLI(itemService, shelfService, stockFacade).start();
                    }
                }
                case "4" -> new ReportCLI(sc, connection).showMenu(user);
                case "0" -> {
                    System.out.println("🔒 Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
