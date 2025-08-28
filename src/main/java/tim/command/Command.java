package tim.command;

import tim.exception.DukeException;
import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Ui;


/**
 * Represents an abstract command that can be executed in the chatbot.
 * All specific command types (e.g. Add, Delete, List, Exit) should extend this class
 * and implement the TaskList, Ui, and Storage method to define their behaviour
 *
 */
public abstract class Command {
    /**
     * Executes the command with access to the current task list, user interface, and storage.
     *
     * @param tasks the TaskList containing current tasks.
     * @param ui the Ui object for interacting with the user.
     * @param storage the Storage used to save or load tasks.
     * @throws DukeException if an error occurs during execution.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws DukeException;

    /**
     * Indicates whether this command will cause the program to exit.
     *
     * @return true if this is an exit command, false otherwise.
     */
    public boolean isExit() {
        return false;
    }
}
