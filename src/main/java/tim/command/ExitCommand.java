package tim.command;

import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Represents a command that terminates the application.
 * When executed, it shows the goodbye message and signals the program to exit.
 */
public class ExitCommand extends Command {
    /**
     * Executes the exit command. Returns the farewell message from the {@link Ui}
     * and signals the application to terminate.
     *
     * @param tasks   The current list of tasks.
     * @param ui      The user interface to interact with the user.
     * @param storage The storage handler for saving/loading tasks.
     * @return A {@code String} containing the farewell message.
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) {
        return ui.showExit();
    }

    /**
     * Indicates whether this command signals the application to exit.
     * Returns {@code true} for this command, as it is the exit command.
     *
     * @return {@code true} if this command signals the application to exit.
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
