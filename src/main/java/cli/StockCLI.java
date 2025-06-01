package cli;

import cli.menus.StockCLIHandler;
import core.dao.ItemDAO;
import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.services.StockService;
import core.facade.StockFacade;

import java.sql.Connection;
import java.util.Scanner;

public class StockCLI {
    private final StockCLIHandler handler;
    private final Scanner sc = new Scanner(System.in);

    public StockCLI(Connection conn) {
        ItemDAO itemRepo = new ItemDAO(conn);
        ItemService itemService = new ItemService(itemRepo);
        StockService stockService = new StockService(conn, itemService);
        ReorderNotifier reorderNotifier = new ReorderNotifier(itemService);
        StockFacade stockFacade = new StockFacade(itemService, stockService, reorderNotifier);

        this.handler = new StockCLIHandler(stockFacade);
    }

    public void showMenu() {
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
