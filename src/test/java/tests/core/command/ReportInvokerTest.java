package tests.core.command;

import core.command.Command;
import core.command.ReportInvoker;
import core.command.GenerateReportCommand;
import core.report.ReportTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class ReportInvokerTest {

    private ReportInvoker reportInvoker;
    private Command generateReportCommand;

    @BeforeEach
    void setUp() {
        reportInvoker = new ReportInvoker();
        generateReportCommand = mock(Command.class);  // Mock the command
    }

    @Test
    void testAddCommandAndRunCommands() {
        // Add the mocked command to the invoker
        reportInvoker.addCommand(generateReportCommand);

        // Run the commands
        reportInvoker.runCommands();

        // Verify that the execute method was called once
        verify(generateReportCommand, times(1)).execute();
    }
}
