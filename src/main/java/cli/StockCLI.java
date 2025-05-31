package cli;

import cli.menus.StockCLIHandler;
import core.dao.ItemDAO;
import core.services.ItemService;
import core.services.StockService;

import java.sql.Connection;
import java.util.Scanner;

public class StockCLI {
    private final StockCLIHandler handler;
    private final Scanner sc = new Scanner(System.in);

    public StockCLI(Connection conn) {

        ItemDAO itemRepo = new ItemDAO(conn);
        ItemService itemService = new ItemService(itemRepo);
        StockService stockService = new StockService(conn, itemService);
        this.handler = new StockCLIHandler(stockService);
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== Stock Management ===");
            System.out.println("1. Add Stock Entry");
            System.out.println("2. Allocate Stock (Expiry-aware)");
            System.out.println("3. View All Stock Entries");
            System.out.println("4. Update Stock Entry");
            System.out.println("5. Delete Stock Entry");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> handler.handleAddStockEntry();
                case "2" -> handler.handleAllocateStock();
                case "3" -> handler.handleViewAllStockEntries();
                case "4" -> handler.handleUpdateStockEntry();
                case "5" -> handler.handleDeleteStockEntry();
                case "0" -> { return; }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
}
