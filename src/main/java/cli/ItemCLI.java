package cli;

import cli.menus.ItemCLIHandler;
import core.services.ItemService;

import java.sql.SQLException;
import java.util.Scanner;

public class ItemCLI {
    private final ItemCLIHandler handler;
    private final Scanner scanner = new Scanner(System.in);

    public ItemCLI(ItemService itemService) {
        this.handler = new ItemCLIHandler(itemService);
    }

    public void start() {
        while (true) {
            System.out.println("\n=== ITEM MANAGEMENT ===");
            System.out.println("1. View All Items");
            System.out.println("2. Search Item by Code");
            System.out.println("3. Add Item");
            System.out.println("4. Update Item");
            System.out.println("5. Update Item Name");
            System.out.println("6. Update Item Price");
            System.out.println("7. Delete Item");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1" -> handler.handleViewAllItems();
                    case "2" -> handler.handleSearchItem();
                    case "3" -> handler.handleAddItem();
                    case "4" -> handler.handleUpdateItem();
                    case "5" -> handler.handleUpdateItemName();
                    case "6" -> handler.handleUpdateItemPrice();
                    case "7" -> handler.handleDeleteItem();
                    case "0" -> {
                        System.out.println("Returning to main menu...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("❌ Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format. Please try again.");
            }
        }
    }
}
