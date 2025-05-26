package cli;

import core.models.User;
import core.services.BillingService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BillingCLI {
    private final Scanner sc = new Scanner(System.in);
    private final BillingService billingService;
    private final User user;

    public BillingCLI(User user, Connection conn) {
        this.user = user;
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

            billingService.createBill(purchasedItems, discount, cash);
            System.out.println("🧾 Bill generated successfully.");

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        } catch (NumberFormatException nfe) {
            System.err.println("❌ Invalid input. Try again.");
        }
    }
}
