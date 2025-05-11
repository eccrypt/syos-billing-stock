package web.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class BillingServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println("BillingServlet invoked");
    }
}