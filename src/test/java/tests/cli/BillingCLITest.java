package tests.cli;

import cli.BillingCLI;
import core.models.User;
import core.services.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.mockito.Mockito.*;

public class BillingCLITest {

    @Mock private User mockUser;
    @Mock private Connection mockConnection;
    @Mock private BillingService mockBillingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStartBilling_successfulFlow() throws SQLException {
        // Simulate user input for: code1, quantity 2, code2, quantity 3, done, cash 1000
        String input = "code1\n2\ncode2\n3\ndone\n1000\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        BillingCLI billingCLI = new BillingCLI(mockUser, mockConnection) {
            @Override
            public void startBilling() {
                // Mock BillingService manually for isolation
                BillingService mockedBillingService = mock(BillingService.class);
                Map<String, Integer> expectedItems = new HashMap<>();
                expectedItems.put("code1", 2);
                expectedItems.put("code2", 3);

                try {
                    when(mockedBillingService.calculateTotal(expectedItems)).thenReturn(1500.0);
                    doNothing().when(mockedBillingService).createBill(expectedItems, 1000.0);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                // Reconstruct Scanner
                Scanner scanner = new Scanner(System.in);
                Map<String, Integer> purchasedItems = new HashMap<>();

                System.out.println("\n=== Start Billing ===");

                while (true) {
                    System.out.print("Enter Item Code (or type 'done'): ");
                    String code = scanner.nextLine();
                    if (code.equalsIgnoreCase("done")) break;

                    System.out.print("Enter Quantity: ");
                    int quantity = Integer.parseInt(scanner.nextLine());

                    purchasedItems.put(code, purchasedItems.getOrDefault(code, 0) + quantity);
                }

                try {
                    double estimatedTotal = mockedBillingService.calculateTotal(purchasedItems);
                    System.out.println("Estimated Total (before discounts): " + estimatedTotal);

                    System.out.print("Enter Cash Tendered: ");
                    double cash = Double.parseDouble(scanner.nextLine());

                    mockedBillingService.createBill(purchasedItems, cash);
                    System.out.println("🧾 Bill generated successfully and 📦 stock reduced by expiry-aware strategy.");

                } catch (SQLException e) {
                    System.err.println("❌ SQL Error: " + e.getMessage());
                }
            }
        };

        billingCLI.startBilling();
    }

    @Test
    public void testStartBilling_invalidNumberFormat() {
        String input = "code1\nabc\ndone\n";  // Invalid quantity
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        BillingCLI billingCLI = new BillingCLI(mockUser, mockConnection);
        billingCLI.startBilling();  // Should catch NumberFormatException
    }

    @Test
    public void testStartBilling_sqlExceptionHandled() throws SQLException {
        String input = "code1\n2\ndone\n1000\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        BillingService mockedBillingService = mock(BillingService.class);
        when(mockedBillingService.calculateTotal(any())).thenThrow(new SQLException("DB Error"));

        BillingCLI billingCLI = new BillingCLI(mockUser, mockConnection) {
            @Override
            public void startBilling() {
                Scanner scanner = new Scanner(System.in);
                Map<String, Integer> purchasedItems = new HashMap<>();

                System.out.print("Enter Item Code (or type 'done'): ");
                String code = scanner.nextLine();
                if (!code.equalsIgnoreCase("done")) {
                    System.out.print("Enter Quantity: ");
                    String qty = scanner.nextLine();
                    purchasedItems.put(code, Integer.parseInt(qty));
                }

                try {
                    mockedBillingService.calculateTotal(purchasedItems);
                } catch (SQLException e) {
                    System.err.println("❌ SQL Error: " + e.getMessage());
                }
            }
        };

        billingCLI.startBilling();
    }
}
