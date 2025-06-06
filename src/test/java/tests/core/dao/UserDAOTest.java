package tests.core.dao;

import core.dao.UserDAO;
import core.models.User;
import core.userfactory.UserFactory;
import core.utils.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        userDAO = new UserDAO(connection);
    }

    @Test
    void testRegisterUser_Success() throws SQLException {
        // Mocking the PreparedStatement for registering a user
        String username = "testUser";
        String role = "admin";
        String password = "testPassword";

        // Mocking the connection and PreparedStatement
        when(connection.prepareStatement(anyString())).thenReturn(stmt); // Mocking PreparedStatement
        when(stmt.executeUpdate()).thenReturn(1); // Mocking the return value of executeUpdate() to indicate successful insertion

        boolean result = userDAO.registerUser(username, role, password);

        assertTrue(result, "User should be registered successfully.");
        verify(stmt, times(1)).setString(1, username);
        verify(stmt, times(1)).setString(2, EncryptionUtil.hashPassword(password));  // Check if password is hashed
        verify(stmt, times(1)).setString(3, role);
    }


    @Test
    void testRegisterUser_Failure() throws SQLException {
        // Mock the behavior to throw SQLException
        String username = "testUser";
        String role = "admin";
        String password = "testPassword";

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        doThrow(new SQLException("Database error")).when(stmt).executeUpdate();

        boolean result = userDAO.registerUser(username, role, password);

        assertFalse(result, "User registration should fail.");
    }

    @Test
    void testAuthenticateUser_Success() throws SQLException {
        String username = "testUser";
        String password = "testPassword";
        String storedHash = EncryptionUtil.hashPassword(password);
        int id = 1;
        String role = "admin";

        // Mocking the ResultSet behavior
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("password_hash")).thenReturn(storedHash);
        when(rs.getInt("id")).thenReturn(id);
        when(rs.getString("user_role")).thenReturn(role);

        User expectedUser = UserFactory.createUser(id, username, role);

        User result = userDAO.authenticateUser(username, password);

        assertNotNull(result, "User should be authenticated successfully.");
        assertEquals(expectedUser.getUsername(), result.getUsername(), "Username should match.");
        assertEquals(expectedUser.getRole(), result.getRole(), "Role should match.");
    }

    @Test
    void testAuthenticateUser_UserNotFound() throws SQLException {
        String username = "nonExistentUser";
        String password = "somePassword";

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); // Simulate user not found

        User result = userDAO.authenticateUser(username, password);

        assertNull(result, "User should not be authenticated if the username doesn't exist.");
    }

    @Test
    void testAuthenticateUser_PasswordMismatch() throws SQLException {
        String username = "testUser";
        String password = "wrongPassword";
        String storedHash = EncryptionUtil.hashPassword("correctPassword");  // Hashed password from DB
        int id = 1;
        String role = "admin";

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("password_hash")).thenReturn(storedHash);
        when(rs.getInt("id")).thenReturn(id);
        when(rs.getString("user_role")).thenReturn(role);

        User result = userDAO.authenticateUser(username, password);

        assertNull(result, "User should not be authenticated if the password does not match.");
    }

    @Test
    void testAuthenticateUser_ExceptionHandling() throws SQLException {
        String username = "testUser";
        String password = "testPassword";

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenThrow(new SQLException("Database error"));

        User result = userDAO.authenticateUser(username, password);

        assertNull(result, "User authentication should return null if an exception occurs.");
    }
}
