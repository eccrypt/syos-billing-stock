package core.services;

import core.billing.Bill;
import core.billing.BillBuilder;
import core.dao.BillDAO;
import core.dao.BillItemDAO;
import core.dao.ItemDAO;
import core.dao.StockEntryDAO;
import core.models.BillItem;
import core.models.Item;
import core.models.StockEntry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class BillingService {
    private final ItemDAO itemDAO;
    private final BillDAO billDAO;
    private final BillItemDAO billItemDAO;
    private final StockEntryDAO stockEntryDAO;

    public BillingService(Connection conn) {
        this.itemDAO = new ItemDAO(conn);
        this.billDAO = new BillDAO(conn);
        this.billItemDAO = new BillItemDAO(conn);
        this.stockEntryDAO = new StockEntryDAO(conn);
    }

    public double calculateTotal(Map<String, Integer> purchasedItems) throws SQLException {
        double total = 0;
        for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
            Item item = itemDAO.getItemByCode(entry.getKey());
            total += item.getPrice() * entry.getValue();
        }
        return total;
    }

    public void createBill(Map<String, Integer> purchasedItems, double discount, double cashTendered) throws SQLException {
        List<BillItem> billItems = new ArrayList<>();
        double total = 0;

        for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
            String itemCode = entry.getKey();
            int quantityNeeded = entry.getValue();

            Item item = itemDAO.getItemByCode(itemCode);
            if (item == null) {
                throw new SQLException("Item not found: " + itemCode);
            }

            double itemTotal = item.getPrice() * quantityNeeded;
            total += itemTotal;

            // Reduce batch-based stock by expiry date
            List<StockEntry> batches = stockEntryDAO.getAvailableStock(itemCode);

            // Sort batches by expiry date (ascending) so soonest expiring batches are used first
            batches.sort(Comparator.comparing(StockEntry::getExpiryDate));

            int remaining = quantityNeeded;
            for (StockEntry batch : batches) {
                if (remaining <= 0) break;

                int reduceQty = Math.min(batch.getQuantity(), remaining);
                stockEntryDAO.reduceStockEntry(batch, reduceQty);
                remaining -= reduceQty;
            }

            if (remaining > 0) {
                throw new SQLException("❌ Insufficient stock for item: " + itemCode);
            }

            billItems.add(new BillItem(item.getCode(), item.getName(), quantityNeeded, itemTotal));
        }

        double netTotal = total - discount;
        double change = cashTendered - netTotal;

        // Build the final decorated bill
        Bill decoratedBill = BillBuilder.build(total, discount, cashTendered, change, billItems);

        // Print it to CLI
        System.out.println(decoratedBill.print());

        // Save bill and bill items
        int billId = billDAO.saveBill(new core.models.Bill(0, new Date(), total, discount, cashTendered, change, billItems));
        billItemDAO.saveBillItems(billId, billItems);
    }
}
