package core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionManager {

    private static volatile DatabaseConnectionManager instance;
    private Connection connection;

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private DatabaseConnectionManager() throws SQLException {
        loadProperties();
        this.connection = createConnection();
    }

    public static DatabaseConnectionManager getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            this.connection = createConnection();
        }
        return connection;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("❌ Unable to find db.properties in classpath.");
            }
            props.load(input);
            dbUrl = props.getProperty("db.url");
            dbUser = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load database properties: " + e.getMessage(), e);
        }
    }
}
