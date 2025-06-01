package core.command;

import core.report.ReportTemplate;

public class GenerateReportCommand implements Command {
    private final ReportTemplate report;

    public GenerateReportCommand(ReportTemplate report) {
        this.report = report;
    }

    @Override
    public void execute() {
        report.generate();
    }
}
