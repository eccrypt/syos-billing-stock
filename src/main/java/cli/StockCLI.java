package cli;

import core.dao.StockEntryDAO;
import core.models.StockEntry;
import core.strategy.stock.StockAllocator;
import core.strategy.stock.strategy.ExpiryAwareStockSelectionStrategy;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class StockCLI {
    private final Connection conn;
    private final StockEntryDAO stockEntryDAO;
    private final Scanner sc = new Scanner(System.in);

    public StockCLI(Connection conn) {
        this.conn = conn;
        this.stockEntryDAO = new StockEntryDAO(conn);
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== Stock Management ===");
            System.out.println("1. Add Stock Entry");
            System.out.println("2. Allocate Stock (Expiry-aware)");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    addStockEntry();
                    break;
                case "2":
                    allocateStock();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void addStockEntry() {
        try {
            System.out.print("Enter item code: ");
            String itemCode = sc.nextLine();
            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(sc.nextLine());
            System.out.print("Enter entry date (yyyy-MM-dd): ");
            Date entryDate = parseDate(sc.nextLine());
            System.out.print("Enter expiry date (yyyy-MM-dd): ");
            Date expiryDate = parseDate(sc.nextLine());

            StockEntry entry = new StockEntry(itemCode, quantity, entryDate, expiryDate);
            stockEntryDAO.insertStockEntry(entry);
            System.out.println("✅ Stock entry added successfully.");

        } catch (SQLException | ParseException | NumberFormatException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void allocateStock() {
        try {
            System.out.print("Enter item code: ");
            String itemCode = sc.nextLine();
            System.out.print("Enter quantity to allocate: ");
            int quantity = Integer.parseInt(sc.nextLine());

            List<StockEntry> available = stockEntryDAO.getAvailableStock(itemCode);
            StockAllocator allocator = new StockAllocator(new ExpiryAwareStockSelectionStrategy());
            List<StockEntry> allocated = allocator.allocate(available, quantity);

            if (allocated.isEmpty()) {
                System.out.println("⚠️ Not enough stock available.");
                return;
            }

            System.out.println("Allocated stock:");
            for (StockEntry entry : allocated) {
                System.out.println("- " + entry.getQuantity() + " units (Expires: " + entry.getExpiryDate() + ")");
                stockEntryDAO.reduceStockEntry(entry, entry.getQuantity());
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private Date parseDate(String input) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(input);
    }
}
