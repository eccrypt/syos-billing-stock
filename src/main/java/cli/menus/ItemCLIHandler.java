package cli.menus;

import core.models.Item;
import core.services.ItemService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ItemCLIHandler {
    private final ItemService itemService;
    private final Scanner scanner = new Scanner(System.in);

    public ItemCLIHandler(ItemService itemService) {
        this.itemService = itemService;
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


    public void handleAddItem() throws SQLException {
        System.out.print("Enter item code: ");
        String code = scanner.nextLine();
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter item price: ");
        double price = Double.parseDouble(scanner.nextLine());

        itemService.addItem(new Item(code, name, price));
        System.out.println("✅ Item added.");
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
}
