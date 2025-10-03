package web.servlets;

import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.Item;
import core.models.StockEntry;
import core.models.User;
import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;
import core.facade.StockFacade;
import core.utils.DatabaseConnectionManager;
import web.async.AsyncRequestProcessor;
import web.async.RequestHandler;
import web.async.RequestTask;

import javax.servlet.AsyncContext;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/stock/*", asyncSupported = true)
public class StockServlet extends HttpServlet {
    private StockFacade facade;
    private ItemService itemService;
    private StockService stockService;
    private ReorderNotifier reorderNotifier;

    @Override
    public void init() throws ServletException {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            ShelfService shelfService = new ShelfService(new ShelfDAO(conn));
            itemService = new ItemService(new ItemDAO(conn), shelfService);
            stockService = new StockService(conn, itemService, shelfService);
            reorderNotifier = new ReorderNotifier(itemService);
            facade = new StockFacade(itemService, stockService, shelfService, reorderNotifier);
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize StockServlet: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AsyncContext asyncContext = request.startAsync();
        RequestHandler handler = (req, res) -> {
            HttpSession session = req.getSession();
            User user = (User) session.getAttribute("user");
            if (user == null) {
                res.sendRedirect("/api/auth/login");
                return;
            }

            String role = user.getRole();
            if (!"admin".equals(role) && !"employee".equals(role)) {
                res.sendRedirect("/index.jsp?error=Access denied: Only admins and employees can access stock management");
                return;
            }

            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                req.setAttribute("userRole", role);
                req.getRequestDispatcher("/stock.jsp").forward(req, res);
                return;
            }

            try {
                switch (path) {
                    case "/view":
                        List<StockEntry> entries = stockService.getAllStockEntries();
                        req.setAttribute("stockEntries", entries);
                        req.getRequestDispatcher("/stock-view.jsp").forward(req, res);
                        break;
                    case "/add":
                        req.getRequestDispatcher("/stock-add.jsp").forward(req, res);
                        break;
                    case "/allocate":
                        req.getRequestDispatcher("/stock-allocate.jsp").forward(req, res);
                        break;
                    case "/reorder":
                        Map<String, Integer> reorderItems = new HashMap<>();
                        for (Item item : itemService.getAllItems()) {
                            int stockQty = stockService.getTotalStockForItem(item.getCode());
                            reorderNotifier.update(item.getCode(), stockQty);
                        }
                        reorderItems.putAll(reorderNotifier.getReorderItems());
                        req.setAttribute("reorderItems", reorderItems);
                        req.getRequestDispatcher("/stock-reorder.jsp").forward(req, res);
                        break;
                    case "/level":
                        String code = req.getParameter("code");
                        if (code == null || code.isEmpty()) {
                            req.getRequestDispatcher("/stock-level.jsp").forward(req, res);
                        } else {
                            int quantity = stockService.getTotalStockForItem(code);
                            req.setAttribute("stockLevel", quantity);
                            req.setAttribute("itemCode", code);
                            req.getRequestDispatcher("/stock-level.jsp").forward(req, res);
                        }
                        break;
                    case "/update":
                        req.getRequestDispatcher("/stock-update.jsp").forward(req, res);
                        break;
                    case "/delete":
                        if (!"admin".equals(role)) {
                            req.setAttribute("error", "Access denied: Only admins can delete stock entries");
                            req.getRequestDispatcher("/stock.jsp").forward(req, res);
                            return;
                        }
                        req.getRequestDispatcher("/stock-delete.jsp").forward(req, res);
                        break;
                    default:
                        req.setAttribute("error", "Unknown action");
                        req.getRequestDispatcher("/stock.jsp").forward(req, res);
                }
            } catch (Exception e) {
                req.setAttribute("error", "Error: " + e.getMessage());
                req.getRequestDispatcher("/stock.jsp").forward(req, res);
            }
        };
        AsyncRequestProcessor.getInstance().submitTask(new RequestTask(asyncContext, handler));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AsyncContext asyncContext = request.startAsync();
        RequestHandler handler = (req, res) -> {
            HttpSession session = req.getSession();
            User user = (User) session.getAttribute("user");
            if (user == null) {
                res.sendRedirect("/index.jsp?error=Please login first");
                return;
            }

            String role = user.getRole();
            if (!"admin".equals(role) && !"employee".equals(role)) {
                res.sendRedirect("/index.jsp?error=Access denied: Only admins and employees can access stock management");
                return;
            }

            String action = req.getParameter("action");

            try {
                switch (action) {
                    case "add":
                        String code = req.getParameter("code");
                        String quantityStr = req.getParameter("quantity");
                        String expiry = req.getParameter("expiry");
                        if (code == null || quantityStr == null || expiry == null) {
                            req.setAttribute("error", "All fields are required.");
                            req.getRequestDispatcher("/stock-add.jsp").forward(req, res);
                            return;
                        } else {
                            int quantity = Integer.parseInt(quantityStr);
                            LocalDate currentDate = LocalDate.now();
                            String entryDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            facade.stockItem(code, quantity, entryDate, expiry);
                            res.sendRedirect("stock/view");
                            return;
                        }
                    case "allocate":
                        code = req.getParameter("code");
                        quantityStr = req.getParameter("quantity");
                        if (code == null || quantityStr == null) {
                            req.setAttribute("error", "All fields are required.");
                            req.getRequestDispatcher("/stock-allocate.jsp").forward(req, res);
                            return;
                        } else {
                            int quantity = Integer.parseInt(quantityStr);
                            facade.allocateStock(code, quantity);
                            res.sendRedirect("stock/view");
                            return;
                        }
                    case "update":
                        String itemCode = req.getParameter("itemCode");
                        quantityStr = req.getParameter("quantity");
                        expiry = req.getParameter("expiry");
                        if (itemCode == null || quantityStr == null || expiry == null) {
                            req.setAttribute("error", "All fields are required.");
                            req.getRequestDispatcher("/stock-update.jsp").forward(req, res);
                            return;
                        } else {
                            int quantity = Integer.parseInt(quantityStr);
                            facade.updateStockEntry(itemCode, quantity, expiry);
                            res.sendRedirect("stock/view");
                            return;
                        }
                    case "delete":
                        if (!"admin".equals(role)) {
                            req.setAttribute("error", "Access denied: Only admins can delete stock entries");
                            req.getRequestDispatcher("/stock-delete.jsp").forward(req, res);
                            return;
                        }
                        String entryIdStr = req.getParameter("entryId");
                        if (entryIdStr == null) {
                            req.setAttribute("error", "Entry ID is required.");
                            req.getRequestDispatcher("/stock-delete.jsp").forward(req, res);
                            return;
                        } else {
                            int entryId = Integer.parseInt(entryIdStr);
                            facade.deleteStockEntry(entryId);
                            res.sendRedirect("stock/view");
                            return;
                        }
                    default:
                        req.setAttribute("error", "Unknown action");
                        req.getRequestDispatcher("/stock.jsp").forward(req, res);
                        return;
                }
            } catch (Exception e) {
                req.setAttribute("error", "Error: " + e.getMessage());
                // Forward to appropriate page based on action
                String jsp = "/stock.jsp";
                if ("add".equals(action)) jsp = "/stock-add.jsp";
                else if ("allocate".equals(action)) jsp = "/stock-allocate.jsp";
                else if ("update".equals(action)) jsp = "/stock-update.jsp";
                else if ("delete".equals(action)) jsp = "/stock-delete.jsp";
                req.getRequestDispatcher(jsp).forward(req, res);
            }
        };
        AsyncRequestProcessor.getInstance().submitTask(new RequestTask(asyncContext, handler));
    }
}