package core.services;

import core.billing.BasicBill;
import core.billing.BillBuilder;
import core.dao.BillDAO;
import core.dao.BillItemDAO;
import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.*;
import core.discount.DiscountContext;
import core.discount.DiscountResult;
import core.utils.SerialNumberGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BillingService {
    private final ItemService itemService;
    private final BillDAO billDAO;
    private final BillItemDAO billItemDAO;
    private final StockService stockService;
    private final DiscountContext discountContext;

    public BillingService(Connection conn) {
        ShelfService shelfService = new ShelfService(new ShelfDAO(conn));

        // Create ItemService instance and pass both ItemDAO and ShelfService
        this.itemService = new ItemService(new ItemDAO(conn), shelfService);
        this.billDAO = new BillDAO(conn);
        this.billItemDAO = new BillItemDAO(conn);

        // Create StockService, passing the ShelfService along with ItemService
        this.stockService = new StockService(conn, itemService, shelfService);  // Pass ShelfService here

        this.discountContext = new DiscountContext();
    }

    // Test constructor
    public BillingService(ItemService itemService, BillDAO billDAO, BillItemDAO billItemDAO,
                          StockService stockService, DiscountContext discountContext) {
        this.itemService = itemService;
        this.billDAO = billDAO;
        this.billItemDAO = billItemDAO;
        this.stockService = stockService;
        this.discountContext = discountContext;
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

    public void createBill(Map<String, Integer> purchasedItems, double cashTendered) throws SQLException {
        List<BillItem> billItems = new ArrayList<>();
        double total = 0;

        for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
            String itemCode = entry.getKey();
            int quantityNeeded = entry.getValue();

            Item item = itemService.getItemByCode(itemCode);
            if (item == null) throw new SQLException("Item not found: " + itemCode);

            double itemTotal = item.getPrice() * quantityNeeded;
            total += itemTotal;

            // Check if there's enough stock in the shelf
            if (!checkShelfStock(itemCode, quantityNeeded)) {
                throw new SQLException("❌ Insufficient stock in shelf for item: " + itemCode);
            }

            billItems.add(new BillItem(item.getCode(), item.getName(), quantityNeeded, itemTotal));

            // **Reduce the shelf's stock after the bill is created**
            reduceShelfStock(itemCode, quantityNeeded);  // Adjust shelf quantity after purchase
        }

        // Apply discount chain
        Bill tempBill = new BasicBill(total, 0, cashTendered, 0, billItems, 0);  // temporary bill for context
        DiscountResult discountResult = discountContext.applyDiscounts(tempBill, total);

        double discount = total - discountResult.getTotalAfterDiscount();
        double netTotal = discountResult.getTotalAfterDiscount();
        double change = cashTendered - netTotal;
        int serialNumber = SerialNumberGenerator.getNextSerial();

        Bill decoratedBill = BillBuilder.build(total, discount, cashTendered, change, billItems, serialNumber);
        System.out.println(decoratedBill.print());

        Bill billToSave = new BasicBill(total, discount, cashTendered, change, billItems, serialNumber);
        billToSave.setSerialNumber(serialNumber);

        int billId = billDAO.saveBill(billToSave);
        billItemDAO.saveBillItems(billId, billItems);

        System.out.println("✅ Discount applied: " + discountResult.getDiscountName());
        System.out.println("Total after discount: " + netTotal);
        System.out.println("Change due: " + change);
    }

    /**
     * Check if the shelf has enough stock for the requested quantity.
     */
    private boolean checkShelfStock(String itemCode, int quantityNeeded) throws SQLException {
        Shelf shelf = stockService.getShelfService().getShelfByProductCode(itemCode);

        if (shelf == null) {
            System.out.println("⚠️ Shelf not found for item code: " + itemCode);
            return false;
        }

        int shelfCurrent = shelf.getShelfCurrent();
        return shelfCurrent >= quantityNeeded;
    }

    /**
     * Reduce the shelf's stock based on the quantity purchased.
     */
    private void reduceShelfStock(String itemCode, int quantityNeeded) throws SQLException {
        Shelf shelf = stockService.getShelfService().getShelfByProductCode(itemCode);

        if (shelf != null) {
            // Reduce shelf's current stock by quantity purchased
            int updatedQuantity = shelf.getShelfCurrent() - quantityNeeded;

            // Update the shelf's current quantity
            shelf.setShelfCurrent(updatedQuantity);
            stockService.getShelfService().updateShelf(shelf);  // Update in the database
            System.out.println("✅ Shelf quantity updated: Current quantity is now " + updatedQuantity + "for " + itemCode);

            // Check if the shelf quantity is below a certain threshold (e.g., 10 units)
            if (updatedQuantity < 10) {
                // Refill the shelf from the available stock based on the nearest expiry batch
                refillShelf(itemCode, updatedQuantity);
            }
        } else {
            System.out.println("⚠️ Shelf not found for item code: " + itemCode);
        }
    }

    /**
     * Refill the shelf from the stock based on the nearest expiry batch.
     */
    private void refillShelf(String itemCode, int shelfQuantity) throws SQLException {
        Shelf shelf = stockService.getShelfService().getShelfByProductCode(itemCode);
        int quantityToRefill = shelf.getShelfDefault() - shelfQuantity;

        if (quantityToRefill > 0) {
            // Allocate stock from the available stock entries based on expiry date priority
            List<StockEntry> allocated = stockService.allocateStock(itemCode, quantityToRefill);

            // Add the allocated stock to the shelf
            int allocatedQuantity = allocated.stream().mapToInt(StockEntry::getQuantity).sum();

            // Update the shelf current value after refilling
            shelf.setShelfCurrent(shelfQuantity + allocatedQuantity);
            stockService.getShelfService().updateShelf(shelf);

            System.out.println("✅ Shelf refilled: Current quantity is now " + shelf.getShelfCurrent());
        }
    }
}
