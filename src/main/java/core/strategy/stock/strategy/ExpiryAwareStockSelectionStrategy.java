// File: core/strategy/stock/strategy/ExpiryAwareStockSelectionStrategy.java
package core.strategy.stock.strategy;

import core.models.StockEntry;

import java.util.*;

public class ExpiryAwareStockSelectionStrategy implements StockSelectionStrategy {
    @Override
    public List<StockEntry> selectStock(List<StockEntry> availableEntries, int quantityNeeded) {
        // Defensive copy and null-safe sort
        List<StockEntry> sorted = new ArrayList<>(availableEntries);
        sorted.removeIf(entry -> entry.getQuantity() <= 0);

        // Sort: First by expiry date, then by entry date
        sorted.sort(Comparator
                .comparing(StockEntry::getExpiryDate)
                .thenComparing(StockEntry::getEntryDate));

        List<StockEntry> selected = new ArrayList<>();
        int remaining = quantityNeeded;

        for (StockEntry entry : sorted) {
            int availableQty = entry.getQuantity();
            if (availableQty <= 0) continue;

            int usedQty = Math.min(availableQty, remaining);
            selected.add(new StockEntry(
                    entry.getItemCode(),
                    usedQty,
                    entry.getEntryDate(),
                    entry.getExpiryDate()
            ));

            remaining -= usedQty;
            if (remaining <= 0) break;
        }

        return selected;
    }
}
