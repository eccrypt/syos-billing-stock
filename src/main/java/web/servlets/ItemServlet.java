package web.servlets;

import cli.menus.ItemCLIHandler;
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

@WebServlet(urlPatterns = "/item/*")
public class ItemServlet extends HttpServlet {
    private ItemCLIHandler handler;

    @Override
    public void init() throws ServletException {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            ShelfService shelfService = new ShelfService(new ShelfDAO(conn));
            ItemService itemService = new ItemService(new ItemDAO(conn), shelfService);
            StockService stockService = new StockService(conn, itemService, shelfService);
            ReorderNotifier reorderNotifier = new ReorderNotifier(itemService);
            StockFacade stockFacade = new StockFacade(itemService, stockService, shelfService, reorderNotifier);
            this.handler = new ItemCLIHandler(itemService, shelfService, stockFacade);
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize ItemServlet: " + e.getMessage(), e);
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

        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            request.getRequestDispatcher("/item.jsp").forward(request, response);
            return;
        }

        // Handle different actions
        try {
            switch (path) {
                case "/view":
                    handler.handleViewAllItems();
                    break;
                case "/search":
                    handler.handleSearchItem();
                    break;
                default:
                    request.setAttribute("error", "Unknown action");
            }
            request.setAttribute("message", "Action completed. Check console for output.");
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
        }

        request.getRequestDispatcher("/item.jsp").forward(request, response);
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
                    handler.handleAddItem();
                    break;
                case "update":
                    handler.handleUpdateItem();
                    break;
                case "updateName":
                    handler.handleUpdateItemName();
                    break;
                case "updatePrice":
                    handler.handleUpdateItemPrice();
                    break;
                case "delete":
                    handler.handleDeleteItem();
                    break;
                default:
                    request.setAttribute("error", "Unknown action");
            }
            request.setAttribute("message", "Action completed. Check console for output.");
        } catch (Exception e) {
            request.setAttribute("error", "Error: " + e.getMessage());
        }

        request.getRequestDispatcher("/item.jsp").forward(request, response);
    }
}