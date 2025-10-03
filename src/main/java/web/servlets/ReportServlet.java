package web.servlets;

import core.dao.BillDAO;
import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.Bill;
import core.models.Item;
import core.models.StockEntry;
import core.models.User;
import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;
import core.utils.DatabaseConnectionManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/report/*")
public class ReportServlet extends HttpServlet {
    private BillDAO billDAO;
    private StockService stockService;
    private ItemService itemService;
    private ShelfService shelfService;
    private ReorderNotifier reorderNotifier;

    @Override
    public void init() throws ServletException {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            this.shelfService = new ShelfService(new ShelfDAO(conn));
            this.itemService = new ItemService(new ItemDAO(conn), shelfService);
            this.reorderNotifier = new ReorderNotifier(itemService);
            this.stockService = new StockService(conn, itemService, shelfService);
            this.stockService.registerObserver(reorderNotifier);
            this.billDAO = new BillDAO(conn);
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize ReportServlet: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/index.jsp?error=Please login first");
            return;
        }

        request.getRequestDispatcher("/report.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/index.jsp?error=Please login first");
            return;
        }

        String action = request.getParameter("action");

        try {
            StringBuilder sb = new StringBuilder();
            switch (action) {
                case "reorder":
                    sb.append("=== Reorder Level Report ===\n");
                    Map<String, Integer> lowStockItems = reorderNotifier.getReorderItems();
                    if (lowStockItems.isEmpty()) {
                        sb.append("All stock levels are sufficient.\n");
                    } else {
                        sb.append(String.format("%-10s %-25s %-10s%n", "Item Code", "Item Name", "Quantity"));
                        sb.append("--------------------------------------------------\n");
                        for (Map.Entry<String, Integer> entry : lowStockItems.entrySet()) {
                            try {
                                Item item = itemService.getItemByCode(entry.getKey());
                                sb.append(String.format("%-10s %-25s %-10d%n", item.getCode(), item.getName(), entry.getValue()));
                            } catch (Exception e) {
                                sb.append("Error loading item: " + entry.getKey() + "\n");
                            }
                        }
                    }
                    break;
                case "daily":
                    String dateStr = request.getParameter("date");
                    if (dateStr == null || dateStr.isEmpty()) {
                        request.setAttribute("error", "Date is required for daily sales report");
                        request.getRequestDispatcher("/report.jsp").forward(request, response);
                        return;
                    }
                    LocalDate date = LocalDate.parse(dateStr);
                    List<Bill> bills = billDAO.getBillsByDate(date);
                    int totalBills = bills.size();
                    double totalRevenue = bills.stream().mapToDouble(Bill::getTotal).sum();
                    sb.append(String.format("=== Daily Sales Report (%s) ===\n", dateStr));
                    sb.append(String.format("Total Bills: %d | Total Revenue: %.2f\n", totalBills, totalRevenue));
                    break;
                case "stock":
                    sb.append("=== Current Stock Report (Batch-wise) ===\n");
                    List<StockEntry> entries = stockService.getAllStockEntries();
                    for (StockEntry entry : entries) {
                        sb.append(String.format("Item: %s | Qty: %d | Entry: %s | Expiry: %s\n",
                                entry.getItemCode(), entry.getQuantity(), entry.getEntryDate(), entry.getExpiryDate()));
                    }
                    break;
                case "bill":
                    dateStr = request.getParameter("date");
                    if (dateStr == null || dateStr.isEmpty()) {
                        request.setAttribute("error", "Date is required for bill report");
                        request.getRequestDispatcher("/report.jsp").forward(request, response);
                        return;
                    }
                    date = LocalDate.parse(dateStr);
                    bills = billDAO.getBillsByDate(date);
                    sb.append(String.format("=== Bill Report (%s) ===\n", dateStr));
                    for (Bill bill : bills) {
                        sb.append(String.format("Bill ID: %d | Total: %.2f | Date: %s\n", bill.getId(), bill.getTotal(), bill.getBillDate()));
                    }
                    break;
                case "all":
                    dateStr = request.getParameter("date");
                    if (dateStr == null || dateStr.isEmpty()) {
                        request.setAttribute("error", "Date is required for all reports");
                        request.getRequestDispatcher("/report.jsp").forward(request, response);
                        return;
                    }
                    date = LocalDate.parse(dateStr);
                    // reorder
                    sb.append("=== Reorder Level Report ===\n");
                    lowStockItems = reorderNotifier.getReorderItems();
                    if (lowStockItems.isEmpty()) {
                        sb.append("All stock levels are sufficient.\n");
                    } else {
                        sb.append(String.format("%-10s %-25s %-10s%n", "Item Code", "Item Name", "Quantity"));
                        sb.append("--------------------------------------------------\n");
                        for (Map.Entry<String, Integer> entry : lowStockItems.entrySet()) {
                            try {
                                Item item = itemService.getItemByCode(entry.getKey());
                                sb.append(String.format("%-10s %-25s %-10d%n", item.getCode(), item.getName(), entry.getValue()));
                            } catch (Exception e) {
                                sb.append("Error loading item: " + entry.getKey() + "\n");
                            }
                        }
                    }
                    sb.append("\n");
                    // daily
                    bills = billDAO.getBillsByDate(date);
                    totalBills = bills.size();
                    totalRevenue = bills.stream().mapToDouble(Bill::getTotal).sum();
                    sb.append(String.format("=== Daily Sales Report (%s) ===\n", dateStr));
                    sb.append(String.format("Total Bills: %d | Total Revenue: %.2f\n", totalBills, totalRevenue));
                    sb.append("\n");
                    // stock
                    sb.append("=== Current Stock Report (Batch-wise) ===\n");
                    entries = stockService.getAllStockEntries();
                    for (StockEntry entry : entries) {
                        sb.append(String.format("Item: %s | Qty: %d | Entry: %s | Expiry: %s\n",
                                entry.getItemCode(), entry.getQuantity(), entry.getEntryDate(), entry.getExpiryDate()));
                    }
                    sb.append("\n");
                    // bill
                    sb.append(String.format("=== Bill Report (%s) ===\n", dateStr));
                    for (Bill bill : bills) {
                        sb.append(String.format("Bill ID: %d | Total: %.2f | Date: %s\n", bill.getId(), bill.getTotal(), bill.getBillDate()));
                    }
                    break;
                default:
                    request.setAttribute("error", "Unknown action");
                    request.getRequestDispatcher("/report.jsp").forward(request, response);
                    return;
            }
            request.setAttribute("report", sb.toString());
            request.setAttribute("view", "report");
        } catch (Exception e) {
            request.setAttribute("error", "Error generating report: " + e.getMessage());
        }

        request.getRequestDispatcher("/report.jsp").forward(request, response);
    }
}