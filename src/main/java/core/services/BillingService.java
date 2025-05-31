package core.services;

import core.billing.Bill;
import core.billing.BillBuilder;
import core.dao.BillDAO;
import core.dao.BillItemDAO;
import core.dao.ItemDAO;
import core.models.BillItem;
import core.models.Item;
import core.models.StockEntry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class BillingService {
    private final ItemService itemService;
    private final BillDAO billDAO;
    private final BillItemDAO billItemDAO;
    private final StockService stockService;

    public BillingService(Connection conn) {
        this.itemService = new ItemService(new ItemDAO(conn));
        this.billDAO = new BillDAO(conn);
        this.billItemDAO = new BillItemDAO(conn);
        this.stockService = new StockService(conn, this.itemService);
    }

    public double calculateTotal(Map<String, Integer> purchasedItems) throws SQLException {
        double total = 0;
        for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
            Item item = itemService.getItemByCode(entry.getKey());
            if (item == null) {
                throw new SQLException("Item not found: " + entry.getKey());
            }
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

            Item item = itemService.getItemByCode(itemCode);
            if (item == null) {
                throw new SQLException("Item not found: " + itemCode);
            }

            double itemTotal = item.getPrice() * quantityNeeded;
            total += itemTotal;

            // Allocate and reduce batch-based stock by expiry date
            List<StockEntry> allocated = stockService.allocateStock(itemCode, quantityNeeded);

            int totalAllocated = allocated.stream().mapToInt(StockEntry::getQuantity).sum();
            if (totalAllocated < quantityNeeded) {
                throw new SQLException("❌ Insufficient stock for item: " + itemCode);
            }

            billItems.add(new BillItem(item.getCode(), item.getName(), quantityNeeded, itemTotal));
        }

        double netTotal = total - discount;
        double change = cashTendered - netTotal;

        // Build and print decorated bill
        Bill decoratedBill = BillBuilder.build(total, discount, cashTendered, change, billItems);
        System.out.println(decoratedBill.print());

        // Persist bill
        int billId = billDAO.saveBill(new core.models.Bill(0, new Date(), total, discount, cashTendered, change, billItems));
        billItemDAO.saveBillItems(billId, billItems);
    }
}
