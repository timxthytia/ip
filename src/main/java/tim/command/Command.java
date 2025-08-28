package tim.command;

import tim.exception.DukeException;
import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Ui;

public abstract class Command {
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException;
    public boolean isExit() {
        return false;
    }
}
