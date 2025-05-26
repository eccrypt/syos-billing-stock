package core.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static DatabaseConnectionManager instance;
    private Connection connection;

    private DatabaseConnectionManager() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/db_sysos_over_the_counter", "root", ""
        );
    }

    public static DatabaseConnectionManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
