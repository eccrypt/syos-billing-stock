package cli.menus;

import core.facade.StockFacade;

import java.util.Scanner;

public class StockCLIHandler {
    private final StockFacade stockFacade;
    private final Scanner sc = new Scanner(System.in);

    public StockCLIHandler(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public void handleAddStockEntry() {
        System.out.print("Enter Item Code: ");
        String code = sc.nextLine();

        System.out.print("Enter Quantity: ");
        int quantity = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Entry Date (yyyy-MM-dd): ");
        String entryDate = sc.nextLine();

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

    public void handleUpdateStockEntry() {
        System.out.print("Enter Stock Entry ID to update: ");
        int entryId = Integer.parseInt(sc.nextLine());

        System.out.print("Enter New Quantity: ");
        int quantity = Integer.parseInt(sc.nextLine());

        System.out.print("Enter New Expiry Date (yyyy-MM-dd): ");
        String expiryDate = sc.nextLine();

        stockFacade.updateStockEntry(entryId, quantity, expiryDate);
    }

    public void handleDeleteStockEntry() {
        System.out.print("Enter Stock Entry ID to delete: ");
        int entryId = Integer.parseInt(sc.nextLine());

        stockFacade.deleteStockEntry(entryId);
    }
}
