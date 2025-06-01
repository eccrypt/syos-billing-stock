package core.command;

import java.util.ArrayList;
import java.util.List;

public class ReportInvoker {
    private final List<Command> commands = new ArrayList<>();

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void runCommands() {
        for (Command cmd : commands) {
            cmd.execute();
            System.out.println();
        }
    }
}
