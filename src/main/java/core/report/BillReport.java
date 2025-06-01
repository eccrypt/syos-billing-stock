package core.report;

import core.dao.BillDAO;
import core.models.Bill;

import java.time.LocalDate;
import java.util.List;

public class BillReport implements ReportTemplate {
    private final BillDAO billDAO;
    private final LocalDate targetDate;

    public BillReport(BillDAO billDAO, LocalDate targetDate) {
        this.billDAO = billDAO;
        this.targetDate = targetDate;
    }

    @Override
    public void generate() {
        try {
            List<Bill> bills = billDAO.getBillsByDate(targetDate);
            System.out.printf("=== Bill Report (%s) ===%n", targetDate);
            for (Bill bill : bills) {
                System.out.printf("Bill #%d | Total: %.2f | Discount: %.2f | Cash: %.2f | Change: %.2f%n",
                        bill.getId(), bill.getTotal(), bill.getDiscount(), bill.getCashTendered(), bill.getChangeDue());
            }
        } catch (Exception e) {
            System.out.println("❌ Error generating bill report: " + e.getMessage());
        }
    }
}

