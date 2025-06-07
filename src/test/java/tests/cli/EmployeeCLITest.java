package tests.cli;

import cli.EmployeeCLI;
import cli.ItemCLI;
import core.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.text.ParseException;
import java.util.Scanner;

import static org.mockito.Mockito.*;

public class EmployeeCLITest {

    @Mock
    private Connection connection;

    @Mock
    private User user;

    @Mock
    private ItemCLI mockItemCLI;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogoutOption() throws Exception {
        String input = "0\n"; // Simulates user pressing logout
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        EmployeeCLI employeeCLI = new EmployeeCLI(scanner, connection);
        employeeCLI.menu(user);
    }

    @Test
    public void testInvalidChoiceThenLogout() throws Exception {
        String input = "99\n0\n"; // Invalid choice followed by logout
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        EmployeeCLI employeeCLI = new EmployeeCLI(scanner, connection);
        employeeCLI.menu(user);
    }

    @Test
    public void testItemManagementOption() throws Exception {
        String input = "3\n0\n"; // Select Item Management and then logout
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        // Use the constructor that allows injecting a mock ItemCLI
        EmployeeCLI employeeCLI = new EmployeeCLI(scanner, connection, mockItemCLI);

        // Mock the `start` method to ensure it gets triggered but does nothing
        doNothing().when(mockItemCLI).start();

        employeeCLI.menu(user);

        verify(mockItemCLI, times(1)).start(); // Ensure item management was triggered
    }
}
