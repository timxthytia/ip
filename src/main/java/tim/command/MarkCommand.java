package tim.command;

import tim.exception.TimException;
import tim.storage.Storage;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that marks a task as completed.
 * The command uses the given index to find the task in the task list,
 * marks it as done, and saves the updated list to storage.
 */
public class MarkCommand extends Command {
    private final int index;

    /**
     * Constructs a MarkCommand with the specified task index.
     *
     * @param index The 1-based index of the task to mark as done.
     */
    public MarkCommand(int index) {
        this.index = index;
    }

    /**
     * Executes the command to mark a task as done.
     *
     * @param tasks   The list of tasks.
     * @param ui      The user interface for displaying messages.
     * @param storage The storage handler for saving the updated task list.
     * @return A message indicating the task has been marked as done.
     * @throws TimException If the specified index is out of range.
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) throws TimException {
        if (index < 1 || index > tasks.size()) {
            throw new TimException("OOPS!!! Task number out of range.");
        }
        Task done = tasks.get(index - 1);
        done.markAsDone();
        storage.save(tasks);
        return ui.showMark(done);
    }
}
