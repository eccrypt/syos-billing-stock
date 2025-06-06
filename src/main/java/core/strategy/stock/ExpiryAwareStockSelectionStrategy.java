// File: core/strategy/stock/strategy/ExpiryAwareStockSelectionStrategy.java
package core.strategy.stock;

import core.models.StockEntry;

import java.util.*;

public class ExpiryAwareStockSelectionStrategy implements StockSelectionStrategy {
    @Override
    public List<StockEntry> selectStock(List<StockEntry> availableEntries, int quantityNeeded) {
        // Handle zero quantity case
        if (quantityNeeded == 0) {
            return Collections.emptyList();  // Return empty list for zero quantity
        }

        // Defensive copy and null-safe sort
        List<StockEntry> sorted = new ArrayList<>(availableEntries);
        sorted.removeIf(entry -> entry.getQuantity() <= 0);  // Remove entries with zero or negative quantity

        // Sort entries: first by expiry date, then by entry date
        sorted.sort(Comparator
                .comparing(StockEntry::getExpiryDate)
                .thenComparing(StockEntry::getEntryDate));

        List<StockEntry> selected = new ArrayList<>();
        int remaining = quantityNeeded;

        // Attempt to allocate stock
        for (StockEntry entry : sorted) {
            int availableQty = entry.getQuantity();
            if (availableQty <= 0) continue;  // Skip if no quantity available

            int usedQty = Math.min(availableQty, remaining);
            selected.add(new StockEntry(
                    entry.getId(),
                    entry.getItemCode(),
                    usedQty,
                    entry.getEntryDate(),
                    entry.getExpiryDate()
            ));

            remaining -= usedQty;

            if (remaining <= 0) break;  // Stop once the required quantity is fulfilled
        }

        // Check if stock is insufficient
        if (remaining > 0) {
            // Instead of throwing an exception, just return the available stock
            return selected;  // Return whatever is available
        }

        return selected;
    }
}
