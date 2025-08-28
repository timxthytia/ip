package tim.command;

import tim.storage.Storage;
import tim.task.Event;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

import java.time.LocalDateTime;

public class AddEventCommand extends Command {
    private final String desc;
    private final LocalDateTime start;
    private final LocalDateTime end;

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