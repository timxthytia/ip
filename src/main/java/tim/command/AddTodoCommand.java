package tim.command;

import tim.exception.DukeException;
import tim.storage.Storage;
import tim.task.Task;
import tim.task.TaskList;
import tim.task.Todo;
import tim.ui.Ui;

public class AddTodoCommand extends Command {
    private final String desc;

    public AddTodoCommand(String desc) {
        this.desc = desc;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        if (desc.isBlank()) {
            throw new DukeException("OOPS!!! The description of a todo cannot be empty.");
        }
        Task newTask = new Todo(desc);
        tasks.add(newTask);
        storage.save(tasks);
        ui.showAdded(newTask, tasks);
    }
}