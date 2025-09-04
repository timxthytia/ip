package tim.command;

import tim.exception.DukeException;
import tim.storage.Storage;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that marks a task as not done.
 * The command uses the given index to find the task in the task list,
 * marks it as undone, and saves the updated list to storage.
 */
public class UnmarkCommand extends Command {
    private final int index;

    /**
     * Constructs an UnmarkCommand with the specified task index.
     *
     * @param index the 1-based index of the task to be marked as undone
     */
    public UnmarkCommand(int index) {
        this.index = index;
    }

    /**
     * Executes the unmark command by marking the specified task as not done.
     *
     * @param tasks the task list containing the tasks
     * @param ui the user interface to interact with the user
     * @param storage the storage to save the updated task list
     * @return a message indicating the task has been unmarked
     * @throws DukeException if the task index is out of range
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        if (index < 1 || index > tasks.size()) {
            throw new DukeException("OOPS!!! Task number out of range.");
        }
        Task undone = tasks.get(index - 1);
        undone.markAsUndone();
        storage.save(tasks);
        return ui.showUnmark(undone);
    }
}
