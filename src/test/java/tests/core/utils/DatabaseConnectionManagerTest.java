package tests.core.utils;

import core.utils.DatabaseConnectionManager;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseConnectionManagerTest {

    @Test
    @Order(1)
    public void testGetInstanceReturnsSingleton() throws SQLException {
        DatabaseConnectionManager instance1 = DatabaseConnectionManager.getInstance();
        DatabaseConnectionManager instance2 = DatabaseConnectionManager.getInstance();

        assertNotNull(instance1, "Instance should not be null");
        assertSame(instance1, instance2, "Both instances should be the same (singleton)");
    }

    @Test
    @Order(2)
    public void testGetConnectionReturnsValidConnection() throws SQLException {
        DatabaseConnectionManager instance = DatabaseConnectionManager.getInstance();
        Connection conn = instance.getConnection();

        assertNotNull(conn, "Connection should not be null");
        assertFalse(conn.isClosed(), "Connection should be open");
    }

    @Test
    @Order(3)
    public void testConnectionReopensIfClosed() throws SQLException {
        DatabaseConnectionManager instance = DatabaseConnectionManager.getInstance();
        Connection conn1 = instance.getConnection();
        conn1.close();

        Connection conn2 = instance.getConnection();

        assertNotNull(conn2, "New connection should not be null after closing previous one");
        assertFalse(conn2.isClosed(), "New connection should be open");
    }

    @Test
    @Order(4)
    public void testConnectionPropertiesLoaded() throws SQLException {
        DatabaseConnectionManager instance = DatabaseConnectionManager.getInstance();
        Connection conn = instance.getConnection();

        String dbProductName = conn.getMetaData().getDatabaseProductName();
        assertNotNull(dbProductName, "Database product name should be available");
    }
}
