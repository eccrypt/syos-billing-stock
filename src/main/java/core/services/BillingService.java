package core.services;

import core.billing.BillBuilder;
import core.billing.Bill;
import core.dao.BillDAO;
import core.dao.BillItemDAO;
import core.dao.ItemDAO;
import core.models.BillItem;
import core.models.Item;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class BillingService {
    private final ItemDAO itemDAO;
    private final BillDAO billDAO;
    private final BillItemDAO billItemDAO;

    public BillingService(Connection conn) {
        this.itemDAO = new ItemDAO(conn);
        this.billDAO = new BillDAO(conn);
        this.billItemDAO = new BillItemDAO(conn);
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

        // Prepare BillItems and calculate total
        for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
            Item item = itemDAO.getItemByCode(entry.getKey());
            int quantity = entry.getValue();
            double itemTotal = item.getPrice() * quantity;
            total += itemTotal;

            billItems.add(new BillItem(item.getCode(), item.getName(), quantity, itemTotal));
            itemDAO.updateItemQuantity(item.getCode(), quantity);
        }

        // Final amounts
        double netTotal = total - discount;
        double change = cashTendered - netTotal;

        // Build the decorated Bill
        Bill decoratedBill = BillBuilder.build(total, discount, cashTendered, change, billItems);

        // Print it
        System.out.println(decoratedBill.print());

        // Save to DB
        int billId = billDAO.saveBill(new core.models.Bill(0, new Date(), total, discount, cashTendered, change, billItems));
        billItemDAO.saveBillItems(billId, billItems);
    }
}
