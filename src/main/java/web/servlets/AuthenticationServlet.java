package web.servlets;

import core.services.AuthenticationService;
import core.dao.UserDAO;
import core.models.User;
import core.utils.TaskExecutor;
import core.utils.DatabaseConnectionManager;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(value = "/api/auth/*", asyncSupported = true)
public class AuthenticationServlet extends HttpServlet {
    private AuthenticationService authService;

    @Override
    public void init() throws ServletException {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);
            this.authService = new AuthenticationService(userDAO);
        } catch (SQLException e) {
            throw new ServletException("❌ Failed to initialize AuthenticationServlet: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo(); // e.g., /register or /login
        if (path == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            return;
        }

        // Enable async mode
        AsyncContext asyncContext = req.startAsync();

        switch (path) {
            case "/register":
                handleRegister(asyncContext);
                break;
            case "/login":
                handleLogin(asyncContext);
                break;
            default:
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
                asyncContext.complete();
        }
    }

    private void handleRegister(AsyncContext asyncContext) {
        HttpServletRequest req = (HttpServletRequest) asyncContext.getRequest();
        HttpServletResponse resp = (HttpServletResponse) asyncContext.getResponse();

        String username = req.getParameter("username");
        String role = req.getParameter("role");
        String password = req.getParameter("password");

        TaskExecutor.submit(() -> {
            try {
                boolean success = authService.registerUser(username, role, password);
                if (success) {
                    resp.sendRedirect("../index.jsp?message=Registration successful");
                } else {
                    resp.sendRedirect("../index.jsp?error=Username already exists");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    resp.sendRedirect("../index.jsp?error=Server error during registration");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } finally {
                asyncContext.complete();
            }
        });
    }

    private void handleLogin(AsyncContext asyncContext) {
        HttpServletRequest req = (HttpServletRequest) asyncContext.getRequest();
        HttpServletResponse resp = (HttpServletResponse) asyncContext.getResponse();

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        TaskExecutor.submit(() -> {
            try {
                User user = authService.login(username, password);
                if (user != null) {
                    HttpSession session = req.getSession();
                    session.setAttribute("user", user);
                    resp.sendRedirect("../dashboard.jsp");
                } else {
                    resp.sendRedirect("../index.jsp?error=Invalid username or password");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    resp.sendRedirect("../index.jsp?error=Server error during login");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } finally {
                asyncContext.complete();
            }
        });
    }

    private void sendError(HttpServletResponse resp, int status, String message) {
        try {
            resp.setStatus(status);
            PrintWriter out = resp.getWriter();
            out.write("{\"error\":\"" + message + "\"}");
            out.flush();
        } catch (IOException ignored) {}
    }
}
