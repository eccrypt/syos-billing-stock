package cli;

import core.dao.StockEntryDAO;
import core.models.StockEntry;
import core.models.User;
import core.services.BillingService;
import core.strategy.stock.StockAllocator;
import core.strategy.stock.strategy.ExpiryAwareStockSelectionStrategy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class BillingCLI {
    private final Scanner sc = new Scanner(System.in);
    private final BillingService billingService;
    private final User user;
    private final Connection conn;

    public BillingCLI(User user, Connection conn) {
        this.user = user;
        this.conn = conn;
        this.billingService = new BillingService(conn);
    }

    public void startBilling() {
        try {
            System.out.println("\n=== Start Billing ===");
            Map<String, Integer> purchasedItems = new HashMap<>();

            while (true) {
                System.out.print("Enter Item Code (or type 'done'): ");
                String code = sc.nextLine();
                if (code.equalsIgnoreCase("done")) break;

                System.out.print("Enter Quantity: ");
                int quantity = Integer.parseInt(sc.nextLine());

                purchasedItems.put(code, purchasedItems.getOrDefault(code, 0) + quantity);
            }

            System.out.print("Enter Discount (0 if none): ");
            double discount = Double.parseDouble(sc.nextLine());

            // Calculate total before payment
            double estimatedTotal = billingService.calculateTotal(purchasedItems);
            System.out.println("Estimated Total (before discount): " + estimatedTotal);

            System.out.print("Enter Cash Tendered: ");
            double cash = Double.parseDouble(sc.nextLine());

            // 1. Create bill
            billingService.createBill(purchasedItems, discount, cash);
            System.out.println("🧾 Bill generated successfully.");

            // 2. Allocate and reduce stock
            StockEntryDAO stockEntryDAO = new StockEntryDAO(conn);
            StockAllocator allocator = new StockAllocator(new ExpiryAwareStockSelectionStrategy());

            for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
                String itemCode = entry.getKey();
                int qty = entry.getValue();

                List<StockEntry> available = stockEntryDAO.getAvailableStock(itemCode);
                List<StockEntry> allocated = allocator.allocate(available, qty);

                for (StockEntry stockToReduce : allocated) {
                    stockEntryDAO.reduceStockEntry(stockToReduce, stockToReduce.getQuantity());
                }
            }

            System.out.println("📦 Stock successfully reduced based on expiry-aware allocation.");

        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
        } catch (NumberFormatException nfe) {
            System.err.println("❌ Invalid input. Try again.");
        }
    }
}
