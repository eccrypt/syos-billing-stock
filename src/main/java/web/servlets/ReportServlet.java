package web.servlets;

import core.command.GenerateReportCommand;
import core.command.ReportInvoker;
import core.dao.BillDAO;
import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.User;
import core.observer.ReorderNotifier;
import core.report.*;
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
            switch (action) {
                case "reorder":
                    ReportTemplate report = new ReorderReport(reorderNotifier, itemService);
                    new GenerateReportCommand(report).execute();
                    break;
                case "daily":
                    String dateStr = request.getParameter("date");
                    if (dateStr == null || dateStr.isEmpty()) {
                        request.setAttribute("error", "Date is required for daily sales report");
                        request.getRequestDispatcher("/report.jsp").forward(request, response);
                        return;
                    }
                    LocalDate date = LocalDate.parse(dateStr);
                    report = new DailySalesReporter(billDAO, date);
                    new GenerateReportCommand(report).execute();
                    break;
                case "stock":
                    report = new StockReport(stockService);
                    new GenerateReportCommand(report).execute();
                    break;
                case "bill":
                    dateStr = request.getParameter("date");
                    if (dateStr == null || dateStr.isEmpty()) {
                        request.setAttribute("error", "Date is required for bill report");
                        request.getRequestDispatcher("/report.jsp").forward(request, response);
                        return;
                    }
                    date = LocalDate.parse(dateStr);
                    report = new BillReport(billDAO, date);
                    new GenerateReportCommand(report).execute();
                    break;
                case "all":
                    dateStr = request.getParameter("date");
                    if (dateStr == null || dateStr.isEmpty()) {
                        request.setAttribute("error", "Date is required for all reports");
                        request.getRequestDispatcher("/report.jsp").forward(request, response);
                        return;
                    }
                    date = LocalDate.parse(dateStr);
                    ReportInvoker invoker = new ReportInvoker();
                    invoker.addCommand(new GenerateReportCommand(new ReorderReport(reorderNotifier, itemService)));
                    invoker.addCommand(new GenerateReportCommand(new DailySalesReporter(billDAO, date)));
                    invoker.addCommand(new GenerateReportCommand(new StockReport(stockService)));
                    invoker.addCommand(new GenerateReportCommand(new BillReport(billDAO, date)));
                    invoker.runCommands();
                    break;
                default:
                    request.setAttribute("error", "Unknown action");
            }
            request.setAttribute("message", "Report generated. Check console for output.");
        } catch (Exception e) {
            request.setAttribute("error", "Error generating report: " + e.getMessage());
        }

        request.getRequestDispatcher("/report.jsp").forward(request, response);
    }
}