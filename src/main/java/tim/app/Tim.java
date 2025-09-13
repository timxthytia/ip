package tim.app;

import tim.command.Command;
import tim.exception.TimException;
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
    private final Ui ui;

    /**
     * Creates a Tim backed by the given data file path.
     *
     * @param dataFilePath relative path to the save file.
     */
    public Tim(String dataFilePath) {
        this.storage = new Storage(dataFilePath);
        assert this.storage != null : "Storage should not be null after initialization.";
        TaskList loaded;
        try {
            loaded = storage.load();
        } catch (TimException e) {
            // Start with an empty list if loading fails
            loaded = new TaskList();
        }
        this.tasks = loaded;
        assert this.tasks != null : "Tasks should not be null after loading.";
        this.ui = new Ui();
        assert this.ui != null : "Ui should not be null after initialization.";
    }

    /**
     * Exposes the live TaskList so GUI features (e.g., reminders) can read it.
     *
     * @return the current TaskList backing this Tim instance
     */
    public TaskList getTaskList() {
        return tasks;
    }

    /**
     * Parses and executes a single line of user input and returns the
     * response message produced by the command.
     *
     * <p>Note: This method treats invalid user input as a normal runtime
     * condition and returns a friendly message rather than using assertions.</p>
     *
     * @param input user input line
     * @return response message to display in the GUI
     */
    public String getResponse(String input) {
        // Guard against null/blank input: this is a user error, not an assertion case.
        if (input == null || input.isBlank()) {
            return ui.showError("Please enter a command.");
        }

        try {
            Command c = Parser.parse(input);
            assert c != null : "Command should not be null after parsing.";
            return c.execute(tasks, ui, storage);
        } catch (TimException e) {
            // Present domain errors consistently through Ui.
            return ui.showError(e.getMessage());
        }
    }
}
