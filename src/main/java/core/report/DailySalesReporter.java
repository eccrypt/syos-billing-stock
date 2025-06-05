package core.report;

import core.dao.BillDAO;
import core.models.Bill;

import java.time.LocalDate;
import java.util.List;

public class DailySalesReporter implements ReportTemplate {
    private final BillDAO billDAO;
    private final LocalDate targetDate;

    public DailySalesReporter(BillDAO billDAO, LocalDate targetDate) {
        this.billDAO = billDAO;
        this.targetDate = targetDate;
    }

    @Override
    public void generate() {
        try {
            List<Bill> bills = billDAO.getBillsByDate(targetDate);
            double totalRevenue = 0;
            int totalBills = bills.size();

            System.out.printf("=== Daily Sales Report (%s) ===%n", targetDate);
            for (Bill bill : bills) {
                totalRevenue += bill.getTotal();
                System.out.printf("Bill #%d | Total: %.2f%n", bill.getSerialNumber(), bill.getTotal());
            }

            System.out.printf("Total Bills: %d | Total Revenue: %.2f%n", totalBills, totalRevenue);
        } catch (Exception e) {
            System.out.println("❌ Error generating daily sales report: " + e.getMessage());
        }
    }
}
