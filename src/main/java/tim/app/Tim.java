package tim.app;

import tim.command.Command;
import tim.exception.DukeException;
import tim.parser.Parser;
import tim.storage.Storage;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Maintains Storage and TaskList state and turns a single user input into
 * the reply String produced by the command logic.
 */
public class Tim {
    private final Storage storage;
    private final TaskList tasks;

    /**
     * Creates a Tim backed by the given data file path.
     *
     * @param dataFilePath relative path to the save file.
     */
    public Tim(String dataFilePath) {
        this.storage = new Storage(dataFilePath);
        TaskList loaded;
        try {
            loaded = storage.load();
        } catch (DukeException e) {
            // Start with an empty list if loading fails
            loaded = new TaskList();
        }
        this.tasks = loaded;
    }

    /**
     * Parses and executes a single line of user input and returns the
     * response message produced by the command.
     *
     * @param input user input line
     * @return response message to display in the GUI
     */
    public String getResponse(String input) {
        try {
            Command c = Parser.parse(input);
            Ui ui = new Ui();
            return c.execute(tasks, ui, storage);
        } catch (DukeException e) {
            return e.getMessage();
        }
    }
}
