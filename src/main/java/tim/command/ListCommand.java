package tim.command;

import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that displays the current list of tasks.
 * When executed, it shows all tasks in the list to the user.
 */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks);
    }
}
