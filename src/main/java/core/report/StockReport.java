package core.report;

import core.models.StockEntry;
import core.services.StockService;

import java.util.List;

public class StockReport implements ReportTemplate {
    private final StockService stockService;

    public StockReport(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public void generate() {
        System.out.println("=== Current Stock Report (Batch-wise) ===");

        try {
            List<StockEntry> stockEntries = stockService.getAllStockEntries();

            for (StockEntry entry : stockEntries) {
                System.out.printf("Item: %s | Qty: %d | Entry: %s | Expiry: %s%n",
                        entry.getItemCode(), entry.getQuantity(),
                        entry.getEntryDate(), entry.getExpiryDate());
            }

            if (stockEntries.isEmpty()) {
                System.out.println("No stock data available.");
            }

        } catch (Exception e) {
            System.out.println("❌ Failed to generate stock report: " + e.getMessage());
        }
    }
}
