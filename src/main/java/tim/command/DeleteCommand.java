package tim.command;

import tim.exception.DukeException;
import tim.storage.Storage;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that deletes a task from the task list.
 * The command takes an index and removes the corresponding task if it exists.
 */
public class DeleteCommand extends Command {
    private final int index;

    /**
     * Constructs a DeleteCommand to remove a task at the specified index.
     *
     * @param index The 1-based index of the task to be deleted from the task list.
     */
    public DeleteCommand(int index) {
        this.index = index;
    }

    /**
     * Executes the delete command, removing the task at the specified index from the task list.
     *
     * @param tasks   The TaskList containing all current tasks.
     * @param ui      The Ui instance for user interaction and message display.
     * @param storage The Storage instance for saving the updated task list.
     * @return A String message indicating the result of the delete operation.
     * @throws DukeException If the provided index is out of range.
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        if (index < 1 || index > tasks.size()) {
            throw new DukeException("OOPS!!! Task number out of range.");
        }
        Task removed = tasks.remove(index - 1);
        storage.save(tasks);
        return ui.showDelete(removed, tasks.size());
    }
}
