package core.services;

import core.billing.Bill;
import core.billing.BillBuilder;
import core.dao.BillDAO;
import core.dao.BillItemDAO;
import core.dao.ItemDAO;
import core.models.BillItem;
import core.models.Item;
import core.models.StockEntry;
import core.utils.SerialNumberGenerator;

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
            if (item == null) throw new SQLException("Item not found: " + itemCode);
            double itemTotal = item.getPrice() * quantityNeeded;
            total += itemTotal;

            List<StockEntry> allocated = stockService.allocateStock(itemCode, quantityNeeded);
            int totalAllocated = allocated.stream().mapToInt(StockEntry::getQuantity).sum();
            if (totalAllocated < quantityNeeded) throw new SQLException("❌ Insufficient stock for item: " + itemCode);

            billItems.add(new BillItem(item.getCode(), item.getName(), quantityNeeded, itemTotal));
        }

        double netTotal = total - discount;
        double change = cashTendered - netTotal;
        int serialNumber = SerialNumberGenerator.getNextSerial();

        Bill decoratedBill = BillBuilder.build(total, discount, cashTendered, change, billItems, serialNumber);
        System.out.println(decoratedBill.print());

        core.models.Bill bill = new core.models.Bill(0, serialNumber, new Date(), total, discount, cashTendered, change, billItems);
        bill.setSerialNumber(serialNumber);
        int billId = billDAO.saveBill(bill);
        billItemDAO.saveBillItems(billId, billItems);
    }

}
