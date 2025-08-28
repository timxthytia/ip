package tim.command;

import tim.exception.DukeException;
import tim.storage.Storage;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

public class UnmarkCommand extends Command {
    private final int index;

    public UnmarkCommand(int index) {
        this.index = index;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException {
        if (index < 1 || index > tasks.size()) {
            throw new DukeException("OOPS!!! Task number out of range.");
        }
        Task undone = tasks.get(index - 1);
        undone.markAsUndone();
        storage.save(tasks);
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println(" " + undone);
    }
}
