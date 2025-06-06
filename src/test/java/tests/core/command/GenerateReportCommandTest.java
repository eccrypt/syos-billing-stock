package tests.core.command;

import core.command.GenerateReportCommand;
import core.report.ReportTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class GenerateReportCommandTest {

    private GenerateReportCommand generateReportCommand;
    private ReportTemplate reportTemplate;

    @BeforeEach
    void setUp() {
        reportTemplate = mock(ReportTemplate.class);
        generateReportCommand = new GenerateReportCommand(reportTemplate);
    }

    @Test
    void testExecute() {
        // Call execute on the command
        generateReportCommand.execute();

        // Verify if generate() method is called once
        verify(reportTemplate, times(1)).generate();
    }
}
