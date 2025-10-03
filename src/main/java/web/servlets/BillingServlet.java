package web.servlets;

import core.models.User;
import core.services.BillingService;
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
import java.util.HashMap;
import java.util.Map;

@WebServlet("/billing")
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
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        request.getRequestDispatcher("/billing.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        Map<String, Integer> purchasedItems = new HashMap<>();
        String[] itemCodes = request.getParameterValues("itemCode");
        String[] quantities = request.getParameterValues("quantity");

        if (itemCodes != null && quantities != null) {
            for (int i = 0; i < itemCodes.length; i++) {
                if (!itemCodes[i].isEmpty() && !quantities[i].isEmpty()) {
                    try {
                        int qty = Integer.parseInt(quantities[i]);
                        purchasedItems.put(itemCodes[i], purchasedItems.getOrDefault(itemCodes[i], 0) + qty);
                    } catch (NumberFormatException e) {
                        request.setAttribute("error", "Invalid quantity for item: " + itemCodes[i]);
                        request.getRequestDispatcher("/billing.jsp").forward(request, response);
                        return;
                    }
                }
            }
        }

        if (purchasedItems.isEmpty()) {
            request.setAttribute("error", "No items added to bill");
            request.getRequestDispatcher("/billing.jsp").forward(request, response);
            return;
        }

        String cashStr = request.getParameter("cash");
        if (cashStr == null || cashStr.isEmpty()) {
            request.setAttribute("error", "Cash tendered is required");
            request.getRequestDispatcher("/billing.jsp").forward(request, response);
            return;
        }

        try {
            double cash = Double.parseDouble(cashStr);
            double total = billingService.calculateTotal(purchasedItems);

            if (cash < total) {
                request.setAttribute("error", "Cash tendered is less than total amount");
                request.getRequestDispatcher("/billing.jsp").forward(request, response);
                return;
            }

            billingService.createBill(purchasedItems, cash);
            request.setAttribute("success", "Bill generated successfully! Total: " + total + ", Change: " + (cash - total));
            request.getRequestDispatcher("/billing.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid cash amount");
            request.getRequestDispatcher("/billing.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/billing.jsp").forward(request, response);
        }
    }
}