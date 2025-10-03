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
                PrintWriter out = resp.getWriter();
                boolean success = authService.registerUser(username, role, password);
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    out.write("{\"message\":\"User registered successfully\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.write("{\"error\":\"Username already exists\"}");
                }
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error during registration");
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
                PrintWriter out = resp.getWriter();
                User user = authService.login(username, password);
                if (user != null) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write("{\"message\":\"Login successful\",\"role\":\"" + user.getRole() + "\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.write("{\"error\":\"Invalid username or password\"}");
                }
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error during login");
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
