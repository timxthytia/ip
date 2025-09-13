package tim.ui;

import java.util.List;
import java.util.Scanner;

import tim.task.Task;

/**
 * Handles interactions with the user via the command line interface (CLI).
 * Provides methods to display messages and read commands from the user.
 */
public class Ui {
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Displays an error message when loading previous tasks fails.
     *
     * @return A string indicating the loading error.
     */
    public String showLoadingError() {
        return "OOPS!!! Couldn't load previous tasks, starting fresh.";
    }

    /**
     * Displays a welcome message to the user.
     *
     * @return A string containing the welcome message.
     */
    public String showWelcome() {
        return "Hello! I'm Tim\nWhat can I do for you?";
    }

    /**
     * Reads a command from the user input.
     *
     * @return The next line of input from the user, or an empty string if no input is available.
     */
    public String readCommand() {
        assert scanner != null : "scanner must be initialized";
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine();
    }

    /**
     * Displays an exit message when the user quits the application.
     *
     * @return A string containing the exit message.
     */
    public String showExit() {
        return "Bye! Hope to see you again soon!";
    }

    /**
     * Displays the list of tasks to the user.
     *
     * @param tasks The list of tasks to be displayed.
     * @return A formatted string listing all tasks.
     */
    public String showList(List<Task> tasks) {
        assert tasks != null : "tasks list must not be null";
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the tasks in your list:\n");
        int i = 1;
        for (Task task : tasks) {
            assert task != null : "task in list must not be null";
            sb.append(i).append(". ").append(task).append("\n");
            i++;
        }
        return sb.toString().trim();
    }

    /**
     * Displays a message indicating that a task has been marked as done.
     *
     * @param task The task that was marked.
     * @return A string confirming the task has been marked.
     */
    public String showMark(Task task) {
        assert task != null : "task must not be null";
        return "I've marked this task as done:\n" + task;
    }

    /**
     * Displays a message indicating that a task has been unmarked.
     *
     * @param task The task that was unmarked.
     * @return A string confirming the task has been unmarked.
     */
    public String showUnmark(Task task) {
        assert task != null : "task must not be null";
        return "I've unmarked this task:\n" + task;
    }

    /**
     * Displays a message indicating that a task has been deleted.
     *
     * @param task The task that was deleted.
     * @param size The new size of the task list after deletion.
     * @return A string confirming the task has been removed and showing the updated task count.
     */
    public String showDelete(Task task, int size) {
        assert task != null : "task must not be null";
        assert size >= 0 : "size must not be negative";
        return "I've removed this task:\n" + task + "\nNow you have " + size + " tasks in the list.";
    }

    /**
     * Displays the list of tasks that match a search query.
     *
     * @param tasks The list of matching tasks.
     * @return A formatted string listing the matching tasks or a message if none found.
     */
    public String showFind(List<Task> tasks) {
        assert tasks != null : "tasks list must not be null";
        if (tasks.isEmpty()) {
            return "No matching tasks found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the matching tasks in your list:\n");
        int i = 1;
        for (Task task : tasks) {
            assert task != null : "task in list must not be null";
            sb.append(i).append(". ").append(task).append("\n");
            i++;
        }
        return sb.toString().trim();
    }

    /**
     * Displays an error message.
     *
     * @param message The error message to display.
     * @return The error message string.
     */
    public String showError(String message) {
        assert message != null : "error message must not be null";
        return message;
    }

    /**
     * Displays a message indicating that a task has been added.
     *
     * @param task The task that was added.
     * @param size The new size of the task list after addition.
     * @return A string confirming the task has been added and showing the updated task count.
     */
    public String showAdd(Task task, int size) {
        assert task != null : "task must not be null";
        assert size >= 0 : "size must not be negative";
        return "Got it. I've added this task:\n" + task + "\nNow you have " + size + " tasks in the list.";
    }
}
