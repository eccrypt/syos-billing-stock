package tests.cli;

import cli.AuthenticationCLI;
import core.models.User;
import core.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationCLITest {

    @Mock
    private AuthenticationService mockAuthService;

    private Scanner scanner;
    private AuthenticationCLI authCLI;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegister_Successful() {
        String input = "testuser\nadmin\npassword123\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        when(mockAuthService.registerUser("testuser", "admin", "password123")).thenReturn(true);

        authCLI = new AuthenticationCLI(scanner, mockAuthService);
        authCLI.register();

        verify(mockAuthService).registerUser("testuser", "admin", "password123");
    }

    @Test
    public void testRegister_Failure() {
        String input = "testuser\nadmin\npassword123\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        when(mockAuthService.registerUser("testuser", "admin", "password123")).thenReturn(false);

        authCLI = new AuthenticationCLI(scanner, mockAuthService);
        authCLI.register();

        verify(mockAuthService).registerUser("testuser", "admin", "password123");
    }

    @Test
    public void testLogin_Successful() {
        String input = "testuser\npassword123\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        User mockUser = new User(1, "testuser", "employee");
        when(mockAuthService.login("testuser", "password123")).thenReturn(mockUser);

        authCLI = new AuthenticationCLI(scanner, mockAuthService);
        User loggedInUser = authCLI.login();

        verify(mockAuthService).login("testuser", "password123");
        assertNotNull(loggedInUser);
        assertEquals("testuser", loggedInUser.getUsername());
    }

    @Test
    public void testLogin_Failure() {
        String input = "wronguser\nwrongpass\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        when(mockAuthService.login("wronguser", "wrongpass")).thenReturn(null);

        authCLI = new AuthenticationCLI(scanner, mockAuthService);
        User loggedInUser = authCLI.login();

        verify(mockAuthService).login("wronguser", "wrongpass");
        assertNull(loggedInUser);
    }
}
