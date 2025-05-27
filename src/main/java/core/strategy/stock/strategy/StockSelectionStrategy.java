package core.strategy.stock.strategy;

import core.models.StockEntry;
import java.util.List;

public interface StockSelectionStrategy {
    List<StockEntry> selectStock(List<StockEntry> availableEntries, int quantityNeeded);
}
