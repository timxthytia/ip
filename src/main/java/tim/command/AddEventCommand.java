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

    /**
     * Creates a new Event task, adds it to the task list, saves the updated list to storage,
     * and returns a confirmation message through the Ui.
     *
     * @param tasks the TaskList to which the new Event will be added
     * @param ui the Ui instance used to generate user interface messages
     * @param storage the Storage instance responsible for saving the task list
     * @return a confirmation message as a String indicating the Event has been added
     * @throws RuntimeException if saving the task list to storage fails
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) {
        Task newTask = new Event(desc, start, end);
        tasks.add(newTask);
        storage.save(tasks);
        return ui.showAdd(newTask, tasks.size());
    }
}
