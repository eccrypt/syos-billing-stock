package web.servlets;

import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.Item;
import core.models.Shelf;
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
import java.util.List;

@WebServlet(urlPatterns = "/item/*", asyncSupported = true)
public class ItemServlet extends HttpServlet {
    private ItemService itemService;
    private ShelfService shelfService;
    private StockFacade stockFacade;

    @Override
    public void init() throws ServletException {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            shelfService = new ShelfService(new ShelfDAO(conn));
            itemService = new ItemService(new ItemDAO(conn), shelfService);
            StockService stockService = new StockService(conn, itemService, shelfService);
            ReorderNotifier reorderNotifier = new ReorderNotifier(itemService);
            stockFacade = new StockFacade(itemService, stockService, shelfService, reorderNotifier);
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize ItemServlet: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AsyncContext asyncContext = request.startAsync();
        RequestHandler handler = (req, res) -> {
            HttpSession session = req.getSession();
            User user = (User) session.getAttribute("user");
            if (user == null) {
                res.sendRedirect("/index.jsp?error=Please login first");
                return;
            }

            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                req.getRequestDispatcher("/item.jsp").forward(req, res);
                return;
            }

            // Handle different actions
            try {
                switch (path) {
                    case "/view":
                        List<Item> items = itemService.getAllItems();
                        req.setAttribute("items", items);
                        req.setAttribute("view", "items");
                        break;
                    case "/search":
                        String code = req.getParameter("code");
                        if (code == null || code.isEmpty()) {
                            req.setAttribute("showSearchForm", true);
                        } else {
                            Item item = itemService.getItemByCode(code);
                            req.setAttribute("item", item);
                            req.setAttribute("view", "item");
                        }
                        break;
                    default:
                        req.setAttribute("error", "Unknown action");
                }
            } catch (Exception e) {
                req.setAttribute("error", "Error: " + e.getMessage());
            }

            req.getRequestDispatcher("/item.jsp").forward(req, res);
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

            String action = req.getParameter("action");

            try {
                switch (action) {
                    case "add":
                        String name = req.getParameter("name");
                        String priceStr = req.getParameter("price");
                        String shelfDefaultStr = req.getParameter("shelfDefault");
                        String quantityStr = req.getParameter("quantity");
                        String expiry = req.getParameter("expiry");
                        if (name == null || priceStr == null || shelfDefaultStr == null || quantityStr == null || expiry == null) {
                            req.setAttribute("showAddForm", true);
                        } else {
                            double price = Double.parseDouble(priceStr);
                            int shelfDefault = Integer.parseInt(shelfDefaultStr);
                            int quantity = Integer.parseInt(quantityStr);
                            LocalDate currentDate = LocalDate.now();
                            String entryDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                            Item newItem = new Item(name, price);
                            itemService.addItem(newItem, shelfDefault, 0);
                            stockFacade.stockItem(newItem.getCode(), quantity, entryDate, expiry);

                            Shelf newShelf = new Shelf(newItem.getCode(), shelfDefault, shelfDefault);
                            stockFacade.getShelfService().addShelf(newShelf);

                            newShelf.setShelfCurrent(newShelf.getShelfDefault());
                            int reducedStockBatchQuantity = quantity - newShelf.getShelfDefault();

                            if (reducedStockBatchQuantity > 0) {
                                stockFacade.updateStockEntry(newItem.getCode(), reducedStockBatchQuantity, expiry);
                            }

                            stockFacade.getShelfService().updateShelf(newShelf);
                            req.setAttribute("message", "Item added with code: " + newItem.getCode());
                        }
                        break;
                    case "update":
                        String code = req.getParameter("code");
                        name = req.getParameter("name");
                        priceStr = req.getParameter("price");
                        if (code == null || name == null || priceStr == null) {
                            req.setAttribute("showUpdateForm", true);
                        } else {
                            double price = Double.parseDouble(priceStr);
                            itemService.updateItem(new Item(code, name, price));
                            req.setAttribute("message", "Item updated.");
                        }
                        break;
                    case "updateName":
                        code = req.getParameter("code");
                        name = req.getParameter("name");
                        if (code == null || name == null) {
                            req.setAttribute("showUpdateNameForm", true);
                        } else {
                            itemService.updateItemName(code, name);
                            req.setAttribute("message", "Item name updated.");
                        }
                        break;
                    case "updatePrice":
                        code = req.getParameter("code");
                        priceStr = req.getParameter("price");
                        if (code == null || priceStr == null) {
                            req.setAttribute("showUpdatePriceForm", true);
                        } else {
                            double price = Double.parseDouble(priceStr);
                            itemService.updateItemPrice(code, price);
                            req.setAttribute("message", "Item price updated.");
                        }
                        break;
                    case "delete":
                        code = req.getParameter("code");
                        if (code == null) {
                            req.setAttribute("showDeleteForm", true);
                        } else {
                            itemService.deleteItem(code);
                            req.setAttribute("message", "Item deleted.");
                        }
                        break;
                    default:
                        req.setAttribute("error", "Unknown action");
                }
            } catch (Exception e) {
                req.setAttribute("error", "Error: " + e.getMessage());
            }

            req.getRequestDispatcher("/item.jsp").forward(req, res);
        };
        AsyncRequestProcessor.getInstance().submitTask(new RequestTask(asyncContext, handler));
    }
}