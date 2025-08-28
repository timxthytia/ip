package tim.command;

import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Tim;
import tim.ui.Ui;

public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks);
    }
}
