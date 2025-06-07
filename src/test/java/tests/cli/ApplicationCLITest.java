package tests.cli;

import cli.ApplicationCLI;
import core.models.User;
import core.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

import static org.mockito.Mockito.*;

public class ApplicationCLITest {

    @Mock private AuthenticationService authService;
    @Mock private Connection connection;

    private Scanner scanner;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExitOption() throws SQLException, ParseException {
        String input = "3\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        ApplicationCLI appCLI = new ApplicationCLI(authService, connection) {
            @Override
            protected Scanner getScanner() {
                return scanner;
            }
        };
        appCLI.start();
    }

    @Test
    public void testInvalidInputThenExit() throws SQLException, ParseException {
        String input = "invalid\n3\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        ApplicationCLI appCLI = new ApplicationCLI(authService, connection) {
            @Override
            protected Scanner getScanner() {
                return scanner;
            }
        };
        appCLI.start();
    }

    @Test
    public void testSuccessfulLoginAsEmployee() throws SQLException, ParseException {
        String input = "2\njohn\npass123\n3\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        User mockUser = new User(1, "john", "employee");
        when(authService.login("john", "pass123")).thenReturn(mockUser);

        ApplicationCLI appCLI = new ApplicationCLI(authService, connection) {
            @Override
            protected Scanner getScanner() {
                return scanner;
            }

            @Override
            protected void routeToCLI(User user) {
                System.out.println("✅ Routing to employee CLI");
            }
        };
        appCLI.start();
    }

    @Test
    public void testFailedLogin() throws SQLException, ParseException {
        String input = "2\nwrong\nbadpass\n3\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        when(authService.login("wrong", "badpass")).thenReturn(null);

        ApplicationCLI appCLI = new ApplicationCLI(authService, connection) {
            @Override
            protected Scanner getScanner() {
                return scanner;
            }
        };
        appCLI.start();
    }
}
