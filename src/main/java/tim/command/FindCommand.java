package tim.command;

import java.util.ArrayList;

import tim.storage.Storage;
import tim.task.Task;
import tim.task.TaskList;
import tim.ui.Ui;

/**
 * Finds and lists tasks that match a given keyword.
 */
public class FindCommand extends Command {
    private static final String NO_MATCH_MSG = "No matching tasks found for keyword: ";
    private final String keyword;

    /**
     * Constructs a FindCommand with the specified keyword.
     *
     * @param keyword the keyword to search for within tasks; leading/trailing spaces are ignored
     * @throws IllegalArgumentException if the keyword is null or blank
     */
    public FindCommand(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword must not be null or blank.");
        }
        this.keyword = keyword.trim().toLowerCase();
    }

    /**
     * Returns true if the task matches the search keyword (case-insensitive).
     */
    private boolean matches(Task task) {
        // Prefer a dedicated accessor if available; fallback to string form.
        return task.toString().toLowerCase().contains(keyword);
    }

    /**
     * Executes the find command by searching for tasks containing the keyword (case-insensitive).
     *
     * @param tasks   the list of tasks to search through
     * @param ui      the user interface to display results
     * @param storage the storage system (unused)
     * @return a string containing the matched tasks or a message if no matches are found
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) {
        ArrayList<Task> matches = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (matches(t)) {
                matches.add(t);
            }
        }
        if (matches.isEmpty()) {
            return NO_MATCH_MSG + keyword;
        } else {
            return ui.showFind(matches);
        }
    }
}
