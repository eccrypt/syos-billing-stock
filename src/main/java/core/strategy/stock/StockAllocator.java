package core.strategy.stock;

import core.models.StockEntry;

import java.util.List;

public class StockAllocator {
    private final StockSelectionStrategy strategy;

    public StockAllocator(StockSelectionStrategy strategy) {
        this.strategy = strategy;
    }

    public List<StockEntry> allocate(List<StockEntry> availableEntries, int quantityNeeded) {
        return strategy.selectStock(availableEntries, quantityNeeded);
    }
}
