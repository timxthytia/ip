package tim.command;

import java.time.LocalDateTime;

import tim.storage.Storage;
import tim.task.Deadline;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that adds a deadline task to the task list.
 * A deadline task has a description and a due date/time.
 */
public class AddDeadlineCommand extends Command {
    private final String desc;
    private final LocalDateTime due;

    /**
     * Creates a new AddDeadlineCommand with the specified description and due date/time.
     *
     * @param desc the description of the deadline task
     * @param due the due date and time of the deadline task
     */
    public AddDeadlineCommand(String desc, LocalDateTime due) {
        this.desc = desc;
        this.due = due;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        Task newTask = new Deadline(desc, due);
        tasks.add(newTask);
        storage.save(tasks);
        ui.showAdded(newTask, tasks);
    }
}
