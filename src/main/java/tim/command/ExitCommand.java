package tim.command;

import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that terminates the application.
 * When executed, it shows the goodbye message and signals the program to exit.
 */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showBye();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
