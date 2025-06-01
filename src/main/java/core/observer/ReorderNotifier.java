package core.observer;

import core.models.Item;
import core.services.ItemService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ReorderNotifier implements StockObserver {

    private final ItemService itemService;
    private final Map<String, Integer> itemQuantities = new HashMap<>();
    private final int REORDER_THRESHOLD = 50;

    public ReorderNotifier(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public void update(String itemCode, int newQuantity) {
        itemQuantities.put(itemCode, newQuantity);

        if (newQuantity < REORDER_THRESHOLD) {
            try {
                Item item = itemService.getItemByCode(itemCode);
                if (item != null) {
                    System.out.printf("⚠️ Reorder Alert: [%s - %s] only has %d left in stock!\n",
                            item.getCode(), item.getName(), newQuantity);
                }
            } catch (SQLException e) {
                System.out.println("Failed to fetch item for reorder alert: " + itemCode);
            }
        }
    }

    public Map<String, Integer> getReorderItems() {
        Map<String, Integer> reorderItems = new HashMap<>();
        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            if (entry.getValue() < REORDER_THRESHOLD) {
                reorderItems.put(entry.getKey(), entry.getValue());
            }
        }
        return reorderItems;
    }
}
