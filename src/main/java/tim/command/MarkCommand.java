package tim.command;

import tim.exception.DukeException;
import tim.storage.Storage;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

public class MarkCommand extends Command {
    private final int index;
    public MarkCommand(int index) { this.index = index; }
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        if (index < 1 || index > tasks.size()) {
            throw new DukeException("OOPS!!! Task number out of range.");
        }
        Task done = tasks.get(index - 1);
        done.markAsDone();
        storage.save(tasks);
        System.out.println("Nice! I've marked this task as done:");
        System.out.println(" " + done);
    }
}
