package cli;

import core.dao.UserDAO;
import core.services.AuthenticationService;
import core.utils.DatabaseConnectionManager;

import java.sql.Connection;

public class MainCLI {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            AuthenticationService authService = new AuthenticationService(new UserDAO(conn));
            new ApplicationCLI(authService, conn).start();
        } catch (Exception e) {
            System.err.println("❌ Failed to start application: " + e.getMessage());
        }
    }
}
