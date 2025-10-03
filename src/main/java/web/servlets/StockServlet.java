package web.servlets;

import cli.menus.StockCLIHandler;
import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.User;
import core.observer.ReorderNotifier;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;
import core.facade.StockFacade;
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

@WebServlet(urlPatterns = "/stock/*")
public class StockServlet extends HttpServlet {
    private StockCLIHandler handler;

    @Override
    public void init() throws ServletException {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            ItemDAO itemRepo = new ItemDAO(conn);
            ShelfService shelfService = new ShelfService(new ShelfDAO(conn));
            ItemService itemService = new ItemService(itemRepo, shelfService);
            StockService stockService = new StockService(conn, itemService, shelfService);
            ReorderNotifier reorderNotifier = new ReorderNotifier(itemService);
            StockFacade stockFacade = new StockFacade(itemService, stockService, shelfService, reorderNotifier);
            this.handler = new StockCLIHandler(stockFacade);
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize StockServlet: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/api/auth/login");
            return;
        }

        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            request.getRequestDispatcher("/stock.jsp").forward(request, response);
            return;
        }

        // Handle different actions
        try {
            switch (path) {
                case "/view":
                    handler.handleViewAllStockEntries();
                    break;
                case "/reorder":
                    handler.handleCheckReorderAlerts();
                    break;
                case "/level":
                    handler.handleViewStockLevel();
                    break;
                default:
                    request.setAttribute("error", "Unknown action");
            }
            request.setAttribute("message", "Action completed. Check console for output.");
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
        }

        request.getRequestDispatcher("/stock.jsp").forward(request, response);
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
                case "add":
                    handler.handleAddStockEntry();
                    break;
                case "allocate":
                    handler.handleAllocateStock();
                    break;
                case "update":
                    handler.handleUpdateStockEntry();
                    break;
                case "delete":
                    handler.handleDeleteStockEntry();
                    break;
                default:
                    request.setAttribute("error", "Unknown action");
            }
            request.setAttribute("message", "Action completed. Check console for output.");
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
        }

        request.getRequestDispatcher("/stock.jsp").forward(request, response);
    }
}