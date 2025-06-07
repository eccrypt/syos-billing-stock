package cli.menus;

import core.models.Item;
import core.models.Shelf;
import core.services.ItemService;
import core.facade.StockFacade;
import core.services.ShelfService;

import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ItemCLIHandler {
    private final ItemService itemService;
    private final ShelfService shelfService;
    private final StockFacade stockFacade;
    private final Scanner scanner;

    // Constructor that accepts a custom Scanner
    public ItemCLIHandler(ItemService itemService, ShelfService shelfService, StockFacade stockFacade, Scanner scanner) {
        this.itemService = itemService;
        this.shelfService = shelfService;
        this.stockFacade = stockFacade;
        this.scanner = scanner;
    }

    // Default constructor using a new Scanner
    public ItemCLIHandler(ItemService itemService, ShelfService shelfService, StockFacade stockFacade) {
        this(itemService, shelfService, stockFacade, new Scanner(System.in));
    }

    public void handleViewAllItems() throws SQLException {
        List<Item> items = itemService.getAllItems();
        if (items.isEmpty()) {
            System.out.println("No items found.");
        } else {
            System.out.println("\n📦 All Items:");
            System.out.println("+------------+--------------------------+------------+");
            System.out.println("| Item Code  | Item Name                | Price (Rs) |");
            System.out.println("+------------+--------------------------+------------+");

            for (Item item : items) {
                System.out.printf("| %-10s | %-24s | %10.2f |\n",
                        item.getCode(), item.getName(), item.getPrice());
            }

            System.out.println("+------------+--------------------------+------------+");
        }
    }

    public void handleSearchItem() throws SQLException {
        System.out.print("Enter item code: ");
        String code = scanner.nextLine();
        Item item = itemService.getItemByCode(code);

        if (item == null) {
            System.out.println("❌ Item not found.");
        } else {
            System.out.println("\n📦 Item Details:");
            System.out.println("+------------+--------------------------+------------+");
            System.out.println("| Item Code  | Item Name                | Price (Rs) |");
            System.out.println("+------------+--------------------------+------------+");

            System.out.printf("| %-10s | %-24s | %10.2f |\n",
                    item.getCode(), item.getName(), item.getPrice());

            System.out.println("+------------+--------------------------+------------+");
        }
    }

    public void handleAddItem() throws SQLException, ParseException {
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter item price: ");
        double price = Double.parseDouble(scanner.nextLine());

        System.out.println("\nThis Shelf is allocated for this Item");
        System.out.print("Enter default shelf quantity: ");
        int shelfDefault = Integer.parseInt(scanner.nextLine());

        System.out.println("\nFirst Stock Batch Creation");
        System.out.print("Enter Quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.println("Entry Date Will be the Current Date");
        LocalDate currentDate = LocalDate.now();
        String entryDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.print("Enter Expiry Date (yyyy-MM-dd): ");
        String expiryDate = scanner.nextLine();

        Item newItem = new Item(name, price);
        itemService.addItem(newItem, shelfDefault, 0);
        System.out.println("✅ Item added with product code: " + newItem.getCode());

        stockFacade.stockItem(newItem.getCode(), quantity, entryDate, expiryDate);

        Shelf newShelf = new Shelf(newItem.getCode(), shelfDefault, shelfDefault);
        stockFacade.getShelfService().addShelf(newShelf);
        System.out.println("✅ Shelf created with default quantity: " + shelfDefault + ".");

        newShelf.setShelfCurrent(newShelf.getShelfDefault());
        int reducedStockBatchQuantity = quantity - newShelf.getShelfDefault();

        if (reducedStockBatchQuantity > 0) {
            stockFacade.updateStockEntry(newItem.getCode(), reducedStockBatchQuantity, expiryDate);
        }

        stockFacade.getShelfService().updateShelf(newShelf);
        System.out.println("✅ Shelf current quantity updated to: " + newShelf.getShelfCurrent());
    }

    public void handleUpdateItem() throws SQLException {
        System.out.print("Enter item code: ");
        String code = scanner.nextLine();
        System.out.print("Enter new name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new price: ");
        double price = Double.parseDouble(scanner.nextLine());

        itemService.updateItem(new Item(code, name, price));
        System.out.println("✅ Item updated.");
    }

    public void handleUpdateItemName() throws SQLException {
        System.out.print("Enter item code: ");
        String code = scanner.nextLine();
        System.out.print("Enter new name: ");
        String name = scanner.nextLine();

        itemService.updateItemName(code, name);
        System.out.println("✅ Item name updated.");
    }

    public void handleUpdateItemPrice() throws SQLException {
        System.out.print("Enter item code: ");
        String code = scanner.nextLine();
        System.out.print("Enter new price: ");
        double price = Double.parseDouble(scanner.nextLine());

        itemService.updateItemPrice(code, price);
        System.out.println("✅ Item price updated.");
    }

    public void handleDeleteItem() throws SQLException {
        System.out.print("Enter item code: ");
        String code = scanner.nextLine();
        itemService.deleteItem(code);
        System.out.println("✅ Item deleted.");
    }

    public void handleAllocateStock() throws SQLException {
        System.out.print("Enter item code for allocation: ");
        String itemCode = scanner.nextLine();
        System.out.print("Enter quantity to allocate: ");
        int quantity = Integer.parseInt(scanner.nextLine());

        stockFacade.allocateStock(itemCode, quantity);
    }

    public void handleViewStockLevel() throws SQLException {
        System.out.print("Enter item code to check stock level: ");
        String itemCode = scanner.nextLine();
        stockFacade.printStockLevel(itemCode);
    }

    public void handleCheckReorderAlerts() throws SQLException {
        stockFacade.checkAndPrintReorderAlerts();
    }
}
