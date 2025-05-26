package core.services;

import core.dao.BillDAO;
import core.dao.BillItemDAO;
import core.dao.ItemDAO;
import core.models.Bill;
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
            int quantity = entry.getValue();
            total += item.getPrice() * quantity;
        }

        return total;
    }

    public void createBill(Map<String, Integer> purchasedItems, double discount, double cashTendered) throws SQLException {
        List<BillItem> billItems = new ArrayList<>();
        double total = 0;

        for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
            Item item = itemDAO.getItemByCode(entry.getKey());
            int quantity = entry.getValue();
            double price = item.getPrice() * quantity;
            total += price;
            billItems.add(new BillItem(item.getCode(), item.getName(), quantity, price));
            itemDAO.updateItemQuantity(item.getCode(), quantity);
        }

        double netTotal = total - discount;
        double change = cashTendered - netTotal;

        Bill bill = new Bill(0, new Date(), total, discount, cashTendered, change, billItems);
        int billId = billDAO.saveBill(bill);
        billItemDAO.saveBillItems(billId, billItems);
    }
}
