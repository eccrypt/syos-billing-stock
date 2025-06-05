package cli;

import cli.menus.StockCLIHandler;
import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;
import core.facade.StockFacade;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

public class StockCLI {
    private final StockCLIHandler handler;
    private final Scanner sc = new Scanner(System.in);

    public StockCLI(Connection conn) {
        // Create the required DAOs and Services
        ItemDAO itemRepo = new ItemDAO(conn);  // ItemDAO
        ShelfService shelfService = new ShelfService(new ShelfDAO(conn));  // ShelfService

        // Create ItemService with both ItemDAO and ShelfService
        ItemService itemService = new ItemService(itemRepo, shelfService);  // Pass both ItemDAO and ShelfService

        // Create StockService, passing ShelfService and ItemService
        StockService stockService = new StockService(conn, itemService, shelfService);  // Pass ShelfService here
        ReorderNotifier reorderNotifier = new ReorderNotifier(itemService);

        // Create StockFacade, passing all the required services
        StockFacade stockFacade = new StockFacade(itemService, stockService, shelfService, reorderNotifier);

        // Initialize the handler with StockFacade
        this.handler = new StockCLIHandler(stockFacade);
    }

    public void showMenu() throws SQLException, ParseException {
        while (true) {
            System.out.println("\n=== Stock Management ===");
            System.out.println("1. Add Stock Entry");
            System.out.println("2. Allocate Stock (Expiry-aware)");
            System.out.println("3. View All Stock Entries");
            System.out.println("4. Check Reorder Alerts");
            System.out.println("5. View Stock Level");
            System.out.println("6. Update Stock Entry");
            System.out.println("7. Delete Stock Entry");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> handler.handleAddStockEntry();
                case "2" -> handler.handleAllocateStock();
                case "3" -> handler.handleViewAllStockEntries();
                case "4" -> handler.handleCheckReorderAlerts();
                case "5" -> handler.handleViewStockLevel();
                case "6" -> handler.handleUpdateStockEntry();
                case "7" -> handler.handleDeleteStockEntry();
                case "0" -> { return; }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
}
