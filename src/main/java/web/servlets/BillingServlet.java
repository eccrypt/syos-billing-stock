package web.servlets;

import core.models.User;
import core.services.BillingService;
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
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = "/billing/*", asyncSupported = true)
public class BillingServlet extends HttpServlet {
    private BillingService billingService;

    @Override
    public void init() throws ServletException {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            this.billingService = new BillingService(conn);
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize BillingServlet: " + e.getMessage(), e);
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

        request.getRequestDispatcher("/billing.jsp").forward(request, response);
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

            Map<String, Integer> purchasedItems = new HashMap<>();
            String[] itemCodes = req.getParameterValues("itemCode");
            String[] quantities = req.getParameterValues("quantity");

            if (itemCodes != null && quantities != null) {
                for (int i = 0; i < itemCodes.length; i++) {
                    if (!itemCodes[i].isEmpty() && !quantities[i].isEmpty()) {
                        try {
                            int qty = Integer.parseInt(quantities[i]);
                            purchasedItems.put(itemCodes[i], purchasedItems.getOrDefault(itemCodes[i], 0) + qty);
                        } catch (NumberFormatException e) {
                            req.setAttribute("error", "Invalid quantity for item: " + itemCodes[i]);
                            req.getRequestDispatcher("/billing.jsp").forward(req, res);
                            return;
                        }
                    }
                }
            }

            if (purchasedItems.isEmpty()) {
                req.setAttribute("error", "No items added to bill");
                req.getRequestDispatcher("/billing.jsp").forward(req, res);
                return;
            }

            String cashStr = req.getParameter("cash");
            if (cashStr == null || cashStr.isEmpty()) {
                req.setAttribute("error", "Cash tendered is required");
                req.getRequestDispatcher("/billing.jsp").forward(req, res);
                return;
            }

            try {
                double cash = Double.parseDouble(cashStr);
                double total = billingService.calculateTotal(purchasedItems);

                if (cash < total) {
                    req.setAttribute("error", "Cash tendered is less than total amount");
                    req.getRequestDispatcher("/billing.jsp").forward(req, res);
                    return;
                }

                billingService.createBill(purchasedItems, cash);
                req.setAttribute("success", "Bill generated successfully! Total: " + total + ", Change: " + (cash - total));
                req.getRequestDispatcher("/billing.jsp").forward(req, res);

            } catch (NumberFormatException e) {
                req.setAttribute("error", "Invalid cash amount");
                req.getRequestDispatcher("/billing.jsp").forward(req, res);
            } catch (SQLException e) {
                req.setAttribute("error", "Database error: " + e.getMessage());
                req.getRequestDispatcher("/billing.jsp").forward(req, res);
            }
        };
        AsyncRequestProcessor.getInstance().submitTask(new RequestTask(asyncContext, handler));
    }
}