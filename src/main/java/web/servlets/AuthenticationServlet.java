package web.servlets;

import core.services.AuthenticationService;
import core.dao.UserDAO;
import core.models.User;
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

@WebServlet(urlPatterns = "/api/auth/*", loadOnStartup = 1)
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            resp.sendRedirect("/index.jsp");
            return;
        }

        switch (path) {
            case "/login":
                resp.sendRedirect("/login.jsp");
                break;
            case "/register":
                resp.sendRedirect("/register.jsp");
                break;
            default:
                resp.sendRedirect("/index.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo(); 
        if (path == null) {
            resp.sendRedirect("/index.jsp?error=Invalid endpoint");
            return;
        }

        switch (path) {
            case "/register":
                handleRegister(req, resp);
                break;
            case "/login":
                handleLogin(req, resp);
                break;
            default:
                resp.sendRedirect("/index.jsp?error=Unknown endpoint");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String role = req.getParameter("role");
        String password = req.getParameter("password");

        try {
            boolean success = authService.registerUser(username, role, password);
            if (success) {
                resp.sendRedirect("/index.jsp?message=Registration successful");
            } else {
                resp.sendRedirect("/index.jsp?error=Username already exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("/index.jsp?error=Server error during registration");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User user = authService.login(username, password);
            if (user != null) {
                HttpSession session = req.getSession();
                session.setAttribute("user", user);
                resp.sendRedirect("/dashboard.jsp");
            } else {
                resp.sendRedirect("/index.jsp?error=Invalid username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("/index.jsp?error=Server error during login");
        }
    }

}
