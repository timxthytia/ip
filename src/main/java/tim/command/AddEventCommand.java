package tim.command;

import java.time.LocalDateTime;

import tim.storage.Storage;
import tim.task.Event;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that adds an event task to the task list.
 * An event task has a description, start date/time, and end date/time.
 */
public class AddEventCommand extends Command {
    private final String desc;
    private final LocalDateTime start;
    private final LocalDateTime end;

    /**
     * Creates a new AddEventCommand with the specified description, start, and end date/time.
     *
     * @param desc the description of the event
     * @param start the start date and time of the event
     * @param end the end date and time of the event
     */
    public AddEventCommand(String desc, LocalDateTime start, LocalDateTime end) {
        this.desc = desc;
        this.start = start;
        this.end = end;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        Task newTask = new Event(desc, start, end);
        tasks.add(newTask);
        storage.save(tasks);
        ui.showAdded(newTask, tasks);
    }
}
