package tim.command;

import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that displays the current list of tasks.
 * When executed, it shows all tasks in the list to the user.
 */
public class ListCommand extends Command {
    /**
     * Executes the list command by displaying all tasks in the task list.
     *
     * @param tasks the task list containing all tasks
     * @param ui the user interface used to interact with the user
     * @param storage the storage managing task persistence
     * @return a string representation of the list of tasks to be shown to the user
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) {
        return ui.showList(tasks.asList());
    }
}
