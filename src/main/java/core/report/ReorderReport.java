package core.report;

import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.models.Item;

import java.util.Map;

public class ReorderReport implements ReportTemplate {
    private final ReorderNotifier reorderNotifier;
    private final ItemService itemService;

    public ReorderReport(ReorderNotifier reorderNotifier, ItemService itemService) {
        this.reorderNotifier = reorderNotifier;
        this.itemService = itemService;
    }

    @Override
    public void generate() {
        System.out.println("=== Reorder Level Report ===");
        Map<String, Integer> lowStockItems = reorderNotifier.getReorderItems();

        if (lowStockItems.isEmpty()) {
            System.out.println("All stock levels are sufficient.");
            return;
        }

        System.out.printf("%-10s %-25s %-10s%n", "Item Code", "Item Name", "Quantity");
        System.out.println("--------------------------------------------------");

        lowStockItems.forEach((code, qty) -> {
            try {
                Item item = itemService.getItemByCode(code);
                System.out.printf("%-10s %-25s %-10d%n", item.getCode(), item.getName(), qty);
            } catch (Exception e) {
                System.out.println("Error loading item: " + code);
            }
        });
    }
}
