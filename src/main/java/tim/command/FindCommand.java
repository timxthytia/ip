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
    private final String keyword;

    /**
     * Constructs a FindCommand with the specified keyword.
     *
     * @param keyword the keyword to search for within tasks
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Executes the find command by searching for tasks containing the keyword.
     *
     * @param tasks   the list of tasks to search through
     * @param ui      the user interface to display results
     * @param storage the storage system (not used in this command)
     * @return a string containing the matched tasks or a message if no matches are found
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) {
        ArrayList<Task> matches = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.toString().contains(keyword)) {
                matches.add(t);
            }
        }

        if (matches.isEmpty()) {
            return "No matching tasks found for keyword: " + keyword;
        } else {
            return ui.showFind(matches);
        }
    }
}
