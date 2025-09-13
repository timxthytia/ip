package tim.command;

import tim.exception.TimException;
import tim.storage.Storage;
import tim.task.Task;
import tim.task.TaskList;
import tim.task.Todo;
import tim.ui.Ui;

/**
 * Represents a command that adds a todo task to the task list.
 * A todo task only has a description and no associated date or time.
 */
public class AddTodoCommand extends Command {
    private final String desc;

    /**
     * Initializes the command with the provided description.
     *
     * @param desc The description of the todo task.
     */
    public AddTodoCommand(String desc) {
        this.desc = desc;
    }

    /**
     * Creates a new Todo task, adds it to the task list, saves the updated list,
     * and returns a confirmation message.
     *
     * @param tasks   The current list of tasks.
     * @param ui      The user interface for interaction.
     * @param storage The storage handler for saving tasks.
     * @return A confirmation message indicating the task was added.
     * @throws TimException If the description is empty or saving fails.
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) throws TimException {
        if (desc.isBlank()) {
            throw new TimException("OOPS!!! The description of a todo cannot be empty.");
        }
        Task newTask = new Todo(desc);
        tasks.add(newTask);
        storage.save(tasks);
        return ui.showAdd(newTask, tasks.size());
    }
}
