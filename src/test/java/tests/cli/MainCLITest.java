package tests.cli;

import cli.MainCLI;
import core.dao.UserDAO;
import core.services.AuthenticationService;
import core.utils.DatabaseConnectionManager;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainCLITest {

    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
    }

    @Test
    void testMainRunsSuccessfully() throws Exception {
        // Mock the DatabaseConnectionManager instance and the static getInstance()
        try (MockedStatic<DatabaseConnectionManager> dbStaticMock = mockStatic(DatabaseConnectionManager.class)) {
            DatabaseConnectionManager mockManager = mock(DatabaseConnectionManager.class);
            Connection mockConn = mock(Connection.class);

            // When getInstance() called, return mockManager
            dbStaticMock.when(DatabaseConnectionManager::getInstance).thenReturn(mockManager);

            // When getConnection() called on mockManager, return mockConn
            when(mockManager.getConnection()).thenReturn(mockConn);

            // Now call your main method
            cli.MainCLI.main(new String[]{});

            // No exceptions expected, test passes if no errors printed
        }
    }

    @Test
    void testMainHandlesException() {
        try (MockedStatic<DatabaseConnectionManager> dbMock = mockStatic(DatabaseConnectionManager.class)) {
            dbMock.when(() -> DatabaseConnectionManager.getInstance().getConnection())
                    .thenThrow(new RuntimeException("Connection failed"));

            MainCLI.main(new String[]{});
            String errOutput = errContent.toString();
            assertTrue(errOutput.contains("❌ Failed to start application"));
            assertTrue(errOutput.contains("Connection failed"));
        }
    }
}
