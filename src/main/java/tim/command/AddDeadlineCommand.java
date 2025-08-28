package tim.command;

import tim.storage.Storage;
import tim.task.Deadline;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

import java.time.LocalDateTime;

public class AddDeadlineCommand extends Command {
    private final String desc;
    private final LocalDateTime due;

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
