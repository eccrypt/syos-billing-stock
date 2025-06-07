package cli.menus;

import core.facade.StockFacade;

import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class StockCLIHandler {
    private final StockFacade stockFacade;
    private final Scanner sc;

    // ✅ Default constructor (used in production)
    public StockCLIHandler(StockFacade stockFacade) {
        this(stockFacade, new Scanner(System.in));
    }

    // ✅ Test constructor (allows injection of a mock Scanner)
    public StockCLIHandler(StockFacade stockFacade, Scanner scanner) {
        this.stockFacade = stockFacade;
        this.sc = scanner;
    }

    public void handleAddStockEntry() {
        System.out.print("Enter Item Code: ");
        String code = sc.nextLine();

        System.out.print("Enter Quantity: ");
        int quantity = Integer.parseInt(sc.nextLine());

        System.out.println("Entry Date Will be the Current Date");
        LocalDate currentDate = LocalDate.now();
        String entryDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.print("Enter Expiry Date (yyyy-MM-dd): ");
        String expiryDate = sc.nextLine();

        stockFacade.stockItem(code, quantity, entryDate, expiryDate);
    }

    public void handleAllocateStock() {
        System.out.print("Enter Item Code: ");
        String code = sc.nextLine();

        System.out.print("Enter Quantity to Allocate: ");
        int quantity = Integer.parseInt(sc.nextLine());

        stockFacade.allocateStock(code, quantity);
    }

    public void handleViewAllStockEntries() {
        stockFacade.printAllStockEntries();
    }

    public void handleCheckReorderAlerts() {
        stockFacade.checkAndPrintReorderAlerts();
    }

    public void handleViewStockLevel() {
        System.out.print("Enter Item Code: ");
        String code = sc.nextLine();
        stockFacade.printStockLevel(code);
    }

    public void handleUpdateStockEntry() throws SQLException, ParseException {
        System.out.print("Enter Item Code to update: ");
        String itemCode = sc.nextLine();

        System.out.print("Enter New Quantity: ");
        int quantity = Integer.parseInt(sc.nextLine());

        System.out.print("Enter New Expiry Date (yyyy-MM-dd): ");
        String expiryDate = sc.nextLine();

        stockFacade.updateStockEntry(itemCode, quantity, expiryDate);
    }

    public void handleDeleteStockEntry() {
        System.out.print("Enter Stock Entry ID to delete: ");
        int entryId = Integer.parseInt(sc.nextLine());

        stockFacade.deleteStockEntry(entryId);
    }
}
