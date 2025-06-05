package core.billing;

import core.models.Bill;
import core.models.BillItem;

import java.text.SimpleDateFormat;

public class FooterBill extends BillDecorator {
    public FooterBill(Bill decoratedBill) {
        super(decoratedBill);
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();

        // Format the bill date as yyyyMMdd (for serial number)
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(getBillDate());

        // Format the serial number to always be two digits
        String serialNumber = String.format("%02d", getSerialNumber());

        // Final serial number: combining date and serial number
        String formattedSerial = dateStr + serialNumber;

        // Bill formatting
        sb.append("===================================================\n");
        sb.append("                   *** BILL ***\n");
        sb.append("===================================================\n");
        sb.append(String.format("Serial Number: %-15s\n", formattedSerial));
        sb.append(String.format("%-25s\n", getBillDate()));
        sb.append("---------------------------------------------------\n");

        // Table header
        sb.append(String.format("%-20s%-10s%-15s%-15s\n", "Item Name", "Quantity", "Price (Rs)", "Total (Rs)"));
        sb.append("---------------------------------------------------\n");

        // Loop through the bill items and print them in table format
        for (BillItem item : getItems()) {
            sb.append(String.format("%-20s%-10d%-15.2f%-15.2f\n",
                    item.getItemName(),
                    item.getQuantity(),
                    item.getTotalPrice() / item.getQuantity(),  // Calculate price by dividing total by quantity
                    item.getTotalPrice()));
        }

        // Adding total amount, discount, cash tendered, and change
        sb.append("---------------------------------------------------\n");
        sb.append(String.format("Total:            %-10.2f\n", getTotal()));
        sb.append(String.format("Discount:         %-10.2f\n", getDiscount()));
        sb.append(String.format("Net Total:        %-10.2f\n", getTotal() - getDiscount()));
        sb.append("---------------------------------------------------\n");

        // Adding cash tendered and change due
        sb.append(String.format("Cash Tendered:    %-10.2f\n", getCashTendered()));
        sb.append(String.format("Change Due:       %-10.2f\n", getChangeDue()));
        sb.append("---------------------------------------------------\n");

        // Thank you message
        sb.append("Thank you for shopping with us!\n");
        sb.append("===================================================\n");

        return sb.toString();
    }
}
