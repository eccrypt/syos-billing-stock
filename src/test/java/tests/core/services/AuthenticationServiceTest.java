package tests.core.services;

import core.dao.UserDAO;
import core.models.User;
import core.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserDAO userDAO;

    private AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationService(userDAO);
    }

    @Test
    public void testRegisterUser_Success() {
        String username = "johnDoe";
        String role = "admin";
        String password = "password123";

        // Mocking successful registration
        when(userDAO.registerUser(username, role, password)).thenReturn(true);

        boolean result = authenticationService.registerUser(username, role, password);

        assertTrue(result);
        verify(userDAO, times(1)).registerUser(username, role, password);
    }

    @Test
    public void testRegisterUser_Fail_UserAlreadyExists() {
        String username = "johnDoe";
        String role = "admin";
        String password = "password123";

        // Mocking failed registration (username already exists)
        when(userDAO.registerUser(username, role, password)).thenReturn(false);

        boolean result = authenticationService.registerUser(username, role, password);

        assertFalse(result);
        verify(userDAO, times(1)).registerUser(username, role, password);
    }

    @Test
    public void testLogin_Success() {
        String username = "johnDoe";
        String password = "password123";
        User mockUser = new User(1, username, "admin"); // Updated to match the User model constructor

        // Mocking successful login
        when(userDAO.authenticateUser(username, password)).thenReturn(mockUser);

        User result = authenticationService.login(username, password);

        assertNotNull(result);
        assertEquals(mockUser, result);
        verify(userDAO, times(1)).authenticateUser(username, password);
    }

    @Test
    public void testLogin_Fail_InvalidUsername() {
        String username = "wrongUsername";
        String password = "password123";

        // Mocking invalid username
        when(userDAO.authenticateUser(username, password)).thenReturn(null);

        User result = authenticationService.login(username, password);

        assertNull(result);
        verify(userDAO, times(1)).authenticateUser(username, password);
    }

    @Test
    public void testLogin_Fail_InvalidPassword() {
        String username = "johnDoe";
        String password = "wrongPassword";

        // Mocking invalid password
        when(userDAO.authenticateUser(username, password)).thenReturn(null);

        User result = authenticationService.login(username, password);

        assertNull(result);
        verify(userDAO, times(1)).authenticateUser(username, password);
    }

    @Test
    public void testLogin_Fail_BothInvalid() {
        String username = "wrongUsername";
        String password = "wrongPassword";

        // Mocking both invalid username and password
        when(userDAO.authenticateUser(username, password)).thenReturn(null);

        User result = authenticationService.login(username, password);

        assertNull(result);
        verify(userDAO, times(1)).authenticateUser(username, password);
    }

    @Test
    public void testRegisterUser_NullUsername() {
        String username = null;
        String role = "admin";
        String password = "password123";

        // Mocking the registration process with null username
        when(userDAO.registerUser(username, role, password)).thenReturn(false);

        boolean result = authenticationService.registerUser(username, role, password);

        assertFalse(result);
        verify(userDAO, times(1)).registerUser(username, role, password);
    }

    @Test
    public void testLogin_NullUsername() {
        String username = null;
        String password = "password123";

        // Mocking the login process with null username
        when(userDAO.authenticateUser(username, password)).thenReturn(null);

        User result = authenticationService.login(username, password);

        assertNull(result);
        verify(userDAO, times(1)).authenticateUser(username, password);
    }

    @Test
    public void testLogin_NullPassword() {
        String username = "johnDoe";
        String password = null;

        // Mocking the login process with null password
        when(userDAO.authenticateUser(username, password)).thenReturn(null);

        User result = authenticationService.login(username, password);

        assertNull(result);
        verify(userDAO, times(1)).authenticateUser(username, password);
    }
}
