package cli.menus;

import core.models.StockEntry;
import core.services.StockService;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class StockCLIHandler {
    private final Scanner sc = new Scanner(System.in);
    private final StockService stockService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public StockCLIHandler(StockService stockService) {
        this.stockService = stockService;
    }

    public void handleAddStockEntry() {
        try {
            System.out.print("Enter item code: ");
            String itemCode = sc.nextLine();
            if (!stockService.itemExists(itemCode)) {
                System.out.println("❌ Item with code '" + itemCode + "' does not exist. Cannot create stock entry.");
                return;
            }

            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(sc.nextLine());

            System.out.print("Enter entry date (yyyy-MM-dd): ");
            String entryDate = sc.nextLine();

            System.out.print("Enter expiry date (yyyy-MM-dd): ");
            String expiryDate = sc.nextLine();

            stockService.addStockEntry(itemCode, quantity, entryDate, expiryDate);
            System.out.println("✅ Stock entry added successfully.");
        } catch (SQLException | ParseException | NumberFormatException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void handleAllocateStock() {
        try {
            System.out.print("Enter item code: ");
            String itemCode = sc.nextLine();

            System.out.print("Enter quantity to allocate: ");
            int quantity = Integer.parseInt(sc.nextLine());

            List<StockEntry> allocated = stockService.allocateStock(itemCode, quantity);
            if (allocated.isEmpty()) {
                System.out.println("⚠️ Not enough stock available.");
                return;
            }

            System.out.println("Allocated stock:");
            for (StockEntry entry : allocated) {
                System.out.println("- " + entry.getQuantity() + " units (Expires: " + dateFormat.format(entry.getExpiryDate()) + ")");
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void handleViewAllStockEntries() {
        try {
            List<StockEntry> entries = stockService.getAllStockEntries();
            if (entries.isEmpty()) {
                System.out.println("No stock entries found.");
                return;
            }

            System.out.println("\n📦 All Stock Entries:");
            System.out.println("----------------------------------------------------");
            System.out.printf("%-5s %-10s %-10s %-15s %-15s%n", "ID", "ItemCode", "Qty", "Entry Date", "Expiry Date");
            System.out.println("----------------------------------------------------");

            int id = 1;
            for (StockEntry entry : entries) {
                System.out.printf("%-5d %-10s %-10d %-15s %-15s%n",
                        id++,
                        entry.getItemCode(),
                        entry.getQuantity(),
                        dateFormat.format(entry.getEntryDate()),
                        dateFormat.format(entry.getExpiryDate())
                );
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to fetch stock entries: " + e.getMessage());
        }
    }

    public void handleUpdateStockEntry() {
        try {
            List<StockEntry> entries = stockService.getAllStockEntries();
            if (entries.isEmpty()) {
                System.out.println("No stock entries available to update.");
                return;
            }

            handleViewAllStockEntries();
            System.out.print("Enter the ID of the stock entry to update: ");
            int id = Integer.parseInt(sc.nextLine());

            if (id < 1 || id > entries.size()) {
                System.out.println("Invalid stock entry ID.");
                return;
            }

            StockEntry entry = entries.get(id - 1);

            System.out.println("Updating stock entry for item code: " + entry.getItemCode());

            System.out.print("Enter new quantity (current: " + entry.getQuantity() + "): ");
            int quantity = Integer.parseInt(sc.nextLine());

            System.out.print("Enter new entry date (yyyy-MM-dd) (current: " + dateFormat.format(entry.getEntryDate()) + "): ");
            Date entryDate = dateFormat.parse(sc.nextLine());

            System.out.print("Enter new expiry date (yyyy-MM-dd) (current: " + dateFormat.format(entry.getExpiryDate()) + "): ");
            Date expiryDate = dateFormat.parse(sc.nextLine());

            entry.setQuantity(quantity);
            entry.setEntryDate(entryDate);
            entry.setExpiryDate(expiryDate);

            stockService.updateStockEntry(entry);
            System.out.println("✅ Stock entry updated successfully.");
        } catch (SQLException | ParseException | NumberFormatException e) {
            System.out.println("❌ Error updating stock entry: " + e.getMessage());
        }
    }

    public void handleDeleteStockEntry() {
        try {
            List<StockEntry> entries = stockService.getAllStockEntries();
            if (entries.isEmpty()) {
                System.out.println("No stock entries available to delete.");
                return;
            }

            handleViewAllStockEntries();
            System.out.print("Enter the ID of the stock entry to delete: ");
            int id = Integer.parseInt(sc.nextLine());

            if (id < 1 || id > entries.size()) {
                System.out.println("Invalid stock entry ID.");
                return;
            }

            StockEntry entry = entries.get(id - 1);

            System.out.print("Are you sure you want to delete stock entry for item code '" + entry.getItemCode() + "'? (y/n): ");
            String confirm = sc.nextLine().trim().toLowerCase();

            if ("y".equals(confirm)) {
                stockService.deleteStockEntry(entry);
                System.out.println("✅ Stock entry deleted successfully.");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("❌ Error deleting stock entry: " + e.getMessage());
        }
    }
}
